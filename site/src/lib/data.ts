// One place to read the generated data (src/data/gen, written by scripts/extract.mjs from the mod's real data).
import itemsJson from '../data/gen/items.json';
import othersJson from '../data/gen/others.json';
import blocksJson from '../data/gen/blocks.json';
import recipesJson from '../data/gen/recipes.json';
import iconsJson from '../data/gen/icons.json';
import rendersJson from '../data/gen/renders.json';
import effectsJson from '../data/gen/effects.json';
import metaJson from '../data/gen/meta.json';
import lootJson from '../data/gen/loot.json';
import modifiersJson from '../data/gen/modifiers.json';
import tradesJson from '../data/gen/trades.json';
import worldgenJson from '../data/gen/worldgen.json';
import biomesJson from '../data/gen/biomes.json';
import configJson from '../data/gen/config.json';
import tagsJson from '../data/gen/tags.json';
import advancementsJson from '../data/gen/advancements.json';
import vanillaFoodsJson from '../data/gen/vanilla-foods.json';
import soupEffectsJson from '../data/gen/soup-effects.json';

export type Ingredient = { items: string[]; tag?: string | null; label?: string | null; action?: string; any?: Ingredient[] };
export type Stack = { id: string; count: number; chance?: number };
export type Recipe = {
	id: string;
	type: string;
	loaded: boolean;
	group?: string;
	grid?: (Ingredient | null)[][];
	ingredients?: Ingredient[];
	result?: Stack;
	results?: Stack[];
	cookingtime?: number;
	experience?: number;
	tab?: string;
	container?: Stack | null;
	containerOverride?: boolean;
	tool?: Ingredient;
	sound?: string | null;
	template?: Ingredient;
	base?: Ingredient;
	addition?: Ingredient;
	special?: boolean;
	conditions?: unknown;
};
export type Effect = { id: string; name: string; duration: number; amplifier: number; probability: number };
export type Item = (typeof itemsJson)[keyof typeof itemsJson] & {
	consumable: { seconds: number; animation: string; effects: Effect[]; other: any[] } | null;
};

export const items = itemsJson as unknown as Record<string, Item>;
export const others = othersJson as Record<string, { id: string; name: string; food: { nutrition: number; saturation: number } | null; stack: number | null }>;
export const blocks = blocksJson as Record<string, any>;
export const recipes = recipesJson as unknown as Recipe[];
export const icons = iconsJson as Record<string, 'flat' | '3d' | null>;
export const renders = rendersJson as Record<string, boolean>;
export const effects = effectsJson;
export const meta = metaJson;
export const loot = lootJson as any;
export const modifiers = modifiersJson as any[];
export const trades = tradesJson as any[];
export const worldgen = worldgenJson as any[];
export const biomes = biomesJson as Record<string, { name: string; temperature: number; precipitation: boolean }>;
export const config = configJson as any[];
export const tags = tagsJson as { resolved: Record<string, Record<string, string[]>>; files: any[] };
export const advancements = advancementsJson as any[];
export const vanillaFoods = vanillaFoodsJson as any[];
export const soupEffects = soupEffectsJson as any[];

const BASE = import.meta.env.BASE_URL.replace(/\/$/, '');
export const url = (p: string) => BASE + (p.startsWith('/') ? p : '/' + p);

export const recipeById = new Map(recipes.map((r) => [r.id, r]));
export const itemList = Object.values(items);
export const bySlug = new Map(itemList.map((i) => [i.slug, i]));

/** Name of any item id (FD or vanilla), from the game's language files. */
export function nameOf(id: string): string {
	return items[id]?.name ?? others[id]?.name ?? id.split(':')[1].replaceAll('_', ' ');
}
export function iconOf(id: string): { src: string; flat: boolean } | null {
	const kind = icons[id];
	if (!kind) return null;
	const [ns, p] = id.split(':');
	return { src: url(`/icons/${ns}/${p}.png`), flat: kind === 'flat' };
}
export function pageOf(id: string): string | null {
	return items[id] ? url(`/items/${items[id].slug}/`) : null;
}
export function renderOf(name: string): string | null {
	return renders[name] ? url(`/renders/${name}.png`) : null;
}

// ------------------------------------------------------------ formatting
export const seconds = (ticks: number) => {
	const s = ticks / 20;
	return Number.isInteger(s) ? `${s} s` : `${+s.toFixed(2)} s`;
};
export function duration(ticks: number) {
	const s = ticks / 20;
	if (s >= 60 && s % 60 === 0) return `${s / 60}:00`;
	if (s >= 60) return `${Math.floor(s / 60)}:${String(Math.round(s % 60)).padStart(2, '0')}`;
	return `0:${String(Math.round(s)).padStart(2, '0')}`;
}
export const pct = (x: number, digits = 1) => `${+(x * 100).toFixed(digits)}%`;
export const roman = (n: number) => ['I', 'II', 'III', 'IV', 'V', 'VI'][n] ?? String(n + 1);
export const short = (id: string) => id.replace(/^minecraft:/, '');
export const recipeTypeName: Record<string, string> = {
	crafting_shaped: 'Crafting',
	crafting_shapeless: 'Crafting (shapeless)',
	'farmersdelight:cooking': 'Cooking Pot',
	'farmersdelight:cutting': 'Cutting Board',
	smelting: 'Furnace',
	smoking: 'Smoker',
	blasting: 'Blast Furnace',
	campfire_cooking: 'Campfire, Stove & Skillet',
	smithing_transform: 'Smithing',
	'farmersdelight:food_serving': 'Serving from a Cooking Pot',
	'farmersdelight:dough': 'Dough from water',
};

// ------------------------------------------------------------ recipe queries
export const playableRecipes = recipes.filter((r) => r.loaded && !r.id.includes('/integration/'));
export function recipesFor(id: string) {
	return playableRecipes.filter((r) => r.result?.id === id || r.results?.some((x) => x.id === id));
}
export function recipesUsing(id: string) {
	const ingr = (r: Recipe) => [...(r.ingredients ?? []), ...(r.grid ?? []).flat(), r.base, r.addition, r.template, r.tool].filter(Boolean) as Ingredient[];
	return playableRecipes.filter((r) => ingr(r).some((i) => i.items.includes(id)) || r.container?.id === id);
}

export const CATEGORIES: Record<string, { label: string; blurb: string }> = {
	workstations: { label: 'Workstations', blurb: 'Cooking Pot, Stove, Cutting Board, Baskets' },
	tools: { label: 'Tools', blurb: 'Knives and the Skillet' },
	crops: { label: 'Crops & seeds', blurb: 'Cabbage, tomato, onion, rice' },
	farming: { label: 'Soil & wild plants', blurb: 'Compost, Rich Soil, colonies, wild crops' },
	ingredients: { label: 'Ingredients', blurb: 'Cuts, dough, sauces and other components' },
	meals: { label: 'Meals', blurb: 'Bowls and plates that grant Nourishment' },
	feasts: { label: 'Feasts', blurb: 'Placeable meals with several servings' },
	snacks: { label: 'Snacks', blurb: 'Sandwiches, wraps and rolls' },
	sweets: { label: 'Sweets', blurb: 'Cookies, custard, popsicles, salads' },
	pies: { label: 'Pies & slices', blurb: 'Whole pies and their slices' },
	drinks: { label: 'Drinks', blurb: 'Bottled drinks with effects' },
	storage: { label: 'Storage', blurb: 'Cabinets, crates, bales and bags' },
	decoration: { label: 'Decoration', blurb: 'Canvas, signs, tatami, rope' },
	materials: { label: 'Materials', blurb: 'Straw, tree bark and other components' },
};

/** All items an ingredient can be, for cycling slot displays (de-duplicated, stable order). */
export const ingredientItems = (i: Ingredient | null | undefined) => (i ? [...new Set(i.items)] : []);
