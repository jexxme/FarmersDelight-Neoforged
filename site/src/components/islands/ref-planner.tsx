/** @jsxImportSource preact */
import { useEffect, useMemo, useRef, useState } from 'preact/hooks';

export type PIng = { items: string[]; tag: string | null; label: string | null; n: number };
export type PRecipe = {
	id: string;
	type: string;
	station: string;
	ins: PIng[];
	outs: { id: string; count: number; chance: number }[];
	container: string | null;
	tool: { items: string[]; tag: string | null; label: string | null } | null;
	time: number | null;
};
export type PItem = { name: string; icon: string | null; href: string | null; cat: string | null; gathered: boolean };
type Choice = { id: string; name: string; cat: string };
type Sel = Record<string, { r?: number; t?: string; c?: boolean }>;

type Node = {
	key: string;
	item: string;
	ing: PIng | null;
	qty: number;
	approx: boolean;
	options: PRecipe[];
	pick: number; // -1 = raw
	recipe: PRecipe | null;
	crafts: number;
	children: Node[];
	cycle: boolean;
};

const MAX_DEPTH = 14;

export default function Planner({ recipes, info, choices, start }: { recipes: PRecipe[]; info: Record<string, PItem>; choices: Choice[]; start: string }) {
	const [target, setTarget] = useState(start);
	const [amount, setAmount] = useState(1);
	const [sel, setSel] = useState<Sel>({});
	const [q, setQ] = useState('');
	const [open, setOpen] = useState(false);
	const [hi, setHi] = useState(0);
	const box = useRef<HTMLDivElement>(null);

	useEffect(() => {
		const read = () => {
			const m = location.hash.match(/item=([\w:./-]+)/);
			if (m && choices.some((c) => c.id === m[1])) (setTarget(m[1]), setSel({}));
		};
		read();
		addEventListener('hashchange', read);
		return () => removeEventListener('hashchange', read);
	}, []);
	const choose = (id: string) => {
		setTarget(id);
		setSel({});
		setQ('');
		setOpen(false);
		history.replaceState(null, '', `#item=${id}`);
	};

	const producers = useMemo(() => {
		const m = new Map<string, PRecipe[]>();
		for (const r of recipes) for (const o of r.outs) m.set(o.id, [...(m.get(o.id) ?? []), r]);
		return m;
	}, [recipes]);

	const nm = (id: string) => info[id]?.name ?? id.split(':')[1].replaceAll('_', ' ');
	/** A tool line in plain words: its label, or "Any axe" for a tool tag, or the item's name. */
	const toolName = (t: NonNullable<PRecipe['tool']>) => t.label ?? (t.tag ? `Any ${t.tag.split(/[/:]/).pop()!.replace(/s$/, '')}` : nm(t.items[0]));
	/** A recipe choice in plain words: where it's made and from what. */
	const recipeLabel = (r: PRecipe) => {
		const names = r.ins.map((i) => (i.label ?? nm(i.items[0])));
		return `${r.type}: from ${names.slice(0, 2).join(', ')}${names.length > 2 ? ', …' : ''}`;
	};
	const defaultItem = (ing: PIng) => {
		if (ing.items.length === 1 || !ing.tag) return ing.items[0];
		const tail = ing.tag.split(/[/:]/).pop()!;
		return ing.items.find((i) => i.endsWith(':' + tail)) ?? ing.items.find((i) => i.endsWith(':' + tail.replace(/s$/, ''))) ?? ing.items[0];
	};
	const isReverse = (r: PRecipe) => r.id.startsWith('salvaging/') || r.ins.some((i) => i.items.length === 1 && info[i.items[0]]?.cat === 'storage');
	const rank = (r: PRecipe, item: string) => (isReverse(r) ? 4 : 0) + (r.id.includes('_from_') ? 1 : 0) + (r.outs[0]?.id !== item ? 2 : 0);

	function build(item: string, qty: number, key: string, ing: PIng | null, ancestors: string[], depth: number): Node {
		const s = sel[key] ?? {};
		const cycle = ancestors.includes(item);
		const options = cycle ? [] : (producers.get(item) ?? []).filter((r) => !r.ins.some((i) => i.items.some((x) => x === item || ancestors.includes(x))));
		const sorted = [...options].sort((a, b) => rank(a, item) - rank(b, item));
		const auto = item.startsWith('minecraft:') || info[item]?.gathered || !sorted.length || rank(sorted[0], item) >= 4 ? -1 : 0;
		let pick = s.r ?? auto;
		if (pick >= sorted.length || depth >= MAX_DEPTH) pick = -1;
		const recipe = pick >= 0 ? sorted[pick] : null;
		let crafts = 0;
		let approx = false;
		const children: Node[] = [];
		if (recipe) {
			const out = recipe.outs.find((o) => o.id === item)!;
			const per = out.count * out.chance;
			approx = out.chance < 1;
			crafts = Math.ceil(qty / per - 1e-9);
			const lines = [...recipe.ins];
			if (recipe.container) lines.push({ items: [recipe.container], tag: null, label: 'Serving container', n: 1 });
			lines.forEach((l, i) => {
				const k = `${key}/${i}`;
				const chosen = sel[k]?.t && l.items.includes(sel[k].t!) ? sel[k].t! : defaultItem(l);
				children.push(build(chosen, l.n * crafts, `${k}@${chosen}`, l, [...ancestors, item], depth + 1));
			});
		}
		return { key, item, ing, qty, approx, options: sorted, pick, recipe, crafts, children, cycle };
	}

	const tree = useMemo(() => build(target, amount, 'root', null, [], 0), [target, amount, sel, producers]);

	// Shopping list (raw materials), stations and tools
	const { raw, stations, tools, steps } = useMemo(() => {
		const raw = new Map<string, { qty: number; approx: boolean; ing: PIng | null }>();
		const stations = new Map<string, number>();
		const tools = new Map<string, PRecipe['tool']>();
		let steps = 0;
		const walk = (n: Node, approx: boolean) => {
			const a = approx || n.approx;
			if (!n.recipe) {
				const e = raw.get(n.item) ?? { qty: 0, approx: false, ing: n.ing };
				e.qty += n.qty;
				e.approx ||= approx;
				raw.set(n.item, e);
				return;
			}
			steps++;
			stations.set(n.recipe.station, (stations.get(n.recipe.station) ?? 0) + n.crafts);
			if (n.recipe.tool) tools.set(n.recipe.tool.tag ?? n.recipe.tool.items[0], n.recipe.tool);
			n.children.forEach((c) => walk(c, a));
		};
		walk(tree, false);
		return { raw: [...raw].sort((x, y) => y[1].qty - x[1].qty), stations: [...stations], tools: [...tools.values()], steps };
	}, [tree]);

	const set = (key: string, patch: { r?: number; t?: string; c?: boolean }) => setSel((s) => ({ ...s, [key]: { ...s[key], ...patch } }));
	const icon = (id: string, size = 32) => {
		const ic = info[id]?.icon;
		return ic ? <img src={ic} alt="" width={size} height={size} loading="lazy" /> : <span class="pl-noicon" style={{ width: size, height: size }} />;
	};

	const row = (n: Node, depth: number) => {
		const collapsed = sel[n.key]?.c ?? false;
		const lineKey = n.key.slice(0, n.key.lastIndexOf('@'));
		return (
			<li key={n.key} class={n.recipe ? 'made' : 'raw'}>
				<div class="pl-row">
					{n.recipe ? (
						<button type="button" class="pl-tog" aria-expanded={!collapsed} aria-label={collapsed ? 'Expand' : 'Collapse'} onClick={() => set(n.key, { c: !collapsed })}>
							{collapsed ? '+' : '-'}
						</button>
					) : (
						<span class="pl-tog pl-leaf" aria-hidden="true" />
					)}
					<span class="pl-icon" data-tip-items={(n.ing && n.ing.items.length > 1 ? n.ing.items : [n.item]).map((i) => `${nm(i)}~${i}`).join('|')}>
						{icon(n.item)}
					</span>
					<span class="pl-qty">
						{n.approx ? '~' : ''}
						{n.qty}×
					</span>
					<span class="pl-name">
						{n.ing && n.ing.items.length > 1 ? null : info[n.item]?.href ? <a href={info[n.item].href!}>{nm(n.item)}</a> : nm(n.item)}
						{n.ing && n.ing.items.length > 1 && (
							<select class="ref-field pl-tag" value={n.item} aria-label="Choose an item for this ingredient" onChange={(e) => set(lineKey, { t: (e.target as HTMLSelectElement).value })}>
								{n.ing.items.map((i) => (
									<option value={i} key={i}>
										{nm(i)}
									</option>
								))}
							</select>
						)}
						{n.ing && n.ing.items.length > 1 && (
							<small>
								any of {n.ing.items.length}
							</small>
						)}
						{n.ing?.label === 'Serving container' && <small>serving container</small>}
						{n.cycle && <small>loops back, shown as raw</small>}
					</span>
					<span class="pl-how">
						{n.recipe && (
							<span class="pl-station" title={`${n.recipe.type}${n.crafts > 1 ? `, ${n.crafts} times` : ''}`}>
								{info[n.recipe.station]?.icon ? icon(n.recipe.station, 20) : <span>{n.recipe.type.replace(/ \(shapeless\)$/, '')}</span>}
								{n.crafts > 1 && <span>×{n.crafts}</span>}
							</span>
						)}
						{n.options.length > 0 && (
							<select class="ref-field" value={String(n.pick)} aria-label={`How to get ${nm(n.item)}`} onChange={(e) => set(n.key, { r: +(e.target as HTMLSelectElement).value })}>
								<option value="-1">Raw material</option>
								{n.options.map((r, i) => (
									<option value={String(i)} key={r.id}>
										{recipeLabel(r)}
									</option>
								))}
							</select>
						)}
						{!n.options.length && !n.cycle && <span class="pl-rawtag">raw</span>}
					</span>
				</div>
				{n.recipe?.tool && !collapsed && (
					<div class="pl-tool">
						Tool: {icon(n.recipe.tool.items[0], 16)} {toolName(n.recipe.tool)}, not used up
					</div>
				)}
				{n.recipe && !collapsed && n.children.length > 0 && <ul>{n.children.map((c) => row(c, depth + 1))}</ul>}
			</li>
		);
	};

	const words = q.toLowerCase().split(/\s+/).filter(Boolean);
	const matches = choices.filter((c) => words.every((w) => c.name.toLowerCase().includes(w) || c.id.includes(w))).slice(0, 40);

	return (
		<div class="planner not-content">
			<div class="pl-pick">
				<div class="pl-target">
					{icon(target, 48)}
					<div>
						<span class="pl-k">Making</span>
						<strong>{nm(target)}</strong>
					</div>
				</div>
				<label class="pl-amount">
					<span class="pl-k">Amount</span>
					<input class="ref-field" type="number" min={1} max={999} value={amount} onInput={(e) => setAmount(Math.max(1, Math.min(999, +(e.target as HTMLInputElement).value || 1)))} />
				</label>
				<div class="pl-search" ref={box}>
					<span class="pl-k">Choose another item</span>
					<input
						class="ref-field"
						type="search"
						placeholder={`Search ${choices.length} craftable items…`}
						value={q}
						role="combobox"
						aria-expanded={open}
						aria-controls="pl-list"
						aria-autocomplete="list"
						onFocus={() => setOpen(true)}
						onBlur={() => setTimeout(() => setOpen(false), 150)}
						onInput={(e) => {
							setQ((e.target as HTMLInputElement).value);
							setOpen(true);
							setHi(0);
						}}
						onKeyDown={(e) => {
							if (e.key === 'ArrowDown') (setHi((h) => Math.min(h + 1, matches.length - 1)), e.preventDefault());
							if (e.key === 'ArrowUp') (setHi((h) => Math.max(h - 1, 0)), e.preventDefault());
							if (e.key === 'Enter' && matches[hi]) choose(matches[hi].id);
							if (e.key === 'Escape') setOpen(false);
						}}
					/>
					{open && (
						<ul id="pl-list" role="listbox" class="pl-list">
							{matches.map((c, i) => (
								<li key={c.id} role="option" aria-selected={i === hi} class={i === hi ? 'hi' : ''} onMouseDown={() => choose(c.id)}>
									{icon(c.id, 24)} {c.name}
								</li>
							))}
							{!matches.length && <li class="none">Nothing craftable matches</li>}
						</ul>
					)}
				</div>
			</div>
			<div class="pl-body">
				<section class="pl-tree" aria-label="Ingredient tree">
					<h3 class="ref-h">Ingredient tree</h3>
					<div class="ref-board">
						<ul class="pl-root">{row(tree, 0)}</ul>
					</div>
				</section>
				<aside class="pl-side">
					<div>
					<h3 class="ref-h">Shopping list</h3>
					<div class="mc-panel"><table class="pl-shop">
						<tbody>
							{raw.map(([id, e]) => (
								<tr key={id}>
									<td class="ic" data-tip-items={`${nm(id)}~${id}`}>
										<span class="slot">
											{icon(id, 32)}
											{e.qty > 1 && <b>{e.qty > 999 ? '999+' : e.qty}</b>}
										</span>
									</td>
									<td>
										{e.approx ? '~' : ''}
										{e.qty}{'× '}
										{info[id]?.href ? <a href={info[id].href!}>{nm(id)}</a> : nm(id)}
										{e.ing && e.ing.items.length > 1 && <small> or any of {e.ing.items.length}</small>}
									</td>
								</tr>
							))}
						</tbody>
					</table></div>
					</div>
					<div>
					<h3 class="ref-h">Stations</h3>
					<div class="mc-panel"><table class="pl-shop">
						<tbody>
							{stations.map(([id, n]) => (
								<tr key={id}>
									<td class="ic">
										<span class="slot">{icon(id, 32)}</span>
									</td>
									<td>
										{nm(id)} <small>×{n}</small>
									</td>
								</tr>
							))}
							{tools.map((t) => (
								<tr key={t!.tag ?? t!.items[0]}>
									<td class="ic">
										<span class="slot">{icon(t!.items[0], 32)}</span>
									</td>
									<td>
										{toolName(t!)} <small>tool</small>
									</td>
								</tr>
							))}
						</tbody>
					</table></div>
					<p class="pl-note">
						{steps} crafting {steps === 1 ? 'step' : 'steps'}. Stations show how many times each is used. Counts round up to whole crafts; ~ marks results with a chance below 100%.
					</p>
					</div>
				</aside>
			</div>
			<style>{`
.planner { margin: 20px 0; }
.pl-k { display: block; font-size: 20px; line-height: 30px; color: var(--sl-color-gray-3); }
.pl-pick { display: grid; grid-template-columns: auto 7rem 1fr; gap: 10px 24px; align-items: end; padding: 0 0 10px; border-bottom: 2px solid var(--fd-border); }
.pl-target { display: flex; gap: 12px; align-items: center; }
.pl-target img { image-rendering: pixelated; }
.pl-target strong { display: block; font-size: 30px; line-height: 40px; color: var(--fd-heading-ink); text-shadow: none; letter-spacing: 0; }
.pl-amount input { width: 100%; }
.pl-search { position: relative; }
.pl-search input { width: 100%; }
.pl-list { position: absolute; z-index: 20; left: 0; right: 0; top: 100%; max-height: 22rem; overflow-y: auto; margin: 0; padding: 4px; list-style: none; background: #100010; box-shadow: inset 0 0 0 2px #2d0a63, 4px 4px 0 #000; }
.pl-list li { display: flex; gap: 10px; align-items: center; padding: 2px 6px; margin: 0; cursor: pointer; font-size: 20px; line-height: 30px; color: #fff; text-shadow: 2px 2px 0 #3f3f3f; }
.pl-list li.hi, .pl-list li:hover { background: #2d0a63; color: #ffff55; }
.pl-list li.none { color: #a8a8a8; cursor: default; }
.pl-list img { image-rendering: pixelated; }
.pl-body { display: grid; grid-template-columns: minmax(0, 1fr); gap: 10px; margin-top: 10px; }
.pl-side { order: -1; display: grid; grid-template-columns: repeat(auto-fit, minmax(17rem, 1fr)); gap: 0 24px; align-items: start; }
.pl-body .ref-h { margin-top: 10px; }
.pl-tree .ref-board { padding: 8px 12px; }
.pl-tree ul { list-style: none; margin: 0; padding: 0; }
.pl-tree ul ul { margin-left: 14px; padding-left: 14px; border-left: 2px solid var(--fd-frame-light); }
.pl-tree li { margin: 0; }
.pl-row { display: flex; flex-wrap: wrap; gap: 4px 10px; align-items: center; padding: 4px 0; border-bottom: 2px solid var(--fd-border); }
.pl-tog { all: unset; width: 24px; height: 24px; display: grid; place-items: center; font-size: 20px; line-height: 1; color: #fff; text-shadow: 2px 2px 0 #3f3f3f; border-style: solid; border-width: 4px; border-image: url('/FarmersDelight-Neoforged/ui/button.png') 3 fill / 4px stretch; image-rendering: pixelated; cursor: pointer; box-sizing: border-box; }
.pl-tog:hover, .pl-tog:focus-visible { border-image-source: url('/FarmersDelight-Neoforged/ui/button-hover.png'); color: #ffffa0; }
.pl-leaf { border: 0; cursor: default; }
.pl-icon img, .pl-station img, .pl-tool img { image-rendering: pixelated; display: block; }
.pl-noicon { display: inline-block; }
.pl-qty { min-width: 3.5rem; text-align: right; font-size: 20px; color: var(--sl-color-white); }
.pl-name { flex: 1 1 12rem; display: flex; flex-wrap: wrap; align-items: center; gap: 0 10px; font-size: 20px; line-height: 30px; min-width: 0; color: var(--sl-color-white); }
.pl-name a { color: var(--sl-color-white); text-decoration: underline; text-decoration-thickness: 2px; text-underline-offset: 4px; text-decoration-color: var(--fd-border); }
.pl-name a:hover { color: var(--sl-color-text-accent); }
.pl-name small { font-size: 20px; color: var(--sl-color-gray-3); }
.pl-tag { max-width: 14rem; }
.pl-how { margin-left: auto; display: flex; gap: 8px; align-items: center; justify-content: flex-end; flex-wrap: wrap; max-width: 100%; }
.pl-how select { max-width: 16rem; }
.pl-station { display: inline-flex; gap: 6px; align-items: center; font-size: 20px; color: var(--sl-color-gray-2); white-space: nowrap; }
.pl-rawtag { font-size: 20px; color: var(--fd-green); }
.pl-tool { display: flex; gap: 6px; align-items: center; font-size: 20px; color: var(--sl-color-gray-3); padding: 2px 0 2px 34px; }
.pl-side .mc-panel { display: block; color: #404040; padding: 10px 12px; }
.pl-shop { width: 100%; border-collapse: collapse; margin: 0; }
.pl-shop td { padding: 2px 4px; border: 0; vertical-align: middle; font-size: 20px; line-height: 24px; color: #404040; background: none; }
.pl-shop td.ic { width: 40px; padding-left: 0; }
.pl-shop .slot { position: relative; display: grid; place-items: center; width: 36px; height: 36px; background: #8b8b8b; box-shadow: inset 2px 2px 0 #373737, inset -2px -2px 0 #fff; }
.pl-shop .slot img { image-rendering: pixelated; display: block; }
.pl-shop .slot b { position: absolute; right: 1px; bottom: -3px; color: #fff; text-shadow: 2px 2px 0 #3f3f3f; letter-spacing: 0; font-size: 20px; line-height: 20px; }
.pl-shop a { color: #404040; text-decoration: underline; text-decoration-thickness: 2px; text-underline-offset: 4px; }
.pl-shop a:hover { color: #000; }
.pl-shop small { font-size: 20px; color: #707070; }
.pl-note { font-size: 20px; line-height: 30px; color: var(--sl-color-gray-3); margin-top: 14px; }
@media (max-width: 40rem) {
  .pl-pick { grid-template-columns: 1fr 6rem; }
  .pl-search { grid-column: 1 / -1; }
  .pl-how:has(select) { margin-left: 0; width: 100%; justify-content: flex-start; flex-wrap: nowrap; }
  .pl-how select { flex: 1 1 auto; width: 0; max-width: none; }
  .pl-name { flex: 1 1 8rem; }
  .pl-tag { max-width: 100%; }
  .pl-tree ul ul { margin-left: 6px; padding-left: 8px; }
  .pl-qty { min-width: 0; }
}
`}</style>
		</div>
	);
}
