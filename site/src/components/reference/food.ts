// Food rows for the food reference, built from items.json, vanilla-foods.json and soup-effects.json.
import { itemList, vanillaFoods, soupEffects, iconOf, pageOf, nameOf, url, CATEGORIES, config } from '../../lib/data';
import type { FoodRow, FoodEffect } from '../islands/ref-food';

const NOUR = 'farmersdelight:nourishment';
const effectIcon = (id: string) => url(`/gui/effect/${id.split(':')[1]}.png`);
const effName = (id: string, name?: string) => name ?? id.split(':')[1].replaceAll('_', ' ').replace(/\b\w/g, (c) => c.toUpperCase());
const effects = (list: any[] = []): FoodEffect[] =>
	list.map((e) => ({ id: e.id, name: effName(e.id, e.name), icon: effectIcon(e.id), ticks: e.duration, amp: e.amplifier, prob: e.probability }));

function other(list: any[] = [], tooltip: string[] = []): string[] {
	if (tooltip.length) return tooltip;
	return list.map((o) => {
		switch (o.type) {
			case 'farmersdelight:remove_random_effects':
				return o.harmful_only ? 'Clears 1 harmful effect' : 'Clears 1 effect';
			case 'farmersdelight:heal':
				return `Heals ${o.amount} health`;
			case 'farmersdelight:extinguish':
				return 'Puts out fire';
			case 'minecraft:teleport_randomly':
				return 'Random teleport';
			case 'minecraft:remove_effects':
				return `Cures ${nameOfEffect(o.effects)}`;
			case 'minecraft:clear_all_effects':
				return 'Clears all effects';
			default:
				return o.type;
		}
	});
}
const nameOfEffect = (e: string | string[]) => (Array.isArray(e) ? e : [e]).map((x) => effName(x)).join(', ');
const cfg = (key: string) => config.find((c) => c.key === key)?.default;

export function foodRows(): FoodRow[] {
	const fd: FoodRow[] = itemList
		.filter((i) => i.food || i.consumable)
		.map((i) => {
			const ic = iconOf(i.id);
			const eff = effects(i.consumable?.effects);
			const rem = i.remainder as string | null;
			return {
				id: i.id,
				name: i.name,
				href: pageOf(i.id),
				icon: ic?.src ?? null,
				vanilla: false,
				category: i.category,
				nutrition: i.food?.nutrition ?? 0,
				saturation: i.food?.saturation ?? 0,
				seconds: i.consumable?.seconds ?? 0,
				drink: i.consumable?.animation === 'drink',
				alwaysEdible: !!i.food?.alwaysEdible && (i.food?.nutrition ?? 0) > 0,
				effects: eff,
				other: other(i.consumable?.other, i.tooltip as string[]),
				stack: i.stack,
				container: rem ? { id: rem, name: nameOf(rem), icon: iconOf(rem)?.src ?? null } : null,
				nourishment: eff.find((e) => e.id === NOUR)?.ticks ?? 0,
			};
		});
	// Vanilla soups: Nourishment added on eating (enableVanillaSoupExtraEffects). Rabbit Stew is skipped while
	// enableRabbitStewBuff is on: the buff gives it its own Nourishment, already in its exported effects.
	const soupsOn = cfg('enableVanillaSoupExtraEffects') === true;
	const rabbitBuff = cfg('enableRabbitStewBuff') === true;
	const vanilla: FoodRow[] = vanillaFoods
		.filter((v) => v.consumable)
		.map((v) => {
			const eff = effects(v.consumable.effects);
			const soup = soupEffects.find((s) => s.item === v.id);
			if (soup && soupsOn) {
				const skipped = v.id === 'minecraft:rabbit_stew' && rabbitBuff;
				if (!skipped) eff.unshift({ id: soup.effect, name: 'Nourishment', icon: effectIcon(soup.effect), ticks: soup.duration, amp: 0, prob: 1, note: "Added by Farmer's Delight" });
			}
			const ic = iconOf(v.id);
			return {
				id: v.id,
				name: v.name,
				href: null,
				icon: ic?.src ?? null,
				vanilla: true,
				category: 'vanilla',
				nutrition: v.nutrition,
				saturation: v.saturation,
				seconds: v.consumable.seconds,
				drink: v.consumable.animation === 'drink',
				alwaysEdible: false,
				effects: eff,
				other: other(v.consumable.other),
				stack: v.stack,
				container: null,
				nourishment: eff.find((e) => e.id === NOUR)?.ticks ?? 0,
			};
		});
	return [...fd, ...vanilla];
}

export const categoryLabels: Record<string, string> = {
	...Object.fromEntries(Object.entries(CATEGORIES).map(([k, v]) => [k, v.label])),
	vanilla: 'Minecraft',
};

/** All rows sharing the best value of `score` (highest first). */
export function best(rows: FoodRow[], score: (r: FoodRow) => number) {
	const top = Math.max(...rows.map(score));
	return { value: top, rows: rows.filter((r) => Math.abs(score(r) - top) < 1e-6) };
}
