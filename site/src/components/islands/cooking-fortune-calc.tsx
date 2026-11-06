/** @jsxImportSource preact */
// Cutting Board chance results with Fortune. Each unit of a result is kept when nextFloat() <= chance + bonus x Fortune
// (ChanceResult#rollOutput); bonus is the cuttingBoardFortuneBonus config value (default passed in from config.json).
import { useState } from 'preact/hooks';

type Out = { name: string; icon: string; count: number; chance: number };
type Row = { id: string; input: string; icon: string; outs: Out[] };

const pct = (x: number) => `${+(x * 100).toFixed(1)}%`;

export default function FortuneCalc({ rows, bonusDefault, range }: { rows: Row[]; bonusDefault: number; range: [number, number] }) {
	const [fortune, setFortune] = useState(0);
	const [bonus, setBonus] = useState(bonusDefault);
	const eff = (c: number) => Math.min(1, c + bonus * fortune);
	return (
		<div class="fc not-content">
			<div class="ctl">
				<span class="lbl">Fortune</span>
				<div class="opts">
					{[0, 1, 2, 3].map((l) => (
						<button type="button" class={`mc-button ${fortune === l ? 'on' : ''}`} aria-pressed={fortune === l} onClick={() => setFortune(l)}>
							{l === 0 ? 'none' : ['I', 'II', 'III'][l - 1]}
						</button>
					))}
				</div>
				<label class="bonus">
					<span class="lbl">Bonus per level</span>
					<input type="range" min={range[0]} max={range[1]} step={0.05} value={bonus} onInput={(e) => setBonus(+(e.target as HTMLInputElement).value)} />
					<span class="v">{bonus.toFixed(2)}</span>
					{bonus !== bonusDefault && (
						<button type="button" class="mc-button reset" onClick={() => setBonus(bonusDefault)}>
							default {bonusDefault}
						</button>
					)}
				</label>
			</div>
			<div class="scroll">
				<table>
					<thead>
						<tr>
							<th>Input</th>
							<th>Result</th>
							<th>Base</th>
							<th>Per unit</th>
							<th>Expected</th>
							<th>At least one</th>
						</tr>
					</thead>
					<tbody>
						{rows.flatMap((r) =>
							r.outs.map((o, k) => {
								const p = eff(o.chance);
								return (
									<tr key={r.id + k}>
										<td>
											{k === 0 && (
												<span class="it">
													<img src={r.icon} alt="" width={20} height={20} />
													{r.input}
												</span>
											)}
										</td>
										<td>
											<span class="it">
												<img src={o.icon} alt="" width={20} height={20} />
												{o.count > 1 ? `${o.count}× ` : ''}
												{o.name}
											</span>
										</td>
										<td class="num">{pct(o.chance)}</td>
										<td class={`num ${p > o.chance ? 'up' : ''}`}>{pct(p)}</td>
										<td class="num">{+(o.count * p).toFixed(2)}</td>
										<td class="num">{pct(1 - (1 - p) ** o.count)}</td>
									</tr>
								);
							}),
						)}
					</tbody>
				</table>
			</div>
			<style>{`
.fc { margin: 30px 0; font-size: 20px; line-height: 30px; }
.fc .ctl { display: flex; flex-wrap: wrap; gap: 10px 24px; align-items: center; margin-bottom: 14px; }
.fc .lbl { color: var(--sl-color-gray-3); }
.fc .opts { display: flex; flex-wrap: wrap; gap: 6px; }
.fc .opts button { padding: 2px 14px 4px; cursor: pointer; }
.fc .opts button.on { border-image-source: url('/FarmersDelight-Neoforged/ui/button-hover.png'); color: #ffffa0 !important; }
.fc .bonus { display: inline-flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.fc .bonus input { accent-color: var(--fd-orange); width: 10rem; }
.fc .v { color: var(--fd-heading-ink); }
.fc .reset { padding: 2px 10px 4px; cursor: pointer; }
.fc .scroll { overflow-x: auto; background: var(--fd-surface); border: 3px solid var(--fd-frame); box-shadow: 4px 4px 0 #000; }
.fc table { width: 100%; border-collapse: collapse; }
.fc th { text-align: left; font-weight: 400; background: var(--fd-frame); color: var(--fd-paper); border-bottom: 3px solid #000; padding: 4px 10px; white-space: nowrap; }
.fc td { border-bottom: 2px solid var(--fd-border); padding: 4px 10px; vertical-align: middle; }
.fc .it { display: inline-flex; align-items: center; gap: 6px; white-space: nowrap; }
.fc .it img { image-rendering: pixelated; }
.fc .num { text-align: right; white-space: nowrap; }
.fc .num.up { color: var(--fd-green); }
`}</style>
		</div>
	);
}
