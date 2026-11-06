// Logic constants for the farming pages, read from the mod's Java (paths relative to vectorwing/farmersdelight) or,
// where marked `vanilla`, from the decompiled Minecraft 26.3 sources. Each value carries its citation for maintainers;
// the player pages never show it. Plain-language helpers for the pages are at the bottom of each section.

export type Cited<T = number> = { value: T; src: string; vanilla?: boolean };
const c = <T,>(value: T, src: string, vanilla = false): Cited<T> => ({ value, src, vanilla });
/** Split a 'path:lines' citation into the props of <Src>. */
export function cite(x: Cited<any> | string) {
	const s = typeof x === 'string' ? x : x.src;
	const i = s.lastIndexOf(':');
	return i > 0 && /^[\d-]+$/.test(s.slice(i + 1)) ? { path: s.slice(0, i), lines: s.slice(i + 1) } : { path: s };
}

// ------------------------------------------------------------------ vanilla
export const vanilla = {
	/** Default randomTickSpeed game rule: growth checks per 16x16x16 section per game tick. */
	randomTickSpeed: c(3, 'net/minecraft/world/level/gamerules/GameRules.java:74', true),
	/** Each of those ticks picks one random block position in the section (with replacement). */
	sectionTick: c(16 * 16 * 16, 'net/minecraft/server/level/ServerLevel.java:546-552', true),
	/** getBrightness(LightLayer.SKY) reads the raw sky light layer (no day/night darkening). */
	skyBrightness: c(null, 'net/minecraft/world/level/BlockAndLightGetter.java:9-11', true),
	cropRandomTick: c(9, 'net/minecraft/world/level/block/CropBlock.java:72-84', true),
	cropBonemeal: c({ min: 2, max: 5 }, 'net/minecraft/world/level/block/CropBlock.java:91-93', true),
	cropGrowthSpeed: c(null, 'net/minecraft/world/level/block/CropBlock.java:95-139', true),
	cropSurviveLight: c(8, 'net/minecraft/world/level/block/CropBlock.java:148-150', true),
	cakeMaxBites: c(6, 'net/minecraft/world/level/block/CakeBlock.java:32', true),
	playerAttackDamage: c(1, 'net/minecraft/world/entity/player/Player.java:216', true),
	playerAttackSpeed: c(4, 'net/minecraft/world/entity/ai/attributes/Attributes.java:9', true),
	hayFallFactor: c(0.2, 'net/minecraft/world/level/block/HayBlock.java:18', true),
	booleanDefault: c(true, 'net/minecraft/world/level/block/state/properties/BooleanProperty.java:7', true),
	useDispatch: c(null, 'net/minecraft/server/level/ServerPlayerGameMode.java:442-455', true),
};

/** Mean game ticks between two growth checks ("randomTick" calls) of one block at a given randomTickSpeed (vanilla math). */
export const ticksPerRandomTick = (speed = vanilla.randomTickSpeed.value) => vanilla.sectionTick.value / speed;

// ------------------------------------------------------------------ organic compost
export const compost = {
	stages: c(7, 'common/block/OrganicCompostBlock.java:20'),
	cube: c(1, 'common/block/OrganicCompostBlock.java:50'),
	activator: c(0.02, 'common/block/OrganicCompostBlock.java:52-54'),
	water: c(0.1, 'common/block/OrganicCompostBlock.java:55-57'),
	waterAdd: c(0.1, 'common/block/OrganicCompostBlock.java:65'),
	skyThreshold: c(12, 'common/block/OrganicCompostBlock.java:58-64'),
	skyBright: c(0.1, 'common/block/OrganicCompostBlock.java:64'),
	skyDim: c(0.05, 'common/block/OrganicCompostBlock.java:64'),
	roll: c(null, 'common/block/OrganicCompostBlock.java:67-72'),
	comparator: c(null, 'common/block/OrganicCompostBlock.java:80-83'),
	particles: c(10, 'common/block/OrganicCompostBlock.java:88-90'),
};
/** Chance per growth check, as computed in OrganicCompostBlock.randomTick. `activators` counts the whole 3x3x3 cube, the compost included. */
export function compostChance(activators: number, bright: boolean, water: boolean) {
	return activators * compost.activator.value + (bright ? compost.skyBright.value : compost.skyDim.value) + (water ? compost.waterAdd.value : 0);
}

// ------------------------------------------------------------------ rich soil
export const richSoil = {
	randomTick: c(null, 'common/block/RichSoilBlock.java:32-42'),
	colony: c(null, 'common/block/RichSoilBlock.java:77-88'),
	boostRoll: c(null, 'common/block/RichSoilBlock.java:44-47'),
	aboveBelow: c(null, 'common/block/RichSoilBlock.java:49-59'),
	boostPlant: c(null, 'common/block/RichSoilBlock.java:62-75'),
	sustain: c(null, 'common/block/RichSoilBlock.java:93-100'),
	configLine: c(null, 'common/Configuration.java:70-71'),
	tilling: c(null, 'src/main/resources/data/neoforge/data_maps/block/transformables.json'),
	farmlandTick: c(null, 'common/block/RichSoilFarmlandBlock.java:29-41'),
	nearWater: c(4, 'common/block/RichSoilFarmlandBlock.java:43-47'),
	farmlandSurvive: c(null, 'common/block/RichSoilFarmlandBlock.java:55-59'),
	fertile: c(null, 'common/block/RichSoilFarmlandBlock.java:61-67'),
	farmlandSustain: c(null, 'common/block/RichSoilFarmlandBlock.java:76-86'),
	noTrample: c(null, 'common/block/RichSoilFarmlandBlock.java:93-96'),
};

// ------------------------------------------------------------------ mushroom colonies
export const colony = {
	maxAge: c(3, 'common/block/MushroomColonyBlock.java:119-121'),
	growth: c(4, 'common/block/MushroomColonyBlock.java:123-131'),
	shears: c(1, 'common/block/MushroomColonyBlock.java:68-78'),
	knife: c(null, 'common/block/MushroomColonyBlock.java:79-91'),
	survive: c(13, 'common/block/MushroomColonyBlock.java:111-117'),
	bonemeal: c({ min: 1, max: 2 }, 'common/block/MushroomColonyBlock.java:143-161'),
	isKnife: c(null, 'common/utility/ItemUtils.java:36-42'),
};

// ------------------------------------------------------------------ crops
export const crops = {
	cabbageSeed: c(null, 'common/block/CabbageBlock.java:30-33'),
	onionSeed: c(null, 'common/block/OnionBlock.java:35-38'),
	budMaxAge: c(3, 'common/block/BuddingBushBlock.java:36-37'),
	budFarmland: c(null, 'common/block/BuddingBushBlock.java:54-57'),
	budTick: c(9, 'common/block/BuddingBushBlock.java:84-101'),
	budSpeed: c(null, 'common/block/BuddingBushBlock.java:113-154'),
	budSurvive: c(8, 'common/block/BuddingBushBlock.java:166-168'),
	budToVine: c(null, 'common/block/BuddingTomatoBlock.java:42-45'),
	budBonemeal: c({ min: 1, max: 4 }, 'common/block/BuddingTomatoBlock.java:57-71'),
	tomatoMaxAge: c(3, 'common/block/TomatoBlock.java:138-141'),
	tomatoTick: c(9, 'common/block/TomatoBlock.java:110-120'),
	tomatoSpeed: c(5, 'common/block/TomatoBlock.java:113-114'),
	tomatoHarvest: c({ min: 1, max: 2, rotten: 0.05 }, 'common/block/TomatoBlock.java:65-83'),
	tomatoBoneClick: c(null, 'common/block/TomatoBlock.java:57-63'),
	tomatoClimb: c(3, 'common/block/TomatoBlock.java:166-178'),
	tomatoRopeTag: c(null, 'common/block/TomatoBlock.java:154-156'),
	tomatoBonemeal: c(2, 'common/block/TomatoBlock.java:180-183'),
	tomatoBonemealValid: c(null, 'common/block/TomatoBlock.java:185-207'),
	tomatoBonemealClimb: c(0.3, 'common/block/TomatoBlock.java:209-230'),
	tomatoSurvive: c(8, 'common/block/TomatoBlock.java:232-246'),
	ropeBack: c(null, 'common/block/HangingTomatoBlock.java:37-66'),
	riceTick: c(6, 'common/block/RiceBlock.java:52-74'),
	riceChance: c(10, 'common/block/RiceBlock.java:59-60'),
	riceSurvive: c(8, 'common/block/RiceBlock.java:81-90'),
	ricePlace: c(null, 'common/block/RiceBlock.java:139-144'),
	riceBonemeal: c({ min: 1, max: 4 }, 'common/block/RiceBlock.java:146-185'),
	riceHint: c(null, 'common/item/RiceItem.java:21-32'),
	paniclesBonemeal: c(3, 'common/block/RicePaniclesBlock.java:58-61'),
	paniclesSurvive: c(8, 'common/block/RicePaniclesBlock.java:63-71'),
};
/** Vanilla crop roll: random.nextInt((int)(25 / speed) + 1) == 0. */
export const cropRoll = (speed: number) => 1 / (Math.floor(25 / speed) + 1);

// ------------------------------------------------------------------ wild crops
export const wild = {
	effects: c(null, 'common/registry/ModBlocks.java:271-288'),
	placeOn: c(null, 'common/block/WildCropBlock.java:35-38'),
	flammable: c({ flammability: 100, spread: 60 }, 'common/block/WildCropBlock.java:45-53'),
	bonemealChance: c(0.8, 'common/block/WildCropBlock.java:60-63'),
	spreadLimit: c(10, 'common/block/WildCropBlock.java:65-91'),
	spreadArea: c({ xz: 4, y: 1 }, 'common/block/WildCropBlock.java:69'),
	riceSurvive: c(null, 'common/block/WildRiceBlock.java:43-56'),
	ricePlace: c(null, 'common/block/WildRiceBlock.java:82-92'),
	riceBonemeal: c(0.3, 'common/block/WildRiceBlock.java:111-119'),
	shrubSand: c(null, 'common/block/SandyShrubBlock.java:36-39'),
	shrubBonemeal: c(null, 'common/block/SandyShrubBlock.java:41-56'),
	modifier: c(null, 'common/world/modifier/AddFeaturesByFilterBiomeModifier.java:28-42'),
	biomeTagFilter: c(null, 'common/world/filter/BiomeTagFilter.java:37-40'),
	inOrder: c(null, 'common/world/feature/InOrderFeature.java:27-39'),
	riceFeature: c(null, 'common/world/feature/WildRiceFeature.java:29-39'),
};

// ------------------------------------------------------------------ straw, rope and friends
export const rope = {
	props: c(null, 'common/registry/ModBlocks.java:126-129'),
	useItemOn: c(null, 'common/block/RopeBlock.java:54-61'),
	reel: c(null, 'common/block/RopeBlock.java:63-80'),
	bell: c(24, 'common/block/RopeBlock.java:81-95'),
	connect: c(null, 'common/block/RopeBlock.java:106-133'),
	noCollision: c(null, 'common/block/RopeBlock.java:135-143'),
	replace: c(null, 'common/block/RopeBlock.java:145-148'),
	item: c(null, 'common/item/RopeItem.java:21-62'),
	floats: c(null, 'common/item/RopeItem.java:64-67'),
	fence: c(null, 'common/block/RopeFenceBlock.java:42-69'),
	fenceMixin: c(null, 'common/mixin/RopeFenceConnectionMixin.java:15-27'),
	gate: c(null, 'common/block/RopeFenceGateBlock.java:22-23'),
	reelConfig: c(null, 'common/Configuration.java:62-63'),
};
export const net = {
	shape: c({ from: 7, to: 9 }, 'common/block/SafetyNetBlock.java:29'),
	fall: c(0, 'common/block/SafetyNetBlock.java:67-74'),
	bounce: c(0.6, 'common/registry/ModBlocks.java:128-129'),
};
export const bales = {
	riceFall: c(0.2, 'common/block/RiceBaleBlock.java:27-30'),
	riceFacing: c(null, 'common/block/RiceBaleBlock.java:32-35'),
	riceFire: c({ flammability: 20, spread: 60 }, 'common/block/RiceBaleBlock.java:37-45'),
	straw: c(null, 'common/block/StrawBaleBlock.java:9-23'),
	signal: c(null, 'common/mixin/CampfireBaleMixin.java:14-19'),
};
export const tatami = {
	pair: c(null, 'common/block/TatamiBlock.java:36-66'),
	unpair: c(null, 'common/block/TatamiBlock.java:68-74'),
	mat: c(null, 'common/block/TatamiMatBlock.java:27-107'),
	half: c(null, 'common/block/TatamiHalfMatBlock.java:39-47'),
	rug: c(1, 'common/block/CanvasRugBlock.java:13-27'),
};

// ------------------------------------------------------------------ storage
export const storage = {
	cabinetSlots: c(27, 'common/block/entity/CabinetBlockEntity.java:37'),
	cabinetMenu: c(null, 'common/block/entity/CabinetBlockEntity.java:119'),
	cabinetPiglins: c(null, 'common/block/CabinetBlock.java:48'),
	basketSlots: c(27, 'common/block/entity/BasketBlockEntity.java:32'),
	basketArea: c(null, 'common/block/entity/Basket.java:18-25'),
	basketCollect: c(null, 'common/block/entity/Basket.java:81-104'),
	basketCooldown: c(8, 'common/block/entity/BasketBlockEntity.java:124-139'),
	basketTick: c(null, 'common/block/entity/BasketBlockEntity.java:165-173'),
	basketFull: c(null, 'common/block/entity/BasketBlockEntity.java:141-148'),
	basketRedstone: c(null, 'common/block/BasketBlock.java:112-118'),
	basketFacing: c(null, 'common/block/BasketBlock.java:130-136'),
	basketDefault: c(null, 'common/block/BasketBlock.java:67-70'),
	basketInsert: c(8, 'common/block/entity/inventory/BasketInvWrapper.java:18-28'),
	signText: c(null, 'common/mixin/SignBlockEntityMixin.java:23-32'),
	signDark: c(null, 'common/block/state/CanvasSign.java:19-22'),
	cratesCondition: c(null, 'common/crafting/condition/VanillaCrateEnabledCondition.java:19'),
};

// ------------------------------------------------------------------ knives
export const knife = {
	props: c(null, 'common/registry/ModItems.java:94-107'),
	tiers: c(null, 'common/registry/ModItems.java:318-329'),
	baseDamage: c(0.5, 'common/registry/ModItems.java:100'),
	speedModifier: c(-2, 'common/registry/ModItems.java:100'),
	attributes: c(null, 'common/item/KnifeItem.java:60-65'),
	noSweeping: c(null, 'common/item/KnifeItem.java:67-81'),
	knockback: c(0.1, 'common/item/KnifeItem.java:91-98'),
	abilities: c(null, 'common/item/KnifeItem.java:47-53'),
	candleCake: c(null, 'common/item/KnifeItem.java:113-124'),
	cake: c(null, 'common/item/KnifeItem.java:126-141'),
	pumpkin: c(4, 'common/item/KnifeItem.java:145-168'),
	pastry: c(null, 'common/loot/modifier/PastrySlicingModifier.java:33-34'),
	pieSlices: c(4, 'common/block/PieBlock.java:73-75'),
	backstabAngle: c(-0.5, 'common/item/enchantment/BackstabbingEnchantment.java:24-32'),
	backstabEvent: c(null, 'common/item/enchantment/BackstabbingEnchantment.java:44-62'),
	backstabJson: c(null, 'src/generated/resources/data/farmersdelight/enchantment/backstabbing.json'),
	/** Backstabbing enchantment definition: values from G/farmersdelight/enchantment/backstabbing.json. */
	backstab: c(
		{ base: 1.4, perLevel: 0.2, maxLevel: 3, weight: 5, anvilCost: 2, minCost: { base: 15, per: 9 }, maxCost: { base: 50, per: 8 } },
		'src/generated/resources/data/farmersdelight/enchantment/backstabbing.json:2-33',
	),
};

// ------------------------------------------------------------------ villagers and villages
export const village = {
	heaps: c(
		[
			['plains', 5],
			['snowy', 3],
			['savanna', 4],
			['desert', 3],
			['taiga', 4],
		] as [string, number][],
		'common/world/VillageStructures.java:24-34',
	),
	farms: c(
		{
			temperate: { lists: ['farm_plains', 'farm_taiga'], rules: [['wheat', 0.3, 'cabbages'], ['wheat', 0.3, 'tomatoes'], ['wheat', 0.3, 'onions']] },
			cold: { lists: ['farm_snowy'], rules: [['wheat', 0.3, 'cabbages'], ['wheat', 0.3, 'onions'], ['potatoes', 0.2, 'cabbages'], ['potatoes', 0.2, 'onions']] },
			arid: { lists: ['farm_savanna', 'farm_desert'], rules: [['wheat', 0.3, 'cabbages'], ['wheat', 0.3, 'tomatoes']] },
		} as Record<string, { lists: string[]; rules: [string, number, string][] }>,
		'common/world/VillageStructures.java:37-63',
	),
	poi: c(null, 'common/mixin/VillagersTargetRichSoilMixin.java:21-29'),
	heapsConfig: c(null, 'common/Configuration.java:109-112'),
	villagerFood: c(null, 'common/ItemDefaultComponents.java:90-93'),
};

// ------------------------------------------------------------------ helpers for pages
export const oneIn = (p: number) => `1 in ${+(1 / p).toFixed(1)}`;
/** Mean real time (at 20 TPS) for `n` growth checks of one block at the default randomTickSpeed. */
export function randomTicksToTime(n: number) {
	const s = (n * ticksPerRandomTick()) / 20;
	if (s < 120) return `${Math.round(s)} s`;
	if (s < 7200) return `${Math.round(s / 60)} min`;
	return `${+(s / 3600).toFixed(1)} h`;
}

/** Alias with the player-facing name: average time for `n` growth checks. */
export const checksToTime = randomTicksToTime;
/** Average seconds between two growth checks of one block at the default game rule, e.g. 68.3. */
export const checkSeconds = () => +(ticksPerRandomTick() / 20).toFixed(1);
/** Labels for a stage strip: "1", "2", ... and `last` on the final stage. Extra labels override. */
export function stageLabels(count: number, last = 'ripe', extra: Record<string, string> = {}) {
	const out: Record<string, string> = {};
	for (let i = 0; i < count; i++) out[String(i)] = i === count - 1 ? last : `${i + 1}`;
	return { ...out, ...extra };
}
/**
 * Stages added by one bone meal on a crop that divides vanilla's increase (uniform min..max) by `div` with integer
 * division (TomatoBlock / RicePaniclesBlock getBonemealAgeIncrease). Returns each possible result with its chance.
 */
export function bonemealSplit(div: number) {
	const { min, max } = vanilla.cropBonemeal.value;
	const n = max - min + 1;
	const out = new Map<number, number>();
	for (let v = min; v <= max; v++) out.set(Math.floor(v / div), (out.get(Math.floor(v / div)) ?? 0) + 1 / n);
	return [...out.entries()].sort((a, b) => a[0] - b[0]).map(([stages, chance]) => ({ stages, chance }));
}

/** Suspicious stew effect of each wild crop: [block, effect id, seconds] (FlowerBlock takes seconds, vanilla FlowerBlock.java:21-31). */
export const wildStew = c(
	[
		['wild_cabbages', 'minecraft:strength', 6],
		['wild_onions', 'minecraft:fire_resistance', 6],
		['wild_tomatoes', 'minecraft:poison', 10],
		['wild_carrots', 'minecraft:mining_fatigue', 6],
		['wild_potatoes', 'minecraft:nausea', 8],
		['wild_beetroots', 'minecraft:water_breathing', 8],
	] as [string, string, number][],
	'common/registry/ModBlocks.java:271-288',
);
export const flowerSeconds = c(20, 'net/minecraft/world/level/block/FlowerBlock.java:21-31', true);

// ------------------------------------------------------------------ village farm crop odds
/** Vanilla 26.3 farm processor lists (one rule processor each): [input, chance, output]. */
export const vanillaFarmRules = c(
	{
		farm_plains: [['wheat', 0.3, 'carrots'], ['wheat', 0.2, 'potatoes'], ['wheat', 0.1, 'beetroots']],
		farm_desert: [['wheat', 0.2, 'beetroots'], ['wheat', 0.1, 'melon_stem']],
		farm_savanna: [['wheat', 0.1, 'melon_stem']],
		farm_snowy: [['wheat', 0.1, 'carrots'], ['wheat', 0.8, 'potatoes']],
		farm_taiga: [['wheat', 0.3, 'pumpkin_stem'], ['wheat', 0.2, 'potatoes']],
	} as Record<string, [string, number, string][]>,
	'data/minecraft/worldgen/processor_list/farm_*.json',
	true,
);
/** Vanilla's farm processors draw at most this many random numbers per block; FD's processor discards that many first. */
export const vanillaFarmDraws = c(3, 'common/world/VillageStructures.java:84-104');
/**
 * Share of a farm's wheat plots that end up as each crop. Each rule processor makes a fresh random source seeded from
 * the block position (see ruleProcessorSrc); a rule only draws when its input block matches (randomMatchSrc), first
 * match wins. FD's processor starts with never-matching rules that use up the numbers vanilla's processor drew
 * (vanillaFarmDraws), so its own rules roll independently of vanilla's. Exact expected shares.
 */
export function villageFarmShares(list: string, fd: [string, number, string][]) {
	const apply = (dist: Record<string, number>, rules: [string, number, string][]) => {
		const out: Record<string, number> = {};
		for (const [state, w] of Object.entries(dist)) {
			let left = w;
			for (const [input, p, output] of rules) {
				if (input !== state) continue;
				out[output] = (out[output] ?? 0) + left * p;
				left *= 1 - p;
			}
			out[state] = (out[state] ?? 0) + left;
		}
		return out;
	};
	return apply(apply({ wheat: 1 }, vanillaFarmRules.value[list] ?? []), fd);
}
export const ruleProcessorSrc = c(null, 'net/minecraft/world/level/levelgen/structure/templatesystem/RuleProcessor.java:32-40', true);
export const randomMatchSrc = c(null, 'net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.java:29-30', true);
/** Sum of the element weights in the vanilla village house pools (data/minecraft/worldgen/template_pool/village/<type>/houses.json). */
export const vanillaHousePoolWeight = c({ plains: 87, snowy: 68, savanna: 81, desert: 72, taiga: 76 } as Record<string, number>, 'data/minecraft/worldgen/template_pool/village/*/houses.json', true);
