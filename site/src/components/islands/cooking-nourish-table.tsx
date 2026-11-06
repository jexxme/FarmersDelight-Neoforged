/** @jsxImportSource preact */
// Sortable, filterable table of every food that grants Nourishment (rows built from items.json on the page).
import { useMemo, useState } from 'preact/hooks';

type Row = { id: string; name: string; icon: string; href: string; ticks: number; tier: string; nutrition: number; saturation: number; category: string };
type Key = 'name' | 'ticks' | 'nutrition' | 'saturation' | 'category';

const time = (t: number) => {
	const s = t / 20;
	return s >= 60 ? `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}` : `0:${String(s).padStart(2, '0')}`;
};

export default function NourishTable({ rows, tiers }: { rows: Row[]; tiers: [string, number][] }) {
	const [key, setKey] = useState<Key>('ticks');
	const [desc, setDesc] = useState(true);
	const [tier, setTier] = useState<string | null>(null);
	const list = useMemo(() => {
		const l = rows.filter((r) => !tier || r.tier === tier);
		return [...l].sort((a, b) => {
			const x = a[key];
			const y = b[key];
			const c = typeof x === 'string' ? x.localeCompare(y as string) : (x as number) - (y as number);
			return (desc ? -c : c) || a.name.localeCompare(b.name);
		});
	}, [key, desc, tier]);
	const head = (k: Key, label: string, num = false) => (
		<th class={num ? 'num' : ''} aria-sort={key === k ? (desc ? 'descending' : 'ascending') : 'none'}>
			<button
				type="button"
				onClick={() => {
					if (key === k) setDesc(!desc);
					else {
						setKey(k);
						setDesc(num);
					}
				}}
			>
				{label}
				<span class="ar">{key === k ? (desc ? '↓' : '↑') : ''}</span>
			</button>
		</th>
	);
	return (
		<div class="nt not-content">
			<div class="chips" role="group" aria-label="Filter by duration tier">
				<button type="button" class={`mc-button ${!tier ? 'on' : ''}`} onClick={() => setTier(null)}>
					all {rows.length}
				</button>
				{tiers.map(([t, ticks]) => (
					<button type="button" class={`mc-button ${tier === t ? 'on' : ''}`} onClick={() => setTier(tier === t ? null : t)}>
						{t.toLowerCase()} {time(ticks)} · {rows.filter((r) => r.tier === t).length}
					</button>
				))}
			</div>
			<div class="scroll">
				<table>
					<thead>
						<tr>
							{head('name', 'Food')}
							{head('ticks', 'Nourishment', true)}
							{head('nutrition', 'Hunger', true)}
							{head('saturation', 'Saturation', true)}
							{head('category', 'Kind')}
						</tr>
					</thead>
					<tbody>
						{list.map((r) => (
							<tr key={r.id}>
								<td>
									<a href={r.href} class="it">
										<img src={r.icon} alt="" width={22} height={22} />
										{r.name}
									</a>
								</td>
								<td class="num">
									<span class="bar" style={{ width: `${(r.ticks / rows.reduce((m, x) => Math.max(m, x.ticks), 1)) * 4}rem` }} />
									{time(r.ticks)}
								</td>
								<td class="num">{r.nutrition}</td>
								<td class="num">{+r.saturation.toFixed(1)}</td>
								<td class="cat">{r.category}</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			<style>{`
.nt { margin: 30px 0; font-size: 20px; line-height: 30px; }
.nt .chips { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.nt .chips button { padding: 2px 12px 4px; cursor: pointer; }
.nt .chips button.on { border-image-source: url('/FarmersDelight-Neoforged/ui/button-hover.png'); color: #ffffa0 !important; }
.nt .scroll { overflow-x: auto; background: var(--fd-surface); border: 3px solid var(--fd-frame); box-shadow: 4px 4px 0 #000; }
.nt table { width: 100%; border-collapse: collapse; }
.nt th { text-align: left; background: var(--fd-frame); border-bottom: 3px solid #000; padding: 0; white-space: nowrap; font-weight: 400; }
.nt th button { font: inherit; color: var(--fd-paper); background: transparent; border: 0; padding: 4px 10px; cursor: pointer; width: 100%; text-align: inherit; }
.nt th.num button { text-align: right; }
.nt th[aria-sort='ascending'] button, .nt th[aria-sort='descending'] button { color: #ffffa0; }
.nt .ar { display: inline-block; width: 14px; }
.nt td { border-bottom: 2px solid var(--fd-border); padding: 4px 10px; }
.nt .it { display: inline-flex; gap: 8px; align-items: center; color: var(--sl-color-text-accent); text-decoration: none; white-space: nowrap; }
.nt .it:hover { color: var(--fd-heading-ink); }
.nt .it img { image-rendering: pixelated; }
.nt .num { text-align: right; white-space: nowrap; }
.nt .bar { display: inline-block; height: 10px; background: var(--fd-gold); margin-right: 10px; vertical-align: middle; }
.nt .cat { color: var(--sl-color-gray-3); }
`}</style>
		</div>
	);
}
