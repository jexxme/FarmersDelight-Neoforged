// Logic constants for the cooking pages (Cooking Pot, heat, Stove, Skillet, Cutting Board, feasts, pies, drinks,
// effects, animal feed). Each fact cites the Java it was read from: `path:lines`, paths relative to
// src/main/java/vectorwing/farmersdelight. Facts from vanilla Minecraft 26.3 start with `vanilla:`.
// Data values (food, recipes, tags, loot, config defaults) are NOT here: read them from src/data/gen via lib/data.ts.

export type Fact<T = unknown> = { value: T; src: string };
const f = <T>(value: T, src: string): Fact<T> => ({ value, src });
const at = (src: string): Fact<null> => ({ value: null, src });

const POT_BE = 'common/block/entity/CookingPotBlockEntity.java';
const POT_B = 'common/block/CookingPotBlock.java';
export const pot = {
	slots: f({ inputs: [0, 1, 2, 3, 4, 5], meal: 6, container: 7, output: 8 }, `${POT_BE}:81-84`),
	maxIngredients: f(6, 'common/crafting/CookingPotRecipe.java:48'),
	defaultCookTime: f(200, 'common/crafting/CookingPotRecipe.java:42'),
	defaultXp: f(0, 'common/crafting/CookingPotRecipe.java:41'),
	defaultTab: f('misc', 'common/crafting/CookingPotRecipe.java:33'),
	shapeless: at('common/crafting/CookingPotRecipe.java:104-117'),
	cookStep: f(1, `${POT_BE}:321-328`),
	decay: f(2, `${POT_BE}:227-236`),
	mealCap: f(64, `${POT_BE}:572-582`),
	canCook: at(`${POT_BE}:297-319`),
	finish: at(`${POT_BE}:330-354`),
	remainderOverrides: f(
		[
			['minecraft:powder_snow_bucket', 'minecraft:bucket'],
			['minecraft:axolotl_bucket', 'minecraft:bucket'],
			['minecraft:cod_bucket', 'minecraft:bucket'],
			['minecraft:pufferfish_bucket', 'minecraft:bucket'],
			['minecraft:salmon_bucket', 'minecraft:bucket'],
			['minecraft:tropical_fish_bucket', 'minecraft:bucket'],
			['minecraft:suspicious_stew', 'minecraft:bowl'],
			['minecraft:mushroom_stew', 'minecraft:bowl'],
			['minecraft:rabbit_stew', 'minecraft:bowl'],
			['minecraft:beetroot_soup', 'minecraft:bowl'],
			['minecraft:potion', 'minecraft:glass_bottle'],
			['minecraft:splash_potion', 'minecraft:glass_bottle'],
			['minecraft:lingering_potion', 'minecraft:glass_bottle'],
			['minecraft:experience_bottle', 'minecraft:glass_bottle'],
		] as [string, string][],
		`${POT_BE}:87-102`,
	),
	eject: at(`${POT_BE}:357-364`),
	containerRule: at('common/crafting/CookingPotRecipe.java:79-90'),
	autoOutput: at(`${POT_BE}:238-247`),
	moveToOutput: at(`${POT_BE}:447-490`),
	containerValid: at(`${POT_BE}:504-512`),
	serveClick: at(`${POT_B}:71-84`),
	serveTake: at(`${POT_BE}:492-502`),
	menu: f(
		{ inputs: [30, 17], step: 18, meal: [124, 26], container: [92, 55], output: [124, 55] },
		'common/block/entity/container/CookingPotMenu.java:55-78',
	),
	shiftClick: at('common/block/entity/container/CookingPotMenu.java:126-133'),
	hopperSides: at(`${POT_BE}:129-141`),
	hopperHandler: at('common/block/entity/inventory/CookingPotItemHandler.java:49-65'),
	comparator: f(14, 'common/utility/MathUtils.java:24-42'),
	comparatorBlock: at(`${POT_B}:184-191`),
	xpSplit: at(`${POT_BE}:402-410`),
	xpTake: at('common/block/entity/container/CookingPotResultSlot.java:61-67'),
	xpTrack: at(`${POT_BE}:366-372`),
	breakDrops: at(`${POT_BE}:437-445`),
	breakSide: at(`${POT_BE}:623-630`),
	itemComponents: at(`${POT_BE}:560-570`),
	pickBlock: at(`${POT_B}:138-148`),
	placement: at(`${POT_B}:104-118`),
	traySupport: at(`${POT_B}:120-136`),
	handleToggle: at(`${POT_B}:66-70`),
	bubbles: f({ bubble: 0.2, steam: 0.05 }, `${POT_BE}:255-273`),
	boilSound: f(10, `${POT_B}:164-177`),
	recipeBook: at('client/gui/CookingPotScreen.java:44-48'),
	foodServing: at('common/crafting/FoodServingRecipe.java:24-85'),
};

export const heat = {
	isHeated: at('common/block/entity/HeatableBlockEntity.java:19-38'),
	directLit: at('common/block/entity/HeatableBlockEntity.java:22-26'),
	conducted: at('common/block/entity/HeatableBlockEntity.java:28-35'),
	requiresDirect: f(false, 'common/block/entity/HeatableBlockEntity.java:40-45'),
	trayPot: at('common/block/CookingPotBlock.java:131-136'),
	traySkillet: at('common/block/SkilletBlock.java:187-189'),
	heldNear: f(1, 'common/item/SkilletItem.java:123-134'),
	campfireLit: at('vanilla:CampfireBlock.java:56'),
};

const STOVE_B = 'common/block/AbstractStoveBlock.java';
const STOVE_BE = 'common/block/entity/AbstractStoveBlockEntity.java';
export const stove = {
	slots: f(6, 'common/block/entity/StoveBlockEntity.java:48-51'),
	perSlot: f(1, `${STOVE_BE}:235-242`),
	recipeType: at('common/block/entity/StoveBlockEntity.java:15-17'),
	use: at(`${STOVE_B}:62-73`),
	placeFood: at(`${STOVE_B}:131-146`),
	placeSlot: at(`${STOVE_BE}:173-189`),
	cook: f(1, `${STOVE_BE}:115-143`),
	cool: f(2, `${STOVE_BE}:145-156`),
	placedLit: at(`${STOVE_B}:175-180`),
	ignite: at(`${STOVE_B}:75-104`),
	extinguish: at(`${STOVE_B}:106-129`),
	burn: f(1.0, `${STOVE_B}:188-194`),
	grillArea: f({ x: [3, 13], y: [0, 1] }, `${STOVE_B}:51`),
	covered: at(`${STOVE_B}:202-210`),
	coveredDrop: at(`${STOVE_BE}:100-113`),
	pathFire: at(`${STOVE_B}:224-228`),
	breakDrop: at(`${STOVE_BE}:245-251`),
	smoke: f(0.2, 'common/block/entity/StoveBlockEntity.java:24-46'),
};

export const skillet = {
	minTime: f(60, 'common/block/SkilletBlock.java:49'),
	formula: at('common/block/SkilletBlock.java:191-208'),
	defaultTicks: f(600, 'common/block/SkilletBlock.java:197'),
	factor: f(0.2, 'common/block/SkilletBlock.java:199'),
	perFireAspect: f(0.05, 'common/block/SkilletBlock.java:201-203'),
	heldUse: at('common/item/SkilletItem.java:148-185'),
	heldDuration: at('common/item/SkilletItem.java:141-146'),
	heldRelease: at('common/item/SkilletItem.java:210-224'),
	heldFinish: at('common/item/SkilletItem.java:226-251'),
	sneakPlace: at('common/item/SkilletItem.java:291-298'),
	noSweeping: at('common/item/SkilletItem.java:300-314'),
	attributes: f({ damage: 5.0, speed: -3.1, knockback: 1 }, 'common/item/SkilletItem.java:80-85'),
	registration: at('common/registry/ModItems.java:144-151'),
	hitSound: f(0.8, 'common/item/SkilletItem.java:90-109'),
	mineCost: f(1, 'common/item/SkilletItem.java:282-289'),
	placedUse: at('common/block/SkilletBlock.java:63-92'),
	addItem: at('common/block/entity/SkilletBlockEntity.java:181-207'),
	tick: f(2, 'common/block/entity/SkilletBlockEntity.java:60-78'),
	output: at('common/block/entity/SkilletBlockEntity.java:103-121'),
	fireAspectParticles: f(0.05, 'common/block/entity/SkilletBlockEntity.java:90-98'),
	storedSkillet: at('common/block/entity/SkilletBlockEntity.java:165-179'),
	playerBaseDamage: f(1, 'vanilla:Player.java:216'),
	playerBaseSpeed: f(4, 'vanilla:Attributes.java:9'),
};

const BOARD_B = 'common/block/CuttingBoardBlock.java';
const BOARD_BE = 'common/block/entity/CuttingBoardBlockEntity.java';
export const board = {
	use: at(`${BOARD_B}:55-95`),
	cut: at(`${BOARD_BE}:97-139`),
	toolWear: f(1, `${BOARD_BE}:112-118`),
	perCut: f(1, `${BOARD_BE}:124-126`),
	lookup: at(`${BOARD_BE}:141-157`),
	chance: at('common/crafting/ingredient/ChanceResult.java:30-41'),
	maxResults: f(4, 'common/crafting/CuttingBoardRecipe.java:52-70'),
	sounds: at(`${BOARD_BE}:163-177`),
	carveEvent: at(`${BOARD_B}:188-217`),
	carveRule: at(`${BOARD_BE}:218-226`),
	carveBlocks: at(`${BOARD_BE}:184-202`),
	capability: at(`${BOARD_BE}:72-79`),
	dispenserMixin: at('common/mixin/CuttingBoardDispenserMixin.java:37-43'),
	dispenser: at('common/block/entity/dispenser/CuttingBoardDispenseBehavior.java:34-57'),
	comparator: f(14, `${BOARD_B}:149-160`),
	survive: at(`${BOARD_B}:127-131`),
	flat: at('client/renderer/CuttingBoardRenderer.java:154'),
	breakDrop: at(`${BOARD_BE}:274-279`),
	resultSide: at(`${BOARD_BE}:106-111`),
};

export const feast = {
	servings: f(4, 'common/block/FeastBlock.java:41-81'),
	take: at('common/block/FeastBlock.java:103-141'),
	leftovers: at('common/block/FeastBlock.java:104-110'),
	vanish: at('common/block/FeastBlock.java:126-128'),
	containerCheck: at('common/block/FeastBlock.java:115-118'),
	comparator: at('common/block/FeastBlock.java:163-171'),
	survive: at('common/block/FeastBlock.java:148-156'),
	registrations: f(
		{
			'farmersdelight:roast_chicken_block': true,
			'farmersdelight:stuffed_pumpkin_block': false,
			'farmersdelight:honey_glazed_ham_block': true,
			'farmersdelight:shepherds_pie_block': true,
			'farmersdelight:gleaming_salad_block': true,
			'farmersdelight:rice_roll_medley_block': true,
		} as Record<string, boolean>,
		'common/registry/ModBlocks.java:308-325',
	),
	properties: at('common/registry/ModBlocks.java:80-82'),
	saladLightPerServing: f(3, 'common/registry/ModBlocks.java:35-37'),
	saladSparkle: f(0.05, 'common/block/GleamingSaladBlock.java:33-45'),
	medleyServings: f(8, 'common/block/RiceRollMedleyBlock.java:23-57'),
	medleyOrder: f(
		// index = servings left - 1 (RiceRollMedleyBlock#getServingItem)
		['farmersdelight:cod_roll', 'farmersdelight:cod_roll', 'farmersdelight:salmon_roll', 'farmersdelight:salmon_roll', 'farmersdelight:salmon_roll', 'farmersdelight:kelp_roll_slice', 'farmersdelight:kelp_roll_slice', 'farmersdelight:kelp_roll_slice'],
		'common/block/RiceRollMedleyBlock.java:34-62',
	),
};

export const pie = {
	bites: f(4, 'common/block/PieBlock.java:50-75'),
	knife: at('common/block/PieBlock.java:98-105'),
	cutSlice: at('common/block/PieBlock.java:157-175'),
	emptyHand: at('common/block/PieBlock.java:107-120'),
	consumeBite: at('common/block/PieBlock.java:125-152'),
	dispatch: at('vanilla:ServerPlayerGameMode.java:443-455'),
	comparator: at('common/block/PieBlock.java:192-195'),
	survive: at('common/block/PieBlock.java:182-185'),
	pastry: f({ cake: 7, pie: 4 }, 'common/loot/modifier/PastrySlicingModifier.java:33-68'),
	knifeCake: f(6, 'common/item/KnifeItem.java:100-142'),
	isKnife: at('common/utility/ItemUtils.java:36-42'),
	pumpkinPie: at('common/mixin/PlacePumpkinPieMixin.java:24-40'),
};

export const drink = {
	finish: at('common/item/ConsumableItem.java:52-89'),
	foodPath: at('common/item/ConsumableItem.java:58-71'),
	container: at('common/item/ConsumableItem.java:73-87'),
	removeRandom: at('common/item/component/consumable/RemoveRandomStatusEffectsConsumeEffect.java:46-66'),
	heal: at('common/item/component/consumable/HealConsumeEffect.java:15-33'),
	registration: at('common/registry/ModItems.java:385-393'),
	vanillaFinish: at('vanilla:Item.java:251-254'),
};

export const effect = {
	nourishment: at('common/effect/NourishmentEffect.java:22-42'),
	healingCheck: at('common/effect/NourishmentEffect.java:26-33'),
	alwaysEat: at('common/mixin/NourishmentAlwaysEatMixin.java:13-21'),
	tiers: f({ BRIEF: 600, SHORT: 1200, MEDIUM: 3600, LONG: 6000 } as Record<string, number>, 'common/FoodValues.java:22-25'),
	hidden: at('common/FoodValues.java:27-29'),
	soups: at('common/FoodValues.java:273-278'),
	soupEvent: at('common/event/CommonEvents.java:36-54'),
	rabbitSkip: at('common/event/CommonEvents.java:41-43'),
	rabbitBuff: at('common/event/CommonModBusEvents.java:46-48'),
	rabbitTooltip: at('client/event/TooltipEvents.java:33-48'),
	hud: at('client/gui/HUDOverlays.java:79-98'),
	comfort: at('common/effect/ComfortEffect.java:24-43'),
	comfortInterval: f(80, 'common/effect/ComfortEffect.java:40-43'),
	comfortHeal: f(1.0, 'common/effect/ComfortEffect.java:34-36'),
	comfortHud: at('client/gui/HUDOverlays.java:101-124'),
};

export const feed = {
	dogEffects: f(
		[
			{ id: 'minecraft:speed', ticks: 6000, amplifier: 0 },
			{ id: 'minecraft:strength', ticks: 6000, amplifier: 0 },
			{ id: 'minecraft:resistance', ticks: 6000, amplifier: 0 },
		],
		'common/item/DogFoodItem.java:40-43',
	),
	dogUse: at('common/item/DogFoodItem.java:52-85'),
	dogTooltip: at('common/item/DogFoodItem.java:88-113'),
	horseEffects: f(
		[
			{ id: 'minecraft:speed', ticks: 6000, amplifier: 1 },
			{ id: 'minecraft:jump_boost', ticks: 6000, amplifier: 0 },
		],
		'common/item/HorseFeedItem.java:41-43',
	),
	horseUse: at('common/item/HorseFeedItem.java:52-82'),
};

/** Skillet cook time, exactly as SkilletBlock#getSkilletCookingTime computes it (float maths included). */
export function skilletTicks(recipeTicks: number, fireAspect: number): number {
	const time = recipeTicks > 0 ? recipeTicks : skillet.defaultTicks.value;
	const secs = Math.trunc(time / 20);
	let factor = Math.fround(skillet.factor.value);
	if (fireAspect > 0) factor = Math.fround(factor - Math.fround(fireAspect * skillet.perFireAspect.value));
	const result = Math.trunc(Math.fround(secs * factor)) * 20;
	// Mth.clamp(value, min, max) = min(max(value, min), max) (vanilla Mth.java:94-96)
	return Math.min(Math.max(result, skillet.minTime.value), recipeTicks);
}
