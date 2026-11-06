/** @jsxImportSource preact */
import { useMemo, useState } from 'preact/hooks';

export type FoodEffect = { id: string; name: string; icon: string; ticks: number; amp: number; prob: number; note?: string };
export type FoodRow = {
	id: string;
	name: string;
	href: string | null;
	icon: string | null;
	vanilla: boolean;
	category: string;
	nutrition: number;
	saturation: number;
	seconds: number;
	drink: boolean;
	alwaysEdible: boolean;
	effects: FoodEffect[];
	other: string[];
	stack: number;
	container: { id: string; name: string; icon: string | null } | null;
	nourishment: number;
	/** Set when the food doesn't do what its tooltip says right now. */
	issue: string | null;
};
type Key = 'name' | 'nutrition' | 'saturation' | 'ratio' | 'seconds' | 'nourishment' | 'stack' | 'perStack' | 'satStack';

const fmt = (n: number, d = 2) => String(+n.toFixed(d));
const dur = (t: number) => {
	const s = Math.round(t / 20);
	return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
};
const roman = (n: number) => ['', ' II', ' III', ' IV', ' V'][n] ?? ` ${n + 1}`;

export default function FoodTable({ rows, cats, maxS, shank }: { rows: FoodRow[]; cats: Record<string, string>; maxS: number; shank: { full: string; half: string } }) {
	const [q, setQ] = useState('');
	const [vanilla, setVanilla] = useState(false);
	const [nour, setNour] = useState(false);
	const [cat, setCat] = useState('');
	const [sort, setSort] = useState<{ key: Key; dir: 1 | -1 }>({ key: 'saturation', dir: -1 });

	const val = (r: FoodRow, k: Key): number | string => {
		switch (k) {
			case 'name':
				return r.name;
			case 'ratio':
				return r.nutrition ? r.saturation / r.nutrition : 0;
			case 'perStack':
				return r.nutrition * r.stack;
			case 'satStack':
				return r.saturation * r.stack;
			default:
				return r[k];
		}
	};
	const shown = useMemo(() => {
		const words = q.toLowerCase().split(/\s+/).filter(Boolean);
		const out = rows.filter(
			(r) =>
				(vanilla || !r.vanilla) &&
				(!nour || r.nourishment > 0) &&
				(!cat || r.category === cat) &&
				words.every((w) => r.name.toLowerCase().includes(w) || r.id.includes(w) || r.effects.some((e) => e.name.toLowerCase().includes(w))),
		);
		return out.sort((a, b) => {
			const x = val(a, sort.key);
			const y = val(b, sort.key);
			const c = typeof x === 'string' ? x.localeCompare(y as string) : (x as number) - (y as number);
			return c * sort.dir || a.name.localeCompare(b.name);
		});
	}, [q, vanilla, nour, cat, sort]);

	const catKeys = [...new Set(rows.filter((r) => vanilla || !r.vanilla).map((r) => r.category))];
	const head = (k: Key, label: string, title?: string) => (
		<th scope="col" aria-sort={sort.key === k ? (sort.dir === 1 ? 'ascending' : 'descending') : 'none'} title={title}>
			<button type="button" onClick={() => setSort((s) => ({ key: k, dir: s.key === k ? (-s.dir as 1 | -1) : k === 'name' || k === 'seconds' ? 1 : -1 }))}>
				{label}
				<span class="arrow" aria-hidden="true">
					{sort.key === k ? (sort.dir === 1 ? '↑' : '↓') : '·'}
				</span>
			</button>
		</th>
	);

	return (
		<div class="food not-content">
			<div class="ref-bar food-bar">
				<input class="ref-field" type="search" placeholder="Name or effect" value={q} onInput={(e) => setQ((e.target as HTMLInputElement).value)} aria-label="Filter foods" />
				<select class="ref-field" value={cat} onChange={(e) => setCat((e.target as HTMLSelectElement).value)} aria-label="Category">
					<option value="">All kinds</option>
					{catKeys.map((k) => (
						<option value={k} key={k}>
							{cats[k] ?? k}
						</option>
					))}
				</select>
				<label class="ref-check">
					<input type="checkbox" checked={nour} onChange={() => setNour(!nour)} /> Nourishment
				</label>
				<label class="ref-check">
					<input
						type="checkbox"
						checked={vanilla}
						onChange={() => {
							if (vanilla && cat === 'vanilla') setCat('');
							setVanilla(!vanilla);
						}}
					/>{' '}
					Vanilla food too
				</label>
			</div>
			<p class="ref-count food-count">
				<b>{shown.length}</b> foods. Click a column heading to sort.
			</p>
			<div class="food-scroll ref-board">
				<table class="ref-table">
					<thead>
						<tr>
							{head('name', 'Food')}
							{head('nutrition', 'Hunger', 'Hunger points restored (2 = one shank)')}
							{head('saturation', 'Saturation', 'Saturation points added (capped by your hunger level)')}
							{head('seconds', 'Eat time')}
							{head('nourishment', 'Effects', 'Sorts by Nourishment duration')}
							{head('stack', 'Stack')}
						</tr>
					</thead>
					<tbody>
						{shown.map((r) => (
							<tr key={r.id} class={r.vanilla ? 'v' : ''}>
								<th scope="row">
									<a class="food-name" href={r.href ?? undefined} data-tip-items={`${r.name}~${r.id}`}>
										{r.icon ? <img class="ref-icon" src={r.icon} alt="" width={32} height={32} loading="lazy" /> : <span class="noicon" />}
										<span>
											{r.name}
											<small>
												{r.vanilla ? 'Minecraft' : cats[r.category] ?? r.category}
												{r.container && <span title={`Returns a ${r.container.name}`}> + {r.container.name}</span>}
											</small>
										</span>
									</a>
								</th>
								<td class="num hunger">
									<span class="shanks" aria-hidden="true">
										{Array.from({ length: Math.floor(r.nutrition / 2) }, (_, i) => (
											<img src={shank.full} alt="" width={18} height={18} key={i} />
										))}
										{r.nutrition % 2 === 1 && <img src={shank.half} alt="" width={18} height={18} />}
									</span>
									{r.nutrition}
								</td>
								<td class="num">
									<span class="bar" aria-hidden="true">
										<span style={{ width: `${(r.saturation / maxS) * 100}%` }} />
									</span>
									{fmt(r.saturation, 1)}
								</td>
								<td class="num">
									{fmt(r.seconds)} s{r.drink && <small> drink</small>}
								</td>
								<td class="eff">
									{r.effects.map((e) => (
										<span class={`chip${e.id === 'farmersdelight:nourishment' ? ' gold' : ''}`} key={e.id} title={e.note}>
											<img src={e.icon} alt="" width={18} height={18} onError={(ev) => ((ev.target as HTMLImageElement).style.display = 'none')} />
											{e.name}
											{roman(e.amp)} <i>{dur(e.ticks)}</i>
											{e.prob < 1 && <em>{Math.round(e.prob * 100)}%</em>}
											{e.note && <sup>*</sup>}
										</span>
									))}
									{r.other.map((o) => (
										<span class="chip plain" key={o}>
											{o}
										</span>
									))}
									{r.alwaysEdible && <span class="chip plain">Always edible</span>}
									{r.issue && (
										<a class="chip issue" href="../../differences/#known-issues-in-134" title={r.issue}>
											Known issue in 1.3.4
										</a>
									)}
								</td>
								<td class="num">{r.stack}</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			{shown.length === 0 && <p>No food matches.</p>}
			<style>{`
.food { margin-top: 20px; }
.food-bar { flex-wrap: wrap; }
.food-bar select { flex: 0 1 12rem; }
.food-count { margin: 10px 0; }
.food-scroll { overflow: auto; max-height: 80vh; }
.food .ref-table { border-collapse: separate; border-spacing: 0; }
.food thead th { position: sticky; top: 0; z-index: 2; padding: 0; }
.food thead th button { all: unset; cursor: pointer; display: flex; gap: 6px; align-items: center; padding: 4px 10px; font-size: 20px; color: var(--fd-paper); }
.food thead th button:hover, .food thead th button:focus-visible { color: #fff; }
.food .arrow { color: var(--fd-frame-light); }
.food th[aria-sort=ascending] button, .food th[aria-sort=descending] button, .food th[aria-sort=ascending] .arrow, .food th[aria-sort=descending] .arrow { color: #ffff55; }
.food thead th:first-child { left: 0; z-index: 3; }
.food tbody th { position: sticky; left: 0; z-index: 1; background: var(--fd-surface); font-weight: 400; min-width: 15rem; border-right: 2px solid var(--fd-border); }
.food tbody tr:hover > * { background: var(--fd-surface-2); }
.food tr.v > * { background: var(--sl-color-black); }
.food tr.v .food-name small { color: var(--sl-color-text-accent); }
.food-name { display: flex; gap: 10px; align-items: center; color: var(--sl-color-white); text-decoration: none; line-height: 24px; }
a.food-name[href]:hover > span { color: var(--sl-color-text-accent); text-decoration: underline; text-decoration-thickness: 2px; text-underline-offset: 4px; }
.food-name .noicon { width: 32px; height: 32px; flex: none; }
.food-name small { display: block; color: var(--sl-color-gray-3); font-size: 20px; }
.food .num { white-space: nowrap; color: var(--sl-color-white); }
.food .num small { color: var(--sl-color-gray-3); font-size: 20px; }
.food .shanks { display: inline-flex; vertical-align: -3px; margin-right: 6px; min-width: 126px; }
.food .shanks img { image-rendering: pixelated; }
.food .bar { display: inline-block; width: 60px; height: 10px; margin-right: 8px; background: var(--sl-color-black); border: 2px solid var(--fd-frame); vertical-align: 1px; }
.food .bar > span { display: block; height: 100%; background: var(--fd-gold); }
.food .eff { min-width: 18rem; line-height: 24px; }
.food .chip { display: inline-flex; align-items: center; gap: 4px; margin: 2px 8px 2px 0; padding: 0 6px 0 2px; border: 2px solid var(--fd-border); font-size: 20px; white-space: nowrap; color: var(--sl-color-white); background: var(--sl-color-black); }
.food .chip.gold { border-color: var(--fd-gold); }
.food .chip.plain { padding-left: 6px; color: var(--sl-color-gray-2); }
.food .chip.issue { padding-left: 6px; border-color: var(--fd-gold); color: var(--fd-gold); text-decoration: none; }
.food .chip.issue:hover { color: #fff; }
.food .chip img { image-rendering: pixelated; }
.food .chip i { font-style: normal; color: var(--sl-color-gray-3); }
.food .chip em { font-style: normal; color: var(--fd-gold); }
@media (max-width: 40rem) { .food tbody th { min-width: 8.5rem; padding: 4px 6px; } .food-name { gap: 6px; } .food-name small { display: none; } .food .shanks { display: none; } }
`}</style>
		</div>
	);
}
