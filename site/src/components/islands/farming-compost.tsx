/** @jsxImportSource preact */
// Organic Compost calculator: the chance each time the compost gets a turn, and the average time to Rich Soil.
// All constants arrive as props (src/data/code/farming.ts).
import { useState } from 'preact/hooks';

type K = { activator: number; bright: number; dim: number; water: number; threshold: number; successes: number; cube: number; tickSpeed: number; section: number };

const fmt = (s: number) => {
	if (s < 120) return `${Math.round(s)} s`;
	if (s < 7200) return `${+(s / 60).toFixed(1)} min`;
	return `${+(s / 3600).toFixed(1)} h`;
};
const pct = (x: number) => `${+(x * 100).toFixed(1)}%`;

export default function CompostCalc({ k }: { k: K }) {
	const [sky, setSky] = useState(true);
	const [water, setWater] = useState(false);
	const [extra, setExtra] = useState(0);
	const [speed, setSpeed] = useState(k.tickSpeed);
	const activators = 1 + extra; // the compost block itself counts as an activator
	const parts = [
		{ label: `${activators} activator${activators > 1 ? 's' : ''}`, v: activators * k.activator },
		{ label: sky ? 'sunlight' : 'shade', v: sky ? k.bright : k.dim },
		{ label: 'water', v: water ? k.water : 0 },
	];
	const p = Math.min(1, parts.reduce((s, x) => s + x.v, 0));
	const ticksPer = k.section / Math.max(1, speed); // game ticks between two turns of one block (mean)
	const randomTicks = k.successes / p;
	const seconds = (randomTicks * ticksPer) / 20;
	const max = k.cube * k.activator + k.bright + k.water;
	return (
		<div class="fdc not-content">
			<div class="fdc-in">
				<label class="fdc-row">
					<input type="checkbox" checked={sky} onChange={() => setSky(!sky)} />
					<span>Open sky above it or a neighbour (night counts too; torches don't)</span>
				</label>
				<label class="fdc-row">
					<input type="checkbox" checked={water} onChange={() => setWater(!water)} />
					<span>Water touching it, even diagonally (source, flowing or a waterlogged block)</span>
				</label>
				<label class="fdc-col">
					<span>
						Activators around it (compost, Rich Soil, mushrooms…) in the 3×3×3 cube: <b>{extra}</b>
					</span>
					<input type="range" min={0} max={k.cube - 1} value={extra} onInput={(e) => setExtra(+(e.target as HTMLInputElement).value)} />
				</label>
				<label class="fdc-col">
					<span>
						Game rule randomTickSpeed: <b>{speed}</b> <small>(default {k.tickSpeed})</small>
					</span>
					<input type="range" min={1} max={30} value={speed} onInput={(e) => setSpeed(+(e.target as HTMLInputElement).value)} />
				</label>
			</div>
			<div class="fdc-out" aria-live="polite">
				<div class="fdc-big">
					<span class="fdc-k">chance each time it gets a turn</span>
					<span class="fdc-v">{pct(p)}</span>
				</div>
				<div class="fdc-bar" role="img" aria-label={`Chance ${pct(p)} of a maximum ${pct(max)}`}>
					{parts.map((x, i) => (x.v > 0 ? <span class={`s s${i}`} style={{ width: `${(x.v / max) * 100}%` }} title={`${x.label}: +${pct(x.v)}`} /> : null))}
				</div>
				<ul class="fdc-legend">
					{parts.map((x, i) => (
						<li>
							<i class={`s${i}`} /> {x.label} <span>+{pct(x.v)}</span>
						</li>
					))}
				</ul>
				<dl>
					<dt>steps to Rich Soil</dt>
					<dd>{k.successes}</dd>
					<dt>turns needed (average)</dt>
					<dd>{+randomTicks.toFixed(1)}</dd>
					<dt>time between turns (average)</dt>
					<dd>{fmt(ticksPer / 20)}</dd>
					<dt>time to Rich Soil (average)</dt>
					<dd class="fdc-hl">{fmt(seconds)}</dd>
				</dl>
			</div>
		</div>
	);
}
