// Code facts for the reference and pack-maker pages, read from the mod's Java (paths relative to
// src/main/java/vectorwing/farmersdelight). Each value carries the lines it was read from; show it with <Src>.
// NeoForge facts (not in this repo) cite the NeoForge 26.3.0.101 sources jar and are marked `neoforge: true`.

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
/** With enableRabbitStewBuff on, the soup handler returns before adding Nourishment to Rabbit Stew. */
export const rabbitStewSkip = c(null, 'common/event/CommonEvents.java', '41-43');
export const rabbitStewBuff = c(null, 'common/event/CommonModBusEvents.java', '46-48');
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
	/** JEI shows the special dough recipe with two wheat; DoughRecipe needs exactly one. */
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
	onionSoup: c(null, 'common/registry/ModItems.java', '530-533'),
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
