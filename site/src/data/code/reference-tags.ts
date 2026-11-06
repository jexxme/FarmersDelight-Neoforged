// What each Farmer's Delight tag is read by. Java paths are relative to src/main/java/vectorwing/farmersdelight;
// other paths are repo paths. Key: "<registry> <tag>". Tags not listed here are classification tags (used to
// group items for other tags, recipe viewers or mods) and are described generically on the page.
type Use = { src: string; lines?: string };
export type TagPurpose = { text: string; uses?: Use[]; unused?: boolean };

export const tagPurposes: Record<string, TagPurpose> = {
	'block farmersdelight:campfire_signal_smoke': { text: 'A campfire on top of these blocks makes signal smoke, like on a hay bale.', uses: [{ src: 'common/mixin/CampfireBaleMixin.java', lines: '16' }] },
	'block farmersdelight:compost_activators': { text: 'Neighbours that speed up Organic Compost.', uses: [{ src: 'common/block/OrganicCompostBlock.java', lines: '52' }] },
	'block farmersdelight:drops_cake_slice': { text: 'A knife on these blocks (candle cakes) gives a cake slice and leaves a cake with one bite taken.', uses: [{ src: 'common/item/KnifeItem.java', lines: '113' }] },
	'block farmersdelight:drops_straw': { text: 'Breaking these with a knife can drop Straw (loot modifier straw_from_grass).', uses: [{ src: 'src/generated/resources/data/farmersdelight/loot_modifiers/straw_from_grass.json' }] },
	'block farmersdelight:heat_conductors': { text: 'Blocks that pass heat up to a Cooking Pot from a heat source below them.', uses: [{ src: 'common/block/entity/HeatableBlockEntity.java', lines: '28' }] },
	'block farmersdelight:heat_sources': { text: 'Blocks that heat a Cooking Pot or Skillet placed on them, and that count as "near heat" for a Skillet in hand.', uses: [{ src: 'common/block/entity/HeatableBlockEntity.java', lines: '22' }, { src: 'common/item/SkilletItem.java', lines: '129' }] },
	'block farmersdelight:tray_heat_sources': { text: 'Heat sources that also make the Cooking Pot and Skillet show their tray (open fire and lava).', uses: [{ src: 'common/block/CookingPotBlock.java', lines: '132' }, { src: 'common/block/SkilletBlock.java', lines: '188' }] },
	'block farmersdelight:mushroom_colony_growable_on': { text: 'Mushroom colonies only grow on these blocks.', uses: [{ src: 'common/block/MushroomColonyBlock.java', lines: '127' }] },
	'block farmersdelight:planted_from_below': { text: 'Plants that hang below Rich Soil and get its growth boost from above.', uses: [{ src: 'common/block/RichSoilBlock.java', lines: '51-57' }] },
	'block farmersdelight:unaffected_by_rich_soil': { text: 'Plants that Rich Soil never boosts.', uses: [{ src: 'common/block/RichSoilBlock.java', lines: '63' }] },
	'block farmersdelight:ropes': { text: 'Ropes tomato vines can climb (with enableTomatoVineClimbingTaggedRopes on).', uses: [{ src: 'common/block/TomatoBlock.java', lines: '155' }] },
	'block farmersdelight:mineable/knife': { text: 'Blocks a knife mines at its tool speed.', uses: [{ src: 'common/registry/ModItems.java', lines: '104' }] },
	'block farmersdelight:straw_blocks': { text: 'Straw blocks; part of #farmersdelight:mineable/knife.' },
	'block farmersdelight:terrain': { text: 'Ground that wild tomato patches generate on.', uses: [{ src: 'src/main/resources/data/farmersdelight/worldgen/feature/wild_tomatoes.json' }] },
	'item farmersdelight:serving_containers': { text: 'Items accepted in the Cooking Pot container slot.', uses: [{ src: 'common/block/entity/container/CookingPotMenu.java', lines: '127' }] },
	'item farmersdelight:flat_on_cutting_board': { text: 'Tools that lie flat on the Cutting Board instead of standing in it.', uses: [{ src: 'client/renderer/CuttingBoardRenderer.java', lines: '154' }] },
	'item farmersdelight:straw_harvesters': { text: 'Tools that get Straw from grass (loot modifier straw_from_grass).', uses: [{ src: 'src/generated/resources/data/farmersdelight/loot_modifiers/straw_from_grass.json' }] },
	'item farmersdelight:tools/knives': { text: 'All knives. Knife-only loot modifiers and the knife info pages use it.' },
	'item farmersdelight:enchantable/knife': { text: 'Items that can take knife enchantments such as Backstabbing.' },
	'item farmersdelight:flint_tool_materials': { text: 'Declared and filled, but no code or data reads it.', unused: true },
	'entity_type farmersdelight:dog_food_users': { text: 'Animals that Dog Food can be fed to.', uses: [{ src: 'common/item/DogFoodItem.java', lines: '59' }] },
	'entity_type farmersdelight:horse_feed_users': { text: 'Mounts that Horse Feed can be fed to.', uses: [{ src: 'common/item/HorseFeedItem.java', lines: '59' }] },
	'entity_type farmersdelight:horse_feed_tempted': { text: 'Generated, but no code reads it. Tempting with Horse Feed goes through #minecraft:horse_tempt_items instead.', unused: true },
};

/** Biome tags shipped by hand in src/main/resources that no biome modifier or code reads (see the worldgen page). */
export const unusedBiomeTags = { text: 'No biome modifier or code reads this tag. Worldgen uses c: and minecraft: biome tags directly.', unused: true };

/** Mob-effect tags declared in Java without a JSON file, so they are empty unless a data pack fills them. */
export const emptyEffectTags = [
	{ tag: 'farmersdelight:milk_bottle_ignored', text: 'Effects a Milk Bottle never removes.' },
	{ tag: 'farmersdelight:hot_cocoa_ignored', text: 'Effects Hot Cocoa never removes.' },
];
export const emptyEffectTagsSrc = { src: 'common/tag/ModTags.java', lines: '144-146' };
