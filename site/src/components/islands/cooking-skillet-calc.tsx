/** @jsxImportSource preact */
// Skillet cook-time calculator: recipe ticks x Fire Aspect level -> ticks and seconds, using the same formula as
// SkilletBlock#getSkilletCookingTime (imported from src/data/code/cooking.ts).
import { useState } from 'preact/hooks';
import { skilletTicks, skillet } from '../../data/code/cooking';

type R = { id: string; name: string; icon: string; ticks: number };

const fmt = (t: number) => `${+(t / 20).toFixed(2)} s`;

export default function SkilletCalc({ recipes }: { recipes: R[] }) {
	const [sel, setSel] = useState(recipes[0]?.id ?? 'custom');
	const [custom, setCustom] = useState(600);
	const [fa, setFa] = useState(0);
	const r = recipes.find((x) => x.id === sel);
	const ticks = r ? r.ticks : Math.max(0, Math.floor(custom || 0));
	const out = skilletTicks(ticks, fa);
	const time = ticks > 0 ? ticks : skillet.defaultTicks.value;
	const secs = Math.trunc(time / 20);
	const factor = Math.fround(Math.fround(skillet.factor.value) - (fa > 0 ? Math.fround(fa * skillet.perFireAspect.value) : 0));
	const raw = Math.trunc(Math.fround(secs * factor)) * 20;
	const levels = [0, 1, 2, 3];
	const max = Math.max(ticks, 1);

	return (
		<div class="skc not-content">
			<div class="row">
				<span class="lbl">Recipe</span>
				<div class="opts">
					{recipes.map((x) => (
						<button type="button" class={`mc-button ${sel === x.id ? 'on' : ''}`} aria-pressed={sel === x.id} onClick={() => setSel(x.id)} title={`${x.name}: ${x.ticks} ticks`}>
							<img src={x.icon} alt="" width={24} height={24} />
							<span>{x.name}</span>
						</button>
					))}
					<button type="button" class={`mc-button ${sel === 'custom' ? 'on' : ''}`} aria-pressed={sel === 'custom'} onClick={() => setSel('custom')}>
						<span>Custom</span>
					</button>
				</div>
			</div>
			{sel === 'custom' && (
				<label class="row">
					<span class="lbl">Recipe ticks</span>
					<input type="number" min={0} step={20} value={custom} onInput={(e) => setCustom(+(e.target as HTMLInputElement).value)} />
				</label>
			)}
			<div class="row">
				<span class="lbl">Fire Aspect</span>
				<div class="opts">
					{[0, 1, 2, 3, 4].map((l) => (
						<button type="button" class={`mc-button ${fa === l ? 'on' : ''}`} aria-pressed={fa === l} onClick={() => setFa(l)}>
							{l === 0 ? 'none' : ['I', 'II', 'III', 'IV'][l - 1]}
						</button>
					))}
				</div>
			</div>

			<div class="result">
				<div>
					<span class="lbl">Skillet</span>
					<span class="big">{out}</span> <span class="unit">ticks · {fmt(out)}</span>
				</div>
				<div>
					<span class="lbl">Campfire / Stove</span>
					<span class="big dim">{ticks}</span> <span class="unit">ticks · {fmt(ticks)}</span>
				</div>
				<div>
					<span class="lbl">Speed-up</span>
					<span class="big dim">{out > 0 ? `×${+(ticks / out).toFixed(2)}` : '—'}</span>
				</div>
			</div>

			<ol class="steps">
				<li>
					seconds = {ticks > 0 ? '' : `(0 → ${skillet.defaultTicks.value}) `}floor({time} / 20) = <b>{secs}</b>
				</li>
				<li>
					factor = {skillet.factor.value}
					{fa > 0 ? ` − ${skillet.perFireAspect.value} × ${fa}` : ''} = <b>{+factor.toFixed(4)}</b>
				</li>
				<li>
					floor({secs} × {+factor.toFixed(4)}) × 20 = <b>{raw}</b> ticks
				</li>
				<li>
					clamp to [{skillet.minTime.value}, {ticks}] = <b>{out}</b> ticks
				</li>
			</ol>

			<div class="bars" aria-label="Cook time by Fire Aspect level">
				<div class="bar">
					<span class="bl">Campfire</span>
					<span class="track">
						<span class="fill dim" style={{ width: `${(ticks / max) * 100}%` }} />
					</span>
					<span class="bv">{fmt(ticks)}</span>
				</div>
				{levels.map((l) => {
					const t = skilletTicks(ticks, l);
					return (
						<div class={`bar ${l === fa ? 'cur' : ''}`}>
							<span class="bl">Skillet {l ? ['I', 'II', 'III'][l - 1] : ''}</span>
							<span class="track">
								<span class="fill" style={{ width: `${(t / max) * 100}%` }} />
							</span>
							<span class="bv">{fmt(t)}</span>
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
.skc input { width: 8rem; padding: 2px 8px; border: 2px solid #a0a0a0; background: #000; color: #e0e0e0; font: inherit; border-radius: 0; }
.skc .result { display: flex; flex-wrap: wrap; gap: 8px 40px; padding: 12px 0; border-top: 2px solid var(--fd-border); }
.skc .result .lbl { display: block; }
.skc .big { font-size: 40px; line-height: 50px; color: var(--sl-color-text-accent); }
.skc .big.dim { color: var(--fd-heading-ink); font-size: 30px; }
.skc .unit { color: var(--sl-color-gray-3); }
.skc .steps { color: var(--sl-color-gray-2); margin: 4px 0 16px; padding-left: 30px; }
.skc .steps b { color: var(--fd-heading-ink); }
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
