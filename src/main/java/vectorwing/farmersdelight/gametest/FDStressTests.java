package vectorwing.farmersdelight.gametest;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.ItemResource;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.AbstractStoveBlock;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.entity.RottenTomatoEntity;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModEntityTypes;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Exhaustive, edge-case and stress GameTests. Run with {@code ./gradlew runGameTestServer}.
 */
public final class FDStressTests
{
	private static final Identifier BIG_STRUCTURE = FarmersDelight.id("gametest/empty_32x6x32");
	private static final Identifier SMALL_STRUCTURE = FarmersDelight.id("gametest/empty_5x5");
	private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, FarmersDelight.MODID);
	private static final List<Registration> TESTS = new ArrayList<>();

	private record Registration(ResourceKey<Consumer<GameTestHelper>> function, Identifier structure, int maxTicks) {
	}

	static {
		// Data / network round trips
		test("recipes_network_round_trip", SMALL_STRUCTURE, 20, FDStressTests::recipesNetworkRoundTrip);
		test("items_network_round_trip", SMALL_STRUCTURE, 20, FDStressTests::itemsNetworkRoundTrip);
		test("villager_trades_registered", SMALL_STRUCTURE, 20, FDStressTests::villagerTradesRegistered);
		// Exhaustive end-to-end
		test("every_cooking_recipe_cooks", BIG_STRUCTURE, 1200, FDStressTests::everyCookingRecipeCooks);
		test("every_cutting_recipe_cuts", BIG_STRUCTURE, 40, FDStressTests::everyCuttingRecipeCuts);
		test("every_block_places_and_breaks", BIG_STRUCTURE, 40, FDStressTests::everyBlockPlacesAndBreaks);
		test("every_block_entity_persists", BIG_STRUCTURE, 40, FDStressTests::everyBlockEntityPersists);
		// Edge cases
		test("cold_cooking_pot_does_not_cook", SMALL_STRUCTURE, 300, FDStressTests::coldCookingPotDoesNotCook);
		test("wrong_container_is_not_served", SMALL_STRUCTURE, 400, FDStressTests::wrongContainerIsNotServed);
		test("cooking_pot_output_stops_when_full", SMALL_STRUCTURE, 400, FDStressTests::cookingPotOutputStopsWhenFull);
		test("cooking_pot_hopper_automation", SMALL_STRUCTURE, 800, FDStressTests::cookingPotHopperAutomation);
		test("cutting_board_rejects_wrong_tool", SMALL_STRUCTURE, 20, FDStressTests::cuttingBoardRejectsWrongTool);
		test("skillet_rejects_uncookable_item", SMALL_STRUCTURE, 20, FDStressTests::skilletRejectsUncookableItem);
		test("waterlogged_skillet_drops_contents", SMALL_STRUCTURE, 60, FDStressTests::waterloggedSkilletDropsContents);
		test("stove_rejects_when_full", SMALL_STRUCTURE, 20, FDStressTests::stoveRejectsWhenFull);
		test("feast_served_to_empty", SMALL_STRUCTURE, 40, FDStressTests::feastServedToEmpty);
		test("tomato_climbs_rope_with_bonemeal", SMALL_STRUCTURE, 40, FDStressTests::tomatoClimbsRopeWithBonemeal);
		test("rotten_tomato_breaks_on_impact", SMALL_STRUCTURE, 100, FDStressTests::rottenTomatoBreaksOnImpact);
		test("cooking_consumes_ingredients_and_stacks_meals", SMALL_STRUCTURE, 600, FDStressTests::cookingConsumesIngredientsAndStacksMeals);
		test("broken_blocks_drop_contents", BIG_STRUCTURE, 60, FDStressTests::brokenBlocksDropContents);
		test("empty_hand_on_empty_blocks", SMALL_STRUCTURE, 20, FDStressTests::emptyHandOnEmptyBlocks);
		// Stress
		test("stress_100_cooking_pots", BIG_STRUCTURE, 600, FDStressTests::stress100CookingPots);
	}

	private FDStressTests() {
	}

	private static void test(String name, Identifier structure, int maxTicks, Consumer<GameTestHelper> function) {
		FUNCTIONS.register(name, () -> function);
		TESTS.add(new Registration(ResourceKey.create(Registries.TEST_FUNCTION, FarmersDelight.id(name)), structure, maxTicks));
	}

	public static void register(IEventBus modEventBus) {
		FUNCTIONS.register(modEventBus);
		modEventBus.addListener(FDStressTests::registerTests);
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(FarmersDelight.id("stress"));
		for (Registration test : TESTS) {
			event.registerTest(test.function().identifier(), new FunctionGameTestInstance(test.function(), new TestData<>(environment, test.structure(), test.maxTicks(), 0, true)));
		}
	}

	// ------------------------------------------------------------------ helpers

	private static BlockState litStove() {
		return ModBlocks.STOVE.get().defaultBlockState().setValue(AbstractStoveBlock.LIT, true);
	}

	private static Item firstItem(Ingredient ingredient) {
		return ingredient.items().findFirst().map(Holder::value).orElseThrow(() -> new IllegalStateException("Ingredient has no items: " + ingredient));
	}

	private static List<RecipeHolder<?>> modRecipes(GameTestHelper helper) {
		return helper.getLevel().recipeAccess().getRecipes().stream()
				.filter(holder -> holder.id().identifier().getNamespace().equals(FarmersDelight.MODID))
				.toList();
	}

	private static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> recipesOfType(GameTestHelper helper, net.minecraft.world.item.crafting.RecipeType<T> type) {
		return helper.getLevel().recipeAccess().recipeMap().byType(type).stream().toList();
	}

	private static BlockPos grid(int index, int spacing, int y) {
		int perRow = 30 / spacing;
		return new BlockPos(1 + (index % perRow) * spacing, y, 1 + (index / perRow) * spacing);
	}

	private static void failIfAny(GameTestHelper helper, List<String> problems, String what) {
		if (!problems.isEmpty()) {
			helper.fail(problems.size() + " " + what + ": " + String.join("; ", problems.subList(0, Math.min(problems.size(), 15))));
		}
	}

	private static BlockHitResult hit(BlockPos absolute) {
		return new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
	}

	// ------------------------------------------------------------------ data / network

	/** Every Farmer's Delight recipe and its recipe-book display survive the server -> client network codecs. */
	private static void recipesNetworkRoundTrip(GameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		List<String> problems = new ArrayList<>();
		List<RecipeHolder<?>> recipes = modRecipes(helper);
		helper.assertTrue(recipes.size() > 100, "Expected over 100 Farmer's Delight recipes, found " + recipes.size());
		for (RecipeHolder<?> holder : recipes) {
			try {
				RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
				Recipe.STREAM_CODEC.encode(buf, holder.value());
				Recipe<?> decoded = Recipe.STREAM_CODEC.decode(buf);
				if (decoded.getClass() != holder.value().getClass()) {
					problems.add(holder.id().identifier() + " decoded as " + decoded.getClass().getSimpleName());
				}
				for (RecipeDisplay display : holder.value().display()) {
					RegistryFriendlyByteBuf displayBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
					RecipeDisplay.STREAM_CODEC.encode(displayBuf, display);
					RecipeDisplay.STREAM_CODEC.decode(displayBuf);
				}
			} catch (Exception e) {
				problems.add(holder.id().identifier() + ": " + e);
			}
		}
		failIfAny(helper, problems, "recipes failed the network round trip");
		helper.succeed();
	}

	/** Every Farmer's Delight item (with its default components) survives the network and save codecs. */
	private static void itemsNetworkRoundTrip(GameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		List<String> problems = new ArrayList<>();
		BuiltInRegistries.ITEM.listElements()
				.filter(holder -> holder.key().identifier().getNamespace().equals(FarmersDelight.MODID))
				.forEach(holder -> {
					ItemStack stack = new ItemStack(holder.value());
					try {
						RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
						ItemStack.STREAM_CODEC.encode(buf, stack);
						ItemStack decoded = ItemStack.STREAM_CODEC.decode(buf);
						if (!ItemStack.isSameItemSameComponents(stack, decoded)) {
							problems.add(holder.key().identifier() + " changed over the network");
						}
						ItemStack.CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).getOrThrow();
					} catch (Exception e) {
						problems.add(holder.key().identifier() + ": " + e);
					}
				});
		failIfAny(helper, problems, "items failed the round trip");
		helper.succeed();
	}

	/** The data-driven trades exist and are part of the vanilla trade pools. */
	private static void villagerTradesRegistered(GameTestHelper helper) {
		var trades = helper.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE);
		long count = trades.listElementIds().filter(key -> key.identifier().getNamespace().equals(FarmersDelight.MODID)).count();
		helper.assertValueEqual((int) count, 8, "number of Farmer's Delight villager trades");
		var level1 = trades.get(TagKey.create(Registries.VILLAGER_TRADE, Identifier.withDefaultNamespace("farmer/level_1")));
		helper.assertTrue(level1.isPresent() && level1.get().stream().anyMatch(h -> h.unwrapKey().orElseThrow().identifier().getNamespace().equals(FarmersDelight.MODID)),
				"Farmer level 1 trade pool should contain Farmer's Delight trades");
		helper.succeed();
	}

	// ------------------------------------------------------------------ exhaustive end-to-end

	/** Every cooking pot recipe is actually cooked in a pot on a lit stove and served into its container. */
	private static void everyCookingRecipeCooks(GameTestHelper helper) {
		List<RecipeHolder<CookingPotRecipe>> recipes = recipesOfType(helper, ModRecipeTypes.COOKING.get());
		helper.assertTrue(recipes.size() > 20, "Expected over 20 cooking pot recipes, found " + recipes.size());
		helper.assertTrue(recipes.size() <= 100, "Too many recipes for the test area");
		List<BlockPos> pots = new ArrayList<>();
		List<ItemStack> expected = new ArrayList<>();
		for (int i = 0; i < recipes.size(); i++) {
			CookingPotRecipe recipe = recipes.get(i).value();
			BlockPos stove = grid(i, 3, 1);
			helper.setBlock(stove, litStove());
			helper.setBlock(stove.above(), ModBlocks.COOKING_POT.get());
			CookingPotBlockEntity pot = helper.getBlockEntity(stove.above(), CookingPotBlockEntity.class);
			List<Ingredient> ingredients = recipe.getIngredients();
			for (int slot = 0; slot < ingredients.size(); slot++) {
				pot.getInventory().set(slot, ItemResource.of(firstItem(ingredients.get(slot))), 1);
			}
			ItemStack container = recipe.getOutputContainer();
			if (!container.isEmpty()) {
				pot.getInventory().set(CookingPotBlockEntity.CONTAINER_SLOT, ItemResource.of(container), 1);
			}
			pot.setChanged();
			pots.add(stove.above());
			expected.add(recipe.assemble(null));
		}
		helper.succeedWhen(() -> {
			List<String> missing = new ArrayList<>();
			for (int i = 0; i < pots.size(); i++) {
				CookingPotBlockEntity pot = helper.getBlockEntity(pots.get(i), CookingPotBlockEntity.class);
				ItemStack output = pot.getInventory().getResource(CookingPotBlockEntity.OUTPUT_SLOT).toStack(pot.getInventory().getAmountAsInt(CookingPotBlockEntity.OUTPUT_SLOT));
				if (!output.is(expected.get(i).getItem())) {
					missing.add(recipes.get(i).id().identifier().getPath());
				}
			}
			helper.assertTrue(missing.isEmpty(), missing.size() + " recipes not served yet: " + missing);
		});
	}

	/** Every cutting board recipe works with the first tool its recipe accepts. */
	private static void everyCuttingRecipeCuts(GameTestHelper helper) {
		List<RecipeHolder<CuttingBoardRecipe>> recipes = recipesOfType(helper, ModRecipeTypes.CUTTING.get());
		helper.assertTrue(recipes.size() > 50, "Expected over 50 cutting recipes, found " + recipes.size());
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		List<String> problems = new ArrayList<>();
		for (int i = 0; i < recipes.size(); i++) {
			CuttingBoardRecipe recipe = recipes.get(i).value();
			BlockPos pos = grid(i % 225, 2, 1);
			helper.setBlock(pos, Blocks.AIR);
			helper.setBlock(pos, ModBlocks.CUTTING_BOARD.get());
			CuttingBoardBlockEntity board = helper.getBlockEntity(pos, CuttingBoardBlockEntity.class);
			try {
				ItemStack input = new ItemStack(firstItem(recipe.getIngredients().getFirst()));
				ItemStack tool = new ItemStack(firstItem(recipe.getTool()));
				if (!board.addItem(input).isEmpty()) {
					problems.add(recipes.get(i).id().identifier().getPath() + ": board refused " + input);
					continue;
				}
				if (!board.processStoredItemUsingTool(tool, player)) {
					problems.add(recipes.get(i).id().identifier().getPath() + ": not cut with " + tool);
				} else if (!board.getStoredItem().isEmpty()) {
					problems.add(recipes.get(i).id().identifier().getPath() + ": input not consumed");
				}
			} catch (Exception e) {
				problems.add(recipes.get(i).id().identifier().getPath() + ": " + e);
			}
		}
		failIfAny(helper, problems, "cutting recipes failed");
		helper.succeed();
	}

	/** Every block can be placed, computes its drops and can be broken without errors. */
	private static void everyBlockPlacesAndBreaks(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		ItemStack[] tools = {new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_AXE), new ItemStack(ModItems.IRON_KNIFE.get()), ItemStack.EMPTY};
		List<String> problems = new ArrayList<>();
		List<Block> blocks = BuiltInRegistries.BLOCK.listElements()
				.filter(holder -> holder.key().identifier().getNamespace().equals(FarmersDelight.MODID))
				.map(Holder::value).toList();
		helper.assertTrue(blocks.size() > 100, "Expected over 100 Farmer's Delight blocks, found " + blocks.size());
		for (int i = 0; i < blocks.size(); i++) {
			Block block = blocks.get(i);
			BlockPos pos = grid(i % 225, 2, 2);
			try {
				helper.setBlock(pos.below(), ModBlocks.RICH_SOIL_FARMLAND.get());
				helper.setBlock(pos, block.defaultBlockState());
				BlockPos absolute = helper.absolutePos(pos);
				BlockState placed = helper.getLevel().getBlockState(absolute);
				for (ItemStack tool : tools) {
					Block.getDrops(placed, helper.getLevel(), absolute, helper.getLevel().getBlockEntity(absolute), player, tool);
				}
				helper.getLevel().destroyBlock(absolute, true, player);
			} catch (Exception e) {
				problems.add(BuiltInRegistries.BLOCK.getKey(block) + ": " + e);
			}
		}
		failIfAny(helper, problems, "blocks failed");
		helper.succeed();
	}

	/** Every block entity survives a save -> load -> save cycle unchanged (chunk saving / world reload). */
	private static void everyBlockEntityPersists(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		List<String> problems = new ArrayList<>();
		int index = 0;
		for (Block block : BuiltInRegistries.BLOCK) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			if (!id.getNamespace().equals(FarmersDelight.MODID)) continue;
			BlockPos pos = grid(index++ % 225, 2, 1);
			helper.setBlock(pos, block.defaultBlockState());
			BlockPos absolute = helper.absolutePos(pos);
			BlockEntity be = helper.getLevel().getBlockEntity(absolute);
			if (be == null) continue;
			try {
				if (be instanceof CookingPotBlockEntity pot) {
					pot.getInventory().set(0, ItemResource.of(ModItems.TOMATO.get()), 5);
					pot.getInventory().set(CookingPotBlockEntity.CONTAINER_SLOT, ItemResource.of(Items.BOWL), 3);
				} else if (be instanceof CuttingBoardBlockEntity board) {
					board.addItem(new ItemStack(ModItems.CABBAGE.get()));
				}
				CompoundTag first = be.saveWithFullMetadata(registries);
				BlockEntity loaded = BlockEntity.loadStatic(absolute, be.getBlockState(), first, registries);
				if (loaded == null) {
					problems.add(id + ": could not be loaded");
					continue;
				}
				CompoundTag second = loaded.saveWithFullMetadata(registries);
				if (!first.equals(second)) {
					problems.add(id + ": data changed after reload (" + first + " vs " + second + ")");
				}
			} catch (Exception e) {
				problems.add(id + ": " + e);
			}
		}
		failIfAny(helper, problems, "block entities failed to persist");
		helper.succeed();
	}

	// ------------------------------------------------------------------ edge cases

	private static CookingPotBlockEntity potWithTomatoes(GameTestHelper helper, BlockState heat) {
		BlockPos base = new BlockPos(2, 1, 2);
		helper.setBlock(base, heat);
		helper.setBlock(base.above(), ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity pot = helper.getBlockEntity(base.above(), CookingPotBlockEntity.class);
		pot.getInventory().set(0, ItemResource.of(ModItems.TOMATO.get()), 1);
		pot.getInventory().set(1, ItemResource.of(ModItems.TOMATO.get()), 1);
		pot.setChanged();
		return pot;
	}

	private static void coldCookingPotDoesNotCook(GameTestHelper helper) {
		CookingPotBlockEntity pot = potWithTomatoes(helper, Blocks.STONE.defaultBlockState());
		helper.assertFalse(pot.isHeated(), "Pot on stone must not be heated");
		helper.runAfterDelay(250, () -> {
			helper.assertTrue(pot.getMeal().isEmpty(), "Cold pot cooked anyway: " + pot.getMeal());
			helper.succeed();
		});
	}

	private static void wrongContainerIsNotServed(GameTestHelper helper) {
		CookingPotBlockEntity pot = potWithTomatoes(helper, litStove());
		pot.getInventory().set(CookingPotBlockEntity.CONTAINER_SLOT, ItemResource.of(Items.BUCKET), 1);
		pot.setChanged();
		helper.succeedWhen(() -> {
			helper.assertTrue(pot.getMeal().is(ModItems.TOMATO_SAUCE.get()), "Meal should still be cooked");
			helper.assertTrue(pot.getInventory().getResource(CookingPotBlockEntity.OUTPUT_SLOT).isEmpty(), "A bucket must not be used to serve sauce");
			helper.assertTrue(pot.getInventory().getResource(CookingPotBlockEntity.CONTAINER_SLOT).is(Items.BUCKET), "Bucket must stay in the container slot");
		});
	}

	private static void cookingPotOutputStopsWhenFull(GameTestHelper helper) {
		CookingPotBlockEntity pot = potWithTomatoes(helper, litStove());
		pot.getInventory().set(CookingPotBlockEntity.OUTPUT_SLOT, ItemResource.of(ModItems.TOMATO_SAUCE.get()), ModItems.TOMATO_SAUCE.get().getDefaultMaxStackSize());
		pot.getInventory().set(CookingPotBlockEntity.CONTAINER_SLOT, ItemResource.of(Items.BOWL), 1);
		pot.setChanged();
		helper.runAfterDelay(300, () -> {
			int output = pot.getInventory().getAmountAsInt(CookingPotBlockEntity.OUTPUT_SLOT);
			helper.assertTrue(output <= ModItems.TOMATO_SAUCE.get().getDefaultMaxStackSize(), "Output overflowed: " + output);
			helper.succeed();
		});
	}

	/** Hopper above feeds ingredients, hopper at the side feeds bowls; the pot cooks and serves on its own. */
	private static void cookingPotHopperAutomation(GameTestHelper helper) {
		BlockPos stove = new BlockPos(2, 1, 2);
		BlockPos pot = stove.above();
		helper.setBlock(stove, litStove());
		helper.setBlock(pot, ModBlocks.COOKING_POT.get());
		helper.setBlock(pot.above(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN));
		helper.setBlock(pot.east(), Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.WEST));
		// Hoppers stack identical items into one slot (= one ingredient), so preload one tomato and let the hopper add the second.
		helper.getBlockEntity(pot, CookingPotBlockEntity.class).getInventory().set(1, ItemResource.of(ModItems.TOMATO.get()), 1);
		helper.getBlockEntity(pot.above(), HopperBlockEntity.class).setItem(0, new ItemStack(ModItems.TOMATO.get(), 1));
		helper.getBlockEntity(pot.east(), HopperBlockEntity.class).setItem(0, new ItemStack(Items.BOWL, 1));
		helper.succeedWhen(() -> {
			CookingPotBlockEntity cookingPot = helper.getBlockEntity(pot, CookingPotBlockEntity.class);
			helper.assertTrue(cookingPot.getInventory().getResource(CookingPotBlockEntity.OUTPUT_SLOT).is(ModItems.TOMATO_SAUCE.get()),
					"Hopper-fed pot should serve Tomato Sauce, output: " + cookingPot.getInventory().getResource(CookingPotBlockEntity.OUTPUT_SLOT));
			// Automation can pull the served meal from below
			var bottom = helper.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.BLOCK, helper.absolutePos(pot), Direction.DOWN);
			try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
				helper.assertValueEqual(bottom.extract(ItemResource.of(ModItems.TOMATO_SAUCE.get()), 1, tx), 1, "meals extracted from the bottom");
				helper.assertValueEqual(bottom.extract(ItemResource.of(ModItems.TOMATO.get()), 1, tx), 0, "ingredients extracted from the bottom");
			}
		});
	}

	private static void cuttingBoardRejectsWrongTool(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.CUTTING_BOARD.get());
		CuttingBoardBlockEntity board = helper.getBlockEntity(pos, CuttingBoardBlockEntity.class);
		board.addItem(new ItemStack(ModItems.CABBAGE.get()));
		helper.assertFalse(board.processStoredItemUsingTool(new ItemStack(Items.STICK), helper.makeMockPlayer(GameType.SURVIVAL)), "A stick must not cut cabbage");
		helper.assertTrue(board.getStoredItem().is(ModItems.CABBAGE.get()), "Cabbage must stay on the board");
		helper.assertTrue(board.removeItem().is(ModItems.CABBAGE.get()), "Cabbage can be taken back");
		helper.assertTrue(board.isEmpty(), "Board is empty after taking the item");
		helper.succeed();
	}

	private static void skilletRejectsUncookableItem(GameTestHelper helper) {
		BlockPos stove = new BlockPos(2, 1, 2);
		helper.setBlock(stove, litStove());
		helper.setBlock(stove.above(), ModBlocks.SKILLET.get());
		SkilletBlockEntity skillet = helper.getBlockEntity(stove.above(), SkilletBlockEntity.class);
		ItemStack dirt = new ItemStack(Items.DIRT);
		ItemStack rest = skillet.addItemToCook(dirt, helper.makeMockPlayer(GameType.SURVIVAL), helper.getLevel());
		helper.assertTrue(!skillet.hasStoredStack() && rest.is(Items.DIRT), "Skillet must not accept dirt");
		helper.succeed();
	}

	private static void waterloggedSkilletDropsContents(GameTestHelper helper) {
		BlockPos stove = new BlockPos(2, 1, 2);
		BlockPos pos = stove.above();
		helper.setBlock(stove, litStove());
		helper.setBlock(pos, ModBlocks.SKILLET.get());
		SkilletBlockEntity skillet = helper.getBlockEntity(pos, SkilletBlockEntity.class);
		skillet.addItemToCook(new ItemStack(Items.BEEF), helper.makeMockPlayer(GameType.SURVIVAL), helper.getLevel());
		helper.assertTrue(skillet.hasStoredStack(), "Beef should be on the skillet");
		helper.setBlock(pos, helper.getLevel().getBlockState(helper.absolutePos(pos)).setValue(BlockStateProperties.WATERLOGGED, true));
		helper.succeedWhen(() -> {
			helper.assertFalse(helper.getBlockEntity(pos, SkilletBlockEntity.class).hasStoredStack(), "Waterlogged skillet should drop its food");
			helper.assertItemEntityPresent(Items.BEEF, pos, 2.0);
		});
	}

	private static void stoveRejectsWhenFull(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, litStove());
		StoveBlockEntity stove = helper.getBlockEntity(pos, StoveBlockEntity.class);
		int placed = 0;
		for (int i = 0; i < 20; i++) {
			ItemStack beef = new ItemStack(Items.BEEF);
			var recipe = stove.getCookingRecipe(beef);
			if (recipe.isEmpty() || !stove.placeFood(null, beef, recipe.get())) break;
			placed++;
		}
		helper.assertValueEqual(placed, stove.getItems().size(), "food placed on the stove");
		helper.assertTrue(stove.isFull(), "Stove should report full");
		helper.succeed();
	}

	private static void feastServedToEmpty(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 2, 2);
		helper.setBlock(pos.below(), Blocks.STONE);
		helper.setBlock(pos, ModBlocks.ROAST_CHICKEN_BLOCK.get());
		BlockPos absolute = helper.absolutePos(pos);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		FeastBlock feast = (FeastBlock) ModBlocks.ROAST_CHICKEN_BLOCK.get();
		int servings = helper.getLevel().getBlockState(absolute).getValue(feast.getServingsProperty());
		int chickens = 0;
		for (int i = 0; i < servings; i++) {
			player.getInventory().clearContent();
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
			BlockState state = helper.getLevel().getBlockState(absolute);
			feast.useItemOn(player.getMainHandItem(), state, helper.getLevel(), absolute, player, InteractionHand.MAIN_HAND, hit(absolute));
			chickens += player.getInventory().countItem(ModItems.ROAST_CHICKEN.get());
		}
		helper.assertValueEqual(chickens, servings, "roast chicken servings received");
		BlockState after = helper.getLevel().getBlockState(absolute);
		helper.assertTrue(!after.is(ModBlocks.ROAST_CHICKEN_BLOCK.get()) || after.getValue(feast.getServingsProperty()) == 0, "Feast should be used up");
		// Using the empty feast (leftovers) once more removes it
		if (after.is(ModBlocks.ROAST_CHICKEN_BLOCK.get())) {
			feast.useItemOn(ItemStack.EMPTY, after, helper.getLevel(), absolute, player, InteractionHand.MAIN_HAND, hit(absolute));
		}
		helper.succeedWhen(() -> helper.assertBlockNotPresent(ModBlocks.ROAST_CHICKEN_BLOCK.get(), pos));
	}

	private static void tomatoClimbsRopeWithBonemeal(GameTestHelper helper) {
		BlockPos crop = new BlockPos(2, 1, 2);
		helper.setBlock(crop.below(), ModBlocks.RICH_SOIL_FARMLAND.get());
		helper.setBlock(crop, ModBlocks.TOMATO_CROP.get());
		helper.setBlock(crop.above(), ModBlocks.ROPE.get());
		helper.setBlock(crop.above(2), ModBlocks.ROPE.get());
		ServerLevel level = helper.getLevel();
		for (int i = 0; i < 30; i++) {
			BlockState state = level.getBlockState(helper.absolutePos(crop));
			if (state.getBlock() instanceof BonemealableBlock bonemealable && bonemealable.isValidBonemealTarget(level, helper.absolutePos(crop), state)) {
				bonemealable.performBonemeal(level, level.getRandom(), helper.absolutePos(crop), state);
			}
		}
		helper.succeedWhen(() -> helper.assertBlockPresent(ModBlocks.TOMATO_CROP_ON_ROPE.get(), crop.above()));
	}

	private static void rottenTomatoBreaksOnImpact(GameTestHelper helper) {
		RottenTomatoEntity tomato = helper.spawn(ModEntityTypes.ROTTEN_TOMATO.get(), new BlockPos(2, 4, 2));
		tomato.setDeltaMovement(0, -1, 0);
		helper.succeedWhen(() -> helper.assertEntityNotPresent(ModEntityTypes.ROTTEN_TOMATO.get()));
	}

	/** 3 batches of ingredients give exactly 3 meals and are fully used up (no duplication, no loss). */
	private static void cookingConsumesIngredientsAndStacksMeals(GameTestHelper helper) {
		BlockPos base = new BlockPos(2, 1, 2);
		helper.setBlock(base, litStove());
		helper.setBlock(base.above(), ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity pot = helper.getBlockEntity(base.above(), CookingPotBlockEntity.class);
		pot.getInventory().set(0, ItemResource.of(ModItems.TOMATO.get()), 3);
		pot.getInventory().set(1, ItemResource.of(ModItems.TOMATO.get()), 3);
		pot.setChanged();
		helper.succeedWhen(() -> {
			helper.assertValueEqual(pot.getMeal().getCount(), 3, "meals in the pot");
			helper.assertTrue(pot.getInventory().getResource(0).isEmpty() && pot.getInventory().getResource(1).isEmpty(), "All tomatoes must be used up");
		});
	}

	/** Breaking a stove, cooking pot, cutting board, skillet, basket or cabinet drops what was inside. */
	private static void brokenBlocksDropContents(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos stovePos = new BlockPos(2, 1, 2);
		helper.setBlock(stovePos, litStove());
		StoveBlockEntity stove = helper.getBlockEntity(stovePos, StoveBlockEntity.class);
		ItemStack beef = new ItemStack(Items.BEEF);
		stove.placeFood(null, beef, stove.getCookingRecipe(beef).orElseThrow());

		BlockPos potPos = new BlockPos(8, 1, 2);
		helper.setBlock(potPos, ModBlocks.COOKING_POT.get());
		helper.getBlockEntity(potPos, CookingPotBlockEntity.class).getInventory().set(0, ItemResource.of(ModItems.ONION.get()), 7);

		BlockPos boardPos = new BlockPos(14, 1, 2);
		helper.setBlock(boardPos, ModBlocks.CUTTING_BOARD.get());
		helper.getBlockEntity(boardPos, CuttingBoardBlockEntity.class).addItem(new ItemStack(ModItems.CABBAGE.get()));

		BlockPos skilletPos = new BlockPos(20, 1, 2);
		helper.setBlock(skilletPos, ModBlocks.SKILLET.get());
		helper.getBlockEntity(skilletPos, SkilletBlockEntity.class).addItemToCook(new ItemStack(Items.PORKCHOP, 3), player, helper.getLevel());

		BlockPos basketPos = new BlockPos(26, 1, 2);
		helper.setBlock(basketPos, ModBlocks.BAMBOO_BASKET.get());
		((net.minecraft.world.Container) helper.getLevel().getBlockEntity(helper.absolutePos(basketPos))).setItem(0, new ItemStack(ModItems.RICE.get(), 5));

		BlockPos cabinetPos = new BlockPos(2, 1, 10);
		helper.setBlock(cabinetPos, ModBlocks.OAK_CABINET.get());
		((net.minecraft.world.Container) helper.getLevel().getBlockEntity(helper.absolutePos(cabinetPos))).setItem(0, new ItemStack(ModItems.TOMATO.get(), 9));

		for (BlockPos pos : List.of(stovePos, potPos, boardPos, skilletPos, basketPos, cabinetPos)) {
			helper.getLevel().destroyBlock(helper.absolutePos(pos), true, player);
		}
		helper.succeedWhen(() -> {
			helper.assertItemEntityPresent(Items.BEEF, stovePos, 2.0);
			helper.assertItemEntityCountIs(ModItems.ONION.get(), potPos, 2.0, 7);
			helper.assertItemEntityPresent(ModItems.CABBAGE.get(), boardPos, 2.0);
			helper.assertItemEntityPresent(Items.PORKCHOP, skilletPos, 2.0);
			helper.assertItemEntityCountIs(ModItems.RICE.get(), basketPos, 2.0, 5);
			helper.assertItemEntityCountIs(ModItems.TOMATO.get(), cabinetPos, 2.0, 9);
		});
	}

	/** Right-clicking an empty skillet / cutting board / cooking pot / stove with an empty hand must not throw (seen in playtesting). */
	private static void emptyHandOnEmptyBlocks(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		BlockPos stove = new BlockPos(1, 1, 1);
		helper.setBlock(stove, litStove());
		helper.setBlock(stove.above(), ModBlocks.SKILLET.get());
		helper.setBlock(new BlockPos(3, 1, 1), ModBlocks.CUTTING_BOARD.get());
		helper.setBlock(new BlockPos(1, 1, 3), litStove());
		helper.setBlock(new BlockPos(3, 1, 3), ModBlocks.COOKING_POT.get());
		for (BlockPos pos : List.of(stove.above(), new BlockPos(3, 1, 1), new BlockPos(1, 1, 3), new BlockPos(3, 1, 3))) {
			helper.useBlock(pos, player);
		}
		helper.assertTrue(helper.getBlockEntity(new BlockPos(3, 1, 1), CuttingBoardBlockEntity.class).removeItem().isEmpty(), "Empty board gives nothing");
		helper.assertTrue(helper.getBlockEntity(stove.above(), SkilletBlockEntity.class).removeItem().isEmpty(), "Empty skillet gives nothing");
		helper.succeed();
	}

	// ------------------------------------------------------------------ stress

	/** 100 cooking pots cooking at once: all finish and the server keeps a healthy tick time. */
	private static void stress100CookingPots(GameTestHelper helper) {
		List<BlockPos> pots = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			BlockPos stove = grid(i, 3, 1);
			helper.setBlock(stove, litStove());
			helper.setBlock(stove.above(), ModBlocks.COOKING_POT.get());
			CookingPotBlockEntity pot = helper.getBlockEntity(stove.above(), CookingPotBlockEntity.class);
			pot.getInventory().set(0, ItemResource.of(ModItems.TOMATO.get()), 64);
			pot.getInventory().set(1, ItemResource.of(ModItems.TOMATO.get()), 64);
			pot.getInventory().set(CookingPotBlockEntity.CONTAINER_SLOT, ItemResource.of(Items.BOWL), 64);
			pot.setChanged();
			pots.add(stove.above());
		}
		long start = System.nanoTime();
		helper.runAfterDelay(500, () -> {
			long elapsedMs = (System.nanoTime() - start) / 1_000_000;
			double msPerTick = elapsedMs / 500.0;
			int served = 0;
			for (BlockPos pos : pots) {
				served += helper.getBlockEntity(pos, CookingPotBlockEntity.class).getInventory().getAmountAsInt(CookingPotBlockEntity.OUTPUT_SLOT);
			}
			FarmersDelight.LOGGER.info("[stress] 100 cooking pots: {} meals served in 500 ticks, {} ms per tick wall time", served, String.format("%.2f", msPerTick));
			helper.assertTrue(served >= 100 * 4, "Expected at least 4 meals per pot, got " + served + " total");
			helper.assertTrue(msPerTick < 50.0, "Server fell behind: " + msPerTick + " ms per tick");
			helper.succeed();
		});
	}
}
