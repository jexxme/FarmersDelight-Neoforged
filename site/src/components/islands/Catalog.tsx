/** @jsxImportSource preact */
import { useEffect, useMemo, useState } from 'preact/hooks';

type Row = {
	id: string;
	slug: string;
	name: string;
	category: string;
	icon: string;
	flat: boolean;
	nutrition: number | null;
	saturation: number | null;
	effects: string[];
	block: boolean;
	stack: number;
	tags: string[];
};
type Cat = { key: string; label: string; blurb: string; count: number };

export default function Catalog({ rows, cats, base }: { rows: Row[]; cats: Cat[]; base: string }) {
	const [q, setQ] = useState('');
	const [active, setActive] = useState<string[]>([]);
	const [food, setFood] = useState(false);
	const [blocks, setBlocks] = useState(false);
	const [sort, setSort] = useState<'tab' | 'name' | 'nutrition' | 'saturation'>('tab');

	useEffect(() => {
		const h = location.hash.slice(1);
		if (h && cats.some((c) => c.key === h)) setActive([h]);
	}, []);

	const filtered = useMemo(() => {
		const words = q.toLowerCase().split(/\s+/).filter(Boolean);
		let out = rows.filter(
			(r) =>
				(!active.length || active.includes(r.category)) &&
				(!food || r.nutrition !== null) &&
				(!blocks || r.block) &&
				words.every((w) => r.name.toLowerCase().includes(w) || r.id.includes(w) || r.effects.some((e) => e.toLowerCase().includes(w)) || r.tags.some((t) => t.includes(w))),
		);
		if (sort === 'name') out = [...out].sort((a, b) => a.name.localeCompare(b.name));
		if (sort === 'nutrition') out = [...out].sort((a, b) => (b.nutrition ?? -1) - (a.nutrition ?? -1));
		if (sort === 'saturation') out = [...out].sort((a, b) => (b.saturation ?? -1) - (a.saturation ?? -1));
		return out;
	}, [q, active, food, blocks, sort]);

	const grouped = sort === 'tab' && !q;
	const toggle = (k: string) => setActive((a) => (a.includes(k) ? a.filter((x) => x !== k) : [...a, k]));

	const tile = (r: Row) => (
		<a class="cat-tile" href={`${base}${r.slug}/`} key={r.id} data-tip-items={`${r.name}~${r.id}`}>
			<span class="cat-icon">
				<img src={r.icon} alt="" width={48} height={48} loading="lazy" style={{ imageRendering: 'pixelated' }} />
			</span>
			<span class="cat-name">{r.name}</span>
			{r.nutrition !== null && (
				<span class="cat-food">
					{r.nutrition} <small>/ {+r.saturation!.toFixed(1)}</small>
				</span>
			)}
		</a>
	);

	return (
		<div class="catalog not-content">
			<div class="cat-bar">
				<input class="cat-search" type="search" placeholder="Filter by name, id, effect or tag…" value={q} onInput={(e) => setQ((e.target as HTMLInputElement).value)} aria-label="Filter items" />
				<select value={sort} onChange={(e) => setSort((e.target as HTMLSelectElement).value as any)} aria-label="Sort">
					<option value="tab">Creative tab order</option>
					<option value="name">Name A–Z</option>
					<option value="nutrition">Most hunger restored</option>
					<option value="saturation">Most saturation</option>
				</select>
				<label class="cat-check">
					<input type="checkbox" checked={food} onChange={() => setFood(!food)} /> Food only
				</label>
				<label class="cat-check">
					<input type="checkbox" checked={blocks} onChange={() => setBlocks(!blocks)} /> Placeable blocks
				</label>
			</div>
			<div class="cat-chips" role="group" aria-label="Categories">
				{cats.map((c) => (
					<button type="button" class={active.includes(c.key) ? 'on' : ''} onClick={() => toggle(c.key)} aria-pressed={active.includes(c.key)} title={c.blurb}>
						{c.label} <span>{c.count}</span>
					</button>
				))}
				{active.length > 0 && (
					<button type="button" class="clear" onClick={() => setActive([])}>
						Clear
					</button>
				)}
			</div>
			<p class="cat-count">
				{filtered.length} of {rows.length} items
			</p>
			{grouped ? (
				cats
					.filter((c) => filtered.some((r) => r.category === c.key))
					.map((c) => (
						<section key={c.key} id={c.key}>
							<h2 class="cat-h">
								{c.label} <span>{c.blurb}</span>
							</h2>
							<div class="cat-grid">{filtered.filter((r) => r.category === c.key).map(tile)}</div>
						</section>
					))
			) : (
				<div class="cat-grid">{filtered.map(tile)}</div>
			)}
			{filtered.length === 0 && <p>No items match. Try fewer words, or clear the filters.</p>}
			<style>{`
.catalog { margin-top: 1.5rem; }
.cat-bar { display: flex; flex-wrap: wrap; gap: .6rem; align-items: center; position: sticky; top: calc(var(--sl-nav-height) + .5rem); z-index: 5; background: var(--sl-color-black); padding: .5rem 0; }
.cat-search { flex: 1 1 16rem; padding: .6rem .9rem; border-radius: .6rem; border: 1px solid var(--fd-border); background: var(--fd-surface); color: var(--sl-color-white); font-size: 1rem; }
.cat-search:focus { outline: 2px solid var(--sl-color-accent); outline-offset: 1px; }
.catalog select { padding: .55rem .6rem; border-radius: .6rem; border: 1px solid var(--fd-border); background: var(--fd-surface); color: var(--sl-color-white); }
.cat-check { display: inline-flex; gap: .35rem; align-items: center; font-size: var(--sl-text-sm); color: var(--sl-color-gray-2); }
.cat-chips { display: flex; flex-wrap: wrap; gap: .4rem; margin-top: .5rem; }
.cat-chips button { border: 1px solid var(--fd-border); background: var(--fd-surface); color: var(--sl-color-gray-2); border-radius: 2rem; padding: .3rem .75rem; font-size: var(--sl-text-sm); cursor: pointer; }
.cat-chips button span { color: var(--sl-color-gray-4); margin-left: .2rem; font-variant-numeric: tabular-nums; }
.cat-chips button.on { background: var(--sl-color-accent); border-color: var(--sl-color-accent); color: var(--sl-color-black); }
.cat-chips button.on span { color: inherit; opacity: .7; }
.cat-chips .clear { border-style: dashed; }
.cat-count { color: var(--sl-color-gray-3); font-size: var(--sl-text-sm); margin: .75rem 0 0; }
.cat-h { font-family: var(--fd-font-display); font-size: var(--sl-text-h3); margin: 2rem 0 .75rem; display: flex; align-items: baseline; gap: .75rem; flex-wrap: wrap; scroll-margin-top: 8rem; }
.cat-h span { font-family: var(--sl-font); font-size: var(--sl-text-sm); color: var(--sl-color-gray-3); font-weight: 400; }
.cat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(8.5rem, 1fr)); gap: .5rem; margin-top: .75rem; }
.cat-tile { display: flex; flex-direction: column; align-items: center; gap: .35rem; padding: .8rem .5rem .6rem; border-radius: .75rem; background: var(--fd-surface); border: 1px solid var(--fd-border); text-decoration: none; color: var(--sl-color-white); text-align: center; transition: transform .12s, border-color .12s, box-shadow .12s; }
.cat-tile:hover { transform: translateY(-2px); border-color: var(--sl-color-accent); box-shadow: 0 10px 20px -12px rgb(0 0 0 / .5); }
.cat-icon { width: 48px; height: 48px; display: grid; place-items: center; }
.cat-icon img { width: 48px; height: 48px; filter: drop-shadow(0 3px 3px rgb(0 0 0 / .25)); }
.cat-name { font-size: var(--sl-text-sm); font-weight: 550; line-height: 1.25; }
.cat-food { font-size: var(--sl-text-xs); color: var(--fd-gold); font-weight: 650; font-variant-numeric: tabular-nums; }
.cat-food small { color: var(--sl-color-gray-3); font-weight: 450; }
@media (max-width: 30rem) { .cat-grid { grid-template-columns: repeat(auto-fill, minmax(6.5rem, 1fr)); } }
`}</style>
		</div>
	);
}
