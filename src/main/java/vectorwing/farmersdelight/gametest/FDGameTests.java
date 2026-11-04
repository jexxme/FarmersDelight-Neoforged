package vectorwing.farmersdelight.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.transfer.item.ItemResource;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.AbstractStoveBlock;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.function.Consumer;

/**
 * Automated in-world tests for the core gameplay, run with {@code ./gradlew runGameTestServer}.
 * They are only registered when NeoForge's game test framework is enabled (dev runs), not in normal play.
 */
public final class FDGameTests
{
	private static final Identifier EMPTY_STRUCTURE = FarmersDelight.id("gametest/empty_5x5");
	private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, FarmersDelight.MODID);

	private static final ResourceKey<Consumer<GameTestHelper>> COOKING_POT_COOKS_MEAL = function("cooking_pot_cooks_meal", FDGameTests::cookingPotCooksMeal);
	private static final ResourceKey<Consumer<GameTestHelper>> STOVE_COOKS_FOOD = function("stove_cooks_food", FDGameTests::stoveCooksFood);
	private static final ResourceKey<Consumer<GameTestHelper>> SKILLET_COOKS_FOOD = function("skillet_cooks_food", FDGameTests::skilletCooksFood);
	private static final ResourceKey<Consumer<GameTestHelper>> CUTTING_BOARD_CUTS = function("cutting_board_cuts", FDGameTests::cuttingBoardCuts);
	private static final ResourceKey<Consumer<GameTestHelper>> ROPE_STAYS_AFTER_VINE_REMOVAL = function("rope_stays_after_vine_removal", FDGameTests::ropeStaysAfterVineRemoval);
	private static final ResourceKey<Consumer<GameTestHelper>> ITEM_COMPONENTS_AND_DATA = function("item_components_and_data", FDGameTests::itemComponentsAndData);
	private static final ResourceKey<Consumer<GameTestHelper>> COOKING_POT_SERVES_INTO_BOWL = function("cooking_pot_serves_into_bowl", FDGameTests::cookingPotServesIntoBowl);
	private static final ResourceKey<Consumer<GameTestHelper>> FEAST_SERVING = function("feast_serving", FDGameTests::feastServing);
	private static final ResourceKey<Consumer<GameTestHelper>> KNIFE_HARVESTS_STRAW = function("knife_harvests_straw", FDGameTests::knifeHarvestsStraw);
	private static final ResourceKey<Consumer<GameTestHelper>> KNIFE_SLICES_CAKE = function("knife_slices_cake", FDGameTests::knifeSlicesCake);

	private FDGameTests() {
	}

	public static void register(IEventBus modEventBus) {
		FUNCTIONS.register(modEventBus);
		modEventBus.addListener(FDGameTests::registerTests);
	}

	private static ResourceKey<Consumer<GameTestHelper>> function(String name, Consumer<GameTestHelper> test) {
		FUNCTIONS.register(name, () -> test);
		return ResourceKey.create(Registries.TEST_FUNCTION, FarmersDelight.id(name));
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(FarmersDelight.id("default"));
		register(event, environment, COOKING_POT_COOKS_MEAL, 400);
		register(event, environment, STOVE_COOKS_FOOD, 1300);
		register(event, environment, SKILLET_COOKS_FOOD, 1300);
		register(event, environment, CUTTING_BOARD_CUTS, 40);
		register(event, environment, ROPE_STAYS_AFTER_VINE_REMOVAL, 40);
		register(event, environment, ITEM_COMPONENTS_AND_DATA, 20);
		register(event, environment, COOKING_POT_SERVES_INTO_BOWL, 400);
		register(event, environment, FEAST_SERVING, 20);
		register(event, environment, KNIFE_HARVESTS_STRAW, 20);
		register(event, environment, KNIFE_SLICES_CAKE, 20);
	}

	private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, ResourceKey<Consumer<GameTestHelper>> function, int maxTicks) {
		event.registerTest(function.identifier(), new FunctionGameTestInstance(function, new TestData<>(environment, EMPTY_STRUCTURE, maxTicks, 0, true)));
	}

	private static BlockState litStove() {
		return ModBlocks.STOVE.get().defaultBlockState().setValue(AbstractStoveBlock.LIT, true);
	}

	/** Two tomatoes on a lit stove become Tomato Sauce. */
	private static void cookingPotCooksMeal(GameTestHelper helper) {
		BlockPos stove = new BlockPos(2, 1, 2);
		BlockPos pot = stove.above();
		helper.setBlock(stove, litStove());
		helper.setBlock(pot, ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity cookingPot = helper.getBlockEntity(pot, CookingPotBlockEntity.class);
		cookingPot.getInventory().set(0, ItemResource.of(ModItems.TOMATO.get()), 1);
		cookingPot.getInventory().set(1, ItemResource.of(ModItems.TOMATO.get()), 1);
		cookingPot.setChanged();
		helper.assertTrue(cookingPot.isHeated(), "Cooking pot on a lit stove should be heated");
		helper.succeedWhen(() -> helper.assertTrue(cookingPot.getMeal().is(ModItems.TOMATO_SAUCE.get()),
				"Expected Tomato Sauce in the meal slot, got " + cookingPot.getMeal()));
	}

	/** Raw beef placed on a lit stove pops out as Cooked Beef. */
	private static void stoveCooksFood(GameTestHelper helper) {
		BlockPos stovePos = new BlockPos(2, 1, 2);
		helper.setBlock(stovePos, litStove());
		StoveBlockEntity stove = helper.getBlockEntity(stovePos, StoveBlockEntity.class);
		ItemStack beef = new ItemStack(Items.BEEF);
		var recipe = stove.getCookingRecipe(beef);
		helper.assertTrue(recipe.isPresent(), "Raw beef should have a stove (campfire) recipe");
		helper.assertTrue(stove.placeFood(null, beef, recipe.get()), "Stove should accept raw beef");
		helper.succeedWhen(() -> {
			helper.assertItemEntityCountIs(Items.COOKED_BEEF, stovePos.above(), 3.0, 1);
			helper.assertTrue(stove.isEmpty(), "The beef must leave the stove once cooked");
		});
	}

	/** A skillet on a lit stove cooks raw beef. */
	private static void skilletCooksFood(GameTestHelper helper) {
		BlockPos stovePos = new BlockPos(2, 1, 2);
		BlockPos skilletPos = stovePos.above();
		helper.setBlock(stovePos, litStove());
		helper.setBlock(skilletPos, ModBlocks.SKILLET.get());
		SkilletBlockEntity skillet = helper.getBlockEntity(skilletPos, SkilletBlockEntity.class);
		helper.assertTrue(skillet.isHeated(), "Skillet on a lit stove should be heated");
		skillet.addItemToCook(new ItemStack(Items.BEEF), helper.makeMockPlayer(GameType.SURVIVAL), helper.getLevel());
		helper.succeedWhen(() -> helper.assertTrue(
				skillet.getStoredStack().is(Items.COOKED_BEEF) || isItemNear(helper, Items.COOKED_BEEF, skilletPos),
				"Expected Cooked Beef from the skillet, stored: " + skillet.getStoredStack()));
	}

	/** A cabbage cut with an iron knife gives Cabbage Leaves. */
	private static void cuttingBoardCuts(GameTestHelper helper) {
		BlockPos boardPos = new BlockPos(2, 1, 2);
		helper.setBlock(boardPos, ModBlocks.CUTTING_BOARD.get());
		CuttingBoardBlockEntity board = helper.getBlockEntity(boardPos, CuttingBoardBlockEntity.class);
		board.addItem(new ItemStack(ModItems.CABBAGE.get()));
		helper.assertTrue(board.processStoredItemUsingTool(new ItemStack(ModItems.IRON_KNIFE.get()), helper.makeMockPlayer(GameType.SURVIVAL)),
				"Cutting a cabbage with an iron knife should succeed");
		helper.succeedWhen(() -> helper.assertItemEntityPresent(ModItems.CABBAGE_LEAF.get(), boardPos, 3.0));
	}

	/** With rope permanence enabled (default), removing a tomato vine leaves the rope behind. */
	private static void ropeStaysAfterVineRemoval(GameTestHelper helper) {
		BlockPos top = new BlockPos(2, 3, 2);
		BlockPos vine = top.below();
		helper.setBlock(vine.below(), Blocks.DIRT);
		helper.setBlock(top, ModBlocks.ROPE.get());
		helper.setBlock(vine, ModBlocks.TOMATO_CROP_ON_ROPE.get());
		// A real break by a player, not just replacing the block
		helper.getLevel().destroyBlock(helper.absolutePos(vine), true, helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL));
		helper.succeedWhen(() -> {
			helper.assertBlockPresent(ModBlocks.ROPE.get(), vine);
			// The rope stays as a block, so it must not also drop as an item (that would be a rope dupe)
			helper.assertItemEntityNotPresent(ModItems.ROPE.get(), vine, 2.0);
		});
	}

	/** Fuel and compost come from NeoForge data maps, villager food from Villager.FOOD_POINTS; check they are applied. */
	private static void itemComponentsAndData(GameTestHelper helper) {
		helper.assertTrue(BuiltInRegistries.ITEM.wrapAsHolder(ModItems.CANVAS.get()).getData(NeoForgeDataMaps.FURNACE_FUELS) != null, "Canvas should be a furnace fuel");
		helper.assertTrue(BuiltInRegistries.ITEM.wrapAsHolder(ModItems.CABBAGE.get()).getData(NeoForgeDataMaps.COMPOSTABLES) != null, "Cabbage should be compostable");
		helper.assertTrue(Villager.FOOD_POINTS.containsKey(ModItems.CABBAGE.get()), "Cabbage should be villager food");
		Compostable seeds = BuiltInRegistries.ITEM.wrapAsHolder(ModItems.CABBAGE_SEEDS.get()).getData(NeoForgeDataMaps.COMPOSTABLES);
		helper.assertTrue(seeds != null && seeds.canVillagerCompost(), "Cabbage seeds should be villager compostable");
		ResourceKey<Recipe<?>> tomatoSauce = ResourceKey.create(Registries.RECIPE, FarmersDelight.id("cooking/tomato_sauce"));
		helper.assertTrue(helper.getLevel().recipeAccess().byKey(tomatoSauce).isPresent(), "Tomato Sauce recipe should be loaded");
		helper.succeed();
	}

	/** With a bowl in the container slot, the finished meal ends up in the output slot. */
	private static void cookingPotServesIntoBowl(GameTestHelper helper) {
		BlockPos stove = new BlockPos(2, 1, 2);
		BlockPos pot = stove.above();
		helper.setBlock(stove, litStove());
		helper.setBlock(pot, ModBlocks.COOKING_POT.get());
		CookingPotBlockEntity cookingPot = helper.getBlockEntity(pot, CookingPotBlockEntity.class);
		cookingPot.getInventory().set(0, ItemResource.of(ModItems.TOMATO.get()), 1);
		cookingPot.getInventory().set(1, ItemResource.of(ModItems.TOMATO.get()), 1);
		cookingPot.getInventory().set(CookingPotBlockEntity.CONTAINER_SLOT, ItemResource.of(Items.BOWL), 1);
		cookingPot.setChanged();
		helper.succeedWhen(() -> helper.assertTrue(
				cookingPot.getInventory().getResource(CookingPotBlockEntity.OUTPUT_SLOT).is(ModItems.TOMATO_SAUCE.get()),
				"Expected Tomato Sauce in the output slot"));
	}

	/** Using a bowl on a Roast Chicken feast gives a serving and removes one portion. */
	private static void feastServing(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 2, 2);
		helper.setBlock(pos.below(), Blocks.STONE);
		helper.setBlock(pos, ModBlocks.ROAST_CHICKEN_BLOCK.get());
		BlockPos absolute = helper.absolutePos(pos);
		net.minecraft.world.entity.player.Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOWL));
		BlockState state = helper.getLevel().getBlockState(absolute);
		vectorwing.farmersdelight.common.block.FeastBlock feast = (vectorwing.farmersdelight.common.block.FeastBlock) state.getBlock();
		int servingsBefore = state.getValue(feast.getServingsProperty());
		feast.useItemOn(player.getMainHandItem(), state, helper.getLevel(), absolute, player, net.minecraft.world.InteractionHand.MAIN_HAND,
				new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(absolute), net.minecraft.core.Direction.UP, absolute, false));
		helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.ROAST_CHICKEN.get())), "Player should have received a Roast Chicken serving");
		helper.assertValueEqual(helper.getLevel().getBlockState(absolute).getValue(feast.getServingsProperty()), servingsBefore - 1, "servings");
		helper.succeed();
	}

	/** Mature wheat broken with a knife also drops Straw (global loot modifier). */
	private static void knifeHarvestsStraw(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 2, 2);
		helper.setBlock(pos.below(), Blocks.FARMLAND);
		BlockState wheat = Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7);
		helper.setBlock(pos, wheat);
		java.util.List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(wheat, helper.getLevel(), helper.absolutePos(pos), null,
				helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.IRON_KNIFE.get()));
		helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(ModItems.STRAW.get())), "Expected Straw in the drops, got " + drops);
		helper.succeed();
	}

	/** A cake broken with a knife drops Cake Slices (global loot modifier). */
	private static void knifeSlicesCake(GameTestHelper helper) {
		BlockPos pos = new BlockPos(2, 2, 2);
		helper.setBlock(pos.below(), Blocks.STONE);
		helper.setBlock(pos, Blocks.CAKE);
		java.util.List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(Blocks.CAKE.defaultBlockState(), helper.getLevel(), helper.absolutePos(pos), null,
				helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.IRON_KNIFE.get()));
		helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(ModItems.CAKE_SLICE.get())), "Expected Cake Slices in the drops, got " + drops);
		helper.succeed();
	}

	private static boolean isItemNear(GameTestHelper helper, net.minecraft.world.item.Item item, BlockPos pos) {
		try {
			helper.assertItemEntityPresent(item, pos, 3.0);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}
}
