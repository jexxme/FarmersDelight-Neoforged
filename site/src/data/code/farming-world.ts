// Helpers for the worldgen panels: resolved block tags and a readable grouping of biome lists.
import { tags, worldgen, biomes, nameOf } from '../../lib/data';

/**
 * Resolved contents of a block tag. tags.json only resolves tags that contain a Farmer's Delight block, so
 * `#minecraft:sand` is derived from `#farmersdelight:terrain`, which is exactly `#minecraft:substrate_overworld` +
 * `#minecraft:sand` (src/generated/resources/data/farmersdelight/tags/block/terrain.json).
 */
export function blockTag(tag: string): string[] {
	const t = tag.replace(/^#/, '');
	const r = tags.resolved.block[t];
	if (r) return r;
	if (t === 'minecraft:sand') {
		const sub = new Set(tags.resolved.block['minecraft:substrate_overworld'] ?? []);
		return (tags.resolved.block['farmersdelight:terrain'] ?? []).filter((b) => !sub.has(b));
	}
	return [];
}

// Display grouping only (by biome id words); it changes nothing about which biomes are listed.
const GROUPS: [string, RegExp][] = [
	['Oceans', /ocean/],
	['Underground', /caves|deep_dark/],
	['Coast & rivers', /beach|shore|river/],
	['Swamps', /swamp/],
	['Jungles', /jungle/],
	['Forests', /forest|grove|garden|taiga|woods/],
	['Dry lands', /desert|badlands|savanna/],
	['Mountains', /peaks|hills|meadow|slopes|windswept/],
	['Plains', /plains|fields/],
];
export function biomeGroups(ids: string[]): [string, string[]][] {
	const out = new Map<string, string[]>();
	for (const id of ids) {
		const p = id.split(':')[1];
		const g = GROUPS.find(([, re]) => re.test(p))?.[0] ?? 'Other';
		out.set(g, [...(out.get(g) ?? []), id]);
	}
	const order = [...GROUPS.map((g) => g[0]), 'Other'];
	return [...out.entries()]
		.sort((a, b) => order.indexOf(a[0]) - order.indexOf(b[0]))
		.map(([g, l]) => [g, l.sort((a, b) => (biomes[a]?.name ?? a).localeCompare(biomes[b]?.name ?? b))]);
}

/** Rows for the "what grows in biome X" filter island. */
export function biomeFilterData(icon: (id: string) => string | null) {
	const all = new Set<string>();
	// every biome that some modifier considered (the union of the allowed tags, e.g. #minecraft:is_overworld), so empty biomes show up too
	worldgen.forEach((w) => [...w.candidateBiomes, ...w.biomes].forEach((b: string) => all.add(b)));
	const plants = worldgen.map((w) => {
		const item = w.parts?.find((p: any) => p.block?.startsWith('farmersdelight:'))?.block ?? w.id;
		return { id: w.id, item, name: nameOf(item), icon: icon(item), rarity: w.rarity, count: w.count, biomes: w.biomes as string[], anchor: `wg-${w.id.split(':')[1]}` };
	});
	const list = [...all].map((id) => ({ id, name: biomes[id]?.name ?? id, temperature: biomes[id]?.temperature ?? null })).sort((a, b) => a.name.localeCompare(b.name));
	return { biomes: list, plants };
}
