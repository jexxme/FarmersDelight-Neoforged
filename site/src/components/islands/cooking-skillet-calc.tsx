/** @jsxImportSource preact */
// Skillet cook-time calculator: campfire time x Fire Aspect level -> Skillet time, using skilletTicks from
// src/data/code/cooking.ts. Recipe times come in as ticks; everything on screen is in seconds.
import { useState } from 'preact/hooks';
import { skilletTicks, skillet } from '../../data/code/cooking';

type R = { id: string; name: string; icon: string; ticks: number };

const s = (t: number) => `${+(t / 20).toFixed(2)} s`;
const pct = (x: number) => `${+(x * 100).toFixed(1)}%`;
const ROMAN = ['I', 'II', 'III', 'IV'];

export default function SkilletCalc({ recipes }: { recipes: R[] }) {
	const [sel, setSel] = useState(recipes[0]?.id ?? 'custom');
	const [custom, setCustom] = useState(30);
	const [fa, setFa] = useState(0);
	const r = recipes.find((x) => x.id === sel);
	const ticks = r ? r.ticks : Math.max(20, Math.floor(custom || 0) * 20);
	const out = skilletTicks(ticks, fa);
	const share = Math.max(0, skillet.factor.value - fa * skillet.perFireAspect.value);
	const secs = Math.trunc(ticks / 20);
	const raw = Math.trunc(Math.fround(secs * Math.fround(share))) * 20;
	// Fire Aspect goes up to II in survival (vanilla enchantment data)
	const levels = [0, 1, 2];
	const max = Math.max(ticks, 1);

	return (
		<div class="skc not-content">
			<div class="row">
				<span class="lbl">Food</span>
				<div class="opts">
					{recipes.map((x) => (
						<button type="button" class={`mc-button ${sel === x.id ? 'on' : ''}`} aria-pressed={sel === x.id} onClick={() => setSel(x.id)} title={x.name}>
							<img src={x.icon} alt="" width={24} height={24} />
							<span>{x.name}</span>
						</button>
					))}
					<button type="button" class={`mc-button ${sel === 'custom' ? 'on' : ''}`} aria-pressed={sel === 'custom'} onClick={() => setSel('custom')}>
						<span>Other</span>
					</button>
				</div>
			</div>
			{sel === 'custom' && (
				<label class="row">
					<span class="lbl">Campfire time</span>
					<input type="number" min={1} step={1} value={custom} onInput={(e) => setCustom(+(e.target as HTMLInputElement).value)} />
					<span class="unit">seconds</span>
				</label>
			)}
			<div class="row">
				<span class="lbl">Fire Aspect</span>
				<div class="opts">
					{levels.map((l) => (
						<button type="button" class={`mc-button ${fa === l ? 'on' : ''}`} aria-pressed={fa === l} onClick={() => setFa(l)}>
							{l === 0 ? 'none' : ROMAN[l - 1]}
						</button>
					))}
				</div>
			</div>

			<div class="result">
				<div>
					<span class="lbl">Skillet</span>
					<span class="big">{s(out)}</span>
				</div>
				<div>
					<span class="lbl">Campfire / Stove</span>
					<span class="big dim">{s(ticks)}</span>
				</div>
				<div>
					<span class="lbl">Faster by</span>
					<span class="big dim">{out > 0 ? `×${+(ticks / out).toFixed(2)}` : '—'}</span>
				</div>
			</div>

			<p class="how">
				{secs} s × {pct(share)} = {+(secs * share).toFixed(2)} s
				{Math.abs(raw - secs * share * 20) > 1e-6 ? <>, rounded down to {s(raw)}</> : null}
				{out > raw ? <>, raised to the {s(skillet.minTime.value)} minimum</> : null}
				{out < raw ? <>, capped at the campfire time</> : null}
			</p>

			<div class="bars" aria-label="Cook time by Fire Aspect level">
				<div class="bar">
					<span class="bl">Campfire</span>
					<span class="track">
						<span class="fill dim" style={{ width: `${(ticks / max) * 100}%` }} />
					</span>
					<span class="bv">{s(ticks)}</span>
				</div>
				{levels.map((l) => {
					const t = skilletTicks(ticks, l);
					return (
						<div class={`bar ${l === fa ? 'cur' : ''}`}>
							<span class="bl">Skillet {l ? ROMAN[l - 1] : ''}</span>
							<span class="track">
								<span class="fill" style={{ width: `${(t / max) * 100}%` }} />
							</span>
							<span class="bv">{s(t)}</span>
						</div>
					);
				})}
			</div>
			<style>{`
.skc { margin: 30px 0; padding: 16px 18px; background: var(--fd-surface); border: 3px solid var(--fd-frame); box-shadow: 4px 4px 0 #000; font-size: 20px; line-height: 30px; }
.skc .row { display: flex; flex-wrap: wrap; gap: 8px 16px; align-items: center; margin-bottom: 14px; }
.skc .lbl { color: var(--sl-color-gray-3); min-width: 9rem; display: inline-block; }
.skc .opts { display: flex; flex-wrap: wrap; gap: 6px; }
.skc .opts button { gap: 6px; padding: 2px 12px 4px; min-height: 40px; cursor: pointer; }
.skc .opts button.on { border-image-source: url('/FarmersDelight-Neoforged/ui/button-hover.png'); color: #ffffa0 !important; }
.skc .opts img { image-rendering: pixelated; }
.skc input { width: 6rem; padding: 2px 8px; border: 2px solid #a0a0a0; background: #000; color: #e0e0e0; font: inherit; border-radius: 0; }
.skc .result { display: flex; flex-wrap: wrap; gap: 8px 40px; padding: 12px 0; border-top: 2px solid var(--fd-border); }
.skc .result .lbl { display: block; }
.skc .big { font-size: 40px; line-height: 50px; color: var(--sl-color-text-accent); }
.skc .big.dim { color: var(--fd-heading-ink); font-size: 30px; }
.skc .unit { color: var(--sl-color-gray-3); }
.skc .how { color: var(--sl-color-gray-2); margin: 4px 0 16px; }
.skc .bars { display: grid; gap: 6px; }
.skc .bar { display: grid; grid-template-columns: 9rem 1fr 5rem; gap: 10px; align-items: center; color: var(--sl-color-gray-3); }
.skc .bar.cur { color: var(--fd-heading-ink); }
.skc .track { height: 20px; background: #000; border: 2px solid var(--sl-color-gray-4); display: block; }
.skc .fill { display: block; height: 100%; background: var(--fd-orange); }
.skc .fill.dim { background: var(--sl-color-gray-4); }
.skc .bv { text-align: right; }
@media (max-width: 30rem) { .skc .bar { grid-template-columns: 6rem 1fr 4rem; } }
`}</style>
		</div>
	);
}
