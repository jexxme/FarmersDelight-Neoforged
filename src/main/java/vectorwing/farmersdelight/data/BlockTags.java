package vectorwing.farmersdelight.data;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.tag.CommonTags;
import vectorwing.farmersdelight.common.tag.CompatibilityTags;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.concurrent.CompletableFuture;

public class BlockTags extends BlockTagsProvider
{
	public BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, FarmersDelight.MODID);
	}

	@Override
	protected void addTags(HolderLookup.@NotNull Provider provider) {
		this.registerModTags();
		this.registerMinecraftTags();
		this.registerNeoForgeTags();
		this.registerCommonTags();
		this.registerCompatibilityTags();

		this.registerBlockMineables();
	}

	protected void registerBlockMineables() {
		fdTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE).add(
			ModBlocks.WOODEN_BASKET.get(),
			ModBlocks.BAMBOO_BASKET.get(),
			ModBlocks.ROPE_FENCE.get(),
			ModBlocks.ROPE_FENCE_GATE.get(),
			ModBlocks.CUTTING_BOARD.get(),
			ModBlocks.CARROT_CRATE.get(),
			ModBlocks.POTATO_CRATE.get(),
			ModBlocks.BEETROOT_CRATE.get(),
			ModBlocks.CABBAGE_CRATE.get(),
			ModBlocks.TOMATO_CRATE.get(),
			ModBlocks.ONION_CRATE.get(),
			ModBlocks.OAK_CABINET.get(),
			ModBlocks.BIRCH_CABINET.get(),
			ModBlocks.SPRUCE_CABINET.get(),
			ModBlocks.JUNGLE_CABINET.get(),
			ModBlocks.ACACIA_CABINET.get(),
			ModBlocks.DARK_OAK_CABINET.get(),
			ModBlocks.MANGROVE_CABINET.get(),
			ModBlocks.CHERRY_CABINET.get(),
			ModBlocks.PALE_OAK_CABINET.get(),
			ModBlocks.BAMBOO_CABINET.get(),
			ModBlocks.CRIMSON_CABINET.get(),
			ModBlocks.WARPED_CABINET.get(),
			ModBlocks.SANDY_SHRUB.get(),
			ModBlocks.ROAST_CHICKEN_BLOCK.get(),
			ModBlocks.STUFFED_PUMPKIN_BLOCK.get(),
			ModBlocks.SHEPHERDS_PIE_BLOCK.get(),
			ModBlocks.HONEY_GLAZED_HAM_BLOCK.get(),
			ModBlocks.GLEAMING_SALAD_BLOCK.get(),
			ModBlocks.RICE_ROLL_MEDLEY_BLOCK.get()
		);
		fdTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_HOE).add(
			ModBlocks.RICE_BALE.get(),
			ModBlocks.STRAW_BALE.get()
		);
		fdTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE).add(
			ModBlocks.STOVE.get(),
			ModBlocks.COOKING_POT.get(),
			ModBlocks.SKILLET.get()
		);
		fdTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL).add(
			ModBlocks.ORGANIC_COMPOST.get(),
			ModBlocks.RICH_SOIL.get(),
			ModBlocks.RICH_SOIL_FARMLAND.get()
		);
		fdTag(ModTags.Blocks.MINEABLE_WITH_KNIFE).addTags(net.minecraft.tags.BlockTags.WOOL_SLABS, net.minecraft.tags.BlockTags.WOOL_STAIRS).add(
				Blocks.CACTUS,
				Blocks.MELON,
				Blocks.PUMPKIN,
				Blocks.CARVED_PUMPKIN,
				Blocks.JACK_O_LANTERN,
				Blocks.COBWEB,
				Blocks.CAKE,
				ModBlocks.RICE_BAG.get(),
				ModBlocks.APPLE_PIE.get(),
				ModBlocks.SWEET_BERRY_CHEESECAKE.get(),
				ModBlocks.CHOCOLATE_PIE.get(),
				ModBlocks.PUMPKIN_PIE.get())
			.addTag(net.minecraft.tags.BlockTags.WOOL_CARPETS)
			.addTag(net.minecraft.tags.BlockTags.WOOL)
			.addTag(net.minecraft.tags.BlockTags.CANDLE_CAKES)
			.addTag(ModTags.Blocks.STRAW_BLOCKS)
			.addTag(CommonTags.Blocks.MINEABLE_WITH_KNIFE);
		fdTag(CommonTags.Blocks.MINEABLE_WITH_KNIFE);

	}

	protected void registerMinecraftTags() {
		// 26.3: plant support/growth is tag-driven
		fdTag(net.minecraft.tags.BlockTags.SUPPORTS_VEGETATION).add(ModBlocks.RICH_SOIL.get(), ModBlocks.RICH_SOIL_FARMLAND.get());
		fdTag(net.minecraft.tags.BlockTags.SUPPORTS_CROPS).add(ModBlocks.RICH_SOIL_FARMLAND.get());
		fdTag(net.minecraft.tags.BlockTags.GROWS_CROPS).add(ModBlocks.RICH_SOIL_FARMLAND.get());
		fdTag(net.minecraft.tags.BlockTags.SUPPORTS_SUGAR_CANE).add(ModBlocks.RICH_SOIL.get());
		fdTag(net.minecraft.tags.BlockTags.SUPPORTS_AZALEA).add(ModBlocks.RICH_SOIL.get(), ModBlocks.RICH_SOIL_FARMLAND.get());
		fdTag(net.minecraft.tags.BlockTags.SUPPORTS_BIG_DRIPLEAF).add(ModBlocks.RICH_SOIL.get(), ModBlocks.RICH_SOIL_FARMLAND.get());
		fdTag(net.minecraft.tags.BlockTags.SUPPORTS_SMALL_DRIPLEAF).add(ModBlocks.RICH_SOIL.get());
		fdTag(net.minecraft.tags.BlockTags.SUPPORT_OVERRIDE_CACTUS_FLOWER).add(ModBlocks.RICH_SOIL_FARMLAND.get());
		fdTag(net.minecraft.tags.BlockTags.WASHED_AWAY_BY_FLUIDS).add(
			ModBlocks.CABBAGE_CROP.get(), ModBlocks.ONION_CROP.get(), ModBlocks.TOMATO_CROP.get(), ModBlocks.RICE_CROP_PANICLES.get(),
			ModBlocks.WILD_CABBAGES.get(), ModBlocks.WILD_ONIONS.get(), ModBlocks.WILD_TOMATOES.get(), ModBlocks.WILD_CARROTS.get(),
			ModBlocks.WILD_POTATOES.get(), ModBlocks.WILD_BEETROOTS.get(), ModBlocks.SANDY_SHRUB.get(),
			ModBlocks.BROWN_MUSHROOM_COLONY.get(), ModBlocks.RED_MUSHROOM_COLONY.get(), ModBlocks.BUDDING_TOMATO_CROP.get());
		fdTag(net.minecraft.tags.BlockTags.CLIMBABLE).add(
			ModBlocks.ROPE.get(),
			ModBlocks.TOMATO_CROP_ON_ROPE.get());
		fdTag(net.minecraft.tags.BlockTags.FENCES).add(ModBlocks.ROPE_FENCE.get());
		fdTag(net.minecraft.tags.BlockTags.FENCE_GATES).add(ModBlocks.ROPE_FENCE_GATE.get());
		fdTag(net.minecraft.tags.BlockTags.REPLACEABLE).add(
			ModBlocks.SANDY_SHRUB.get());
		fdTag(net.minecraft.tags.BlockTags.REPLACEABLE_BY_TREES).add(
			ModBlocks.SANDY_SHRUB.get());
		fdTag(net.minecraft.tags.BlockTags.SUPPORTS_BAMBOO).add(
			ModBlocks.RICH_SOIL.get());
		fdTag(net.minecraft.tags.BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT).add(
			ModBlocks.ORGANIC_COMPOST.get(),
			ModBlocks.RICH_SOIL.get());
		fdTag(net.minecraft.tags.BlockTags.CROPS).add(
			ModBlocks.CABBAGE_CROP.get(),
			ModBlocks.ONION_CROP.get(),
			ModBlocks.RICE_CROP_PANICLES.get(),
			ModBlocks.BUDDING_TOMATO_CROP.get(),
			ModBlocks.TOMATO_CROP.get(),
			ModBlocks.TOMATO_CROP_ON_ROPE.get(),
			ModBlocks.TOMATO_CROP_ON_SUPPORT.get());
		fdTag(net.minecraft.tags.BlockTags.STANDING_SIGNS).add(
			ModBlocks.CANVAS_SIGN.get(),
			ModBlocks.WHITE_CANVAS_SIGN.get(),
			ModBlocks.ORANGE_CANVAS_SIGN.get(),
			ModBlocks.MAGENTA_CANVAS_SIGN.get(),
			ModBlocks.LIGHT_BLUE_CANVAS_SIGN.get(),
			ModBlocks.YELLOW_CANVAS_SIGN.get(),
			ModBlocks.LIME_CANVAS_SIGN.get(),
			ModBlocks.PINK_CANVAS_SIGN.get(),
			ModBlocks.GRAY_CANVAS_SIGN.get(),
			ModBlocks.LIGHT_GRAY_CANVAS_SIGN.get(),
			ModBlocks.CYAN_CANVAS_SIGN.get(),
			ModBlocks.PURPLE_CANVAS_SIGN.get(),
			ModBlocks.BLUE_CANVAS_SIGN.get(),
			ModBlocks.BROWN_CANVAS_SIGN.get(),
			ModBlocks.GREEN_CANVAS_SIGN.get(),
			ModBlocks.RED_CANVAS_SIGN.get(),
			ModBlocks.BLACK_CANVAS_SIGN.get());
		fdTag(net.minecraft.tags.BlockTags.WALL_SIGNS).add(
			ModBlocks.CANVAS_WALL_SIGN.get(),
			ModBlocks.WHITE_CANVAS_WALL_SIGN.get(),
			ModBlocks.ORANGE_CANVAS_WALL_SIGN.get(),
			ModBlocks.MAGENTA_CANVAS_WALL_SIGN.get(),
			ModBlocks.LIGHT_BLUE_CANVAS_WALL_SIGN.get(),
			ModBlocks.YELLOW_CANVAS_WALL_SIGN.get(),
			ModBlocks.LIME_CANVAS_WALL_SIGN.get(),
			ModBlocks.PINK_CANVAS_WALL_SIGN.get(),
			ModBlocks.GRAY_CANVAS_WALL_SIGN.get(),
			ModBlocks.LIGHT_GRAY_CANVAS_WALL_SIGN.get(),
			ModBlocks.CYAN_CANVAS_WALL_SIGN.get(),
			ModBlocks.PURPLE_CANVAS_WALL_SIGN.get(),
			ModBlocks.BLUE_CANVAS_WALL_SIGN.get(),
			ModBlocks.BROWN_CANVAS_WALL_SIGN.get(),
			ModBlocks.GREEN_CANVAS_WALL_SIGN.get(),
			ModBlocks.RED_CANVAS_WALL_SIGN.get(),
			ModBlocks.BLACK_CANVAS_WALL_SIGN.get());
		fdTag(net.minecraft.tags.BlockTags.CEILING_HANGING_SIGNS).add(
			ModBlocks.HANGING_CANVAS_SIGN.get(),
			ModBlocks.WHITE_HANGING_CANVAS_SIGN.get(),
			ModBlocks.ORANGE_HANGING_CANVAS_SIGN.get(),
			ModBlocks.MAGENTA_HANGING_CANVAS_SIGN.get(),
			ModBlocks.LIGHT_BLUE_HANGING_CANVAS_SIGN.get(),
			ModBlocks.YELLOW_HANGING_CANVAS_SIGN.get(),
			ModBlocks.LIME_HANGING_CANVAS_SIGN.get(),
			ModBlocks.PINK_HANGING_CANVAS_SIGN.get(),
			ModBlocks.GRAY_HANGING_CANVAS_SIGN.get(),
			ModBlocks.LIGHT_GRAY_HANGING_CANVAS_SIGN.get(),
			ModBlocks.CYAN_HANGING_CANVAS_SIGN.get(),
			ModBlocks.PURPLE_HANGING_CANVAS_SIGN.get(),
			ModBlocks.BLUE_HANGING_CANVAS_SIGN.get(),
			ModBlocks.BROWN_HANGING_CANVAS_SIGN.get(),
			ModBlocks.GREEN_HANGING_CANVAS_SIGN.get(),
			ModBlocks.RED_HANGING_CANVAS_SIGN.get(),
			ModBlocks.BLACK_HANGING_CANVAS_SIGN.get());
		fdTag(net.minecraft.tags.BlockTags.WALL_HANGING_SIGNS).add(
			ModBlocks.HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.WHITE_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.ORANGE_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.MAGENTA_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.LIGHT_BLUE_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.YELLOW_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.LIME_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.PINK_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.GRAY_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.LIGHT_GRAY_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.CYAN_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.PURPLE_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.BLUE_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.BROWN_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.GREEN_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.RED_HANGING_CANVAS_WALL_SIGN.get(),
			ModBlocks.BLACK_HANGING_CANVAS_WALL_SIGN.get());
		fdTag(net.minecraft.tags.BlockTags.SMALL_FLOWERS).add(
			ModBlocks.WILD_CARROTS.get(),
			ModBlocks.WILD_POTATOES.get(),
			ModBlocks.WILD_BEETROOTS.get(),
			ModBlocks.WILD_CABBAGES.get(),
			ModBlocks.WILD_TOMATOES.get(),
			ModBlocks.WILD_ONIONS.get()
		);
		fdTag(net.minecraft.tags.BlockTags.FLOWERS).add(ModBlocks.WILD_RICE.get());
		fdTag(net.minecraft.tags.BlockTags.DIRT).add(
			ModBlocks.RICH_SOIL.get());
		fdTag(net.minecraft.tags.BlockTags.MAINTAINS_FARMLAND).add(
			ModBlocks.CABBAGE_CROP.get(),
			ModBlocks.BUDDING_TOMATO_CROP.get(),
			ModBlocks.TOMATO_CROP.get(),
			ModBlocks.ONION_CROP.get(),
			ModBlocks.RICE_CROP.get()
		);
		fdTag(net.minecraft.tags.BlockTags.COMBINATION_STEP_SOUND_BLOCKS).add(
			ModBlocks.CANVAS_RUG.get(),
			ModBlocks.FULL_TATAMI_MAT.get(),
			ModBlocks.HALF_TATAMI_MAT.get(),
			ModBlocks.CUTTING_BOARD.get()
		);
	}

	protected void registerNeoForgeTags() {
		fdTag(Tags.Blocks.ROPES).add(ModBlocks.ROPE.get());
		fdTag(Tags.Blocks.VILLAGER_FARMLANDS).add(ModBlocks.RICH_SOIL_FARMLAND.get());
		fdTag(Tags.Blocks.FENCES).add(ModBlocks.ROPE_FENCE.get());
		fdTag(Tags.Blocks.FENCE_GATES).add(ModBlocks.ROPE_FENCE_GATE.get());
		fdTag(Tags.Blocks.STORAGE_BLOCKS).addTags(
			CommonTags.Blocks.STORAGE_BLOCKS_CARROT,
			CommonTags.Blocks.STORAGE_BLOCKS_POTATO,
			CommonTags.Blocks.STORAGE_BLOCKS_BEETROOT,
			CommonTags.Blocks.STORAGE_BLOCKS_CABBAGE,
			CommonTags.Blocks.STORAGE_BLOCKS_TOMATO,
			CommonTags.Blocks.STORAGE_BLOCKS_ONION,
			CommonTags.Blocks.STORAGE_BLOCKS_RICE,
			CommonTags.Blocks.STORAGE_BLOCKS_RICE_PANICLE,
			CommonTags.Blocks.STORAGE_BLOCKS_STRAW
		);
	}

	protected void registerCommonTags() {
		fdTag(CommonTags.Blocks.MINEABLE_WITH_KNIFE);
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_CARROT).add(ModBlocks.CARROT_CRATE.get());
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_POTATO).add(ModBlocks.POTATO_CRATE.get());
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_BEETROOT).add(ModBlocks.BEETROOT_CRATE.get());
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_CABBAGE).add(ModBlocks.CABBAGE_CRATE.get());
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_TOMATO).add(ModBlocks.TOMATO_CRATE.get());
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_ONION).add(ModBlocks.ONION_CRATE.get());
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_RICE).add(ModBlocks.RICE_BAG.get());
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_RICE_PANICLE).add(ModBlocks.RICE_BALE.get());
		fdTag(CommonTags.Blocks.STORAGE_BLOCKS_STRAW).add(ModBlocks.STRAW_BALE.get());
	}

	protected void registerModTags() {
		fdTag(ModTags.Blocks.FEASTS).add(
			ModBlocks.ROAST_CHICKEN_BLOCK.get(),
			ModBlocks.STUFFED_PUMPKIN_BLOCK.get(),
			ModBlocks.SHEPHERDS_PIE_BLOCK.get(),
			ModBlocks.HONEY_GLAZED_HAM_BLOCK.get(),
			ModBlocks.GLEAMING_SALAD_BLOCK.get(),
			ModBlocks.RICE_ROLL_MEDLEY_BLOCK.get()
		);
		fdTag(ModTags.Blocks.PIES).add(
			ModBlocks.APPLE_PIE.get(),
			ModBlocks.SWEET_BERRY_CHEESECAKE.get(),
			ModBlocks.CHOCOLATE_PIE.get(),
			ModBlocks.PUMPKIN_PIE.get()
		);
		fdTag(ModTags.Blocks.TERRAIN)
			.addTag(net.minecraft.tags.BlockTags.SUBSTRATE_OVERWORLD)
			.addTag(net.minecraft.tags.BlockTags.SAND);
		fdTag(ModTags.Blocks.STRAW_BLOCKS).add(
			ModBlocks.ROPE.get(),
			ModBlocks.SAFETY_NET.get(),
			ModBlocks.CANVAS_RUG.get(),
			ModBlocks.TATAMI.get(),
			ModBlocks.FULL_TATAMI_MAT.get(),
			ModBlocks.HALF_TATAMI_MAT.get()
		);
		fdTag(ModTags.Blocks.WILD_CROPS).add(
			ModBlocks.WILD_CARROTS.get(),
			ModBlocks.WILD_POTATOES.get(),
			ModBlocks.WILD_BEETROOTS.get(),
			ModBlocks.WILD_CABBAGES.get(),
			ModBlocks.WILD_TOMATOES.get(),
			ModBlocks.WILD_ONIONS.get(),
			ModBlocks.WILD_RICE.get());
		fdTag(ModTags.Blocks.CABINETS_WOODEN)
			.add(ModBlocks.OAK_CABINET.get())
			.add(ModBlocks.SPRUCE_CABINET.get())
			.add(ModBlocks.BIRCH_CABINET.get())
			.add(ModBlocks.JUNGLE_CABINET.get())
			.add(ModBlocks.ACACIA_CABINET.get())
			.add(ModBlocks.DARK_OAK_CABINET.get())
			.add(ModBlocks.MANGROVE_CABINET.get())
			.add(ModBlocks.CHERRY_CABINET.get())
			.add(ModBlocks.PALE_OAK_CABINET.get())
			.add(ModBlocks.BAMBOO_CABINET.get())
			.add(ModBlocks.CRIMSON_CABINET.get())
			.add(ModBlocks.WARPED_CABINET.get());
		fdTag(ModTags.Blocks.CABINETS).addTag(ModTags.Blocks.CABINETS_WOODEN);
		fdTag(ModTags.Blocks.MUSHROOM_COLONIES)
			.add(ModBlocks.BROWN_MUSHROOM_COLONY.get())
			.add(ModBlocks.RED_MUSHROOM_COLONY.get());
		fdTag(ModTags.Blocks.ROPES).add(ModBlocks.ROPE.get())
			.add(TagEntry.optionalElement(Identifier.parse("quark:rope")))
			.add(TagEntry.optionalElement(Identifier.parse("supplementaries:rope")));
		fdTag(ModTags.Blocks.TOMATO_SUPPORTS).addTag(ModTags.Blocks.ROPES);
		fdTag(ModTags.Blocks.TRAY_HEAT_SOURCES).add(
				Blocks.LAVA)
			.addTag(net.minecraft.tags.BlockTags.CAMPFIRES)
			.addTag(net.minecraft.tags.BlockTags.FIRE);
		fdTag(ModTags.Blocks.HEAT_SOURCES).add(
				Blocks.MAGMA_BLOCK,
				Blocks.LAVA_CAULDRON,
				ModBlocks.STOVE.get())
			.addTag(ModTags.Blocks.TRAY_HEAT_SOURCES);
		fdTag(ModTags.Blocks.HEAT_CONDUCTORS).add(
				Blocks.HOPPER)
			.add(TagEntry.optionalElement(Identifier.parse("create:chute")));
		fdTag(ModTags.Blocks.DROPS_STRAW).add(Blocks.SHORT_GRASS, Blocks.TALL_GRASS);
		fdTag(ModTags.Blocks.COMPOST_ACTIVATORS).add(Blocks.SHELF_MUSHROOM).add(
				Blocks.BROWN_MUSHROOM,
				Blocks.RED_MUSHROOM,
				Blocks.PODZOL,
				Blocks.MYCELIUM,
				ModBlocks.ORGANIC_COMPOST.get(),
				ModBlocks.RICH_SOIL.get(),
				ModBlocks.RICH_SOIL_FARMLAND.get())
			.addTag(ModTags.Blocks.MUSHROOM_COLONIES);
		fdTag(ModTags.Blocks.UNAFFECTED_BY_RICH_SOIL).add(Blocks.WILDFLOWERS).add(
				Blocks.GRASS_BLOCK,
				Blocks.SHORT_GRASS,
				Blocks.MOSS_BLOCK,
				Blocks.CRIMSON_NYLIUM,
				Blocks.WARPED_NYLIUM,
				Blocks.FERN,
				Blocks.TWISTING_VINES,
				Blocks.TWISTING_VINES_PLANT,
				Blocks.BIG_DRIPLEAF,
				Blocks.BIG_DRIPLEAF_STEM,
				Blocks.PINK_PETALS,
				ModBlocks.SANDY_SHRUB.get())
			.addTag(ModTags.Blocks.MUSHROOM_COLONIES)
			.addTag(ModTags.Blocks.WILD_CROPS)
			.addTag(Tags.Blocks.FLOWERS_TALL);
		fdTag(ModTags.Blocks.MUSHROOM_COLONY_GROWABLE_ON).add(ModBlocks.RICH_SOIL.get());
		fdTag(ModTags.Blocks.DROPS_CAKE_SLICE).add(
			Blocks.CANDLE_CAKE,
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.WHITE),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.ORANGE),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.MAGENTA),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.YELLOW),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.LIME),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.PINK),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.GRAY),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.LIGHT_GRAY),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.CYAN),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.PURPLE),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.BLUE),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.BROWN),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.GREEN),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.RED),
			Blocks.DYED_CANDLE_CAKE.pick(net.minecraft.world.item.DyeColor.BLACK));
		fdTag(ModTags.Blocks.CAMPFIRE_SIGNAL_SMOKE).add(ModBlocks.STRAW_BALE.get()).add(ModBlocks.RICE_BALE.get());
		fdTag(ModTags.Blocks.PLANTED_FROM_BELOW).add(Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT);
	}

	private void registerCompatibilityTags() {
		fdTag(CompatibilityTags.CREATE_FAN_TRANSPARENT).add(ModBlocks.SAFETY_NET.get());
		fdTag(CompatibilityTags.CREATE_PASSIVE_BOILER_HEATERS).add(ModBlocks.STOVE.get());
		fdTag(CompatibilityTags.CREATE_BRITTLE).add(
				ModBlocks.CUTTING_BOARD.get(),
				ModBlocks.FULL_TATAMI_MAT.get(),
				ModBlocks.HALF_TATAMI_MAT.get())
			.addTag(ModTags.Blocks.FEASTS)
			.addTag(ModTags.Blocks.PIES);

		fdTag(CompatibilityTags.SABLE_SUPER_LIGHT)
			.add(ModBlocks.CUTTING_BOARD.get())
			.add(ModBlocks.CANVAS_RUG.get())
			.add(ModBlocks.FULL_TATAMI_MAT.get())
			.add(ModBlocks.HALF_TATAMI_MAT.get())
			.add(ModBlocks.SAFETY_NET.get());
		fdTag(CompatibilityTags.SABLE_LIGHT)
			.addTag(ModTags.Blocks.CABINETS)
			.add(ModBlocks.WOODEN_BASKET.get())
			.add(ModBlocks.BAMBOO_BASKET.get());

		fdTag(CompatibilityTags.SERENE_SEASONS_AUTUMN_CROPS_BLOCK).add(
			ModBlocks.CABBAGE_CROP.get(),
			ModBlocks.ONION_CROP.get(),
			ModBlocks.RICE_CROP.get(),
			ModBlocks.RICE_CROP_PANICLES.get());
		fdTag(CompatibilityTags.SERENE_SEASONS_SPRING_CROPS_BLOCK).add(
			ModBlocks.ONION_CROP.get());
		fdTag(CompatibilityTags.SERENE_SEASONS_SUMMER_CROPS_BLOCK).add(
			ModBlocks.BUDDING_TOMATO_CROP.get(),
			ModBlocks.TOMATO_CROP.get(),
			ModBlocks.TOMATO_CROP_ON_ROPE.get(),
			ModBlocks.TOMATO_CROP_ON_SUPPORT.get(),
			ModBlocks.RICE_CROP.get(),
			ModBlocks.RICE_CROP_PANICLES.get());
		fdTag(CompatibilityTags.SERENE_SEASONS_WINTER_CROPS_BLOCK).add(
			ModBlocks.CABBAGE_CROP.get());
		fdTag(CompatibilityTags.SERENE_SEASONS_UNBREAKABLE_FERTILE_CROPS).add(
			ModBlocks.ONION_CROP.get());
	}

	private vectorwing.farmersdelight.data.tools.FDTagAppender<Block> fdTag(net.minecraft.tags.TagKey<Block> tag) {
		return new vectorwing.farmersdelight.data.tools.FDTagAppender<>(this.tag(tag), BuiltInRegistries.BLOCK);
	}
}
