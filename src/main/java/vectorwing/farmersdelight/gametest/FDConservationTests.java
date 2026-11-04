package vectorwing.farmersdelight.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.AbstractStoveBlock;
import vectorwing.farmersdelight.common.block.entity.AbstractStoveBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Item conservation (property) tests: random sequences of inserts, extracts, aborted transactions, hand use,
 * save/load and finally breaking the block, on every block that holds items. After every step:
 * items put in - items taken out == items inside. After breaking: items inside == item entities dropped.
 * Any duplication or loss fails the test with the seed and the step that caused it.
 */
public final class FDConservationTests
{
	private static final Identifier STRUCTURE = FarmersDelight.id("gametest/empty_32x6x32");
	private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, FarmersDelight.MODID);
	private static final List<ResourceKey<Consumer<GameTestHelper>>> TESTS = new ArrayList<>();
	private static final Direction[] SIDES = {null, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	private static final int SEEDS = 8;
	private static final int STEPS = 150;

	static {
		test("conservation_random_actions", FDConservationTests::randomActions);
		test("conservation_heated_cooking_pot", FDConservationTests::heatedCookingPot);
	}

	private FDConservationTests() {
	}

	private static void test(String name, Consumer<GameTestHelper> function) {
		FUNCTIONS.register(name, () -> function);
		TESTS.add(ResourceKey.create(Registries.TEST_FUNCTION, FarmersDelight.id(name)));
	}

	public static void register(IEventBus modEventBus) {
		FUNCTIONS.register(modEventBus);
		modEventBus.addListener(FDConservationTests::registerTests);
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(FarmersDelight.id("conservation"));
		for (ResourceKey<Consumer<GameTestHelper>> function : TESTS) {
			event.registerTest(function.identifier(), new FunctionGameTestInstance(function, new TestData<>(environment, STRUCTURE, 1200, 0, true)));
		}
	}

	// ------------------------------------------------------------------ accounting

	/** Item counts keyed by resource (item + components), so a lost or added component also counts as a mismatch. */
	static final class Ledger
	{
		final Map<ItemResource, Long> counts = new HashMap<>();

		void add(ItemResource resource, long amount) {
			if (resource.isEmpty() || amount == 0) return;
			counts.merge(resource, amount, Long::sum);
			counts.remove(resource, 0L);
		}

		void add(ItemStack stack) {
			add(ItemResource.of(stack), stack.getCount());
		}

		void addAll(ResourceHandler<ItemResource> handler) {
			for (int i = 0; i < handler.size(); i++) {
				add(handler.getResource(i), handler.getAmountAsLong(i));
			}
		}

		Ledger copy() {
			Ledger copy = new Ledger();
			copy.counts.putAll(counts);
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Ledger ledger && ledger.counts.equals(counts);
		}

		@Override
		public int hashCode() {
			return counts.hashCode();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("{");
			counts.forEach((resource, amount) -> builder.append(resource.getItem()).append(resource.getComponentsPatch().isEmpty() ? "" : "+components").append('=').append(amount).append(' '));
			return builder.append('}').toString();
		}
	}

	/** One block under test. */
	private static final class Subject
	{
		final String name;
		final BlockPos pos;
		final Random random;
		final long seed;
		final Ledger expected = new Ledger();
		final List<String> history = new ArrayList<>();

		Subject(String name, BlockPos pos, long seed) {
			this.name = name;
			this.pos = pos;
			this.seed = seed;
			this.random = new Random(seed);
		}

		String describe() {
			return name + " (seed " + seed + "), last steps: " + String.join(", ", history.subList(Math.max(0, history.size() - 6), history.size()));
		}
	}

	/** Everything the block holds, by reading its storage directly (not through a side-limited handler). */
	static Ledger contents(BlockEntity be) {
		Ledger ledger = new Ledger();
		switch (be) {
			case CookingPotBlockEntity pot -> ledger.addAll(pot.getInventory());
			case CuttingBoardBlockEntity board -> ledger.addAll(board.getInventory());
			case SkilletBlockEntity skillet -> ledger.addAll(skillet.getInventory());
			case AbstractStoveBlockEntity stove -> ledger.addAll(stove.getItems());
			case Container container -> {
				for (int i = 0; i < container.getContainerSize(); i++) {
					ledger.add(container.getItem(i));
				}
			}
			default -> throw new IllegalStateException("No item storage known for " + be);
		}
		return ledger;
	}

	private static List<ItemStack> pool() {
		ItemStack namedTomato = new ItemStack(ModItems.TOMATO.get());
		namedTomato.set(DataComponents.CUSTOM_NAME, Component.literal("Special tomato"));
		return List.of(
				new ItemStack(ModItems.TOMATO.get()),
				namedTomato,
				new ItemStack(ModItems.ONION.get()),
				new ItemStack(ModItems.CABBAGE.get()),
				new ItemStack(Items.BOWL),
				new ItemStack(Items.BEEF),
				new ItemStack(Items.PORKCHOP),
				new ItemStack(Items.IRON_SWORD),
				new ItemStack(ModItems.TOMATO_SAUCE.get()));
	}

	// ------------------------------------------------------------------ random actions on every inventory block

	private static void randomActions(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		List<ItemStack> pool = pool();
		List<Subject> subjects = new ArrayList<>();
		// Unheated so nothing cooks: every item that goes in must come out unchanged.
		List<BlockState> blocks = List.of(
				ModBlocks.COOKING_POT.get().defaultBlockState(),
				ModBlocks.OAK_CABINET.get().defaultBlockState(),
				ModBlocks.BAMBOO_BASKET.get().defaultBlockState(),
				ModBlocks.CUTTING_BOARD.get().defaultBlockState(),
				ModBlocks.SKILLET.get().defaultBlockState(),
				ModBlocks.STOVE.get().defaultBlockState().setValue(AbstractStoveBlock.LIT, false));
		int index = 0;
		for (BlockState state : blocks) {
			for (int seed = 0; seed < SEEDS; seed++) {
				// 4 blocks apart (8 x 6 grid inside the 32x32 structure) so baskets don't pick up other blocks' drops
				BlockPos pos = new BlockPos(2 + (index % 8) * 4, 1, 2 + (index / 8) * 4);
				index++;
				helper.setBlock(pos, state);
				subjects.add(new Subject(Objects.requireNonNull(state.getBlock().getDescriptionId()), pos, seed * 7919L + index));
			}
		}

		for (int tick = 1; tick <= STEPS; tick++) {
			helper.runAfterDelay(tick, () -> {
				for (Subject subject : subjects) {
					step(helper, subject, pool, player);
				}
			});
		}
		helper.runAfterDelay(STEPS + 1, () -> {
			for (Subject subject : subjects) {
				breakAndCountDrops(helper, subject, player);
			}
			helper.succeed();
		});
	}

	private static void step(GameTestHelper helper, Subject subject, List<ItemStack> pool, Player player) {
		BlockPos absolute = helper.absolutePos(subject.pos);
		BlockEntity be = helper.getLevel().getBlockEntity(absolute);
		helper.assertTrue(be != null, "Block entity vanished: " + subject.describe());
		Random random = subject.random;
		ItemStack template = pool.get(random.nextInt(pool.size()));
		int amount = 1 + random.nextInt(random.nextBoolean() ? 3 : 80);
		Direction side = SIDES[random.nextInt(SIDES.length)];
		ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, absolute, side);
		int action = random.nextInt(10);

		if (handler != null && action < 3) {
			// Insert through automation, committed
			try (Transaction tx = Transaction.openRoot()) {
				int inserted = handler.insert(ItemResource.of(template), amount, tx);
				helper.assertTrue(inserted >= 0 && inserted <= amount, "insert returned " + inserted + " for " + amount + ": " + subject.describe());
				tx.commit();
				subject.expected.add(ItemResource.of(template), inserted);
			}
			subject.history.add("insert " + side + " " + amount + "x" + template.getItem());
		} else if (handler != null && action < 6) {
			// Extract through automation, committed: pick something that is actually inside most of the time
			ItemResource resource = ItemResource.of(template);
			if (random.nextInt(4) != 0 && handler.size() > 0) {
				resource = handler.getResource(random.nextInt(handler.size()));
			}
			if (!resource.isEmpty()) {
				try (Transaction tx = Transaction.openRoot()) {
					int extracted = handler.extract(resource, amount, tx);
					helper.assertTrue(extracted >= 0 && extracted <= amount, "extract returned " + extracted + " for " + amount + ": " + subject.describe());
					tx.commit();
					subject.expected.add(resource, -extracted);
				}
				subject.history.add("extract " + side + " " + amount + "x" + resource.getItem());
			}
		} else if (handler != null && action < 7) {
			// Aborted transaction: must leave everything as it was
			Ledger before = contents(be);
			try (Transaction tx = Transaction.openRoot()) {
				handler.insert(ItemResource.of(template), amount, tx);
				if (handler.size() > 0) {
					ItemResource inside = handler.getResource(random.nextInt(handler.size()));
					if (!inside.isEmpty()) handler.extract(inside, amount, tx);
				}
			}
			helper.assertTrue(before.equals(contents(be)), "Aborted transaction changed contents: " + before + " -> " + contents(be) + " " + subject.describe());
			subject.history.add("aborted " + side);
		} else if (action < 9) {
			// Block-specific hand interaction
			handInteraction(helper, subject, be, template.copyWithCount(Math.min(amount, template.getMaxStackSize())), player);
		} else {
			// Save / load round trip keeps every item
			HolderLookup.Provider registries = helper.getLevel().registryAccess();
			CompoundTag tag = be.saveWithFullMetadata(registries);
			BlockEntity loaded = BlockEntity.loadStatic(absolute, be.getBlockState(), tag, registries);
			helper.assertTrue(loaded != null, "Could not reload: " + subject.describe());
			helper.assertTrue(contents(be).equals(contents(loaded)), "Save/load changed contents: " + contents(be) + " -> " + contents(loaded) + " " + subject.describe());
			subject.history.add("save/load");
		}

		Ledger actual = contents(be);
		helper.assertTrue(subject.expected.equals(actual), "Items not conserved. expected " + subject.expected + " but inside " + actual + " after " + subject.describe());
	}

	private static void handInteraction(GameTestHelper helper, Subject subject, BlockEntity be, ItemStack stack, Player player) {
		boolean put = subject.random.nextBoolean();
		if (be instanceof CuttingBoardBlockEntity board) {
			if (put) {
				ItemStack rest = board.addItem(stack.copy());
				subject.expected.add(ItemResource.of(stack), stack.getCount() - rest.getCount());
				subject.history.add("board put " + stack);
			} else {
				ItemStack taken = board.removeItem();
				subject.expected.add(ItemResource.of(taken), -taken.getCount());
				subject.history.add("board take " + taken);
			}
		} else if (be instanceof SkilletBlockEntity skillet) {
			if (put) {
				ItemStack rest = skillet.addItemToCook(stack.copy(), player, helper.getLevel());
				subject.expected.add(ItemResource.of(stack), stack.getCount() - (ItemStack.isSameItemSameComponents(rest, stack) ? rest.getCount() : 0));
				subject.history.add("skillet put " + stack + " rest " + rest);
			} else {
				ItemStack taken = skillet.removeItem();
				subject.expected.add(ItemResource.of(taken), -taken.getCount());
				subject.history.add("skillet take " + taken);
			}
		} else if (be instanceof AbstractStoveBlockEntity stove) {
			var recipe = stove.getCookingRecipe(stack);
			if (put && recipe.isPresent()) {
				ItemStack held = stack.copy();
				if (stove.placeFood(player, held, recipe.get())) {
					subject.expected.add(ItemResource.of(stack), stack.getCount() - held.getCount());
				}
				helper.assertTrue(held.getCount() >= stack.getCount() - 1, "Stove took more than one item: " + subject.describe());
				subject.history.add("stove put " + stack);
			}
		}
	}

	private static void breakAndCountDrops(GameTestHelper helper, Subject subject, Player player) {
		BlockPos absolute = helper.absolutePos(subject.pos);
		BlockEntity be = helper.getLevel().getBlockEntity(absolute);
		Ledger inside = contents(be);
		Block block = be.getBlockState().getBlock();
		var area = new net.minecraft.world.phys.AABB(absolute).inflate(2.0);
		for (ItemEntity old : helper.getLevel().getEntitiesOfClass(ItemEntity.class, area)) {
			old.discard();
		}
		helper.getLevel().destroyBlock(absolute, true, player);
		Ledger dropped = new Ledger();
		for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, area)) {
			ItemStack stack = entity.getItem();
			if (stack.is(block.asItem())) {
				// The block itself; a cooking pot keeps its meal inside the item
				if (be instanceof CookingPotBlockEntity) {
					dropped.add(CookingPotBlockEntity.getMealFromItem(stack));
				}
				continue;
			}
			dropped.add(stack);
		}
		helper.assertTrue(inside.equals(dropped), "Breaking lost or duplicated items: inside " + inside + " but dropped " + dropped + " " + subject.describe());
	}

	// ------------------------------------------------------------------ cooking keeps the books balanced

	/**
	 * Heated pots fed tomatoes and bowls at random from the top and the sides while meals are pulled from the bottom.
	 * Tomato Sauce takes 2 tomatoes and is served in a bowl, so at every tick:
	 * tomatoes used == 2 x sauces made, and bowls used == sauces served (in the output slot or pulled out).
	 */
	private static void heatedCookingPot(GameTestHelper helper) {
		List<BlockPos> pots = new ArrayList<>();
		List<Random> randoms = new ArrayList<>();
		List<long[]> books = new ArrayList<>(); // tomatoes in, bowls in, sauces out
		for (int i = 0; i < 6; i++) {
			BlockPos stove = new BlockPos(2 + i * 4, 1, 4);
			helper.setBlock(stove, ModBlocks.STOVE.get().defaultBlockState().setValue(AbstractStoveBlock.LIT, true));
			helper.setBlock(stove.above(), ModBlocks.COOKING_POT.get());
			pots.add(stove.above());
			randoms.add(new Random(1000 + i));
			books.add(new long[3]);
		}
		ItemResource tomato = ItemResource.of(ModItems.TOMATO.get());
		ItemResource bowl = ItemResource.of(Items.BOWL);
		ItemResource sauce = ItemResource.of(ModItems.TOMATO_SAUCE.get());
		int[] sauceTotal = new int[pots.size()];

		helper.onEachTick(() -> {
			for (int i = 0; i < pots.size(); i++) {
				BlockPos absolute = helper.absolutePos(pots.get(i));
				CookingPotBlockEntity pot = (CookingPotBlockEntity) helper.getLevel().getBlockEntity(absolute);
				Random random = randoms.get(i);
				long[] book = books.get(i);
				if (random.nextInt(4) == 0) {
					try (Transaction tx = Transaction.openRoot()) {
						ResourceHandler<ItemResource> top = helper.getLevel().getCapability(Capabilities.Item.BLOCK, absolute, Direction.UP);
						// Only slots 0 and 1: a shapeless recipe needs exactly as many filled slots as ingredients
						int slot = random.nextInt(2);
						book[0] += top.insert(slot, tomato, 1 + random.nextInt(3), tx);
						ResourceHandler<ItemResource> side = helper.getLevel().getCapability(Capabilities.Item.BLOCK, absolute, Direction.Plane.HORIZONTAL.getRandomDirection(net.minecraft.util.RandomSource.create(random.nextLong())));
						book[1] += side.insert(bowl, random.nextInt(2), tx);
						tx.commit();
					}
				}
				if (random.nextInt(8) == 0) {
					try (Transaction tx = Transaction.openRoot()) {
						ResourceHandler<ItemResource> bottom = helper.getLevel().getCapability(Capabilities.Item.BLOCK, absolute, Direction.DOWN);
						book[2] += bottom.extract(sauce, 1 + random.nextInt(4), tx);
						tx.commit();
					}
				}
				var inv = pot.getInventory();
				long tomatoesInside = 0, bowlsInside = 0;
				for (int slot = 0; slot < inv.size(); slot++) {
					if (inv.getResource(slot).equals(tomato)) tomatoesInside += inv.getAmountAsLong(slot);
					if (inv.getResource(slot).equals(bowl)) bowlsInside += inv.getAmountAsLong(slot);
				}
				long saucesInDisplay = inv.getResource(CookingPotBlockEntity.MEAL_DISPLAY_SLOT).equals(sauce) ? inv.getAmountAsLong(CookingPotBlockEntity.MEAL_DISPLAY_SLOT) : 0;
				long saucesInOutput = inv.getResource(CookingPotBlockEntity.OUTPUT_SLOT).equals(sauce) ? inv.getAmountAsLong(CookingPotBlockEntity.OUTPUT_SLOT) : 0;
				long saucesMade = saucesInDisplay + saucesInOutput + book[2];
				long saucesServed = saucesInOutput + book[2];
				long tomatoesUsed = book[0] - tomatoesInside;
				long bowlsUsed = book[1] - bowlsInside;
				String state = "pot " + i + ": tomatoes in " + book[0] + " inside " + tomatoesInside + ", bowls in " + book[1] + " inside " + bowlsInside
						+ ", sauce display " + saucesInDisplay + " output " + saucesInOutput + " pulled " + book[2];
				helper.assertTrue(tomatoesUsed == 2 * saucesMade, "Tomatoes not conserved, " + state);
				helper.assertTrue(bowlsUsed == saucesServed, "Bowls not conserved, " + state);
				sauceTotal[i] = (int) saucesMade;
			}
		});
		helper.runAfterDelay(1000, () -> {
			for (int i = 0; i < pots.size(); i++) {
				helper.assertTrue(sauceTotal[i] >= 3, "Pot " + i + " only made " + sauceTotal[i] + " sauces, cooking isn't being exercised");
			}
			helper.succeed();
		});
	}
}
