package vectorwing.farmersdelight.data;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;
import vectorwing.farmersdelight.common.tag.CompatibilityTags;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.concurrent.CompletableFuture;

public class ItemTags extends ItemTagsProvider
{
	public ItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider) {
		super(output, provider, FarmersDelight.MODID);
	}

	@Override
	protected void addTags(HolderLookup.@NotNull Provider provider) {
		// TODO: Manual copy is fine, but try to figure out how to reimplement copy() for block/item mirror tags.

//		copy(ModTags.Blocks.WILD_CROPS, ModTags.Items.WILD_CROPS);
		fdTag(ModTags.Items.WILD_CROPS).add(
			ModItems.WILD_CARROTS.get(),
			ModItems.WILD_POTATOES.get(),
			ModItems.WILD_BEETROOTS.get(),
			ModItems.WILD_CABBAGES.get(),
			ModItems.WILD_TOMATOES.get(),
			ModItems.WILD_ONIONS.get(),
			ModItems.WILD_RICE.get());
//		copy(BlockTags.SMALL_FLOWERS, net.minecraft.tags.BlockItemTags.SMALL_FLOWERS.item());
		fdTag(net.minecraft.tags.BlockItemTags.SMALL_FLOWERS.item()).add(
			ModItems.WILD_CARROTS.get(),
			ModItems.WILD_POTATOES.get(),
			ModItems.WILD_BEETROOTS.get(),
			ModItems.WILD_CABBAGES.get(),
			ModItems.WILD_TOMATOES.get(),
			ModItems.WILD_ONIONS.get()
		);

		this.registerMinecraftTags();
		this.registerModTags();
		this.registerNeoForgeTags();
		this.registerCommonTags();
		this.registerCompatibilityTags();
	}

	private void registerMinecraftTags() {
		fdTag(Tags.Items.HIDDEN_FROM_RECIPE_VIEWERS).add(ModItems.DEBUG_PUMPKIN_PIE.get());
		fdTag(net.minecraft.tags.ItemTags.BREAKS_DECORATED_POTS).addTag(ModTags.Items.KNIVES);
		fdTag(net.minecraft.tags.BlockItemTags.FLOWERS.item()).add(ModItems.WILD_RICE.get());
		fdTag(net.minecraft.tags.ItemTags.PIGLIN_LOVED).add(ModItems.GOLDEN_KNIFE.get());
		fdTag(net.minecraft.tags.ItemTags.SIGNS).addTag(ModTags.Items.CANVAS_SIGNS);
		fdTag(net.minecraft.tags.ItemTags.HANGING_SIGNS).addTag(ModTags.Items.HANGING_CANVAS_SIGNS);
		fdTag(net.minecraft.tags.ItemTags.VILLAGER_PLANTABLE_SEEDS)
			.add(ModItems.CABBAGE_SEEDS.get())
			.add(ModItems.TOMATO_SEEDS.get())
			.add(ModItems.ONION.get());
		fdTag(net.minecraft.tags.ItemTags.VILLAGER_PICKS_UP)
			.add(ModItems.CABBAGE.get())
			.add(ModItems.TOMATO.get())
			.add(ModItems.ONION.get())
			.add(ModItems.RICE.get())
			.add(ModItems.CABBAGE_SEEDS.get())
			.add(ModItems.RICE_PANICLE.get());

		fdTag(net.minecraft.tags.ItemTags.DURABILITY_ENCHANTABLE).addTag(ModTags.Items.KNIVES).add(ModItems.SKILLET.get());
		fdTag(net.minecraft.tags.ItemTags.WEAPON_ENCHANTABLE).addTag(ModTags.Items.KNIVES).add(ModItems.SKILLET.get());
		fdTag(net.minecraft.tags.ItemTags.SHARP_WEAPON_ENCHANTABLE).addTag(ModTags.Items.KNIVES).add(ModItems.SKILLET.get());
		fdTag(net.minecraft.tags.ItemTags.FIRE_ASPECT_ENCHANTABLE).addTag(ModTags.Items.KNIVES).add(ModItems.SKILLET.get());
		fdTag(net.minecraft.tags.ItemTags.MELEE_WEAPON_ENCHANTABLE).addTag(ModTags.Items.KNIVES).add(ModItems.SKILLET.get());
		fdTag(net.minecraft.tags.ItemTags.MINING_ENCHANTABLE).addTag(ModTags.Items.KNIVES);
		fdTag(net.minecraft.tags.ItemTags.MINING_LOOT_ENCHANTABLE).addTag(ModTags.Items.KNIVES);

		fdTag(net.minecraft.tags.ItemTags.MEAT)
			.add(ModItems.MINCED_BEEF.get())
			.add(ModItems.BEEF_PATTY.get())
			.add(ModItems.CHICKEN_CUTS.get())
			.add(ModItems.COOKED_CHICKEN_CUTS.get())
			.add(ModItems.BACON.get())
			.add(ModItems.COOKED_BACON.get())
			.add(ModItems.MUTTON_CHOPS.get())
			.add(ModItems.COOKED_MUTTON_CHOPS.get())
			.add(ModItems.HAM.get())
			.add(ModItems.SMOKED_HAM.get())
			.add(ModItems.DOG_FOOD.get());
		fdTag(net.minecraft.tags.ItemTags.CAT_FOOD)
			.add((ModItems.SALMON_SLICE.get()))
			.add((ModItems.COD_SLICE.get()));
		fdTag(net.minecraft.tags.ItemTags.CHICKEN_FOOD)
			.add(ModItems.CABBAGE_SEEDS.get())
			.add(ModItems.TOMATO_SEEDS.get())
			.add(ModItems.RICE.get());
		fdTag(net.minecraft.tags.ItemTags.PIG_FOOD)
			.add(ModItems.CABBAGE.get())
			.add(ModItems.TOMATO.get());
		fdTag(net.minecraft.tags.ItemTags.RABBIT_FOOD)
			.add(ModItems.CABBAGE.get());
		fdTag(net.minecraft.tags.ItemTags.PARROT_FOOD)
			.add(ModItems.CABBAGE_SEEDS.get())
			.add(ModItems.TOMATO_SEEDS.get())
			.add(ModItems.RICE.get());
		fdTag(net.minecraft.tags.ItemTags.HORSE_TEMPT_ITEMS)
			.add(ModItems.HORSE_FEED.get());
	}

	private void registerModTags() {
		fdTag(ModTags.Items.FLINT_TOOL_MATERIALS).add(Items.FLINT);
		fdTag(ModTags.Items.SNACKS).add(
			ModItems.BARBECUE_STICK.get(),
			ModItems.EGG_SANDWICH.get(),
			ModItems.CHICKEN_SANDWICH.get(),
			ModItems.HAMBURGER.get(),
			ModItems.BACON_SANDWICH.get(),
			ModItems.MUTTON_WRAP.get(),
			ModItems.DUMPLINGS.get(),
			ModItems.STUFFED_POTATO.get(),
			ModItems.CABBAGE_ROLLS.get(),
			ModItems.SALMON_ROLL.get(),
			ModItems.COD_ROLL.get(),
			ModItems.KELP_ROLL.get(),
			ModItems.KELP_ROLL_SLICE.get()
		);
		fdTag(ModTags.Items.MEALS).add(
			Items.MUSHROOM_STEW,
			Items.BEETROOT_SOUP,
			Items.RABBIT_STEW,
			ModItems.MIXED_SALAD.get(),
			ModItems.COOKED_RICE.get(),
			ModItems.BONE_BROTH.get(),
			ModItems.BEEF_STEW.get(),
			ModItems.VEGETABLE_SOUP.get(),
			ModItems.FISH_STEW.get(),
			ModItems.CHICKEN_SOUP.get(),
			ModItems.FRIED_RICE.get(),
			ModItems.PUMPKIN_SOUP.get(),
			ModItems.BAKED_COD_STEW.get(),
			ModItems.NOODLE_SOUP.get(),
			ModItems.ONION_SOUP.get(),
			ModItems.BACON_AND_EGGS.get(),
			ModItems.RATATOUILLE.get(),
			ModItems.STEAK_AND_POTATOES.get(),
			ModItems.PASTA_WITH_MEATBALLS.get(),
			ModItems.PASTA_WITH_MUTTON_CHOP.get(),
			ModItems.MUSHROOM_RICE.get(),
			ModItems.ROASTED_MUTTON_CHOPS.get(),
			ModItems.VEGETABLE_NOODLES.get(),
			ModItems.SQUID_INK_PASTA.get(),
			ModItems.GRILLED_SALMON.get(),
			ModItems.ROAST_CHICKEN.get(),
			ModItems.STUFFED_PUMPKIN.get(),
			ModItems.HONEY_GLAZED_HAM.get(),
			ModItems.SHEPHERDS_PIE.get(),
			ModItems.GLEAMING_SALAD.get()
		);
		fdTag(ModTags.Items.DRINKS).add(
			ModItems.MILK_BOTTLE.get(),
			ModItems.APPLE_CIDER.get(),
			ModItems.MELON_JUICE.get(),
			ModItems.HOT_COCOA.get()
		);
		fdTag(ModTags.Items.SWEETS).add(
			Items.CAKE,
			Items.COOKIE,
			ModItems.CAKE_SLICE.get(),
			ModItems.APPLE_PIE_SLICE.get(),
			ModItems.SWEET_BERRY_CHEESECAKE_SLICE.get(),
			ModItems.CHOCOLATE_PIE_SLICE.get(),
			ModItems.PUMPKIN_PIE_SLICE.get(),
			ModItems.SWEET_BERRY_COOKIE.get(),
			ModItems.HONEY_COOKIE.get(),
			ModItems.MELON_POPSICLE.get(),
			ModItems.GLOW_BERRY_CUSTARD.get(),
			ModItems.FRUIT_SALAD.get()
		);
//		copy(ModTags.Blocks.FEASTS, ModTags.Items.FEASTS);
		fdTag(ModTags.Items.FEASTS).add(
			ModItems.ROAST_CHICKEN_BLOCK.get(),
			ModItems.STUFFED_PUMPKIN_BLOCK.get(),
			ModItems.SHEPHERDS_PIE_BLOCK.get(),
			ModItems.HONEY_GLAZED_HAM_BLOCK.get(),
			ModItems.GLEAMING_SALAD_BLOCK.get(),
			ModItems.RICE_ROLL_MEDLEY_BLOCK.get()
		);
		fdTag(ModTags.Items.PIES).add(
			Items.PUMPKIN_PIE,
			ModItems.APPLE_PIE.get(),
			ModItems.SWEET_BERRY_CHEESECAKE.get(),
			ModItems.CHOCOLATE_PIE.get()
		);
		fdTag(ModTags.Items.KNIVES).add(ModItems.FLINT_KNIFE.get(), ModItems.COPPER_KNIFE.get(), ModItems.IRON_KNIFE.get(), ModItems.DIAMOND_KNIFE.get(), ModItems.GOLDEN_KNIFE.get(), ModItems.NETHERITE_KNIFE.get());
		fdTag(ModTags.Items.KNIFE_ENCHANTABLE).addTag(ModTags.Items.KNIVES);
		fdTag(ModTags.Items.STRAW_HARVESTERS).addTag(ModTags.Items.KNIVES);
		fdTag(ModTags.Items.CANVAS_SIGNS)
			.add(ModItems.CANVAS_SIGN.get())
			.add(ModItems.WHITE_CANVAS_SIGN.get())
			.add(ModItems.ORANGE_CANVAS_SIGN.get())
			.add(ModItems.MAGENTA_CANVAS_SIGN.get())
			.add(ModItems.LIGHT_BLUE_CANVAS_SIGN.get())
			.add(ModItems.YELLOW_CANVAS_SIGN.get())
			.add(ModItems.LIME_CANVAS_SIGN.get())
			.add(ModItems.PINK_CANVAS_SIGN.get())
			.add(ModItems.GRAY_CANVAS_SIGN.get())
			.add(ModItems.LIGHT_GRAY_CANVAS_SIGN.get())
			.add(ModItems.CYAN_CANVAS_SIGN.get())
			.add(ModItems.PURPLE_CANVAS_SIGN.get())
			.add(ModItems.BLUE_CANVAS_SIGN.get())
			.add(ModItems.BROWN_CANVAS_SIGN.get())
			.add(ModItems.GREEN_CANVAS_SIGN.get())
			.add(ModItems.RED_CANVAS_SIGN.get())
			.add(ModItems.BLACK_CANVAS_SIGN.get());
		fdTag(ModTags.Items.HANGING_CANVAS_SIGNS)
			.add(ModItems.HANGING_CANVAS_SIGN.get())
			.add(ModItems.WHITE_HANGING_CANVAS_SIGN.get())
			.add(ModItems.ORANGE_HANGING_CANVAS_SIGN.get())
			.add(ModItems.MAGENTA_HANGING_CANVAS_SIGN.get())
			.add(ModItems.LIGHT_BLUE_HANGING_CANVAS_SIGN.get())
			.add(ModItems.YELLOW_HANGING_CANVAS_SIGN.get())
			.add(ModItems.LIME_HANGING_CANVAS_SIGN.get())
			.add(ModItems.PINK_HANGING_CANVAS_SIGN.get())
			.add(ModItems.GRAY_HANGING_CANVAS_SIGN.get())
			.add(ModItems.LIGHT_GRAY_HANGING_CANVAS_SIGN.get())
			.add(ModItems.CYAN_HANGING_CANVAS_SIGN.get())
			.add(ModItems.PURPLE_HANGING_CANVAS_SIGN.get())
			.add(ModItems.BLUE_HANGING_CANVAS_SIGN.get())
			.add(ModItems.BROWN_HANGING_CANVAS_SIGN.get())
			.add(ModItems.GREEN_HANGING_CANVAS_SIGN.get())
			.add(ModItems.RED_HANGING_CANVAS_SIGN.get())
			.add(ModItems.BLACK_HANGING_CANVAS_SIGN.get());
//		copy(ModTags.Blocks.CABINETS, ModTags.Items.CABINETS);
//		copy(ModTags.Blocks.CABINETS_WOODEN, ModTags.Items.CABINETS_WOODEN);
		fdTag(ModTags.Items.CABINETS_WOODEN)
			.add(ModItems.OAK_CABINET.get())
			.add(ModItems.SPRUCE_CABINET.get())
			.add(ModItems.BIRCH_CABINET.get())
			.add(ModItems.JUNGLE_CABINET.get())
			.add(ModItems.ACACIA_CABINET.get())
			.add(ModItems.DARK_OAK_CABINET.get())
			.add(ModItems.MANGROVE_CABINET.get())
			.add(ModItems.CHERRY_CABINET.get())
			.add(ModItems.PALE_OAK_CABINET.get())
			.add(ModItems.BAMBOO_CABINET.get())
			.add(ModItems.CRIMSON_CABINET.get())
			.add(ModItems.WARPED_CABINET.get());
		fdTag(ModTags.Items.CABINETS).addTag(ModTags.Items.CABINETS_WOODEN);

//		copy(ModTags.Blocks.MUSHROOM_COLONIES, ModTags.Items.MUSHROOM_COLONIES);
		fdTag(ModTags.Items.MUSHROOM_COLONIES)
			.add(ModItems.BROWN_MUSHROOM_COLONY.get())
			.add(ModItems.RED_MUSHROOM_COLONY.get());

		fdTag(ModTags.Items.SERVING_CONTAINERS).add(Items.BOWL, Items.GLASS_BOTTLE, Items.BUCKET);
		fdTag(ModTags.Items.FLAT_ON_CUTTING_BOARD).add(Items.TRIDENT, Items.SPYGLASS)
			.add(TagEntry.optionalElement(Identifier.parse("supplementaries:quiver")))
			.add(TagEntry.optionalElement(Identifier.parse("autumnity:turkey")))
			.add(TagEntry.optionalElement(Identifier.parse("autumnity:cooked_turkey")));
	}

	@SuppressWarnings("unchecked")
	private void registerNeoForgeTags() {
		// Add our custom tags to "common" tag groups
		fdTag(Tags.Items.CROPS)
			.addTag(CommonTags.Items.CROPS_GRAIN);
		fdTag(Tags.Items.DRINKS)
			.addTag(ModTags.Items.DRINKS);
		fdTag(Tags.Items.FOODS)
			.add(ModItems.TOMATO_SAUCE.get())
			.add(ModItems.PIE_CRUST.get())
			.add(ModItems.PUMPKIN_SLICE.get())
			.add(ModItems.HAM.get())
			.add(ModItems.SMOKED_HAM.get())
			.add(ModItems.DOG_FOOD.get())
			.addTag(ModTags.Items.SNACKS)
			.addTag(ModTags.Items.MEALS)
			.addTag(ModTags.Items.SWEETS)
			.addTag(CommonTags.Items.FOODS_LEAFY_GREEN)
			.addTag(CommonTags.Items.FOODS_DOUGH)
			.addTag(CommonTags.Items.FOODS_PASTA)
			.addTag(CommonTags.Items.FOODS_COOKED_EGG);

		fdTag(Tags.Items.FENCES).add(ModItems.ROPE_FENCE.get());
		fdTag(Tags.Items.FENCE_GATES).add(ModItems.ROPE_FENCE_GATE.get());

		fdTag(Tags.Items.DRINKS_MILK).add(ModItems.MILK_BOTTLE.get());

		fdTag(Tags.Items.FOODS_VEGETABLE).add(ModItems.ONION.get(), ModItems.TOMATO.get());
		fdTag(Tags.Items.FOODS_COOKIE).add(ModItems.HONEY_COOKIE.get(), ModItems.SWEET_BERRY_COOKIE.get());
		fdTag(Tags.Items.FOODS_DOUGH).addTag(CommonTags.Items.FOODS_DOUGH_WHEAT);
		fdTag(Tags.Items.FOODS_RAW_MEAT).addTags(CommonTags.Items.FOODS_RAW_CHICKEN, CommonTags.Items.FOODS_RAW_PORK, CommonTags.Items.FOODS_RAW_BEEF, CommonTags.Items.FOODS_RAW_MUTTON);
		fdTag(Tags.Items.FOODS_RAW_FISH).addTags(CommonTags.Items.FOODS_RAW_COD, CommonTags.Items.FOODS_RAW_SALMON);
		fdTag(Tags.Items.FOODS_COOKED_MEAT).addTags(CommonTags.Items.FOODS_COOKED_CHICKEN, CommonTags.Items.FOODS_COOKED_PORK, CommonTags.Items.FOODS_COOKED_BEEF, CommonTags.Items.FOODS_COOKED_MUTTON);
		fdTag(Tags.Items.FOODS_COOKED_FISH).addTags(CommonTags.Items.FOODS_COOKED_COD, CommonTags.Items.FOODS_COOKED_SALMON);
		fdTag(Tags.Items.FOODS_FOOD_POISONING).add(
			ModItems.WHEAT_DOUGH.get(),
			ModItems.RAW_PASTA.get(),
			ModItems.CHICKEN_CUTS.get(),
			ModItems.NETHER_SALAD.get()
		);
		fdTag(Tags.Items.FOODS_EDIBLE_WHEN_PLACED)
			.add(ModItems.APPLE_PIE.get())
			.add(ModItems.SWEET_BERRY_CHEESECAKE.get())
			.add(ModItems.CHOCOLATE_PIE.get())
			.addTag(ModTags.Items.FEASTS);
		fdTag(Tags.Items.FOODS_SOUP)
			.add(ModItems.BONE_BROTH.get())
			.add(ModItems.BEEF_STEW.get())
			.add(ModItems.VEGETABLE_SOUP.get())
			.add(ModItems.CHICKEN_SOUP.get())
			.add(ModItems.FISH_STEW.get())
			.add(ModItems.PUMPKIN_SOUP.get())
			.add(ModItems.BAKED_COD_STEW.get())
			.add(ModItems.NOODLE_SOUP.get());
		fdTag(Tags.Items.FOODS_PIE)
			.add(ModItems.APPLE_PIE_SLICE.get())
			.add(ModItems.SWEET_BERRY_CHEESECAKE_SLICE.get())
			.add(ModItems.CHOCOLATE_PIE_SLICE.get())
			.add(ModItems.PUMPKIN_PIE_SLICE.get());

		fdTag(Tags.Items.TOOLS).addTag(CommonTags.Items.TOOLS_KNIFE);
		fdTag(Tags.Items.ROPES).add(ModItems.ROPE.get());
		fdTag(Tags.Items.SEEDS).add(ModItems.CABBAGE_SEEDS.get(), ModItems.RICE.get(), ModItems.TOMATO_SEEDS.get());
		fdTag(Tags.Items.CROPS).addTags(CommonTags.Items.CROPS_CABBAGE, CommonTags.Items.CROPS_ONION, CommonTags.Items.CROPS_RICE, CommonTags.Items.CROPS_TOMATO);
		fdTag(Tags.Items.STORAGE_BLOCKS).addTags(
			CommonTags.Items.STORAGE_BLOCKS_CARROT,
			CommonTags.Items.STORAGE_BLOCKS_POTATO,
			CommonTags.Items.STORAGE_BLOCKS_BEETROOT,
			CommonTags.Items.STORAGE_BLOCKS_CABBAGE,
			CommonTags.Items.STORAGE_BLOCKS_TOMATO,
			CommonTags.Items.STORAGE_BLOCKS_ONION,
			CommonTags.Items.STORAGE_BLOCKS_RICE,
			CommonTags.Items.STORAGE_BLOCKS_RICE_PANICLE,
			CommonTags.Items.STORAGE_BLOCKS_STRAW
		);
	}

	public void registerCommonTags() {
		fdTag(CommonTags.Items.CROPS_CABBAGE).add(ModItems.CABBAGE.get(), ModItems.CABBAGE_LEAF.get());
		fdTag(CommonTags.Items.CROPS_ONION).add(ModItems.ONION.get());
		fdTag(CommonTags.Items.CROPS_TOMATO).add(ModItems.TOMATO.get());
		fdTag(CommonTags.Items.CROPS_RICE).add(ModItems.RICE.get());

		fdTag(CommonTags.Items.FOODS_CABBAGE).add(ModItems.CABBAGE.get(), ModItems.CABBAGE_LEAF.get());
		fdTag(CommonTags.Items.FOODS_TOMATO).add(ModItems.TOMATO.get());
		fdTag(CommonTags.Items.FOODS_ONION).add(ModItems.ONION.get());

		fdTag(CommonTags.Items.FOODS_DOUGH_WHEAT).add(ModItems.WHEAT_DOUGH.get());
		fdTag(CommonTags.Items.CROPS_GRAIN).add(Items.WHEAT, ModItems.RICE.get());
		fdTag(CommonTags.Items.FOODS_PASTA).add(ModItems.RAW_PASTA.get());
		fdTag(CommonTags.Items.FOODS_LEAFY_GREEN).addTag(CommonTags.Items.FOODS_CABBAGE);

		fdTag(CommonTags.Items.FOODS_RAW_BACON).add(ModItems.BACON.get());
		fdTag(CommonTags.Items.FOODS_RAW_BEEF).add(Items.BEEF, ModItems.MINCED_BEEF.get());
		fdTag(CommonTags.Items.FOODS_RAW_CHICKEN).add(Items.CHICKEN, ModItems.CHICKEN_CUTS.get());
		fdTag(CommonTags.Items.FOODS_RAW_PORK).add(Items.PORKCHOP).addTag(CommonTags.Items.FOODS_RAW_BACON);
		fdTag(CommonTags.Items.FOODS_RAW_MUTTON).add(Items.MUTTON, ModItems.MUTTON_CHOPS.get());
		fdTag(CommonTags.Items.FOODS_RAW_COD).add(Items.COD, ModItems.COD_SLICE.get());
		fdTag(CommonTags.Items.FOODS_RAW_SALMON).add(Items.SALMON, ModItems.SALMON_SLICE.get());
		fdTag(CommonTags.Items.FOODS_SAFE_RAW_FISH).addTag(Tags.Items.FOODS_RAW_FISH).remove(Items.PUFFERFISH);

		fdTag(CommonTags.Items.FOODS_COOKED_BACON).add(ModItems.COOKED_BACON.get());
		fdTag(CommonTags.Items.FOODS_COOKED_BEEF).add(Items.COOKED_BEEF, ModItems.BEEF_PATTY.get());
		fdTag(CommonTags.Items.FOODS_COOKED_CHICKEN).add(Items.COOKED_CHICKEN, ModItems.COOKED_CHICKEN_CUTS.get());
		fdTag(CommonTags.Items.FOODS_COOKED_PORK).add(Items.COOKED_PORKCHOP).addTag(CommonTags.Items.FOODS_COOKED_BACON);
		fdTag(CommonTags.Items.FOODS_COOKED_MUTTON).add(Items.COOKED_MUTTON, ModItems.COOKED_MUTTON_CHOPS.get());
		fdTag(CommonTags.Items.FOODS_COOKED_COD).add(Items.COOKED_COD, ModItems.COOKED_COD_SLICE.get());
		fdTag(CommonTags.Items.FOODS_COOKED_SALMON).add(Items.COOKED_SALMON, ModItems.COOKED_SALMON_SLICE.get());
		fdTag(CommonTags.Items.FOODS_COOKED_EGG).add(ModItems.FRIED_EGG.get());

		fdTag(CommonTags.Items.STORAGE_BLOCKS_CARROT).add(ModItems.CARROT_CRATE.get());
		fdTag(CommonTags.Items.STORAGE_BLOCKS_POTATO).add(ModItems.POTATO_CRATE.get());
		fdTag(CommonTags.Items.STORAGE_BLOCKS_BEETROOT).add(ModItems.BEETROOT_CRATE.get());
		fdTag(CommonTags.Items.STORAGE_BLOCKS_CABBAGE).add(ModItems.CABBAGE_CRATE.get());
		fdTag(CommonTags.Items.STORAGE_BLOCKS_TOMATO).add(ModItems.TOMATO_CRATE.get());
		fdTag(CommonTags.Items.STORAGE_BLOCKS_ONION).add(ModItems.ONION_CRATE.get());
		fdTag(CommonTags.Items.STORAGE_BLOCKS_RICE).add(ModItems.RICE_BAG.get());
		fdTag(CommonTags.Items.STORAGE_BLOCKS_RICE_PANICLE).add(ModItems.RICE_BALE.get());
		fdTag(CommonTags.Items.STORAGE_BLOCKS_STRAW).add(ModItems.STRAW_BALE.get());

		fdTag(CommonTags.Items.TOOLS_KNIFE).add(ModItems.FLINT_KNIFE.get(), ModItems.COPPER_KNIFE.get(), ModItems.IRON_KNIFE.get(), ModItems.DIAMOND_KNIFE.get(), ModItems.GOLDEN_KNIFE.get(), ModItems.NETHERITE_KNIFE.get());
	}

	public void registerCompatibilityTags() {
		fdTag(CompatibilityTags.CREATE_UPRIGHT_ON_BELT)
			.addTag(ModTags.Items.MEALS)
			.addTag(ModTags.Items.DRINKS)
			.addTag(ModTags.Items.FEASTS)
			.add(ModItems.TOMATO_SAUCE.get())
			.add(ModItems.DOG_FOOD.get())
			.add(ModItems.FRUIT_SALAD.get())
			.add(ModItems.NETHER_SALAD.get())
			.add(ModItems.PIE_CRUST.get())
			.add(ModItems.APPLE_PIE.get())
			.add(ModItems.SWEET_BERRY_CHEESECAKE.get())
			.add(ModItems.CHOCOLATE_PIE.get());

		fdTag(CompatibilityTags.CREATE_CA_PLANT_FOODS)
			.add(ModItems.PUMPKIN_SLICE.get())
			.add(ModItems.ROTTEN_TOMATO.get())
			.add(ModItems.RICE_PANICLE.get());
		fdTag(CompatibilityTags.CREATE_CA_PLANTS)
			.add(ModItems.SANDY_SHRUB.get())
			.add(ModItems.BROWN_MUSHROOM_COLONY.get())
			.add(ModItems.RED_MUSHROOM_COLONY.get());

		fdTag(CompatibilityTags.ORIGINS_MEAT)
			.add(ModItems.FRIED_EGG.get())
			.add(ModItems.COD_SLICE.get())
			.add(ModItems.COOKED_COD_SLICE.get())
			.add(ModItems.SALMON_SLICE.get())
			.add(ModItems.COOKED_SALMON_SLICE.get())
			.add(ModItems.BACON_AND_EGGS.get());

		fdTag(CompatibilityTags.SERENE_SEASONS_AUTUMN_CROPS)
			.add(ModItems.CABBAGE_SEEDS.get())
			.add(ModItems.ONION.get())
			.add(ModItems.RICE.get());
		fdTag(CompatibilityTags.SERENE_SEASONS_SPRING_CROPS)
			.add(ModItems.ONION.get());
		fdTag(CompatibilityTags.SERENE_SEASONS_SUMMER_CROPS)
			.add(ModItems.TOMATO_SEEDS.get())
			.add(ModItems.RICE.get());
		fdTag(CompatibilityTags.SERENE_SEASONS_WINTER_CROPS)
			.add(ModItems.CABBAGE_SEEDS.get());

		fdTag(CompatibilityTags.TINKERS_CONSTRUCT_SEEDS).add(ModItems.ONION.get());
	}

	private vectorwing.farmersdelight.data.tools.FDTagAppender<Item> fdTag(net.minecraft.tags.TagKey<Item> tag) {
		return new vectorwing.farmersdelight.data.tools.FDTagAppender<>(this.tag(tag), BuiltInRegistries.ITEM);
	}
}
