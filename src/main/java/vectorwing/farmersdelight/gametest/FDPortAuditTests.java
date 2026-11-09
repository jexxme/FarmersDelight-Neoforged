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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
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
import vectorwing.farmersdelight.common.registry.ModEffects;
import vectorwing.farmersdelight.common.entity.RottenTomatoEntity;
import vectorwing.farmersdelight.common.block.TomatoBlock;
import vectorwing.farmersdelight.common.block.RiceRollMedleyBlock;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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
		test("pistons_pop_feasts_and_pies", BIG_STRUCTURE, FDPortAuditTests::pistonsPopFeastsAndPies);
		test("pie_and_cake_slices_are_fast_food", STRUCTURE, FDPortAuditTests::pieAndCakeSlicesAreFastFood);
		test("knives_and_skillet_lose_one_durability_per_hit", STRUCTURE, FDPortAuditTests::knivesAndSkilletLoseOneDurabilityPerHit);
		test("chest_loot_respects_config", STRUCTURE, FDPortAuditTests::chestLootRespectsConfig);
		test("knife_loot_needs_a_knife", STRUCTURE, FDPortAuditTests::knifeLootNeedsAKnife);
		test("skillet_item_places_on_stove", STRUCTURE, FDPortAuditTests::skilletItemPlacesOnStove);
		test("every_fd_recipe_is_placeable", STRUCTURE, FDPortAuditTests::everyFdRecipeIsPlaceable);
		test("mature_crops_drop_their_harvest", STRUCTURE, FDPortAuditTests::matureCropsDropTheirHarvest);
		test("clicks_reach_pie_rope_and_tomato_actions", STRUCTURE, FDPortAuditTests::clicksReachPieRopeAndTomatoActions);
		test("drinks_apply_their_effects", STRUCTURE, FDPortAuditTests::drinksApplyTheirEffects);
		test("meals_match_1_21_food_values", STRUCTURE, FDPortAuditTests::mealsMatch121FoodValues);
		test("rice_roll_medley_serves_rolls", STRUCTURE, FDPortAuditTests::riceRollMedleyServesRolls);
		test("thrown_rotten_tomato_is_a_rotten_tomato", STRUCTURE, FDPortAuditTests::thrownRottenTomatoIsARottenTomato);
		test("village_farms_grow_fd_crops", STRUCTURE, FDPortAuditTests::villageFarmsGrowFdCrops);
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
		var features = level.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
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
			var holder = features.get(ResourceKey.create(Registries.CONFIGURED_FEATURE, FarmersDelight.id(c.feature())));
			if (holder.isEmpty()) {
				problems.add("feature " + c.feature() + " missing");
				continue;
			}
			ConfiguredFeature<?, ?> feature = holder.get().value();
			RandomSource random = RandomSource.create(42L);
			for (int attempt = 0; attempt < 64; attempt++) { // the placed features run each patch with "count": 64
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
			Consumable consumable = slice.get(DataComponents.CONSUMABLE);
			boolean hasEffects = consumable != null && !consumable.onConsumeEffects().isEmpty();
			if (!result.consumesAction()) {
				problems.add(name(pie) + ": eating a slice did nothing (" + result + ")");
			} else if (actual != expected) {
				problems.add(name(pie) + ": food went 2 -> " + actual + ", one slice should give " + expected);
			} else if (hasEffects && player.getActiveEffects().isEmpty()) {
				problems.add(name(pie) + ": eating a slice did not apply the slice's effects");
			}
			player.removeAllEffects();
			x += 2;
		}
		finish(helper, problems);
	}

	// ------------------------------------------------------------------ pistons (upstream #1382, fixed in 1.3.4)

	/**
	 * Feasts and pies have to pop when a piston pushes them (upstream 1.3.4); moving them could duplicate food (#1382).
	 * Each food gets its own lane, so afterwards the block has to be gone and at most one of it may lie around as an item.
	 */
	private static void pistonsPopFeastsAndPies(GameTestHelper helper) {
		List<Block> foods = new ArrayList<>();
		for (Block block : BuiltInRegistries.BLOCK) {
			if ((block instanceof FeastBlock || block instanceof PieBlock) && BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(FarmersDelight.MODID)) {
				foods.add(block);
			}
		}
		List<BlockPos> pistons = new ArrayList<>();
		for (int i = 0; i < foods.size(); i++) {
			BlockPos piston = new BlockPos((i % 7) * 4, 2, (i / 7) * 5);
			for (int dz = 0; dz <= 2; dz++) {
				helper.setBlock(piston.south(dz).below(), Blocks.STONE);
			}
			helper.setBlock(piston, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.SOUTH));
			helper.setBlock(piston.south(), foods.get(i).defaultBlockState());
			pistons.add(piston);
		}
		helper.startSequence()
			.thenExecuteAfter(5, () -> pistons.forEach(piston -> helper.setBlock(piston.east(), Blocks.REDSTONE_BLOCK)))
			.thenExecuteAfter(15, () -> {
				List<String> problems = new ArrayList<>();
				for (int i = 0; i < foods.size(); i++) {
					Block food = foods.get(i);
					BlockPos piston = pistons.get(i);
					for (int dz = 1; dz <= 2; dz++) {
						if (helper.getBlockState(piston.south(dz)).is(food)) {
							problems.add(name(food) + " was " + (dz == 1 ? "not pushed" : "moved instead of popping"));
						}
					}
					int items = 0;
					AABB lane = new AABB(helper.absolutePos(piston)).expandTowards(0, 0, 3).inflate(1.5, 2, 0.5);
					for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, lane)) {
						if (entity.getItem().is(food.asItem())) items += entity.getItem().getCount();
					}
					if (items > 1) {
						problems.add(name(food) + " dropped " + items + " times");
					}
				}
				helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(40)).forEach(ItemEntity::discard);
				finish(helper, problems);
			});
	}

	// ------------------------------------------------------------------ slices and weapons (adversarial review of this port)

	/** Pie and cake slices are fast food (16 ticks) as in 1.21; a 26.x Consumable defaults to 32 ticks. */
	private static void pieAndCakeSlicesAreFastFood(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		List<Item> slices = new ArrayList<>(List.of(ModItems.CAKE_SLICE.get()));
		for (Block pie : List.of(ModBlocks.APPLE_PIE.get(), ModBlocks.SWEET_BERRY_CHEESECAKE.get(), ModBlocks.CHOCOLATE_PIE.get(), ModBlocks.PUMPKIN_PIE.get())) {
			slices.add(((PieBlock) pie).getPieSliceItem().getItem());
		}
		for (Item slice : slices) {
			int ticks = new ItemStack(slice).getUseDuration(player);
			if (ticks != 16) {
				problems.add(BuiltInRegistries.ITEM.getKey(slice).getPath() + " takes " + ticks + " ticks to eat, expected 16");
			}
		}
		finish(helper, problems);
	}

	/**
	 * In 1.21 a hit cost a knife or the skillet 1 durability. On 26.x the Weapon component damages the item per hit
	 * by itself, so an extra hurtAndBreak in postHurtEnemy made knives lose 3 and the skillet 2.
	 */
	private static void knivesAndSkilletLoseOneDurabilityPerHit(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player attacker = helper.makeMockPlayer(GameType.SURVIVAL);
		Player target = helper.makeMockPlayer(GameType.SURVIVAL);
		List<Item> weapons = List.of(ModItems.FLINT_KNIFE.get(), ModItems.COPPER_KNIFE.get(), ModItems.IRON_KNIFE.get(), ModItems.GOLDEN_KNIFE.get(),
			ModItems.DIAMOND_KNIFE.get(), ModItems.NETHERITE_KNIFE.get(), ModItems.SKILLET.get(), Items.IRON_SWORD);
		for (Item weapon : weapons) {
			ItemStack stack = new ItemStack(weapon);
			attacker.setItemInHand(InteractionHand.MAIN_HAND, stack);
			stack.postHurtEnemy(target, attacker);
			if (stack.getDamageValue() != 1) {
				problems.add(BuiltInRegistries.ITEM.getKey(weapon).getPath() + " lost " + stack.getDamageValue() + " durability on one hit, expected 1");
			}
		}
		attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		finish(helper, problems);
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

	// ------------------------------------------------------------------ reports on KN_Yeon's port

	/** "placing a skillet on a stove instantly crashed my game": the skillet item used on a lit stove. */
	private static void skilletItemPlacesOnStove(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos stove = new BlockPos(2, 1, 2);
		helper.setBlock(stove, ModBlocks.STOVE.get().defaultBlockState().setValue(vectorwing.farmersdelight.common.block.AbstractStoveBlock.LIT, true));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SKILLET.get()));
		player.setShiftKeyDown(true); // skillets are placed while sneaking
		BlockPos abs = helper.absolutePos(stove);
		try {
			ModItems.SKILLET.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false)));
		} catch (RuntimeException e) {
			problems.add("using the skillet on a stove threw " + e);
		}
		BlockState placed = helper.getBlockState(stove.above());
		if (!placed.is(ModBlocks.SKILLET.get())) {
			problems.add("the skillet was not placed on the stove (" + placed + ")");
		} else if (placed.getValue(vectorwing.farmersdelight.common.block.SkilletBlock.SUPPORT)) {
			problems.add("a skillet on a stove got a tray (only fire, lava and campfires give one, as in 1.21)");
		}
		// over a campfire it stands on a tray
		BlockPos campfire = new BlockPos(0, 1, 0);
		helper.setBlock(campfire, Blocks.CAMPFIRE);
		BlockPos campAbs = helper.absolutePos(campfire);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SKILLET.get()));
		ModItems.SKILLET.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(campAbs).add(0, 0.5, 0), Direction.UP, campAbs, false)));
		BlockState overFire = helper.getBlockState(campfire.above());
		if (!overFire.is(ModBlocks.SKILLET.get()) || !overFire.getValue(vectorwing.farmersdelight.common.block.SkilletBlock.SUPPORT)) {
			problems.add("a skillet over a campfire should stand on a tray (" + overFire + ")");
		}
		finish(helper, problems);
	}

	/** "everything exists as items but cannot be crafted": every FD recipe must have ingredients that exist. */
	private static void everyFdRecipeIsPlaceable(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		int checked = 0;
		for (var holder : helper.getLevel().getServer().getRecipeManager().getRecipes()) {
			if (!holder.id().identifier().getNamespace().equals(FarmersDelight.MODID)) continue;
			// cutting board recipes have no recipe book, so they are never "placeable"; check the crafting table and cooking pot ones
			var type = holder.value().getType();
			if (type != net.minecraft.world.item.crafting.RecipeType.CRAFTING && type != vectorwing.farmersdelight.common.registry.ModRecipeTypes.COOKING.get()) continue;
			if (holder.value().isSpecial()) continue; // dynamic recipes (food serving, dough from water) have no fixed ingredients
			checked++;
			if (holder.value().placementInfo().isImpossibleToPlace()) {
				problems.add(holder.id().identifier().getPath());
			}
		}
		if (checked < 60) problems.add("only " + checked + " FD crafting/cooking recipes loaded");
		finish(helper, problems);
	}

	/** "the crops do not drop when farming": mature crops drop their crop, young ones their seeds. */
	private static void matureCropsDropTheirHarvest(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		Vec3 origin = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 1, 2)));
		record Case(BlockState state, Item expected) {
		}
		List<Case> cases = List.of(
			new Case(ModBlocks.CABBAGE_CROP.get().defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7), ModItems.CABBAGE.get()),
			new Case(ModBlocks.CABBAGE_CROP.get().defaultBlockState(), ModItems.CABBAGE_SEEDS.get()),
			new Case(ModBlocks.ONION_CROP.get().defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7), ModItems.ONION.get()),
			new Case(ModBlocks.TOMATO_CROP.get().defaultBlockState().setValue(vectorwing.farmersdelight.common.block.TomatoBlock.VINE_AGE, 3), ModItems.TOMATO.get()),
			new Case(ModBlocks.RICE_CROP_PANICLES.get().defaultBlockState().setValue(vectorwing.farmersdelight.common.block.RicePaniclesBlock.RICE_AGE, 3), ModItems.RICE_PANICLE.get()),
			new Case(ModBlocks.WILD_CABBAGES.get().defaultBlockState(), ModItems.CABBAGE_SEEDS.get()),
			new Case(Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7), net.minecraft.world.item.Items.WHEAT));
		for (Case c : cases) {
			LootTable table = level.getServer().reloadableRegistries().getLootTable(c.state().getBlock().getLootTable().orElseThrow());
			int found = 0;
			for (int i = 0; i < 20; i++) {
				found += count(table.getRandomItems(blockParams(level, c.state(), ItemStack.EMPTY, player, origin)), c.expected());
			}
			if (found == 0) {
				problems.add(c.state() + " never dropped " + BuiltInRegistries.ITEM.getKey(c.expected()).getPath());
			}
		}
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

	// ------------------------------------------------------------------ found while writing the docs (bug hand-over)

	/**
	 * On 26.x a block's useItemOn has to return TRY_WITH_EMPTY_HAND for useWithoutItem to run, and PASS to let the held
	 * item act. The port returned PASS or SUCCESS, so pies could not be eaten by clicking, ropes did not reel or ring
	 * bells, rope did not extend downward and bone meal did nothing on unripe tomatoes. These go through the same click
	 * path as the server (GameTestHelper.useBlock).
	 */
	private static void clicksReachPieRopeAndTomatoActions(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		// Pie, empty hand: eats a bite.
		BlockPos pie = new BlockPos(0, 1, 0);
		helper.setBlock(pie, ModBlocks.APPLE_PIE.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.getFoodData().setFoodLevel(2);
		helper.useBlock(pie, player);
		if (player.getFoodData().getFoodLevel() <= 2) problems.add("clicking a pie with an empty hand did not eat a bite");

		// Rope with rope in hand: extends the rope downward.
		helper.setBlock(new BlockPos(2, 5, 0), Blocks.STONE);
		for (int y = 2; y <= 4; y++) helper.setBlock(new BlockPos(2, y, 0), ModBlocks.ROPE.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ROPE.get(), 4));
		helper.useBlock(new BlockPos(2, 4, 0), player);
		if (!helper.getBlockState(new BlockPos(2, 1, 0)).is(ModBlocks.ROPE.get())) problems.add("clicking a rope with rope did not extend it downward");

		// Rope, sneaking with an empty hand: reels in the bottom rope.
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.getInventory().clearContent();
		player.setShiftKeyDown(true);
		helper.useBlock(new BlockPos(2, 4, 0), player);
		player.setShiftKeyDown(false);
		if (helper.getBlockState(new BlockPos(2, 1, 0)).is(ModBlocks.ROPE.get())) problems.add("sneak-clicking a rope did not reel it in");
		else if (!player.getInventory().contains(new ItemStack(ModItems.ROPE.get()))) problems.add("reeling did not give the rope back");

		// Rope under a bell, empty hand: rings the bell.
		helper.setBlock(new BlockPos(4, 5, 0), Blocks.STONE);
		helper.setBlock(new BlockPos(4, 4, 0), Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
		for (int y = 2; y <= 3; y++) helper.setBlock(new BlockPos(4, y, 0), ModBlocks.ROPE.get());
		player.getInventory().clearContent(); // reeling put the rope into the empty hand
		helper.useBlock(new BlockPos(4, 2, 0), player);

		// Unripe tomato, bone meal in hand: grows.
		BlockPos tomato = new BlockPos(0, 2, 3);
		helper.setBlock(tomato.below(), Blocks.FARMLAND);
		helper.setBlock(tomato, ModBlocks.TOMATO_CROP.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_MEAL, 8));
		helper.useBlock(tomato, player);
		BlockState grown = helper.getBlockState(tomato);
		if (!(grown.getBlock() instanceof TomatoBlock tomatoBlock) || grown.getValue(tomatoBlock.getAgeProperty()) == 0) {
			problems.add("bone meal on an unripe tomato did nothing");
		}
		// The bell starts shaking when its block event runs, later in the tick.
		helper.runAfterDelay(2, () -> {
			if (!helper.getBlockEntity(new BlockPos(4, 4, 0), BellBlockEntity.class).shaking) problems.add("clicking a rope under a bell did not ring it");
			finish(helper, problems);
		});
	}

	/** Milk Bottle, Hot Cocoa and Melon Juice have no FOOD; their Consumable effects were skipped. */
	private static void drinksApplyTheirEffects(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		player.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
		ItemStack milk = new ItemStack(ModItems.MILK_BOTTLE.get());
		milk.finishUsingItem(helper.getLevel(), player);
		if (player.hasEffect(MobEffects.POISON)) problems.add("milk bottle did not remove Poison");
		if (!milk.isEmpty()) problems.add("milk bottle was not used up");

		player.removeAllEffects();
		player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600));
		new ItemStack(ModItems.HOT_COCOA.get()).finishUsingItem(helper.getLevel(), player);
		if (player.hasEffect(MobEffects.SLOWNESS)) problems.add("hot cocoa did not remove Slowness");

		player.setHealth(10.0F);
		new ItemStack(ModItems.MELON_JUICE.get()).finishUsingItem(helper.getLevel(), player);
		if (player.getHealth() < 11.9F) problems.add("melon juice healed " + (player.getHealth() - 10.0F) + ", expected 2");
		finish(helper, problems);
	}

	/** Nutrition and Nourishment length of the meals the port had wrong, against Farmer's Delight 1.21. */
	private static void mealsMatch121FoodValues(GameTestHelper helper) {
		record Meal(Item item, int nutrition, int nourishment) {
		}
		List<String> problems = new ArrayList<>();
		for (Meal meal : List.of(
			new Meal(ModItems.ONION_SOUP.get(), 12, 3600), new Meal(ModItems.CHICKEN_SOUP.get(), 12, 3600),
			new Meal(ModItems.FRIED_RICE.get(), 12, 3600), new Meal(ModItems.GRILLED_SALMON.get(), 14, 3600),
			new Meal(ModItems.SHEPHERDS_PIE.get(), 14, 6000), new Meal(Items.RABBIT_STEW, 14, 6000))) {
			ItemStack stack = new ItemStack(meal.item());
			String name = BuiltInRegistries.ITEM.getKey(meal.item()).getPath();
			FoodProperties food = stack.get(DataComponents.FOOD);
			if (food == null || food.nutrition() != meal.nutrition()) {
				problems.add(name + " nutrition " + (food == null ? "none" : food.nutrition()) + ", expected " + meal.nutrition());
			}
			int nourishment = 0;
			Consumable consumable = stack.get(DataComponents.CONSUMABLE);
			if (consumable != null) {
				for (var effect : consumable.onConsumeEffects()) {
					if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
						for (MobEffectInstance instance : apply.effects()) {
							if (instance.getEffect().equals(ModEffects.NOURISHMENT)) nourishment = instance.getDuration();
						}
					}
				}
			}
			if (nourishment != meal.nourishment()) problems.add(name + " gives " + nourishment + " ticks of Nourishment, expected " + meal.nourishment());
		}
		finish(helper, problems);
	}

	/** The rolls have no container, so the medley never served them. */
	private static void riceRollMedleyServesRolls(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, ModBlocks.RICE_ROLL_MEDLEY_BLOCK.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		int before = helper.getBlockState(pos).getValue(RiceRollMedleyBlock.ROLL_SERVINGS);
		helper.useBlock(pos, player);
		BlockState after = helper.getBlockState(pos);
		int taken = 0;
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) taken += player.getInventory().getItem(slot).getCount();
		if (!after.is(ModBlocks.RICE_ROLL_MEDLEY_BLOCK.get()) || after.getValue(RiceRollMedleyBlock.ROLL_SERVINGS) != before - 1) problems.add("the medley did not lose a serving");
		if (taken != 1) problems.add("the player got " + taken + " items, expected one roll");
		finish(helper, problems);
	}

	/** Thrown by a player, the rotten tomato was a vanilla Snowball. */
	private static void thrownRottenTomatoIsARottenTomato(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		Vec3 at = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 2, 2)));
		player.snapTo(at.x, at.y, at.z, 0.0F, -30.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ROTTEN_TOMATO.get()));
		ModItems.ROTTEN_TOMATO.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		AABB around = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(12);
		int tomatoes = helper.getLevel().getEntitiesOfClass(RottenTomatoEntity.class, around).size();
		int snowballs = helper.getLevel().getEntitiesOfClass(Snowball.class, around).size();
		helper.getLevel().getEntitiesOfClass(RottenTomatoEntity.class, around).forEach(RottenTomatoEntity::discard);
		helper.getLevel().getEntitiesOfClass(Snowball.class, around).forEach(Snowball::discard);
		if (tomatoes != 1 || snowballs != 0) problems.add(tomatoes + " rotten tomato and " + snowballs + " snowball projectiles, expected 1 and 0");
		finish(helper, problems);
	}

	/**
	 * Runs wheat through each village farm's processor list, as structure placement does, and counts the crops. FD's
	 * rules reused vanilla's random numbers, so plains and taiga farms never got cabbages.
	 * <p>
	 * The processors read the world at each position and seed their random numbers from it (a hash, so neighbouring
	 * blocks are as random as distant ones). The samples stay in a 64 by 63 block area at the test, a few chunks: spread
	 * over thousands of chunks they made the server generate all of them, which ran CI out of memory on 26.1.x.
	 */
	private static void villageFarmsGrowFdCrops(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		var processorLists = helper.getLevel().registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);
		Map<String, List<Block>> expected = Map.of(
			"farm_plains", List.of(ModBlocks.CABBAGE_CROP.get(), ModBlocks.TOMATO_CROP.get(), ModBlocks.ONION_CROP.get(), Blocks.CARROTS),
			"farm_taiga", List.of(ModBlocks.CABBAGE_CROP.get(), ModBlocks.TOMATO_CROP.get(), ModBlocks.ONION_CROP.get(), Blocks.PUMPKIN_STEM),
			"farm_snowy", List.of(ModBlocks.CABBAGE_CROP.get(), ModBlocks.ONION_CROP.get(), Blocks.POTATOES),
			"farm_savanna", List.of(ModBlocks.CABBAGE_CROP.get(), ModBlocks.TOMATO_CROP.get(), Blocks.MELON_STEM),
			"farm_desert", List.of(ModBlocks.CABBAGE_CROP.get(), ModBlocks.TOMATO_CROP.get(), Blocks.BEETROOTS));
		int samples = 4000;
		BlockPos origin = helper.absolutePos(BlockPos.ZERO);
		for (var entry : expected.entrySet()) {
			StructureProcessorList list = processorLists.getValue(Identifier.withDefaultNamespace(entry.getKey()));
			if (list == null) {
				problems.add(entry.getKey() + " is missing");
				continue;
			}
			Map<Block, Integer> counts = new HashMap<>();
			for (int i = 0; i < samples; i++) {
				BlockPos pos = origin.offset(i % 64, 0, i / 64);
				StructureTemplate.StructureBlockInfo original = new StructureTemplate.StructureBlockInfo(pos, Blocks.WHEAT.defaultBlockState(), null);
				StructureTemplate.StructureBlockInfo info = original;
				for (StructureProcessor processor : list.list()) {
					info = processor.processBlock(helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, original.pos(), info, new StructurePlaceSettings());
					if (info == null) break;
				}
				if (info != null) counts.merge(info.state().getBlock(), 1, Integer::sum);
			}
			for (Block crop : entry.getValue()) {
				int share = counts.getOrDefault(crop, 0) * 100 / samples;
				if (share < 3) problems.add(entry.getKey() + ": " + name(crop) + " on " + share + "% of the wheat");
			}
		}
		finish(helper, problems);
	}

	private static String name(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).getPath();
	}
}
