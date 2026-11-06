// Turns generated data into short readable sentences (loot chances, loot modifiers, links to mechanic pages).
import { nameOf, short, pct, tags, blocks as blockData, type Item } from './data';

const MECHANICS: [RegExp, string, string][] = [
	[/^cooking_pot$/, '/mechanics/cooking-pot/', 'the Cooking Pot'],
	[/^stove$/, '/mechanics/stove/', 'the Stove'],
	[/^skillet$/, '/mechanics/skillet/', 'the Skillet'],
	[/^cutting_board$/, '/mechanics/cutting-board/', 'the Cutting Board'],
	[/_knife$/, '/mechanics/knives/', 'knives'],
	[/^rich_soil/, '/mechanics/rich-soil/', 'Rich Soil'],
	[/^organic_compost$/, '/mechanics/organic-compost/', 'Organic Compost'],
	[/mushroom_colony$/, '/mechanics/mushroom-colonies/', 'mushroom colonies'],
	[/^wild_|^sandy_shrub$/, '/mechanics/wild-crops/', 'wild crops'],
	[/_block$/, '/mechanics/feasts/', 'feasts'],
	[/(_pie|cheesecake|cake_slice|pie_slice|_slice)$/, '/mechanics/pies/', 'pies'],
	[/^(milk_bottle|hot_cocoa|apple_cider|melon_juice)$/, '/mechanics/drinks/', 'drinks'],
	[/^(dog_food|horse_feed)$/, '/mechanics/animal-feed/', 'animal feed'],
	[/^(cabbage|cabbage_seeds|cabbage_leaf|tomato|tomato_seeds|onion|rice|rice_panicle|rotten_tomato)$/, '/mechanics/crops/', 'these crops'],
	[/^(straw|rope|safety_net|straw_bale|rope_fence|rope_fence_gate)$/, '/mechanics/straw-and-rope/', 'straw and rope'],
	[/cabinet|basket|crate|_bag|_bale|tatami|canvas|sign|rug/, '/mechanics/storage-and-decoration/', 'storage and decoration'],
];
export function mechanicFor(item: Item): { href: string; label: string } | null {
	if (item.category === 'meals' || item.category === 'snacks' || item.category === 'sweets' || item.category === 'ingredients') {
		if (item.consumable?.effects.some((e) => e.id === 'farmersdelight:nourishment')) return { href: '/mechanics/effects/', label: 'Nourishment' };
	}
	for (const [re, href, label] of MECHANICS) if (re.test(item.slug)) return { href, label };
	return null;
}

export function chestName(table: string) {
	const p = table.split('/').pop()!.replace(/^fd_/, '');
	return p.replaceAll('_', ' ').replace(/\b\w/g, (c) => c.toUpperCase()).replace('Village ', 'Village: ');
}

/** Chance that a chest contains the item at least once, from the table's pools (rolls are uniform). */
export function chestChance(table: any, id: string) {
	let miss = 1;
	const parts: string[] = [];
	for (const pool of table.pools) {
		const e = pool.entries.find((x: any) => x.item === id);
		if (!e) continue;
		const p = e.chance as number;
		const a = pool.rolls.min ?? 1;
		const b = pool.rolls.max ?? a;
		let poolMiss = 0;
		for (let r = a; r <= b; r++) poolMiss += (1 - p) ** r / (b - a + 1);
		miss *= poolMiss;
		const count = e.count?.min === e.count?.max ? `${e.count.min}` : `${e.count.min}–${e.count.max}`;
		parts.push(count);
	}
	return { chance: 1 - miss, text: `in about ${pct(1 - miss, 0)} of these chests, ${[...new Set(parts)].join(' or ')} at a time.` };
}

const ent = (e: any) => (Array.isArray(e) ? e : [e]).map((x: string) => (x.startsWith('#') ? `#${short(x.slice(1))}` : nameOf(x.replace('minecraft:', 'minecraft:')) ?? short(x))).join(', ');
const entityName = (e: string) => short(e).replaceAll('_', ' ').replace(/\b\w/g, (c) => c.toUpperCase());

export function describeModifier(m: any): string {
	const item = m.item ? `<strong>${nameOf(m.item)}</strong>` : '';
	const knife = m.attackerTool?.includes('knives') ? ' with a <a href="../../mechanics/knives/">knife</a>' : '';
	if (m.entity) {
		const who = (Array.isArray(m.entity) ? m.entity : [m.entity]).map((e: string) => (e.startsWith('#') ? `#${short(e.slice(1))}` : entityName(e))).join(', ');
		let chance = m.chance !== undefined ? `${pct(m.chance)}` : 'always';
		if (m.lootingChance) {
			const ec = m.lootingChance.enchanted;
			chance = `${pct(m.lootingChance.base)} (${pct(ec.base)} with Looting I, +${pct(ec.per_level_above_first)} per extra level)`;
		}
		const fire = m.onFire === true ? ' while it is on fire' : m.onFire === false ? ' (not on fire)' : '';
		return `Killing a <strong>${who}</strong>${fire}${knife} drops 1 ${item}: ${chance}.`;
	}
	if (m.type?.endsWith('add_item') && m.block) {
		// Name the blocks a player knows, not the tag: "Short Grass or Tall Grass"
		const blockName = (b: string) => blockData[b]?.name ?? nameOf(b);
		const list = (ids: string[]) => (ids.length <= 3 ? ids.map(blockName).join(' or ') : `${ids.slice(0, 3).map(blockName).join(', ')} and ${ids.length - 3} more`);
		const raw = m.block.blocks;
		const blocks = typeof raw === 'string' ? (raw.startsWith('#') ? list(tags.resolved.block?.[raw.slice(1)] ?? []) || short(raw.slice(1)) : blockName(raw)) : Array.isArray(raw) ? list(raw) : '';
		const state = m.block.state && 'age' in m.block.state ? ' when fully grown' : '';
		const tool = m.tool ? ` with ${String(m.tool).includes('knives') || String(m.tool).includes('straw_harvesters') ? 'a <a href="../../mechanics/knives/">knife</a>' : m.tool}` : '';
		return `Breaking <strong>${blocks}</strong>${state}${tool} drops ${m.count ?? 1} ${item}: ${m.chance !== undefined ? pct(m.chance) : 'always'}.`;
	}
	if (m.type?.endsWith('pastry_slicing')) return `Breaking the placed pie or cake with a knife drops its remaining slices.`;
	if (m.type?.endsWith('replace_item')) return `Breaking a pumpkin with a knife (without Silk Touch) gives slices instead of the pumpkin.`;
	return `Added by the loot modifier <code>${m.id}</code>.`;
}
