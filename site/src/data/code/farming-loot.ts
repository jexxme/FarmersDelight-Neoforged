// Loot tables for the farming pages: loot.json (generated) plus the details it does not carry yet, and helpers that
// put drops and conditions into words for players.
// scripts/extract.mjs only reads entry-level `modifier`s, so pool-level set_count / apply_bonus (tomatoes, the allium of
// wild onions) and the parameters of set_damage / enchant_* are taken here from the mod's own loot table files.
// Once the extractor reads them, `withPoolModifiers` becomes a no-op.
import { loot } from '../../lib/data';

const rawFiles = import.meta.glob('../../../../src/generated/resources/data/farmersdelight/loot_table/**/*.json', { eager: true, import: 'default' }) as Record<string, any>;
const raw = (id: string) => {
	const p = id.replace('farmersdelight:', '');
	const key = Object.keys(rawFiles).find((k) => k.endsWith(`/loot_table/${p}.json`));
	return key ? rawFiles[key] : null;
};
const list = (x: any) => (x == null ? [] : Array.isArray(x) ? x : [x]);
const num = (n: any) => (typeof n === 'number' ? { min: n, max: n } : n?.type?.endsWith('uniform') ? { min: n.min, max: n.max } : n?.type?.endsWith('constant') ? { min: n.value, max: n.value } : null);

export type Drop = { item?: string; kind?: string; weight: number; chance: number; count: { min: number; max: number }; fortune: any; conditions: string[]; modifiers: string[]; details?: string[] };
export type Pool = { rolls: { min: number; max: number }; conditions: string[]; entries: Drop[] };
export type Table = { pools: Pool[]; type: string; source: string };

function details(mods: any[]): string[] {
	const out: string[] = [];
	for (const m of mods) {
		const t = String(m.type).replace('minecraft:', '');
		if (t === 'set_damage') {
			const d = num(m.damage);
			if (d) out.push(`${Math.round(d.min * 100)}–${Math.round(d.max * 100)}% durability`);
		} else if (t === 'enchant_randomly') out.push('random enchantment');
		else if (t === 'enchant_with_levels') {
			const l = num(m.levels);
			if (l) out.push(`enchanted at level ${l.min}–${l.max}`);
		}
	}
	return out;
}

/** A loot table from loot.json with pool-level count/fortune modifiers and modifier details merged in from the raw file. */
export function lootTable(id: string): Table {
	const kind = id.split(':')[1].startsWith('chests/') ? 'chests' : 'blocks';
	const t = loot[kind][id];
	if (!t) throw new Error(`No loot table ${id}`);
	const r = raw(id);
	const pools: Pool[] = t.pools.map((p: any, i: number) => {
		const rp = r?.pools?.[i];
		const pm = list(rp?.modifier ?? rp?.functions);
		const pc = pm.find((m: any) => String(m.type).endsWith('set_count'));
		const pb = pm.find((m: any) => String(m.type).endsWith('apply_bonus'));
		// flattened in the same order as the extractor (children of alternatives/groups depth-first)
		const rawEntries: any[] = [];
		const flat = (e: any) => (/(alternatives|group|sequence)$/.test(e.type ?? '') ? e.children.forEach(flat) : rawEntries.push(e));
		list(rp?.entries).forEach(flat);
		return {
			rolls: p.rolls,
			conditions: p.conditions,
			entries: p.entries.map((e: any, j: number) => {
				const re = rawEntries[j];
				const em = list(re?.modifier ?? re?.functions);
				const def = !e.count || (e.count.min === 1 && e.count.max === 1);
				return {
					...e,
					count: def && pc ? num(pc.count) ?? e.count : e.count ?? { min: 1, max: 1 },
					fortune: e.fortune ?? (pb ? { formula: String(pb.formula).replace('minecraft:', ''), ...pb.parameters } : null),
					details: details(em),
				};
			}),
		};
	});
	return { pools, type: t.type, source: `src/generated/resources/data/farmersdelight/loot_table/${id.split(':')[1]}.json` };
}

/**
 * Player-language version of one condition string written by the extractor ("age = 7 and with #...knives").
 * `maxAge` turns "age = <max>" into "fully grown" and other ages into "stage N". Parts a player never has to think
 * about (which half of a double plant, the legacy rope-vine flag) come back as an empty string.
 */
export function conditionText(c: string, maxAge?: number): string {
	return c
		.split(/\s+and\s+/)
		.map((part) => {
			const age = part.match(/^age = (\d+)$/);
			if (age) return maxAge !== undefined && +age[1] === maxAge ? 'fully grown' : `stage ${+age[1] + 1}`;
			if (/^not \(neoforge:can_item_perform_ability\)$/.test(part)) return 'not with shears';
			if (/^neoforge:can_item_perform_ability$/.test(part)) return 'with shears';
			if (/^with #farmersdelight:(tools\/knives|straw_harvesters)$/.test(part)) return 'with a knife';
			if (/^(half = (lower|upper)|location_check|ropelogged = false)$/.test(part)) return '';
			return part;
		})
		.filter(Boolean)
		.join(', ');
}

/** Fortune bonus of a drop, in words. */
export function fortuneText(f: any): string {
	if (!f) return '';
	const pct = (x: number) => `${Math.round(x * 100)}%`;
	if (f.formula === 'binomial_with_bonus_count') return `plus up to ${f.extra} more (${pct(f.probability)} each); Fortune adds one more try per level`;
	if (f.formula === 'uniform_bonus_count') return `Fortune adds up to ${f.bonusMultiplier} more per level`;
	if (f.formula === 'ore_drops') return 'more with Fortune';
	return 'more with Fortune';
}

export type DropLine = { item: string | null; count: string; prefix: string | null; notes: string[] };
export type DropGroup = { when: string | null; lines: DropLine[] };
/**
 * A block's drops as plain lines for players. Each group is one pool; inside a group with alternatives only the first
 * matching line drops ("otherwise ..."). `pools` picks which pools to show (e.g. one half of a double plant).
 */
export function dropGroups(block: string, maxAge?: number, pools?: number[]): DropGroup[] {
	const t = lootTable(`farmersdelight:blocks/${block}`);
	const count = (c: { min: number; max: number }) => (c.min === c.max ? `${c.min}` : `${c.min}–${c.max}`);
	return t.pools
		.filter((_, i) => !pools || pools.includes(i))
		.map((p) => {
			const alt = p.entries.some((e) => e.conditions.includes('otherwise'));
			const when = p.conditions.map((c) => conditionText(c, maxAge)).filter(Boolean).join(', ');
			return {
				when: when || null,
				lines: p.entries.map((e, i) => ({
					item: e.item ?? null,
					count: count(e.count),
					prefix: alt && i > 0 ? 'otherwise' : null,
					notes: [...e.conditions.filter((c) => c !== 'otherwise').map((c) => conditionText(c, maxAge)), fortuneText(e.fortune), ...(e.details ?? [])].filter(Boolean),
				})),
			};
		});
}
