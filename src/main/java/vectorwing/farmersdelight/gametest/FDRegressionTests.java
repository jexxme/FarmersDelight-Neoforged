package vectorwing.farmersdelight.gametest;

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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.AbstractStoveBlock;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModDataComponents;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Regression tests for bugs found by the adversarial review: client sync, client-side decisions, the recipe book,
 * automation edge cases, broken save data, shared component stacks and recipe ids. Each test names the bug it guards.
 */
public final class FDRegressionTests
{
	private static final Identifier STRUCTURE = FarmersDelight.id("gametest/empty_5x5");
	private static final Identifier BIG_STRUCTURE = FarmersDelight.id("gametest/empty_32x6x32");
	private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, FarmersDelight.MODID);
	private static final List<Registration> TESTS = new ArrayList<>();

	private record Registration(ResourceKey<Consumer<GameTestHelper>> function, Identifier structure) {
	}

	static {
		test("client_sync_keeps_contents", STRUCTURE, FDRegressionTests::clientSyncKeepsContents);
		test("campfire_input_set_matches_recipes", STRUCTURE, FDRegressionTests::campfireInputSetMatchesRecipes);
		test("skillet_in_hand_cooks", STRUCTURE, FDRegressionTests::skilletInHandCooks);
		test("recipe_book_places_ingredients", STRUCTURE, FDRegressionTests::recipeBookPlacesIngredients);
		test("shift_click_meal_awards_crafted_stat", STRUCTURE, FDRegressionTests::shiftClickMealAwardsCraftedStat);
		test("carving_tool_is_not_duplicated", STRUCTURE, FDRegressionTests::carvingToolIsNotDuplicated);
		test("hopper_unlocks_carving_board", STRUCTURE, FDRegressionTests::hopperUnlocksCarvingBoard);
		test("stuck_carving_board_repairs_on_load", STRUCTURE, FDRegressionTests::stuckCarvingBoardRepairsOnLoad);
		test("unreadable_item_keeps_other_slots", STRUCTURE, FDRegressionTests::unreadableItemKeepsOtherSlots);
		test("serving_from_pot_item_leaves_copies_alone", STRUCTURE, FDRegressionTests::servingFromPotItemLeavesCopiesAlone);
		test("clear_content_empties_stove", STRUCTURE, FDRegressionTests::clearContentEmptiesStove);
		test("upstream_recipe_ids_exist", BIG_STRUCTURE, FDRegressionTests::upstreamRecipeIdsExist);
	}

	private FDRegressionTests() {
	}

	private static void test(String name, Identifier structure, Consumer<GameTestHelper> function) {
		FUNCTIONS.register(name, () -> function);
		TESTS.add(new Registration(ResourceKey.create(Registries.TEST_FUNCTION, FarmersDelight.id(name)), structure));
	}

	public static void register(IEventBus modEventBus) {
		FUNCTIONS.register(modEventBus);
		modEventBus.addListener(FDRegressionTests::registerTests);
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(FarmersDelight.id("regression"));
		for (Registration test : TESTS) {
			event.registerTest(test.function().identifier(), new FunctionGameTestInstance(test.function(), new TestData<>(environment, test.structure(), 100, 0, true)));
		}
	}

	private static BlockState litStove() {
		return ModBlocks.STOVE.get().defaultBlockState().setValue(AbstractStoveBlock.LIT, true);
	}

	// ------------------------------------------------------------------ client sync

	/**
	 * What the client gets (getUpdateTag -> handleUpdateTag on a fresh block entity) must hold the same items as the
	 * server. The stove sent its items under a key the client never read, so food on stoves was invisible.
	 */
	private static void clientSyncKeepsContents(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		List<String> problems = new ArrayList<>();

		helper.setBlock(new BlockPos(0, 1, 0), ModBlocks.STOVE.get().defaultBlockState().setValue(AbstractStoveBlock.LIT, false));
		AbstractStoveBlockEntity stove = helper.getBlockEntity(new BlockPos(0, 1, 0), AbstractStoveBlockEntity.class);
		ItemStack beef = new ItemStack(Items.BEEF);
		stove.placeFood(null, beef, stove.getCookingRecipe(beef).orElseThrow());
		ItemStack mutton = new ItemStack(Items.MUTTON);
		stove.placeFood(null, mutton, stove.getCookingRecipe(mutton).orElseThrow());

		helper.setBlock(new BlockPos(2, 1, 0), ModBlocks.SKILLET.get());
		helper.getBlockEntity(new BlockPos(2, 1, 0), SkilletBlockEntity.class).addItemToCook(new ItemStack(Items.PORKCHOP, 3), player, helper.getLevel());

		helper.setBlock(new BlockPos(4, 1, 0), ModBlocks.CUTTING_BOARD.get());
		helper.getBlockEntity(new BlockPos(4, 1, 0), CuttingBoardBlockEntity.class).addItem(new ItemStack(ModItems.CABBAGE.get()));

		helper.setBlock(new BlockPos(0, 1, 2), ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity pot = helper.getBlockEntity(new BlockPos(0, 1, 2), CookingPotBlockEntity.class);
		pot.getInventory().set(0, ItemResource.of(ModItems.TOMATO.get()), 5);
		pot.getInventory().set(CookingPotBlockEntity.MEAL_DISPLAY_SLOT, ItemResource.of(ModItems.TOMATO_SAUCE.get()), 3);
		pot.getInventory().set(CookingPotBlockEntity.CONTAINER_SLOT, ItemResource.of(Items.BOWL), 2);

		for (BlockPos pos : List.of(new BlockPos(0, 1, 0), new BlockPos(2, 1, 0), new BlockPos(4, 1, 0), new BlockPos(0, 1, 2))) {
			BlockEntity server = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
			BlockEntity client = clientCopy(helper, server, server.getUpdateTag(registries));
			FDConservationTests.Ledger expected = FDConservationTests.contents(server);
			FDConservationTests.Ledger synced = FDConservationTests.contents(client);
			if (!expected.equals(synced)) {
				problems.add(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(server.getType()) + ": server " + expected + " but client got " + synced);
			}
		}
		helper.assertTrue(!FDConservationTests.contents(stove).counts.isEmpty(), "Stove test setup failed");

		// A renamed cooking pot keeps its name on the client
		CompoundTag named = pot.saveWithFullMetadata(registries);
		named.put("CustomName", StringTag.valueOf("Fancy Pot"));
		CookingPotBlockEntity renamed = (CookingPotBlockEntity) BlockEntity.loadStatic(pot.getBlockPos(), pot.getBlockState(), named, registries);
		CookingPotBlockEntity renamedClient = (CookingPotBlockEntity) clientCopy(helper, renamed, renamed.getUpdateTag(registries));
		if (renamedClient.getCustomName() == null || !renamedClient.getCustomName().getString().equals("Fancy Pot")) {
			problems.add("cooking pot: custom name lost on the client (" + renamedClient.getCustomName() + ")");
		}

		if (!problems.isEmpty()) {
			helper.fail(String.join("; ", problems));
		}
		helper.succeed();
	}

	/** A fresh block entity of the same type that received the given update tag, like the client does. */
	private static BlockEntity clientCopy(GameTestHelper helper, BlockEntity server, CompoundTag updateTag) {
		BlockState state = server.getBlockState();
		BlockEntity client = ((EntityBlock) state.getBlock()).newBlockEntity(server.getBlockPos(), state);
		helper.assertTrue(client != null, "No block entity for " + state);
		client.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), updateTag));
		return client;
	}

	// ------------------------------------------------------------------ client-side decisions

	/**
	 * The client has no recipes: the skillet and the stove decide with the synced campfire input set instead. That is
	 * only right if the set accepts exactly the items that have a campfire recipe, so client and server agree.
	 */
	private static void campfireInputSetMatchesRecipes(GameTestHelper helper) {
		RecipePropertySet campfireInput = helper.getLevel().recipeAccess().propertySet(RecipePropertySet.CAMPFIRE_INPUT);
		List<String> mismatches = new ArrayList<>();
		int cookable = 0;
		for (Item item : BuiltInRegistries.ITEM) {
			ItemStack stack = new ItemStack(item);
			if (stack.isEmpty()) continue;
			boolean inSet = campfireInput.test(stack);
			boolean hasRecipe = SkilletItem.getCookingRecipe(stack, helper.getLevel()).isPresent();
			if (hasRecipe) cookable++;
			if (inSet != hasRecipe) {
				mismatches.add(BuiltInRegistries.ITEM.getKey(item) + " (set " + inSet + ", recipe " + hasRecipe + ")");
			}
		}
		helper.assertTrue(cookable > 5, "Expected campfire recipes, found " + cookable);
		if (!mismatches.isEmpty()) {
			helper.fail("Client and server would disagree on " + mismatches.size() + " items: " + String.join(", ", mismatches.subList(0, Math.min(10, mismatches.size()))));
		}
		helper.succeed();
	}

	/** Skillet in the main hand, raw food in the other hand, next to a stove: one piece goes into the skillet. */
	private static void skilletInHandCooks(GameTestHelper helper) {
		BlockPos stove = new BlockPos(2, 1, 2);
		helper.setBlock(stove, litStove());
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(stove.east())));
		ItemStack skillet = new ItemStack(ModItems.SKILLET.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, skillet);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BEEF, 3));

		InteractionResult result = skillet.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(result.consumesAction(), "Skillet use with raw beef should consume the click, was " + result);
		ItemStack inSkillet = player.getItemInHand(InteractionHand.MAIN_HAND).getOrDefault(ModDataComponents.SKILLET_INGREDIENT, vectorwing.farmersdelight.common.item.component.ItemStackWrapper.EMPTY).getStack();
		helper.assertTrue(inSkillet.is(Items.BEEF) && inSkillet.getCount() == 1, "Skillet should hold one beef, holds " + inSkillet);
		helper.assertValueEqual(player.getItemInHand(InteractionHand.OFF_HAND).getCount(), 2, "beef left in the off hand");

		// Something that can't be cooked is not taken
		Player other = helper.makeMockPlayer(GameType.SURVIVAL);
		other.setPos(Vec3.atBottomCenterOf(helper.absolutePos(stove.east())));
		ItemStack otherSkillet = new ItemStack(ModItems.SKILLET.get());
		other.setItemInHand(InteractionHand.MAIN_HAND, otherSkillet);
		other.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.DIRT, 3));
		InteractionResult dirt = otherSkillet.use(helper.getLevel(), other, InteractionHand.MAIN_HAND);
		helper.assertFalse(dirt.consumesAction(), "Dirt can't be cooked, the click must pass");
		helper.assertValueEqual(other.getItemInHand(InteractionHand.OFF_HAND).getCount(), 3, "dirt left in the off hand");
		helper.succeed();
	}

	// ------------------------------------------------------------------ cooking pot menu

	private static RecipeHolder<?> recipe(GameTestHelper helper, String path) {
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, FarmersDelight.id(path));
		return helper.getLevel().recipeAccess().byKey(key).orElseThrow(() -> new IllegalStateException("Missing recipe " + path));
	}

	private static int count(Player player, Item item) {
		int total = 0;
		for (ItemStack stack : player.getInventory()) {
			if (stack.is(item)) total += stack.getCount();
		}
		return total;
	}

	/** Clicking a recipe in the cooking pot's recipe book moves the ingredients from the player into the pot (it did nothing). */
	private static void recipeBookPlacesIngredients(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity pot = helper.getBlockEntity(pos, CookingPotBlockEntity.class);
		Player player = helper.makeMockServerPlayer(GameType.SURVIVAL);
		player.getInventory().add(new ItemStack(ModItems.TOMATO.get(), 5));
		CookingPotMenu menu = new CookingPotMenu(1, player.getInventory(), pot, new SimpleContainerData(4));

		menu.handlePlacement(false, true, recipe(helper, "cooking/tomato_sauce"), helper.getLevel(), player.getInventory());
		int inPot = 0;
		int filledSlots = 0;
		for (int i = 0; i < CookingPotMenu.INDEX_MEAL; i++) {
			if (pot.getInventory().getResource(i).is(ModItems.TOMATO.get())) {
				inPot += pot.getInventory().getAmountAsInt(i);
				filledSlots++;
			}
		}
		helper.assertValueEqual(inPot, 2, "tomatoes placed into the pot");
		helper.assertValueEqual(filledSlots, 2, "ingredient slots used");
		helper.assertValueEqual(count(player, ModItems.TOMATO.get()), 3, "tomatoes left in the inventory");

		// Placing the maximum moves the rest, and nothing is created or lost
		menu.handlePlacement(true, true, recipe(helper, "cooking/tomato_sauce"), helper.getLevel(), player.getInventory());
		int total = count(player, ModItems.TOMATO.get());
		for (int i = 0; i < CookingPotMenu.INDEX_MEAL; i++) {
			if (pot.getInventory().getResource(i).is(ModItems.TOMATO.get())) total += pot.getInventory().getAmountAsInt(i);
		}
		helper.assertValueEqual(total, 5, "tomatoes in total after placing the maximum");
		helper.succeed();
	}

	/** Shift-clicking meals out of the pot counts them as crafted (the stat was always 0). */
	private static void shiftClickMealAwardsCraftedStat(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity pot = helper.getBlockEntity(pos, CookingPotBlockEntity.class);
		pot.getInventory().set(CookingPotBlockEntity.OUTPUT_SLOT, ItemResource.of(ModItems.TOMATO_SAUCE.get()), 3);
		ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
		CookingPotMenu menu = new CookingPotMenu(1, player.getInventory(), pot, new SimpleContainerData(4));
		int before = player.getStats().getValue(Stats.ITEM_CRAFTED.get(ModItems.TOMATO_SAUCE.get()));

		menu.quickMoveStack(player, CookingPotMenu.INDEX_OUTPUT);

		helper.assertValueEqual(count(player, ModItems.TOMATO_SAUCE.get()), 3, "sauces moved to the inventory");
		helper.assertTrue(pot.getInventory().getResource(CookingPotBlockEntity.OUTPUT_SLOT).isEmpty(), "Output slot should be empty");
		helper.assertValueEqual(player.getStats().getValue(Stats.ITEM_CRAFTED.get(ModItems.TOMATO_SAUCE.get())) - before, 3, "sauces counted as crafted");
		helper.succeed();
	}

	// ------------------------------------------------------------------ cutting board

	/** Carving a tool onto the board takes it from the player: it used to report failure, so the player kept a copy. */
	private static void carvingToolIsNotDuplicated(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.CUTTING_BOARD.get());
		CuttingBoardBlockEntity board = helper.getBlockEntity(pos, CuttingBoardBlockEntity.class);
		ItemStack axe = new ItemStack(Items.DIAMOND_AXE);
		helper.assertTrue(board.carveToolOnBoard(axe), "Carving a diamond axe onto an empty board should succeed");
		helper.assertTrue(board.isItemCarvingBoard(), "Board should be carving");
		helper.assertTrue(board.getStoredItem().is(Items.DIAMOND_AXE), "Axe should be on the board");
		helper.succeed();
	}

	/** A hopper pulling the carved tool off the board must leave a usable board (it stayed locked for good). */
	private static void hopperUnlocksCarvingBoard(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.CUTTING_BOARD.get());
		CuttingBoardBlockEntity board = helper.getBlockEntity(pos, CuttingBoardBlockEntity.class);
		helper.assertTrue(board.carveToolOnBoard(new ItemStack(Items.IRON_AXE)), "Carving should succeed");
		var below = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), Direction.DOWN);
		try (Transaction tx = Transaction.openRoot()) {
			helper.assertValueEqual(below.extract(ItemResource.of(Items.IRON_AXE), 1, tx), 1, "axes pulled by automation");
			tx.commit();
		}
		helper.assertFalse(board.isItemCarvingBoard(), "An empty board must not stay in carving mode");
		helper.assertTrue(board.addItem(new ItemStack(ModItems.CABBAGE.get())).isEmpty(), "The board must accept items again");
		helper.succeed();
	}

	/** Boards saved while stuck (carving, but empty) load as normal boards. */
	private static void stuckCarvingBoardRepairsOnLoad(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.CUTTING_BOARD.get());
		CuttingBoardBlockEntity board = helper.getBlockEntity(pos, CuttingBoardBlockEntity.class);
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		CompoundTag tag = board.saveWithFullMetadata(registries);
		tag.putBoolean("IsItemCarved", true);
		CuttingBoardBlockEntity loaded = (CuttingBoardBlockEntity) BlockEntity.loadStatic(board.getBlockPos(), board.getBlockState(), tag, registries);
		helper.assertFalse(loaded.isItemCarvingBoard(), "An empty board loaded as carving");
		helper.succeed();
	}

	// ------------------------------------------------------------------ save data

	/**
	 * One stack that can't be read (an item from a removed mod) must not wipe the whole inventory, and a saved list
	 * with too many entries must not resize it.
	 */
	private static void unreadableItemKeepsOtherSlots(GameTestHelper helper) {
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		BlockPos potPos = new BlockPos(1, 1, 1);
		helper.setBlock(potPos, ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity pot = helper.getBlockEntity(potPos, CookingPotBlockEntity.class);
		pot.getInventory().set(0, ItemResource.of(ModItems.TOMATO.get()), 5);
		pot.getInventory().set(1, ItemResource.of(ModItems.ONION.get()), 3);
		CompoundTag tag = pot.saveWithFullMetadata(registries);
		ListTag stacks = tag.getCompoundOrEmpty("Inventory").getListOrEmpty("stacks");
		helper.assertTrue(stacks.size() == CookingPotBlockEntity.INVENTORY_SIZE, "Unexpected save layout: " + tag);
		stacks.getCompoundOrEmpty(0).putString("id", "removedmod:ghost_tomato");
		CompoundTag extra = new CompoundTag();
		extra.putString("id", "minecraft:diamond");
		extra.putInt("count", 1);
		stacks.add(extra);
		CookingPotBlockEntity loaded = (CookingPotBlockEntity) BlockEntity.loadStatic(pot.getBlockPos(), pot.getBlockState(), tag, registries);
		helper.assertValueEqual(loaded.getInventory().size(), CookingPotBlockEntity.INVENTORY_SIZE, "pot slots after loading");
		helper.assertTrue(loaded.getInventory().getResource(0).isEmpty(), "The unreadable slot should be empty");
		helper.assertTrue(loaded.getInventory().getResource(1).is(ModItems.ONION.get()) && loaded.getInventory().getAmountAsInt(1) == 3,
				"The onions must survive a broken neighbouring slot, slot 1: " + loaded.getInventory().getResource(1));

		BlockPos stovePos = new BlockPos(3, 1, 1);
		helper.setBlock(stovePos, ModBlocks.STOVE.get().defaultBlockState().setValue(AbstractStoveBlock.LIT, false));
		AbstractStoveBlockEntity stove = helper.getBlockEntity(stovePos, AbstractStoveBlockEntity.class);
		for (Item food : List.of(Items.BEEF, Items.MUTTON)) {
			ItemStack stack = new ItemStack(food);
			stove.placeFood(null, stack, stove.getCookingRecipe(stack).orElseThrow());
		}
		CompoundTag stoveTag = stove.saveWithFullMetadata(registries);
		stoveTag.getListOrEmpty("stacks").getCompoundOrEmpty(0).putString("id", "removedmod:ghost_beef");
		AbstractStoveBlockEntity loadedStove = (AbstractStoveBlockEntity) BlockEntity.loadStatic(stove.getBlockPos(), stove.getBlockState(), stoveTag, registries);
		helper.assertTrue(loadedStove.getItems().getResource(1).is(Items.MUTTON), "The mutton must survive a broken neighbouring slot");
		helper.succeed();
	}

	// ------------------------------------------------------------------ shared stacks

	/** Taking a serving from one cooking pot item must not change a copy of it (copies share component values). */
	private static void servingFromPotItemLeavesCopiesAlone(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity pot = helper.getBlockEntity(pos, CookingPotBlockEntity.class);
		pot.getInventory().set(CookingPotBlockEntity.MEAL_DISPLAY_SLOT, ItemResource.of(ModItems.TOMATO_SAUCE.get()), 4);
		ItemStack original = pot.getAsItem();
		ItemStack copy = original.copy();
		CookingPotBlockEntity.takeServingFromItem(copy);
		helper.assertValueEqual(CookingPotBlockEntity.getMealFromItem(copy).getCount(), 3, "servings left in the copy");
		helper.assertValueEqual(CookingPotBlockEntity.getMealFromItem(original).getCount(), 4, "servings left in the original");
		CookingPotBlockEntity.getMealFromItem(original).shrink(4);
		helper.assertValueEqual(CookingPotBlockEntity.getMealFromItem(original).getCount(), 4, "servings after changing a returned stack");

		helper.setBlock(pos.east(), ModBlocks.SKILLET.get());
		SkilletBlockEntity skillet = helper.getBlockEntity(pos.east(), SkilletBlockEntity.class);
		skillet.getSkilletAsItem().setCount(0);
		helper.assertFalse(skillet.getSkilletAsItem().isEmpty(), "Changing the returned skillet item must not change the placed skillet");
		helper.succeed();
	}

	/** Clearable#clearContent (used by /setblock and structures) really empties the stove, so replacing it drops nothing. */
	private static void clearContentEmptiesStove(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 1, 2);
		helper.setBlock(pos, litStove());
		AbstractStoveBlockEntity stove = helper.getBlockEntity(pos, AbstractStoveBlockEntity.class);
		ItemStack beef = new ItemStack(Items.BEEF);
		stove.placeFood(null, beef, stove.getCookingRecipe(beef).orElseThrow());
		stove.clearContent();
		helper.assertTrue(stove.isEmpty(), "Stove should be empty after clearContent");
		helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
		List<ItemEntity> beefDrops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(pos)).inflate(2), entity -> entity.getItem().is(Items.BEEF));
		helper.assertTrue(beefDrops.isEmpty(), "Cleared food must not drop");
		helper.succeed();
	}

	// ------------------------------------------------------------------ data

	/** Every recipe id of Farmer's Delight 1.21 still exists, so packs and scripts that refer to them keep working. */
	private static void upstreamRecipeIdsExist(GameTestHelper helper) {
		List<String> missing = new ArrayList<>();
		int checked = 0;
		try (InputStream in = FDRegressionTests.class.getResourceAsStream("/farmersdelight_gametest/upstream_1_21_recipe_ids.txt")) {
			helper.assertTrue(in != null, "Recipe id list is missing from the jar");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.strip();
				if (line.isEmpty() || line.startsWith("#")) continue;
				checked++;
				ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, FarmersDelight.id(line));
				if (helper.getLevel().recipeAccess().byKey(key).isEmpty()) {
					missing.add(line);
				}
			}
		} catch (java.io.IOException e) {
			helper.fail("Could not read the recipe id list: " + e);
		}
		helper.assertTrue(checked > 300, "Only " + checked + " recipe ids checked");
		if (!missing.isEmpty()) {
			helper.fail(missing.size() + " recipe ids from Farmer's Delight 1.21 are missing: " + String.join(", ", missing));
		}
		helper.succeed();
	}
}
