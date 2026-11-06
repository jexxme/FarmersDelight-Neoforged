/** @jsxImportSource preact */
// "Which wild crops can generate in biome X?" Data comes in as props from worldgen.json / biomes.json.
import { useMemo, useState } from 'preact/hooks';

type Biome = { id: string; name: string; temperature: number | null };
type Plant = { id: string; item: string; name: string; icon: string | null; rarity: number; count: number; biomes: string[]; anchor: string };

export default function BiomeFilter({ biomes, plants, initial }: { biomes: Biome[]; plants: Plant[]; initial?: string }) {
	const [sel, setSel] = useState(initial ?? 'minecraft:plains');
	const [q, setQ] = useState('');
	const here = useMemo(() => plants.filter((p) => p.biomes.includes(sel)).sort((a, b) => a.rarity - b.rarity), [sel]);
	const counts = useMemo(() => Object.fromEntries(biomes.map((b) => [b.id, plants.filter((p) => p.biomes.includes(b.id)).length])), []);
	const shown = biomes.filter((b) => !q || b.name.toLowerCase().includes(q.toLowerCase()));
	const cur = biomes.find((b) => b.id === sel);
	return (
		<div class="fdb not-content">
			<div class="fdb-side">
				<input type="search" placeholder="Find a biome" value={q} onInput={(e) => setQ((e.target as HTMLInputElement).value)} aria-label="Filter biomes" />
				<ul role="listbox" aria-label="Biomes">
					{shown.map((b) => (
						<li key={b.id}>
							<button type="button" role="option" aria-selected={b.id === sel} class={b.id === sel ? 'on' : ''} onClick={() => setSel(b.id)}>
								<span>{b.name}</span>
								<span class="c">{counts[b.id] || '·'}</span>
							</button>
						</li>
					))}
				</ul>
			</div>
			<div class="fdb-main" aria-live="polite">
				<div class="fdb-h">
					<span class="fdb-name">{cur?.name}</span>
					<span class="fdb-t">base temperature {cur?.temperature ?? '?'}</span>
				</div>
				{here.length === 0 ? (
					<p class="fdb-none">No Farmer's Delight plant generates here.</p>
				) : (
					<table>
						<thead>
							<tr>
								<th>Plant</th>
								<th>Patch chance</th>
								<th>Attempts</th>
							</tr>
						</thead>
						<tbody>
							{here.map((p) => (
								<tr key={p.id}>
									<td>
										<a href={`#${p.anchor}`}>
											{p.icon && <img src={p.icon} alt="" width={32} height={32} />}
											{p.name}
										</a>
									</td>
									<td class="m">1 in {p.rarity} chunks</td>
									<td class="m">{p.count}</td>
								</tr>
							))}
						</tbody>
					</table>
				)}
				<p class="fdb-foot">Counts on the left: plants allowed in that biome. Patches still need the right ground under open air, so cave and ocean biomes rarely see one.</p>
			</div>
		</div>
	);
}
