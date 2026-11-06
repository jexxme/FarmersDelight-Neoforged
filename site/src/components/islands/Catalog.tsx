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
.cat-bar { display: flex; flex-wrap: wrap; gap: 0; align-items: stretch; position: sticky; top: var(--sl-nav-height); z-index: 5; background: var(--sl-color-black); padding: .5rem 0; border-bottom: 1px solid var(--fd-rule); }
.cat-search { flex: 1 1 16rem; padding: .55rem .2rem; border: 0; border-bottom: 1px solid transparent; background: transparent; color: var(--sl-color-white); font: inherit; font-size: 1.05rem; border-radius: 0; }
.cat-search:focus { outline: none; border-bottom-color: var(--sl-color-accent); }
.catalog select { padding: .4rem .5rem; border: 1px solid var(--fd-border); border-radius: 0; background: var(--sl-color-black); color: var(--sl-color-white); font-family: var(--fd-font-mono); font-size: var(--sl-text-xs); margin-left: .75rem; }
.cat-check { display: inline-flex; gap: .35rem; align-items: center; font-family: var(--fd-font-mono); font-size: var(--sl-text-xs); color: var(--sl-color-gray-2); margin-left: .9rem; }
.cat-chips { display: flex; flex-wrap: wrap; gap: 0 1.1rem; margin-top: .6rem; }
.cat-chips button { border: 0; border-bottom: 2px solid transparent; background: none; color: var(--sl-color-gray-2); padding: .2rem 0; font: inherit; font-size: var(--sl-text-sm); cursor: pointer; border-radius: 0; }
.cat-chips button:hover { color: var(--sl-color-white); }
.cat-chips button span { font-family: var(--fd-font-mono); font-size: .8em; color: var(--sl-color-gray-4); margin-left: .25rem; }
.cat-chips button.on { color: var(--sl-color-accent); border-bottom-color: var(--sl-color-accent); }
.cat-chips .clear { color: var(--sl-color-gray-3); font-family: var(--fd-font-mono); font-size: var(--sl-text-xs); }
.cat-count { font-family: var(--fd-font-mono); color: var(--sl-color-gray-3); font-size: var(--sl-text-xs); margin: .9rem 0 0; }
.cat-h { font-family: var(--fd-font-display); font-weight: 400; font-size: var(--sl-text-h3); margin: 2.5rem 0 0; padding-top: .5rem; border-top: 2px solid var(--fd-rule); display: flex; align-items: baseline; gap: .75rem; flex-wrap: wrap; scroll-margin-top: 8rem; color: var(--sl-color-white); }
.cat-h span { font-family: var(--fd-font-mono); font-size: var(--sl-text-xs); color: var(--sl-color-gray-3); font-weight: 400; }
.cat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(8rem, 1fr)); margin-top: .75rem; border-top: 1px solid var(--fd-border); border-left: 1px solid var(--fd-border); }
.cat-tile { display: flex; flex-direction: column; align-items: center; gap: .3rem; padding: .85rem .4rem .6rem; border-right: 1px solid var(--fd-border); border-bottom: 1px solid var(--fd-border); text-decoration: none; color: var(--sl-color-white); text-align: center; }
.cat-tile:hover { background: var(--sl-color-accent-low); }
.cat-tile:hover .cat-name { color: var(--sl-color-accent); }
.cat-icon { width: 48px; height: 48px; display: grid; place-items: center; }
.cat-icon img { width: 48px; height: 48px; }
.cat-name { font-size: var(--sl-text-sm); line-height: 1.25; }
.cat-food { font-family: var(--fd-font-mono); font-size: var(--sl-text-xs); color: var(--fd-gold); font-variant-numeric: tabular-nums; }
.cat-food small { color: var(--sl-color-gray-3); }
@media (max-width: 30rem) { .cat-grid { grid-template-columns: repeat(auto-fill, minmax(6rem, 1fr)); } .cat-check { margin-left: 0; margin-right: .9rem; } .catalog select { margin-left: 0; } }
`}</style>
		</div>
	);
}
