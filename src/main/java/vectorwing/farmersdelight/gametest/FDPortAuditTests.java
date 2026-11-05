package vectorwing.farmersdelight.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tests from the comparison with the other 26.x ports (BuaichiZY, KN_Yeon, Mottle, LexonBlackzz), Farmer's Delight
 * Refabricated and upstream 1.3.4. Each test names the bug it guards and where it was found.
 */
public final class FDPortAuditTests
{
	private static final Identifier STRUCTURE = FarmersDelight.id("gametest/empty_5x5");
	private static final Identifier BIG_STRUCTURE = FarmersDelight.id("gametest/empty_32x6x32");
	private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, FarmersDelight.MODID);
	private static final List<Registration> TESTS = new ArrayList<>();

	/** The blocks 1.21's #minecraft:dirt held. On 26.3 that tag only holds dirt, coarse dirt and rooted dirt. */
	private static final List<Block> OVERWORLD_SUBSTRATES = List.of(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.GRASS_BLOCK,
		Blocks.PODZOL, Blocks.MYCELIUM, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS, Blocks.MOSS_BLOCK, Blocks.PALE_MOSS_BLOCK);

	private record Registration(ResourceKey<Consumer<GameTestHelper>> function, Identifier structure) {
	}

	static {
		test("wild_crops_survive_on_overworld_substrates", BIG_STRUCTURE, FDPortAuditTests::wildCropsSurviveOnOverworldSubstrates);
		test("wild_crop_features_generate_on_grass_and_sand", BIG_STRUCTURE, FDPortAuditTests::wildCropFeaturesGenerate);
		test("rice_plants_on_overworld_substrates", BIG_STRUCTURE, FDPortAuditTests::ricePlantsOnOverworldSubstrates);
		test("pie_slice_feeds_once", STRUCTURE, FDPortAuditTests::pieSliceFeedsOnce);
		test("pistons_do_not_duplicate_food_blocks", BIG_STRUCTURE, FDPortAuditTests::pistonsDoNotDuplicateFoodBlocks);
		test("chest_loot_respects_config", STRUCTURE, FDPortAuditTests::chestLootRespectsConfig);
		test("knife_loot_needs_a_knife", STRUCTURE, FDPortAuditTests::knifeLootNeedsAKnife);
	}

	private FDPortAuditTests() {
	}

	private static void test(String name, Identifier structure, Consumer<GameTestHelper> function) {
		FUNCTIONS.register(name, () -> function);
		TESTS.add(new Registration(ResourceKey.create(Registries.TEST_FUNCTION, FarmersDelight.id(name)), structure));
	}

	public static void register(IEventBus modEventBus) {
		FUNCTIONS.register(modEventBus);
		modEventBus.addListener(FDPortAuditTests::registerTests);
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(FarmersDelight.id("port_audit"));
		for (Registration test : TESTS) {
			event.registerTest(test.function().identifier(), new FunctionGameTestInstance(test.function(), new TestData<>(environment, test.structure(), 200, 0, true)));
		}
	}

	private static void finish(GameTestHelper helper, List<String> problems) {
		if (problems.isEmpty()) {
			helper.succeed();
		} else {
			helper.fail(problems.size() + " problem(s): " + String.join(" | ", problems.subList(0, Math.min(problems.size(), 12))));
		}
	}

	// ------------------------------------------------------------------ 26.3 narrowed #minecraft:dirt (found by BuaichiZY, Refabricated)

	/**
	 * Wild crops grew on any 1.21 #dirt block (grass, podzol, mud, moss...) or sand. On 26.3 #dirt is only dirt,
	 * coarse dirt and rooted dirt, so wild crops on grass broke on the next block update and worldgen dropped them.
	 */
	private static void wildCropsSurviveOnOverworldSubstrates(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		List<Block> wildCrops = List.of(ModBlocks.WILD_CABBAGES.get(), ModBlocks.WILD_ONIONS.get(), ModBlocks.WILD_TOMATOES.get(),
			ModBlocks.WILD_CARROTS.get(), ModBlocks.WILD_POTATOES.get(), ModBlocks.WILD_BEETROOTS.get());
		List<Block> substrates = new ArrayList<>(OVERWORLD_SUBSTRATES);
		substrates.add(Blocks.SAND);
		int x = 0;
		for (Block crop : wildCrops) {
			int z = 0;
			for (Block substrate : substrates) {
				BlockPos soil = new BlockPos(x, 1, z);
				helper.setBlock(soil, substrate);
				BlockPos plant = soil.above();
				if (!crop.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(plant))) {
					problems.add(name(crop) + " can't stay on " + name(substrate));
				}
				z += 1;
			}
			x += 2;
		}
		finish(helper, problems);
	}

	/** Runs every wild crop feature over a grass (or sand) field: each has to place at least one plant. */
	private static void wildCropFeaturesGenerate(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<String> problems = new ArrayList<>();
		record Case(String feature, Block ground, Block plant) {
		}
		List<Case> cases = List.of(
			new Case("wild_carrots", Blocks.GRASS_BLOCK, ModBlocks.WILD_CARROTS.get()),
			new Case("wild_onions", Blocks.GRASS_BLOCK, ModBlocks.WILD_ONIONS.get()),
			new Case("wild_potatoes", Blocks.GRASS_BLOCK, ModBlocks.WILD_POTATOES.get()),
			new Case("wild_tomatoes", Blocks.GRASS_BLOCK, ModBlocks.WILD_TOMATOES.get()),
			new Case("wild_cabbages", Blocks.SAND, ModBlocks.WILD_CABBAGES.get()),
			new Case("wild_beetroots", Blocks.SAND, ModBlocks.WILD_BEETROOTS.get()));
		var features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
		BlockPos center = new BlockPos(16, 1, 16);
		for (Case c : cases) {
			// fresh field: ground at y=1, air above
			for (int dx = 0; dx < 32; dx++) {
				for (int dz = 0; dz < 32; dz++) {
					helper.setBlock(new BlockPos(dx, 1, dz), c.ground());
					for (int y = 2; y < 6; y++) {
						helper.setBlock(new BlockPos(dx, y, dz), Blocks.AIR);
					}
				}
			}
			var holder = features.get(ResourceKey.create(Registries.FEATURE, FarmersDelight.id(c.feature())));
			if (holder.isEmpty()) {
				problems.add("feature " + c.feature() + " missing");
				continue;
			}
			Feature feature = holder.get().value();
			RandomSource random = RandomSource.create(42L);
			for (int attempt = 0; attempt < 8; attempt++) {
				feature.place(level, level.getChunkSource().getGenerator(), random, helper.absolutePos(center.above()));
			}
			int placed = 0;
			for (int dx = 0; dx < 32; dx++) {
				for (int dz = 0; dz < 32; dz++) {
					for (int y = 2; y < 6; y++) {
						if (helper.getBlockState(new BlockPos(dx, y, dz)).is(c.plant())) {
							placed++;
						}
					}
				}
			}
			if (placed == 0) {
				problems.add(c.feature() + " placed nothing on " + name(c.ground()));
			}
		}
		finish(helper, problems);
	}

	/** Rice planted by hand underwater: 1.21 allowed every #dirt block (mud, grass...), 26.3's #dirt doesn't. */
	private static void ricePlantsOnOverworldSubstrates(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		int x = 0;
		for (Block substrate : OVERWORLD_SUBSTRATES) {
			BlockPos soil = new BlockPos(x, 1, 1);
			helper.setBlock(soil, substrate);
			helper.setBlock(soil.above(), Blocks.WATER);
			helper.setBlock(soil.above(2), Blocks.AIR);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.RICE.get(), 4));
			BlockPos abs = helper.absolutePos(soil);
			BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
			ModItems.RICE.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
			if (!helper.getBlockState(soil.above()).is(ModBlocks.RICE_CROP.get())) {
				problems.add("rice can't be planted on " + name(substrate) + " underwater");
			}
			x += 2;
		}
		finish(helper, problems);
	}

	// ------------------------------------------------------------------ pie slices (found by Mottle)

	/** finishUsingItem already feeds through the slice's Consumable; PieBlock fed a second time before that. */
	private static void pieSliceFeedsOnce(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		int x = 0;
		for (Block pie : List.of(ModBlocks.APPLE_PIE.get(), ModBlocks.SWEET_BERRY_CHEESECAKE.get(), ModBlocks.CHOCOLATE_PIE.get(), ModBlocks.PUMPKIN_PIE.get())) {
			BlockPos pos = new BlockPos(x, 1, 1);
			helper.setBlock(pos, pie);
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			player.getFoodData().setFoodLevel(2);
			player.getFoodData().setSaturation(0.0F);
			BlockPos abs = helper.absolutePos(pos);
			BlockState state = helper.getBlockState(pos);
			InteractionResult result = state.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
			ItemStack slice = new ItemStack(((PieBlock) pie).getPieSliceItem().getItem());
			FoodProperties food = slice.get(DataComponents.FOOD);
			int expected = Math.min(20, 2 + (food == null ? 0 : food.nutrition()));
			int actual = player.getFoodData().getFoodLevel();
			if (!result.consumesAction()) {
				problems.add(name(pie) + ": eating a slice did nothing (" + result + ")");
			} else if (actual != expected) {
				problems.add(name(pie) + ": food went 2 -> " + actual + ", one slice should give " + expected);
			}
			x += 2;
		}
		finish(helper, problems);
	}

	// ------------------------------------------------------------------ piston dupe (upstream #1382, fixed in 1.3.4)

	/**
	 * A sticky piston pushes and pulls every feast and pie back and forth, with full floor, floor only at the start and
	 * floor only at the target. Afterwards each lane may hold at most one of the food (as block or dropped item).
	 */
	private static void pistonsDoNotDuplicateFoodBlocks(GameTestHelper helper) {
		List<Block> foods = new ArrayList<>();
		for (Block block : BuiltInRegistries.BLOCK) {
			if ((block instanceof FeastBlock || block instanceof PieBlock) && BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(FarmersDelight.MODID)) {
				foods.add(block);
			}
		}
		record Lane(Block food, int x, int z, int floorMode) {
		}
		List<Lane> lanes = new ArrayList<>();
		int i = 0;
		for (Block food : foods) {
			for (int floorMode = 0; floorMode < 3; floorMode++) {
				int x = (i % 10) * 3;
				int z = (i / 10) * 6;
				lanes.add(new Lane(food, x, z, floorMode));
				i++;
			}
		}
		for (Lane lane : lanes) {
			BlockPos piston = new BlockPos(lane.x(), 2, lane.z());
			helper.setBlock(piston, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.SOUTH));
			helper.setBlock(piston.below(), Blocks.STONE);
			// floor: 0 = under start and target, 1 = only under start, 2 = only under target
			if (lane.floorMode() != 2) helper.setBlock(piston.south().below(), Blocks.STONE);
			if (lane.floorMode() != 1) helper.setBlock(piston.south(2).below(), Blocks.STONE);
			helper.setBlock(piston.south(), lane.food().defaultBlockState());
		}
		helper.startSequence()
			.thenExecuteAfter(5, () -> lanes.forEach(l -> helper.setBlock(new BlockPos(l.x() + 1, 2, l.z()), Blocks.REDSTONE_BLOCK)))
			.thenExecuteAfter(10, () -> lanes.forEach(l -> helper.setBlock(new BlockPos(l.x() + 1, 2, l.z()), Blocks.AIR)))
			.thenExecuteAfter(10, () -> lanes.forEach(l -> helper.setBlock(new BlockPos(l.x() + 1, 2, l.z()), Blocks.REDSTONE_BLOCK)))
			.thenExecuteAfter(10, () -> lanes.forEach(l -> helper.setBlock(new BlockPos(l.x() + 1, 2, l.z()), Blocks.AIR)))
			.thenExecuteAfter(15, () -> {
				List<String> problems = new ArrayList<>();
				for (Lane lane : lanes) {
					int count = 0;
					for (int dz = 0; dz <= 4; dz++) {
						if (helper.getBlockState(new BlockPos(lane.x(), 2, lane.z() + dz)).is(lane.food())) count++;
					}
					Item item = lane.food().asItem();
					AABB box = new AABB(helper.absolutePos(new BlockPos(lane.x(), 0, lane.z()))).inflate(1.2, 3, 0).expandTowards(0, 0, 5);
					for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, box)) {
						if (entity.getItem().is(item)) count += entity.getItem().getCount();
					}
					if (count > 1) {
						problems.add(name(lane.food()) + " x" + count + " (floor mode " + lane.floorMode() + ")");
					}
				}
				helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(40)).forEach(ItemEntity::discard);
				finish(helper, problems);
			});
	}

	// ------------------------------------------------------------------ loot (upstream 1.3.4, BuaichiZY fix2)

	/** "generateFDChestLoot = false" has to remove FD items from chests; the generated modifiers bypassed the check. */
	@SuppressWarnings("unchecked")
	private static void chestLootRespectsConfig(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<String> problems = new ArrayList<>();
		ModConfigSpec.ConfigValue<Boolean> option = (ModConfigSpec.ConfigValue<Boolean>) Configuration.GENERATE_FD_CHEST_LOOT;
		boolean before = option.get();
		ResourceKey<LootTable> plainsHouse = ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("chests/village/village_plains_house"));
		LootTable table = level.getServer().reloadableRegistries().getLootTable(plainsHouse);
		try {
			option.set(true);
			int withLoot = countFdItems(level, table, helper.absolutePos(BlockPos.ZERO), 200);
			option.set(false);
			int without = countFdItems(level, table, helper.absolutePos(BlockPos.ZERO), 200);
			if (withLoot == 0) problems.add("no FD items in 200 plains house chests with the option on");
			if (without > 0) problems.add(without + " FD items in plains house chests although generateFDChestLoot = false");
		} finally {
			option.set(before);
		}
		finish(helper, problems);
	}

	private static int countFdItems(ServerLevel level, LootTable table, BlockPos origin, int rolls) {
		int count = 0;
		for (int i = 0; i < rolls; i++) {
			LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin)).create(LootContextParamSets.CHEST);
			for (ItemStack stack : table.getRandomItems(params)) {
				if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(FarmersDelight.MODID)) count++;
			}
		}
		return count;
	}

	/** Scavenging drops need a knife, and straw needs a knife on grass; nothing else may pick them up. */
	private static void knifeLootNeedsAKnife(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		Vec3 origin = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 1, 2)));

		LivingEntity pig = EntityTypes.PIG.create(level, EntitySpawnReason.TRIGGERED);
		pig.setPos(origin);
		LootTable pigLoot = level.getServer().reloadableRegistries().getLootTable(pig.getLootTable().orElseThrow());
		int hamWithKnife = 0;
		int hamWithoutKnife = 0;
		for (int i = 0; i < 300; i++) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.IRON_KNIFE.get()));
			hamWithKnife += count(pigLoot.getRandomItems(entityParams(level, pig, player, origin)), ModItems.HAM.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			hamWithoutKnife += count(pigLoot.getRandomItems(entityParams(level, pig, player, origin)), ModItems.HAM.get());
		}
		if (hamWithKnife == 0) problems.add("pigs killed with a knife never dropped ham");
		if (hamWithoutKnife > 0) problems.add("pigs killed without a knife dropped ham " + hamWithoutKnife + "x");

		LootTable grassLoot = level.getServer().reloadableRegistries().getLootTable(Blocks.SHORT_GRASS.getLootTable().orElseThrow());
		LootTable stoneLoot = level.getServer().reloadableRegistries().getLootTable(Blocks.STONE.getLootTable().orElseThrow());
		int strawWithKnife = 0;
		int strawWithoutKnife = 0;
		int fdFromStone = 0;
		for (int i = 0; i < 300; i++) {
			strawWithKnife += count(grassLoot.getRandomItems(blockParams(level, Blocks.SHORT_GRASS.defaultBlockState(), new ItemStack(ModItems.IRON_KNIFE.get()), player, origin)), ModItems.STRAW.get());
			strawWithoutKnife += count(grassLoot.getRandomItems(blockParams(level, Blocks.SHORT_GRASS.defaultBlockState(), ItemStack.EMPTY, player, origin)), ModItems.STRAW.get());
			for (ItemStack stack : stoneLoot.getRandomItems(blockParams(level, Blocks.STONE.defaultBlockState(), new ItemStack(ModItems.IRON_KNIFE.get()), player, origin))) {
				if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(FarmersDelight.MODID)) fdFromStone++;
			}
		}
		if (strawWithKnife == 0) problems.add("grass cut with a knife never dropped straw");
		if (strawWithoutKnife > 0) problems.add("grass broken by hand dropped straw " + strawWithoutKnife + "x");
		if (fdFromStone > 0) problems.add("stone mined with a knife dropped FD items " + fdFromStone + "x");
		finish(helper, problems);
	}

	private static LootParams entityParams(ServerLevel level, LivingEntity victim, Player killer, Vec3 origin) {
		DamageSource source = level.damageSources().playerAttack(killer);
		return new LootParams.Builder(level)
			.withParameter(LootContextParams.THIS_ENTITY, victim)
			.withParameter(LootContextParams.ORIGIN, origin)
			.withParameter(LootContextParams.DAMAGE_SOURCE, source)
			.withParameter(LootContextParams.ATTACKING_ENTITY, killer)
			.withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, killer)
			.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer)
			.create(LootContextParamSets.ENTITY);
	}

	private static LootParams blockParams(ServerLevel level, BlockState state, ItemStack tool, Player player, Vec3 origin) {
		return new LootParams.Builder(level)
			.withParameter(LootContextParams.BLOCK_STATE, state)
			.withParameter(LootContextParams.ORIGIN, origin)
			.withParameter(LootContextParams.TOOL, tool)
			.withParameter(LootContextParams.THIS_ENTITY, player)
			.create(LootContextParamSets.BLOCK);
	}

	private static int count(List<ItemStack> stacks, Item item) {
		int n = 0;
		for (ItemStack stack : stacks) {
			if (stack.is(item)) n += stack.getCount();
		}
		return n;
	}

	private static String name(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).getPath();
	}
}
