// What each config option changes, in player words, with the code that reads it (paths relative to
// src/main/java/vectorwing/farmersdelight) for maintainers. Read from the mod's Java; see docs-facts-items.md section 4.
// `when`: when a change takes effect. 'reload' = when the world loads or after /reload (the option is a condition on
// data files), 'restart' = at game start (it changes item defaults, so client and server must match), 'new' = only in
// newly generated chunks. Without it, changes apply right away. `broken`: listed in the known-issues note on the page (1.3.4).
type Use = { src: string; lines: string };
export type ConfigNote = { text: string; uses: Use[]; link?: string; when?: 'reload' | 'restart' | 'new'; broken?: boolean };

export const configNotes: Record<string, ConfigNote> = {
	enableFarmerFDTrades: {
		text: "Novice and Apprentice Farmers sometimes buy Farmer's Delight crops for emeralds.",
		uses: [{ src: 'common/crafting/condition/ConfigEnabledCondition.java', lines: '22' }],
		link: '/mechanics/villagers/',
		when: 'reload',
	},
	enableWanderingTraderFDTrades: {
		text: "The Wandering Trader sometimes sells Farmer's Delight seeds and plants.",
		uses: [{ src: 'common/crafting/condition/ConfigEnabledCondition.java', lines: '23' }],
		link: '/mechanics/villagers/',
		when: 'reload',
	},
	enableRopeReeling: {
		text: 'Sneak and use an empty hand on a rope to pull the rope below it back up into your inventory.',
		uses: [
			{ src: 'common/block/RopeBlock.java', lines: '65' },
			{ src: 'common/block/RopeBlock.java', lines: '55-61' },
		],
		link: '/mechanics/straw-and-rope/',
		broken: true,
	},
	canvasSignDarkBackgroundList: {
		text: 'Canvas Signs in these colours start with white text instead of black, so the text stays readable.',
		uses: [{ src: 'common/block/state/CanvasSign.java', lines: '21' }],
		link: '/mechanics/storage-and-decoration/',
	},
	richSoilBoostChance: {
		text: 'The chance that Rich Soil gives the plant on it (or a hanging plant under it) a free bone-meal boost each time its turn comes. 0 turns the boost off.',
		uses: [{ src: 'common/block/RichSoilBlock.java', lines: '33-59' }],
		link: '/mechanics/rich-soil/',
	},
	enableTomatoVineClimbingTaggedRopes: {
		text: "Tomato vines can also climb other mods' ropes (anything in the farmersdelight:ropes tag). Careful: when such a vine is broken, it leaves the Default Tomato Vine Rope behind, not the rope it climbed.",
		uses: [
			{ src: 'common/block/TomatoBlock.java', lines: '155' },
			{ src: 'common/block/TomatoVineBlock.java', lines: '28' },
		],
		link: '/mechanics/crops/',
	},
	defaultTomatoVineRope: {
		text: 'The rope a broken tomato vine leaves behind.',
		uses: [
			{ src: 'common/block/HangingTomatoBlock.java', lines: '57' },
			{ src: 'common/block/TomatoBlock.java', lines: '272' },
		],
	},
	enableCookingPotRecipeBook: {
		text: 'Shows the recipe book button in the Cooking Pot.',
		uses: [{ src: 'client/gui/CookingPotScreen.java', lines: '45' }],
		link: '/mechanics/cooking-pot/',
	},
	enableVanillaCropCrates: {
		text: 'Lets you craft storage crates for carrots, potatoes and beetroots.',
		uses: [{ src: 'common/crafting/condition/VanillaCrateEnabledCondition.java', lines: '19' }],
		link: '/mechanics/storage-and-decoration/',
		when: 'reload',
	},
	cuttingBoardFortuneBonus: {
		text: 'Each level of Fortune on the tool you cut with adds this much to the chance of every bonus result on the Cutting Board.',
		uses: [{ src: 'common/crafting/ingredient/ChanceResult.java', lines: '30-41' }],
		link: '/mechanics/cutting-board/',
	},
	enableVanillaSoupExtraEffects: {
		text: "Mushroom Stew, Beetroot Soup and Rabbit Stew give Nourishment, like Farmer's Delight meals.",
		uses: [
			{ src: 'common/event/CommonEvents.java', lines: '36-54' },
			{ src: 'client/event/TooltipEvents.java', lines: '33' },
		],
		link: '/reference/food/',
	},
	enableRabbitStewBuff: {
		text: 'Gives Rabbit Stew better food values, to match what it costs to make.',
		uses: [
			{ src: 'common/event/CommonModBusEvents.java', lines: '46-48' },
			{ src: 'common/event/CommonEvents.java', lines: '41-43' },
		],
		link: '/differences/',
		when: 'restart',
	},
	enablePumpkinPieSneakToPlace: {
		text: 'You can always place a Pumpkin Pie as a block. With this on, you have to sneak to do it.',
		uses: [
			{ src: 'common/mixin/PlacePumpkinPieMixin.java', lines: '28' },
			{ src: 'client/event/TooltipEvents.java', lines: '30' },
		],
		link: '/mechanics/pies/',
	},
	enableCuttingBoardDispenserBehavior: {
		text: 'A Dispenser facing a Cutting Board uses the tools inside it to cut whatever lies on the board.',
		uses: [{ src: 'common/mixin/CuttingBoardDispenserMixin.java', lines: '39' }],
		link: '/mechanics/cutting-board/',
	},
	enableStackableSoupItems: {
		text: "The soups listed in Soup Items stack like Farmer's Delight meals instead of one at a time.",
		uses: [{ src: 'common/event/CommonModBusEvents.java', lines: '36-45' }],
		when: 'restart',
	},
	soupItemList: {
		text: "The items Stackable Soup Items applies to. Names the game doesn't know are skipped.",
		uses: [{ src: 'common/event/CommonModBusEvents.java', lines: '37-43' }],
		when: 'restart',
	},
	generateFDChestLoot: {
		text: "Adds Farmer's Delight items (ropes, crops, tools and more) to chest loot all over the world.",
		uses: [{ src: 'common/loot/modifier/FDAddTableLootModifier.java', lines: '38-43' }],
		link: '/mechanics/loot/',
	},
	generateVillageCompostHeaps: {
		text: 'Villages sometimes have a compost heap with Organic Compost.',
		uses: [{ src: 'common/world/VillageStructures.java', lines: '25' }],
		link: '/mechanics/villagers/',
		when: 'new',
	},
	generateFDCropsOnVillageFarms: {
		text: "Some village farm plots grow Farmer's Delight crops instead of wheat and potatoes.",
		uses: [{ src: 'common/world/VillageStructures.java', lines: '36' }],
		link: '/mechanics/villagers/',
		when: 'new',
	},
	enableTomatoRopePermanence: {
		text: 'Breaking a tomato vine that hangs on a rope leaves the rope in place. Turn it off if it gets in the way when you edit a build with commands.',
		uses: [{ src: 'common/block/HangingTomatoBlock.java', lines: '51' }],
	},
	enableNourishmentHungerOverlay: {
		text: 'Gilds your hunger bar while you have Nourishment.',
		uses: [{ src: 'client/gui/HUDOverlays.java', lines: '79-98' }],
		link: '/mechanics/effects/',
	},
	enableComfortHealthOverlay: {
		text: "Shows an overlay on your hearts while you have Comfort. No food gives Comfort any more, so you won't see it in normal play.",
		uses: [{ src: 'client/gui/HUDOverlays.java', lines: '101-124' }],
		link: '/mechanics/effects/',
	},
	enableFoodEffectTooltip: {
		text: "Lists a food's effects in its tooltip, also for Dog Food, Horse Feed and the vanilla soups.",
		uses: [
			{ src: 'common/item/ConsumableItem.java', lines: '99' },
			{ src: 'common/item/DogFoodItem.java', lines: '90' },
			{ src: 'common/item/HorseFeedItem.java', lines: '89' },
			{ src: 'client/event/TooltipEvents.java', lines: '33' },
		],
	},
};
