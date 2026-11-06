// What each config option actually changes, with the code that reads it (paths relative to
// src/main/java/vectorwing/farmersdelight). Read from the mod's Java; see docs-facts-items.md section 4.
type Use = { src: string; lines: string };
export type ConfigNote = { text: string; uses: Use[]; link?: string };

export const configNotes: Record<string, ConfigNote> = {
	enableFarmerFDTrades: {
		text: 'Villager trades are data files in 26.3. This option is checked by the farmersdelight:config_enabled load condition on the farmer trade files, so it applies when data packs load.',
		uses: [{ src: 'common/crafting/condition/ConfigEnabledCondition.java', lines: '22' }],
		link: '/mechanics/villagers/',
	},
	enableWanderingTraderFDTrades: {
		text: 'Same load condition, on the wandering trader trade files.',
		uses: [{ src: 'common/crafting/condition/ConfigEnabledCondition.java', lines: '23' }],
		link: '/mechanics/villagers/',
	},
	enableRopeReeling: {
		text: 'Sneak-use with an empty hand on a rope pulls the rope column back into your inventory.',
		uses: [{ src: 'common/block/RopeBlock.java', lines: '65' }],
		link: '/mechanics/straw-and-rope/',
	},
	canvasSignDarkBackgroundList: {
		text: 'Canvas Signs dyed one of these colours start with white text instead of black.',
		uses: [{ src: 'common/block/state/CanvasSign.java', lines: '21' }],
		link: '/mechanics/storage-and-decoration/',
	},
	richSoilBoostChance: {
		text: 'Chance, each time Rich Soil gets a random tick, that it bone-meals the plant on top (or a #farmersdelight:planted_from_below plant underneath). 0 turns the boost off.',
		uses: [{ src: 'common/block/RichSoilBlock.java', lines: '33-59' }],
		link: '/mechanics/rich-soil/',
	},
	enableTomatoVineClimbingTaggedRopes: {
		text: 'Lets tomato vines climb any block in #farmersdelight:ropes (for example other mods\' ropes). Climbed ropes become the rope set in defaultTomatoVineRope when the vine is broken.',
		uses: [
			{ src: 'common/block/TomatoBlock.java', lines: '155' },
			{ src: 'common/block/TomatoVineBlock.java', lines: '28' },
		],
		link: '/mechanics/crops/',
	},
	defaultTomatoVineRope: {
		text: 'Block id left behind when a tomato vine on a rope is broken.',
		uses: [
			{ src: 'common/block/HangingTomatoBlock.java', lines: '57' },
			{ src: 'common/block/TomatoBlock.java', lines: '272' },
		],
	},
	enableCookingPotRecipeBook: {
		text: 'Shows the recipe book button in the Cooking Pot screen.',
		uses: [{ src: 'client/gui/CookingPotScreen.java', lines: '45' }],
		link: '/mechanics/cooking-pot/',
	},
	enableVanillaCropCrates: {
		text: 'Carrot, potato and beetroot crate recipes carry the farmersdelight:vanilla_crates_enabled load condition, which reads this option.',
		uses: [{ src: 'common/crafting/condition/VanillaCrateEnabledCondition.java', lines: '19' }],
		link: '/pack-makers/recipes/#load-conditions',
	},
	cuttingBoardFortuneBonus: {
		text: 'Added to the chance of every Cutting Board result, per Fortune level on the tool.',
		uses: [{ src: 'common/crafting/ingredient/ChanceResult.java', lines: '30-41' }],
		link: '/mechanics/cutting-board/',
	},
	enableVanillaSoupExtraEffects: {
		text: 'Mushroom Stew, Beetroot Soup and Rabbit Stew grant Nourishment when finished eating, and show it in their tooltips. With enableRabbitStewBuff on (the default), Rabbit Stew is skipped, so only its tooltip shows Nourishment.',
		uses: [
			{ src: 'common/event/CommonEvents.java', lines: '36-54' },
			{ src: 'client/event/TooltipEvents.java', lines: '33' },
		],
		link: '/reference/food/',
	},
	enableRabbitStewBuff: {
		text: 'Replaces Rabbit Stew\'s food component at startup. The soup-effect handler returns early for Rabbit Stew while this is on.',
		uses: [
			{ src: 'common/event/CommonModBusEvents.java', lines: '46-48' },
			{ src: 'common/event/CommonEvents.java', lines: '41-43' },
		],
		link: '/differences/',
	},
	enablePumpkinPieSneakToPlace: {
		text: 'Vanilla Pumpkin Pie can always be placed as a block; this only decides whether you must sneak to do it.',
		uses: [
			{ src: 'common/mixin/PlacePumpkinPieMixin.java', lines: '28' },
			{ src: 'client/event/TooltipEvents.java', lines: '30' },
		],
		link: '/mechanics/pies/',
	},
	enableCuttingBoardDispenserBehavior: {
		text: 'A dispenser facing a Cutting Board uses its items as cutting tools on the board.',
		uses: [{ src: 'common/mixin/CuttingBoardDispenserMixin.java', lines: '39' }],
		link: '/mechanics/cutting-board/',
	},
	enableStackableSoupItems: {
		text: 'Sets the maximum stack size of every item in soupItemList to 16, at startup.',
		uses: [{ src: 'common/event/CommonModBusEvents.java', lines: '36-45' }],
	},
	soupItemList: {
		text: 'Unknown ids are logged and skipped.',
		uses: [{ src: 'common/event/CommonModBusEvents.java', lines: '37-43' }],
	},
	generateFDChestLoot: {
		text: 'Checked by the farmersdelight:add_loot_table loot modifier, which all 14 chest modifiers use.',
		uses: [{ src: 'common/loot/modifier/FDAddTableLootModifier.java', lines: '38-43' }],
		link: '/mechanics/loot/',
	},
	generateVillageCompostHeaps: {
		text: 'Adds compost-heap houses to the village house pools.',
		uses: [{ src: 'common/world/VillageStructures.java', lines: '25' }],
		link: '/mechanics/villagers/',
	},
	generateFDCropsOnVillageFarms: {
		text: 'Adds rule processors to the vanilla village farm processor lists that swap some wheat and potatoes for Farmer\'s Delight crops.',
		uses: [{ src: 'common/world/VillageStructures.java', lines: '36' }],
		link: '/mechanics/villagers/',
	},
	enableTomatoRopePermanence: {
		text: 'Breaking a tomato vine that hangs on rope places a rope block back.',
		uses: [{ src: 'common/block/HangingTomatoBlock.java', lines: '51' }],
	},
	enableNourishmentHungerOverlay: {
		text: 'Gilded hunger bar while Nourished.',
		uses: [{ src: 'client/gui/HUDOverlays.java', lines: '79-98' }],
		link: '/mechanics/effects/',
	},
	enableComfortHealthOverlay: {
		text: 'Health bar overlay for Comfort. No food grants Comfort any more, so this overlay never shows in normal play.',
		uses: [{ src: 'client/gui/HUDOverlays.java', lines: '101-124' }],
		link: '/mechanics/effects/',
	},
	enableFoodEffectTooltip: {
		text: 'Effect lines in food tooltips, also for Dog Food, Horse Feed and vanilla soups.',
		uses: [
			{ src: 'common/item/ConsumableItem.java', lines: '99' },
			{ src: 'common/item/DogFoodItem.java', lines: '90' },
			{ src: 'common/item/HorseFeedItem.java', lines: '89' },
			{ src: 'client/event/TooltipEvents.java', lines: '33' },
		],
	},
};
