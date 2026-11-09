package vectorwing.farmersdelight.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.TomatoBlock;
import vectorwing.farmersdelight.common.block.TomatoOnSupportBlock;
import vectorwing.farmersdelight.common.block.entity.TomatoSupportBlockEntity;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tomatoes climb {@code #farmersdelight:tomato_supports}: ropes as before, any other block (another mod's trellis) is
 * kept inside the vine and put back however the vine goes.
 * <p>
 * The GameTest server loads test-only data (src/gametest/resources/gametest_data, never in the jar) that puts
 * {@code minecraft:iron_chain} into the tag as a stand-in for a trellis: a thin block with a horizontal axis.
 */
public final class FDTomatoSupportTests
{
	private static final Identifier STRUCTURE = FarmersDelight.id("gametest/empty_5x5");
	/** The test-only data pack, a folder in the GameTest server's view of the mod (see build.gradle). */
	private static final String TEST_PACK = "gametest_data";
	private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, FarmersDelight.MODID);
	private static final List<ResourceKey<Consumer<GameTestHelper>>> TESTS = new ArrayList<>();

	static {
		test("tomato_climbs_support_and_keeps_it", FDTomatoSupportTests::climbsSupportAndKeepsIt);
		test("support_column_grows_as_high_as_a_rope", FDTomatoSupportTests::supportColumnGrowsAsHighAsARope);
		test("player_break_leaves_support", FDTomatoSupportTests::playerBreakLeavesSupport);
		test("explosion_leaves_support", FDTomatoSupportTests::explosionLeavesSupport);
		test("fire_leaves_support", FDTomatoSupportTests::fireLeavesSupport);
		test("no_plant_below_leaves_support", FDTomatoSupportTests::noPlantBelowLeavesSupport);
		test("rope_still_climbs_as_before", FDTomatoSupportTests::ropeStillClimbsAsBefore);
	}

	private FDTomatoSupportTests() {
	}

	public static void register(IEventBus modEventBus) {
		FUNCTIONS.register(modEventBus);
		modEventBus.addListener(FDTomatoSupportTests::registerTests);
		modEventBus.addListener(FDTomatoSupportTests::addTestPack);
	}

	private static void test(String name, Consumer<GameTestHelper> function) {
		FUNCTIONS.register(name, () -> function);
		TESTS.add(ResourceKey.create(Registries.TEST_FUNCTION, FarmersDelight.id(name)));
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(FarmersDelight.id("tomato_supports"));
		for (ResourceKey<Consumer<GameTestHelper>> function : TESTS) {
			event.registerTest(function.identifier(), new FunctionGameTestInstance(function, new TestData<>(environment, STRUCTURE, 100, 0, true)));
		}
	}

	/** The stand-in support data, only where the GameTest server's view of the mod has it (never in other runs or the jar). */
	private static void addTestPack(AddPackFindersEvent event) {
		if (event.getPackType() == PackType.SERVER_DATA && FDTomatoSupportTests.class.getResource("/" + TEST_PACK + "/pack.mcmeta") != null) {
			event.addPackFinders(FarmersDelight.id(TEST_PACK), PackType.SERVER_DATA, Component.literal("Farmer's Delight GameTest data"),
					PackSource.BUILT_IN, true, Pack.Position.TOP);
		}
	}

	// ------------------------------------------------------------------ setup

	/** The stand-in trellis: an iron chain lying along X (not its default axis, so a lost state would show). */
	private static BlockState standIn(GameTestHelper helper) {
		BlockState chain = Blocks.IRON_CHAIN.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X);
		helper.assertTrue(chain.is(ModTags.Blocks.TOMATO_SUPPORTS), "The GameTest data should put minecraft:iron_chain into #farmersdelight:tomato_supports");
		helper.assertTrue(TomatoOnSupportBlock.canHold(chain), "A tomato vine should be able to hold the stand-in support");
		return chain;
	}

	/** Farmland, a ripe tomato vine on it, and {@code support} above; returns the vine's position. */
	private static BlockPos ripeTomatoUnder(GameTestHelper helper, BlockPos plant, BlockState support) {
		helper.setBlock(plant.below(), Blocks.FARMLAND);
		helper.setBlock(plant, ModBlocks.TOMATO_CROP.get().defaultBlockState().setValue(TomatoBlock.VINE_AGE, 3));
		helper.setBlock(plant.above(), support);
		return plant;
	}

	/** The plant at {@code plant} climbs into the block above (the step a random tick or bone meal takes). */
	private static void climb(GameTestHelper helper, BlockPos plant) {
		BlockState state = helper.getBlockState(plant);
		helper.assertTrue(state.getBlock() instanceof TomatoBlock, "Expected a tomato vine at " + plant + ", found " + state);
		((TomatoBlock) state.getBlock()).climbRopeAbove(helper.getLevel(), helper.absolutePos(plant));
	}

	/** A ripe tomato under the stand-in, grown into it; returns the position of the vine on the support. */
	private static BlockPos vineOnSupport(GameTestHelper helper, BlockPos plant, BlockState support) {
		ripeTomatoUnder(helper, plant, support);
		climb(helper, plant);
		BlockPos vine = plant.above();
		helper.assertBlockPresent(ModBlocks.TOMATO_CROP_ON_SUPPORT.get(), vine);
		return vine;
	}

	private static BlockState heldSupport(GameTestHelper helper, BlockPos vine) {
		return helper.getBlockEntity(vine, TomatoSupportBlockEntity.class).getSupport();
	}

	private static int itemsNear(GameTestHelper helper, Item item, BlockPos pos) {
		AABB box = new AABB(helper.absolutePos(pos)).inflate(2.0);
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, box, e -> e.getItem().is(item)).stream().mapToInt(e -> e.getItem().getCount()).sum();
	}

	// ------------------------------------------------------------------ tests

	/**
	 * A ripe tomato climbs the support above it: the vine keeps the support's exact block state, collides like it, and
	 * the support survives saving and reaches the client.
	 */
	private static void climbsSupportAndKeepsIt(GameTestHelper helper) {
		BlockState chain = standIn(helper);
		BlockPos plant = new BlockPos(2, 2, 2);
		ripeTomatoUnder(helper, plant, chain);
		TomatoBlock crop = (TomatoBlock) ModBlocks.TOMATO_CROP.get();
		helper.assertTrue(crop.canClimbBlock(chain), "A ripe tomato should climb the stand-in support");
		climb(helper, plant);
		BlockPos vine = plant.above();
		helper.assertBlockPresent(ModBlocks.TOMATO_CROP_ON_SUPPORT.get(), vine);
		helper.assertValueEqual(heldSupport(helper, vine), chain, "support kept inside the vine");

		ServerLevel level = helper.getLevel();
		BlockPos abs = helper.absolutePos(vine);
		helper.assertValueEqual(helper.getBlockState(vine).getCollisionShape(level, abs, CollisionContext.empty()).bounds(),
				chain.getCollisionShape(level, abs, CollisionContext.empty()).bounds(), "collision follows the support");
		helper.assertFalse(helper.getBlockState(vine).getCollisionShape(level, abs, CollisionContext.empty()).isEmpty(), "the support still has collision");

		// Saved and loaded, and sent to the client (the update tag), the support comes along
		TomatoSupportBlockEntity server = helper.getBlockEntity(vine, TomatoSupportBlockEntity.class);
		TomatoSupportBlockEntity loaded = new TomatoSupportBlockEntity(abs, helper.getBlockState(vine));
		loaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), server.saveWithFullMetadata(level.registryAccess())));
		helper.assertValueEqual(loaded.getSupport(), chain, "support after save and load");
		TomatoSupportBlockEntity client = new TomatoSupportBlockEntity(abs, helper.getBlockState(vine));
		client.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), server.getUpdateTag(level.registryAccess())));
		helper.assertValueEqual(client.getSupport(), chain, "support on the client");

		// A waterlogged support is not climbed (the vine would drain it)
		helper.assertFalse(crop.canClimbBlock(chain.setValue(BlockStateProperties.WATERLOGGED, true)), "A waterlogged support should not be climbed");
		helper.succeed();
	}

	/**
	 * Like on ropes, a vine reaches three blocks above the plant, and no higher, also on a column that mixes a rope and
	 * supports (the rope in the middle).
	 */
	private static void supportColumnGrowsAsHighAsARope(GameTestHelper helper) {
		BlockState chain = standIn(helper);
		BlockPos plant = new BlockPos(2, 1, 2);
		helper.setBlock(plant.below(), Blocks.FARMLAND);
		helper.setBlock(plant, ModBlocks.TOMATO_CROP.get().defaultBlockState().setValue(TomatoBlock.VINE_AGE, 3));
		for (int y = 1; y <= 4; y++) {
			helper.setBlock(plant.above(y), y == 2 ? ModBlocks.ROPE.get().defaultBlockState() : chain);
		}
		for (int y = 0; y <= 3; y++) {
			climb(helper, plant.above(y));
		}
		helper.assertBlockPresent(ModBlocks.TOMATO_CROP_ON_SUPPORT.get(), plant.above(1));
		helper.assertBlockPresent(ModBlocks.TOMATO_CROP_ON_ROPE.get(), plant.above(2));
		helper.assertBlockPresent(ModBlocks.TOMATO_CROP_ON_SUPPORT.get(), plant.above(3));
		helper.assertBlockState(plant.above(4), chain);
		helper.succeed();
	}

	/** A player breaks a ripe vine on a support: the support stays exactly as it was, tomatoes drop, the support doesn't. */
	private static void playerBreakLeavesSupport(GameTestHelper helper) {
		BlockState chain = standIn(helper);
		BlockPos vine = vineOnSupport(helper, new BlockPos(2, 2, 2), chain);
		ServerLevel level = helper.getLevel();
		BlockPos abs = helper.absolutePos(vine);
		level.setBlock(abs, helper.getBlockState(vine).setValue(TomatoBlock.VINE_AGE, 3), Block.UPDATE_CLIENTS);
		helper.assertValueEqual(heldSupport(helper, vine), chain, "support kept while the vine ripens");

		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		helper.assertTrue(player.gameMode.destroyBlock(abs), "The player should be able to break the vine");
		helper.assertBlockState(vine, chain);
		int tomatoes = itemsNear(helper, ModItems.TOMATO.get(), vine);
		helper.assertTrue(tomatoes == 1 || tomatoes == 2, "A ripe vine should drop 1-2 tomatoes, dropped " + tomatoes);
		helper.assertValueEqual(itemsNear(helper, net.minecraft.world.item.Items.IRON_CHAIN, vine), 0, "support items dropped (it stays as a block)");
		helper.succeed();
	}

	/** An explosion takes the vine (and the plant below), the support comes back. */
	private static void explosionLeavesSupport(GameTestHelper helper) {
		BlockState chain = standIn(helper);
		BlockPos vine = vineOnSupport(helper, new BlockPos(2, 2, 2), chain);
		Vec3 boom = Vec3.atCenterOf(helper.absolutePos(vine));
		helper.getLevel().explode(null, boom.x, boom.y, boom.z, 1.0F, Level.ExplosionInteraction.TNT);
		helper.succeedWhen(() -> {
			helper.assertBlockState(vine, chain);
			helper.assertValueEqual(itemsNear(helper, net.minecraft.world.item.Items.IRON_CHAIN, vine), 0, "support items dropped (it stays as a block)");
		});
	}

	/**
	 * Fire: a vine burnt away (removed) and one turned into fire both leave the support (which puts the fire out). The
	 * vine burns like its support: not at all on the chain, like wood on planks.
	 */
	private static void fireLeavesSupport(GameTestHelper helper) {
		BlockState chain = standIn(helper);
		BlockPos burnt = vineOnSupport(helper, new BlockPos(1, 2, 1), chain);
		BlockPos onFire = vineOnSupport(helper, new BlockPos(3, 2, 3), chain);
		ServerLevel level = helper.getLevel();
		helper.assertValueEqual(helper.getBlockState(burnt).getFlammability(level, helper.absolutePos(burnt), Direction.UP), 0, "flammability on a chain");

		// FireBlock#checkBurnOut either removes the block or replaces it with fire
		level.removeBlock(helper.absolutePos(burnt), false);
		level.setBlockAndUpdate(helper.absolutePos(onFire), Blocks.FIRE.defaultBlockState());
		helper.assertBlockState(burnt, chain);
		helper.assertBlockState(onFire, chain);

		// On a wooden support the vine catches fire like the wood would
		BlockPos wooden = new BlockPos(1, 2, 3);
		BlockPos woodenAbs = helper.absolutePos(wooden);
		TomatoOnSupportBlock.place(level, woodenAbs, ModBlocks.TOMATO_CROP_ON_SUPPORT.get().defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState());
		helper.assertValueEqual(helper.getBlockState(wooden).getFlammability(level, woodenAbs, Direction.UP),
				Blocks.OAK_PLANKS.defaultBlockState().getFlammability(level, woodenAbs, Direction.UP), "flammability on planks");
		helper.succeed();
	}

	/** The plant below goes: the vine above can't survive and goes on its next tick, leaving the support. */
	private static void noPlantBelowLeavesSupport(GameTestHelper helper) {
		BlockState chain = standIn(helper);
		BlockPos plant = new BlockPos(2, 2, 2);
		BlockPos vine = vineOnSupport(helper, plant, chain);
		helper.setBlock(plant, Blocks.AIR);
		helper.succeedWhen(() -> helper.assertBlockState(vine, chain));
	}

	/** Ropes are in the tag (through #farmersdelight:ropes) and keep their own vine, as before. */
	private static void ropeStillClimbsAsBefore(GameTestHelper helper) {
		BlockState rope = ModBlocks.ROPE.get().defaultBlockState();
		helper.assertTrue(rope.is(ModTags.Blocks.TOMATO_SUPPORTS), "#farmersdelight:tomato_supports should hold the ropes");
		helper.assertFalse(TomatoOnSupportBlock.canHold(rope), "A rope should not be held like a trellis");
		BlockPos plant = new BlockPos(2, 2, 2);
		ripeTomatoUnder(helper, plant, rope);
		climb(helper, plant);
		helper.assertBlockPresent(ModBlocks.TOMATO_CROP_ON_ROPE.get(), plant.above());
		helper.succeed();
	}
}
