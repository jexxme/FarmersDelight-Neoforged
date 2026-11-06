// Code facts for the reference pages, read from the mod's Java (paths relative to
// src/main/java/vectorwing/farmersdelight). Each value carries the lines it was read from, for maintainers only:
// the player pages never show citations. NeoForge facts (not in this repo) cite the NeoForge 26.3.0.101 sources jar
// and are marked `neoforge: true`. Plain-language helpers for the pages are at the bottom.

export type Cited<T> = { value: T; src: string; lines: string; neoforge?: boolean };
const c = <T>(value: T, src: string, lines: string, neoforge = false): Cited<T> => ({ value, src, lines, neoforge });

// ------------------------------------------------------------------ recipes
export const cooking = {
	codec: c(null, 'common/crafting/CookingPotRecipe.java', '31-43'),
	group: c('', 'common/crafting/CookingPotRecipe.java', '32'),
	tab: c('misc', 'common/crafting/CookingPotRecipe.java', '33'),
	tabs: c(['meals', 'drinks', 'misc'], 'client/recipebook/CookingPotRecipeBookTab.java', '13-23'),
	maxIngredients: c(6, 'common/crafting/CookingPotRecipe.java', '34,48'),
	experience: c(0, 'common/crafting/CookingPotRecipe.java', '41'),
	cookingtime: c(200, 'common/crafting/CookingPotRecipe.java', '42'),
	container: c(null, 'common/crafting/CookingPotRecipe.java', '79-90'),
	matches: c(null, 'common/crafting/CookingPotRecipe.java', '104-117'),
};

export const cutting = {
	codec: c(null, 'common/crafting/CuttingBoardRecipe.java', '33-63'),
	ingredients: c(1, 'common/crafting/CuttingBoardRecipe.java', '35-45'),
	maxResults: c(4, 'common/crafting/CuttingBoardRecipe.java', '52-57,70'),
	chance: c(1, 'common/crafting/ingredient/ChanceResult.java', '19-22'),
	roll: c(null, 'common/crafting/ingredient/ChanceResult.java', '30-41'),
	sound: c(null, 'common/crafting/CuttingBoardRecipe.java', '62'),
};

export const serializers = c(['cooking', 'cutting', 'food_serving', 'dough'], 'common/registry/ModRecipeSerializers.java', '22-28');
export const foodServing = c(null, 'common/crafting/FoodServingRecipe.java', '24-50');
export const dough = c(null, 'common/crafting/DoughRecipe.java', '23-63');

export const itemAbility = {
	registered: c('item_ability', 'common/registry/ModIngredientTypes.java', '15'),
	codec: c(null, 'common/crafting/ingredient/ItemAbilityIngredient.java', '30-32'),
	/** Ability names NeoForge 26.3 removed from vanilla tools, and the tool tag the ingredient checks instead. */
	fallbacks: c(
		[
			['axe_dig', 'minecraft:axes'],
			['axe_strip', 'minecraft:axes'],
			['axe_scrape', 'minecraft:axes'],
			['axe_wax_off', 'minecraft:axes'],
			['pickaxe_dig', 'minecraft:pickaxes'],
			['shovel_dig', 'minecraft:shovels'],
			['shovel_flatten', 'minecraft:shovels'],
			['hoe_dig', 'minecraft:hoes'],
			['hoe_till', 'minecraft:hoes'],
			['sword_dig', 'minecraft:swords'],
		] as [string, string][],
		'common/crafting/ingredient/ItemAbilityIngredient.java',
		'46-65',
	),
};

export const conditions = {
	registered: c(['vanilla_crates_enabled', 'config_enabled'], 'common/registry/ModConditionCodecs.java', '17-18'),
	crates: c('enableVanillaCropCrates', 'common/crafting/condition/VanillaCrateEnabledCondition.java', '11-20'),
	configOptions: c(['enableFarmerFDTrades', 'enableWanderingTraderFDTrades', 'enableVanillaCropCrates'], 'common/crafting/condition/ConfigEnabledCondition.java', '21-32'),
	/** NeoForge: data files may carry "neoforge:conditions"; a file whose conditions fail is skipped. */
	key: c('neoforge:conditions', 'net/neoforged/neoforge/common/conditions/ConditionalOps.java', '54', true),
	never: c('neoforge:never', 'net/neoforged/neoforge/common/NeoForgeMod.java', '382', true),
	/** NeoForge patches SimpleJsonResourceReloadListener so a file whose conditions fail is skipped (loot modifiers load through it). */
	skip: c(null, 'patches/net/minecraft/server/packs/resources/SimpleJsonResourceReloadListener.java.patch', '36-52', true),
};

// ------------------------------------------------------------------ loot modifiers
export const lootModifiers = {
	registered: c(['add_item', 'replace_item', 'add_loot_table', 'pastry_slicing'], 'common/registry/ModLootModifiers.java', '19-22'),
	addItem: c(null, 'common/loot/modifier/AddItemModifier.java', '22-29'),
	addItemApply: c(null, 'common/loot/modifier/AddItemModifier.java', '49-66'),
	replaceItem: c(null, 'common/loot/modifier/ReplaceItemModifier.java', '23-31'),
	replaceItemApply: c(null, 'common/loot/modifier/ReplaceItemModifier.java', '52-72'),
	addTable: c(null, 'common/loot/modifier/FDAddTableLootModifier.java', '24-27'),
	addTableGate: c('generateFDChestLoot', 'common/loot/modifier/FDAddTableLootModifier.java', '38-43'),
	pastry: c(null, 'common/loot/modifier/PastrySlicingModifier.java', '28-31'),
	pastryBites: c({ cake: 7, pie: 4 }, 'common/loot/modifier/PastrySlicingModifier.java', '33-34,54-65'),
	/** NeoForge base fields every loot modifier has. */
	baseFields: c(null, 'net/neoforged/neoforge/common/loot/LootModifier.java', '35-39', true),
	defaultPriority: c(1000, 'net/neoforged/neoforge/common/loot/IGlobalLootModifier.java', '33', true),
	folder: c('loot_modifiers', 'net/neoforged/neoforge/common/loot/LootModifierManager.java', '26-32', true),
	order: c(null, 'net/neoforged/neoforge/common/loot/LootModifierManager.java', '43-45', true),
};

// ------------------------------------------------------------------ worldgen
export const biomeModifier = {
	codec: c(null, 'common/registry/ModBiomeModifiers.java', '36-44'),
	modify: c(null, 'common/world/modifier/AddFeaturesByFilterBiomeModifier.java', '28-42'),
	biomeTagFilter: c(null, 'common/world/filter/BiomeTagFilter.java', '17-20,37-40'),
	placementRegistered: c('biome_tag', 'common/registry/ModPlacementModifiers.java', '16'),
	features: c(['in_order', 'wild_rice'], 'common/registry/ModBiomeFeatures.java', '18-19'),
	inOrder: c(null, 'common/world/feature/InOrderFeature.java', '27-39'),
	wildRice: c(null, 'common/world/feature/WildRiceFeature.java', '29-39'),
};

// ------------------------------------------------------------------ food
export const nourishmentTiers = c(
	[
		['Brief', 600],
		['Short', 1200],
		['Medium', 3600],
		['Long', 6000],
	] as [string, number][],
	'common/FoodValues.java',
	'22-25',
);
export const vanillaSoups = c(null, 'common/FoodValues.java', '274-278');
export const soupHandler = c(null, 'common/event/CommonEvents.java', '36-54');
/** With enableRabbitStewBuff on, the soup handler skips Rabbit Stew: the buffed stew brings its own Nourishment. */
export const rabbitStewSkip = c(null, 'common/event/CommonEvents.java', '41-43');
export const rabbitStewBuff = c(null, 'common/event/CommonModBusEvents.java', '46-51');
export const stackableSoups = c(16, 'common/event/CommonModBusEvents.java', '36-45');

// ------------------------------------------------------------------ config
export const configFiles = {
	common: c('farmersdelight-common.toml', 'FarmersDelight.java', '38-39'),
	client: c('farmersdelight-client.toml', 'FarmersDelight.java', '40'),
	screen: c(null, 'FarmersDelight.java', '35'),
	spec: c(null, 'common/Configuration.java', '54-131'),
};


// ------------------------------------------------------------------ integrations
export const jei = {
	categories: c(['Cooking', 'Cutting', 'Decomposition'], 'integration/jei/JEIPlugin.java', '36-40'),
	recipes: c(null, 'integration/jei/JEIPlugin.java', '43-49'),
	info: c(null, 'integration/jei/JEIPlugin.java', '51-67'),
	catalysts: c(null, 'integration/jei/JEIPlugin.java', '71-77'),
	clickArea: c(null, 'integration/jei/JEIPlugin.java', '80-82'),
	transfer: c(null, 'integration/jei/JEIPlugin.java', '85-87'),
	/** JEI's display of the special dough recipe. Checked in game by the Port Lead: it shows one wheat, like DoughRecipe needs. */
	doughDisplay: c(null, 'integration/jei/FDRecipes.java', '52-60'),
};
export const rei = {
	categories: c(null, 'integration/rei/FDReiClientPlugin.java', '36-42'),
	info: c(null, 'integration/rei/FDReiClientPlugin.java', '45-58'),
	clickArea: c(null, 'integration/rei/FDReiClientPlugin.java', '66-68'),
	transfer: c(null, 'integration/rei/FDReiClientPlugin.java', '71-73'),
	displays: c(null, 'integration/rei/FDReiCommonPlugin.java', '45-58'),
};
export const doughNeedsOne = c(null, 'common/crafting/DoughRecipe.java', '28-43');

// ------------------------------------------------------------------ port differences
export const port = {
	aliases: c(null, 'common/registry/RegistryAliases.java', '9-15'),
	signEntities: c(null, 'common/event/CommonModBusEvents.java', '51-64'),
	defaultComponents: c(null, 'common/ItemDefaultComponents.java', '19-30'),
	rottenTomatoThrow: c(null, 'common/item/RottenTomatoItem.java', '28-47'),
	rottenTomatoDispense: c(null, 'common/item/RottenTomatoItem.java', '49-52'),
	smokerCook: c('smoker_cook', 'common/registry/ModLootFunctions.java', '18'),
};

// ------------------------------------------------------------------ consume effects (minecraft:consumable on_consume_effects)
export const consumeEffects = {
	registered: c(['remove_random_effects', 'heal', 'extinguish'], 'common/registry/ModConsumeEffectTypes.java', '17-22'),
	removeRandom: c(null, 'common/item/component/consumable/RemoveRandomStatusEffectsConsumeEffect.java', '27-30'),
	heal: c(null, 'common/item/component/consumable/HealConsumeEffect.java', '17-19'),
	extinguish: c(null, 'common/item/component/consumable/ExtinguishConsumeEffect.java', '14'),
};

// ------------------------------------------------------------------ add-on recipes (player wording)
/** What the built-in recipes for other mods do (src/main/resources/data/farmersdelight/recipe/integration/<mod>/).
 * Recipe counts come from recipes.json; this is only the summary. */
export const addonRecipes: Record<string, { name: string; text: string; src: string }> = {
	create: {
		name: 'Create',
		text: 'Millstone milling for the wild crops and Rice Panicles, Mixer recipes for Cabbage Leaves, Pie Crust and Tomato Sauce, and a Spout recipe that fills a Pie Crust into a Chocolate Pie.',
		src: 'src/main/resources/data/farmersdelight/recipe/integration/create/',
	},
	immersiveengineering: {
		name: 'Immersive Engineering',
		text: 'Cabbages, onions, rice and tomatoes grow in the Garden Cloche, the Crusher grinds the wild crops and Rice Panicles, the Metal Press cuts pumpkins into Pumpkin Slices, and tomatoes and seeds go into the Fermenter and Squeezer.',
		src: 'src/main/resources/data/farmersdelight/recipe/integration/immersiveengineering/',
	},
	silentgear: {
		name: 'Silent Gear',
		text: 'Strip Netherwood logs on the Cutting Board with an axe to get Tree Bark.',
		src: 'src/main/resources/data/farmersdelight/recipe/integration/silentgear/',
	},
};
/** Files of Create potato-cannon ammo that Farmer's Delight ships (each lists its items). */
export const potatoCannonDir = 'src/main/resources/data/farmersdelight/create/potato_projectile/type';

/** What Farmer's Delight puts into other mods' tags, in player words. Key: "<registry> <tag>". */
export const foreignTagText: Record<string, string> = {
	'block create:passive_boiler_heaters': 'the Stove heats a steam boiler',
	'item create:upright_on_belt': 'meals, drinks and feasts stand upright on belts',
	'block create:fan_transparent': 'air from fans blows through the Safety Net',
	'item tconstruct:seeds': 'onions count as seeds',
	'item origins:meat': 'fish slices, fried eggs and other meaty food count as meat',
};
export const modNames: Record<string, string> = {
	create: 'Create',
	createaddition: 'Create Crafts & Additions',
	immersiveengineering: 'Immersive Engineering',
	silentgear: 'Silent Gear',
	sereneseasons: 'Serene Seasons',
	tconstruct: "Tinkers' Construct",
	origins: 'Origins',
	sable: 'Sable',
	quark: 'Quark',
	supplementaries: 'Supplementaries',
	autumnity: 'Autumnity',
};

// ------------------------------------------------------------------ advancements (player wording)
/** How to get the advancements whose criteria aren't simply "get, place or eat this item". */
export const advancementHints: Record<string, { text: string; src: string }> = {
	'farmersdelight:main/root': { text: 'Unlocks as soon as you pick up any item.', src: 'src/generated/resources/data/farmersdelight/advancement/main/root.json' },
	'farmersdelight:main/eat_nourishing_food': { text: 'Get the Nourishment effect, from any food that gives it.', src: 'src/generated/resources/data/farmersdelight/advancement/main/eat_nourishing_food.json' },
	'farmersdelight:main/harvest_ropelogged_tomato': { text: 'Pick ripe tomatoes by hand from a vine that climbed a rope.', src: 'src/generated/resources/data/farmersdelight/advancement/main/harvest_ropelogged_tomato.json' },
	'farmersdelight:main/hit_raider_with_rotten_tomato': { text: 'Hit a raider with a Rotten Tomato you threw.', src: 'src/generated/resources/data/farmersdelight/advancement/main/hit_raider_with_rotten_tomato.json' },
	'farmersdelight:main/use_cutting_board': { text: 'Cut anything on a Cutting Board.', src: 'common/block/entity/CuttingBoardBlockEntity.java:129' },
	'farmersdelight:main/use_skillet': { text: 'Finish cooking a food in a Skillet you hold.', src: 'common/item/SkilletItem.java:240' },
};

// ------------------------------------------------------------------ formatting helpers
/** Game ticks as plain words: "30 seconds", "5 minutes", "1 minute 30 seconds". */
export function timeText(ticks: number) {
	const s = Math.round(ticks / 20);
	const m = Math.floor(s / 60);
	const r = s % 60;
	const mins = m === 1 ? '1 minute' : `${m} minutes`;
	const secs = r === 1 ? '1 second' : `${r} seconds`;
	if (!m) return secs;
	return r ? `${mins} ${secs}` : mins;
}
/** A Maven version range as words: "[a,)" -> "a or newer", "[a,b)" -> "a or newer, below b". */
export function rangeText(range: string) {
	const m = range.match(/^\[([^,]*),([^\])]*)[\])]$/);
	if (!m) return range;
	const [, lo, hi] = m;
	return hi ? `${lo} or newer, below ${hi}` : `${lo} or newer`;
}
