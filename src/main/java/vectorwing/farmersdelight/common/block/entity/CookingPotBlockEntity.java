package vectorwing.farmersdelight.common.block.entity;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.slf4j.Logger;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.block.entity.inventory.CookingPotItemHandler;
import vectorwing.farmersdelight.common.block.entity.inventory.RecipeWrapper;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.*;
import vectorwing.farmersdelight.common.utility.ItemUtils;
import vectorwing.farmersdelight.common.utility.TextUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.Map.entry;

@EventBusSubscriber(modid = FarmersDelight.MODID)
public class CookingPotBlockEntity extends SyncedBlockEntity implements MenuProvider, HeatableBlockEntity, Nameable, RecipeCraftingHolder, Clearable
{
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int MEAL_DISPLAY_SLOT = 6;
	public static final int CONTAINER_SLOT = 7;
	public static final int OUTPUT_SLOT = 8;
	public static final int INVENTORY_SIZE = OUTPUT_SLOT + 1;
	private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);

	public static final Map<Item, Item> INGREDIENT_REMAINDER_OVERRIDES = Map.ofEntries(
		entry(Items.POWDER_SNOW_BUCKET, Items.BUCKET),
		entry(Items.AXOLOTL_BUCKET, Items.BUCKET),
		entry(Items.COD_BUCKET, Items.BUCKET),
		entry(Items.PUFFERFISH_BUCKET, Items.BUCKET),
		entry(Items.SALMON_BUCKET, Items.BUCKET),
		entry(Items.TROPICAL_FISH_BUCKET, Items.BUCKET),
		entry(Items.SUSPICIOUS_STEW, Items.BOWL),
		entry(Items.MUSHROOM_STEW, Items.BOWL),
		entry(Items.RABBIT_STEW, Items.BOWL),
		entry(Items.BEETROOT_SOUP, Items.BOWL),
		entry(Items.POTION, Items.GLASS_BOTTLE),
		entry(Items.SPLASH_POTION, Items.GLASS_BOTTLE),
		entry(Items.LINGERING_POTION, Items.GLASS_BOTTLE),
		entry(Items.EXPERIENCE_BOTTLE, Items.GLASS_BOTTLE)
	);

	private final ItemStacksResourceHandler inventory;
	private final ResourceHandler<ItemResource> inputHandler;
	private final ResourceHandler<ItemResource> outputHandler;

	private int cookTime;
	private int cookTimeTotal;
	private ItemStack mealContainerStack;
	private Component customName;

	protected final ContainerData cookingPotData;
	private final Object2IntOpenHashMap<ResourceKey<Recipe<?>>> usedRecipeTracker;

	private final RecipeManager.CachedCheck<RecipeInput, CookingPotRecipe> quickCheck;

	public CookingPotBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntityTypes.COOKING_POT.get(), pos, state);
		this.inventory = createHandler();
		this.inputHandler = new CookingPotItemHandler(inventory, Direction.UP);
		this.outputHandler = new CookingPotItemHandler(inventory, Direction.DOWN);
		this.mealContainerStack = ItemStack.EMPTY;
		this.cookingPotData = createIntArray();
		this.usedRecipeTracker = new Object2IntOpenHashMap<>();
		this.quickCheck = RecipeManager.createCheck(ModRecipeTypes.COOKING.get());
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
			Capabilities.Item.BLOCK,
			ModBlockEntityTypes.COOKING_POT.get(),
			(be, context) -> {
				if (context == Direction.UP) {
					return be.inputHandler;
				}
				return be.outputHandler;
			}
		);
	}

	public static ItemStack getMealFromItem(ItemStack cookingPotStack) {
		if (!cookingPotStack.is(ModItems.COOKING_POT.get())) {
			return ItemStack.EMPTY;
		}

		return cookingPotStack.getOrDefault(ModDataComponents.MEAL, ItemStackWrapper.EMPTY).getStack();
	}

	public static void takeServingFromItem(ItemStack cookingPotStack) {
		if (!cookingPotStack.is(ModItems.COOKING_POT.get())) {
			return;
		}

		ItemStack mealStack = cookingPotStack.getOrDefault(ModDataComponents.MEAL, ItemStackWrapper.EMPTY).getStack();
		mealStack.shrink(1);
		cookingPotStack.set(ModDataComponents.MEAL, new ItemStackWrapper(mealStack));
	}

	public static ItemStack getContainerFromItem(ItemStack cookingPotStack) {
		if (!cookingPotStack.is(ModItems.COOKING_POT.get())) {
			return ItemStack.EMPTY;
		}
		return cookingPotStack.getOrDefault(ModDataComponents.CONTAINER.get(), ItemStackWrapper.EMPTY).getStack();
	}


	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		inventory.deserialize(input.childOrEmpty("Inventory"));
		cookTime = input.getIntOr("CookTime", 0);
		cookTimeTotal = input.getIntOr("CookTimeTotal", 0);
		mealContainerStack = input.read("Container", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		customName = input.read("CustomName", ComponentSerialization.CODEC).orElse(null);
		usedRecipeTracker.clear();
		usedRecipeTracker.putAll(input.read("RecipesUsed", RECIPES_USED_CODEC).orElse(Collections.emptyMap()));
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("CookTime", cookTime);
		output.putInt("CookTimeTotal", cookTimeTotal);
		output.storeNullable("Container", ItemStack.CODEC, mealContainerStack.isEmpty() ? null : mealContainerStack);
		if (customName != null) {
			output.storeNullable("CustomName", ComponentSerialization.CODEC, customName);
		}
		inventory.serialize(output.child("Inventory"));
		output.store("RecipesUsed", RECIPES_USED_CODEC, usedRecipeTracker);
	}


	private void writeItems(ValueOutput output) {
		super.saveAdditional(output);
		output.storeNullable("Container", ItemStack.CODEC, mealContainerStack.isEmpty() ? null : mealContainerStack);
		inventory.serialize(output.child("Inventory"));
	}

	public void writeMeal(ValueOutput output) {
		if (getMeal().isEmpty()) return;

		ItemStacksResourceHandler drops = new ItemStacksResourceHandler(INVENTORY_SIZE);
		for (int i = 0; i < INVENTORY_SIZE; ++i) {
			drops.set(i, i == MEAL_DISPLAY_SLOT ? inventory.getResource(i) : ItemResource.EMPTY, i == MEAL_DISPLAY_SLOT ? inventory.getAmountAsInt(i) : 0);
		}
		if (customName != null) {
			output.storeNullable("CustomName", ComponentSerialization.CODEC, customName);
		}
		output.storeNullable("Container", ItemStack.OPTIONAL_CODEC, mealContainerStack.isEmpty() ? null : mealContainerStack);
		drops.serialize(output.child("Inventory"));
	}

	public ItemStack getAsItem() {
		ItemStack stack = new ItemStack(ModItems.COOKING_POT.get());
		stack.applyComponents(collectComponents());
		return stack;
	}

	public static void cookingTick(ServerLevel level, BlockPos pos, BlockState state, CookingPotBlockEntity cookingPot) {
		boolean isHeated = cookingPot.isHeated(level, pos);
		boolean didInventoryChange = false;

		if (isHeated && cookingPot.hasInput()) {
			Optional<RecipeHolder<CookingPotRecipe>> recipe = cookingPot.getMatchingRecipe(new RecipeWrapper(cookingPot.inventory), level);
			if (recipe.isPresent() && cookingPot.canCook(recipe.get().value())) {
				didInventoryChange = cookingPot.processCooking(recipe.get(), cookingPot);
			} else {
				cookingPot.cookTime = Mth.clamp(cookingPot.cookTime - 2, 0, cookingPot.cookTimeTotal);
			}
		} else if (cookingPot.cookTime > 0) {
			cookingPot.cookTime = Mth.clamp(cookingPot.cookTime - 2, 0, cookingPot.cookTimeTotal);
		}

		ItemStack mealStack = cookingPot.getMeal();
		if (!mealStack.isEmpty()) {
			if (!cookingPot.doesMealHaveContainer(mealStack)) {
				cookingPot.moveMealToOutput();
				didInventoryChange = true;
			} else if (!cookingPot.inventory.getResource(CONTAINER_SLOT).isEmpty()) {
				cookingPot.useStoredContainersOnMeal();
				didInventoryChange = true;
			}
		}

		if (didInventoryChange) {
			cookingPot.inventoryChanged();
		}
	}


	public static void animationTick(Level level, BlockPos pos, BlockState state, CookingPotBlockEntity cookingPot) {
		if (cookingPot.isHeated(level, pos)) {
			RandomSource random = level.getRandom();
			if (random.nextFloat() < 0.2F) {
				double x = (double) pos.getX() + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
				double y = (double) pos.getY() + 0.7D;
				double z = (double) pos.getZ() + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
				level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0D, 0.0D, 0.0D);
			}
			if (random.nextFloat() < 0.05F) {
				double x = (double) pos.getX() + 0.5D + (random.nextDouble() * 0.4D - 0.2D);
				double y = (double) pos.getY() + 0.5D;
				double z = (double) pos.getZ() + 0.5D + (random.nextDouble() * 0.4D - 0.2D);
				double motionY = random.nextBoolean() ? 0.015D : 0.005D;
				level.addParticle(ModParticleTypes.STEAM.get(), x, y, z, 0.0D, motionY, 0.0D);
			}
		}

	}

	private Optional<RecipeHolder<CookingPotRecipe>> getMatchingRecipe(RecipeWrapper inventoryWrapper, ServerLevel level) {
		if (level == null) return Optional.empty();
		return hasInput() ? quickCheck.getRecipeFor(inventoryWrapper, level) : Optional.empty();
	}

	public ItemStack getContainer() {
		ItemStack mealStack = getMeal();
		if (!mealStack.isEmpty() && !mealContainerStack.isEmpty()) {
			return mealContainerStack;
		} else if (mealStack.getCraftingRemainder() != null) {
			return mealStack.getCraftingRemainder().create();
		}
		return ItemStack.EMPTY;
	}

	private boolean hasInput() {
		for (int i = 0; i < MEAL_DISPLAY_SLOT; ++i) {
			if (!inventory.getResource(i).isEmpty()) return true;
		}
		return false;
	}

	protected boolean canCook(CookingPotRecipe recipe) {
		if (level == null) return false;

		if (hasInput()) {
			ItemStack resultStack = recipe.assemble(new RecipeWrapper(inventory));
			if (resultStack.isEmpty()) {
				return false;
			} else {
				ItemStack storedMealStack = stackIn(MEAL_DISPLAY_SLOT);
				if (storedMealStack.isEmpty()) {
					return true;
				} else if (!ItemStack.isSameItemSameComponents(storedMealStack, resultStack)) {
					return false;
				} else if (storedMealStack.getCount() + resultStack.getCount() <= Math.max(64, storedMealStack.getMaxStackSize())) {
					return true;
				} else {
					return storedMealStack.getCount() + resultStack.getCount() <= resultStack.getMaxStackSize();
				}
			}
		} else {
			return false;
		}
	}

	private boolean processCooking(RecipeHolder<CookingPotRecipe> recipe, CookingPotBlockEntity cookingPot) {
		if (level == null) return false;

		++cookTime;
		cookTimeTotal = recipe.value().getCookTime();
		if (cookTime < cookTimeTotal) {
			return false;
		}

		cookTime = 0;
		mealContainerStack = recipe.value().getOutputContainer();
		ItemStack resultStack = recipe.value().assemble(new RecipeWrapper(inventory));
		ItemStack storedMealStack = stackIn(MEAL_DISPLAY_SLOT);
		if (storedMealStack.isEmpty()) {
			setStack(MEAL_DISPLAY_SLOT, resultStack.copy());
		} else if (ItemStack.isSameItemSameComponents(storedMealStack, resultStack)) {
			storedMealStack.grow(resultStack.getCount());
			setStack(MEAL_DISPLAY_SLOT, storedMealStack);
		}
		cookingPot.setRecipeUsed(recipe);

		for (int i = 0; i < MEAL_DISPLAY_SLOT; ++i) {
			ItemStack slotStack = stackIn(i);
			if (slotStack.getCraftingRemainder() != null) {
				ejectIngredientRemainder(slotStack.getCraftingRemainder().create());
			} else if (INGREDIENT_REMAINDER_OVERRIDES.containsKey(slotStack.getItem())) {
				ejectIngredientRemainder(INGREDIENT_REMAINDER_OVERRIDES.get(slotStack.getItem()).getDefaultInstance());
			}
			if (!slotStack.isEmpty()) {
				slotStack.shrink(1);
				setStack(i, slotStack);
			}
		}
		return true;
	}

	protected void ejectIngredientRemainder(ItemStack remainderStack) {
		Direction direction = getBlockState().getValue(CookingPotBlock.FACING).getCounterClockWise();
		double x = worldPosition.getX() + 0.5 + (direction.getStepX() * 0.25);
		double y = worldPosition.getY() + 0.7;
		double z = worldPosition.getZ() + 0.5 + (direction.getStepZ() * 0.25);
		ItemUtils.spawnItemEntity(level, remainderStack, x, y, z,
			direction.getStepX() * 0.08F, 0.25F, direction.getStepZ() * 0.08F);
	}

	@Override
	public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
		if (recipe != null) {
			ResourceKey<Recipe<?>> recipeID = recipe.id();
			usedRecipeTracker.addTo(recipeID, 1);
		}
	}

	@Nullable
	@Override
	public RecipeHolder<?> getRecipeUsed() {
		return null;
	}

	@Override
	public void awardUsedRecipes(Player player, List<ItemStack> items) {
		if (player.level() instanceof ServerLevel serverLevel) {
			List<RecipeHolder<?>> usedRecipes = getUsedRecipesAndPopExperience(serverLevel, player.position());
			player.awardRecipes(usedRecipes);
			usedRecipeTracker.clear();
		}
	}

	public List<RecipeHolder<?>> getUsedRecipesAndPopExperience(ServerLevel level, Vec3 pos) {
		List<RecipeHolder<?>> list = Lists.newArrayList();

		for (Object2IntMap.Entry<ResourceKey<Recipe<?>>> entry : usedRecipeTracker.object2IntEntrySet()) {
			level.recipeAccess().byKey(entry.getKey()).ifPresent((recipe) -> {
				list.add(recipe);
				splitAndSpawnExperience(level, pos, entry.getIntValue(), ((CookingPotRecipe) recipe.value()).getExperience());
			});
		}

		return list;
	}

	private static void splitAndSpawnExperience(ServerLevel level, Vec3 pos, int craftedAmount, float experience) {
		int expTotal = Mth.floor((float) craftedAmount * experience);
		float expFraction = Mth.frac((float) craftedAmount * experience);
		if (expFraction != 0.0F && Math.random() < (double) expFraction) {
			++expTotal;
		}

		ExperienceOrb.award(level, pos, expTotal);
	}

	public boolean isHeated() {
		if (level == null) return false;
		return this.isHeated(level, worldPosition);
	}

	public ItemStacksResourceHandler getInventory() {
		return inventory;
	}

	/**
	 * The stack in a slot, with its real amount. ItemResource#toStack() alone always has a count of 1, and is a copy:
	 * changes must be written back with {@link #setStack}.
	 */
	private ItemStack stackIn(int slot) {
		return inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot));
	}

	private void setStack(int slot, ItemStack stack) {
		inventory.set(slot, stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack), stack.isEmpty() ? 0 : stack.getCount());
	}

	public ItemStack getMeal() {
		return stackIn(MEAL_DISPLAY_SLOT);
	}

	public NonNullList<ItemStack> getDroppableInventory() {
		NonNullList<ItemStack> drops = NonNullList.create();
		for (int i = 0; i < INVENTORY_SIZE; ++i) {
			if (i != MEAL_DISPLAY_SLOT) {
				drops.add(stackIn(i));
			}
		}
		return drops;
	}

	private void moveMealToOutput() {
		ItemStack mealStack = stackIn(MEAL_DISPLAY_SLOT);
		ItemStack outputStack = stackIn(OUTPUT_SLOT);
		int mealCount = Math.min(mealStack.getCount(), mealStack.getMaxStackSize() - outputStack.getCount());
		if (mealCount <= 0) {
			return;
		}
		if (outputStack.isEmpty()) {
			setStack(OUTPUT_SLOT, mealStack.split(mealCount));
			setStack(MEAL_DISPLAY_SLOT, mealStack);
		} else if (ItemStack.isSameItemSameComponents(mealStack, outputStack)) {
			mealStack.shrink(mealCount);
			outputStack.grow(mealCount);
			setStack(MEAL_DISPLAY_SLOT, mealStack);
			setStack(OUTPUT_SLOT, outputStack);
		}
	}

	private void useStoredContainersOnMeal() {
		ItemStack mealStack = stackIn(MEAL_DISPLAY_SLOT);
		ItemStack containerInputStack = stackIn(CONTAINER_SLOT);
		ItemStack outputStack = stackIn(OUTPUT_SLOT);

		if (isContainerValid(containerInputStack) && (outputStack.isEmpty() || outputStack.getCount() < outputStack.getMaxStackSize())) {
			int smallerStackCount = Math.min(mealStack.getCount(), containerInputStack.getCount());
			int mealCount = Math.min(smallerStackCount, mealStack.getMaxStackSize() - outputStack.getCount());
			if (mealCount <= 0) {
				return;
			}
			if (outputStack.isEmpty()) {
				containerInputStack.shrink(mealCount);
				setStack(OUTPUT_SLOT, mealStack.split(mealCount));
			} else if (ItemStack.isSameItemSameComponents(outputStack, mealStack)) {
				mealStack.shrink(mealCount);
				containerInputStack.shrink(mealCount);
				outputStack.grow(mealCount);
				setStack(OUTPUT_SLOT, outputStack);
			} else {
				return;
			}
			setStack(MEAL_DISPLAY_SLOT, mealStack);
			setStack(CONTAINER_SLOT, containerInputStack);
		}
	}

	public ItemStack useHeldItemOnMeal(ItemStack container) {
		ItemStack meal = getMeal();
		if (isContainerValid(container) && !meal.isEmpty()) {
			container.shrink(1);
			ItemStack serving = meal.split(1);
			setStack(MEAL_DISPLAY_SLOT, meal);
			inventoryChanged();
			return serving;
		}
		return ItemStack.EMPTY;
	}

	private boolean doesMealHaveContainer(ItemStack meal) {
		return !mealContainerStack.isEmpty() || meal.getCraftingRemainder() != null;
	}

	public boolean isContainerValid(ItemStack containerItem) {
		if (containerItem.isEmpty()) return false;
		if (!mealContainerStack.isEmpty()) return ItemStack.isSameItem(mealContainerStack, containerItem);
		return false;
	}

	@Override
	public Component getName() {
		return customName != null ? customName : TextUtils.container("cooking_pot");
	}

	@Override
	public Component getDisplayName() {
		return getName();
	}

	@Override
	@Nullable
	public Component getCustomName() {
		return customName;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
		return new CookingPotMenu(id, player, this, cookingPotData);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag var4;
		try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
			TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, registries);
			writeItems(tagValueOutput);
			var4 = tagValueOutput.buildResult();
		}
		return var4;
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter componentInput) {
		super.applyImplicitComponents(componentInput);
		this.customName = componentInput.get(DataComponents.CUSTOM_NAME);
		ItemStack stack = componentInput.getOrDefault(ModDataComponents.MEAL, ItemStackWrapper.EMPTY).getStack();
		getInventory().set(MEAL_DISPLAY_SLOT, ItemResource.of(stack), stack.count());
		this.mealContainerStack = componentInput.getOrDefault(ModDataComponents.CONTAINER, ItemStackWrapper.EMPTY).getStack();
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(DataComponents.CUSTOM_NAME, this.customName);
		if (!getMeal().isEmpty()) {
			components.set(ModDataComponents.MEAL, new ItemStackWrapper(getMeal()));
		}
		if (!getContainer().isEmpty()) {
			components.set(ModDataComponents.CONTAINER, new ItemStackWrapper(getContainer()));
		}
	}

	private ItemStacksResourceHandler createHandler() {
		return new ItemStacksResourceHandler(INVENTORY_SIZE)
		{
			@Override
			protected int getCapacity(int index, ItemResource resource) {
				if (index == MEAL_DISPLAY_SLOT) {
					// Meals that don't stack (e.g. stews) can still be cooked up to 64 at once
					return Math.max(64, resource.isEmpty() ? 64 : resource.getMaxStackSize());
				}
				return super.getCapacity(index, resource);
			}

			@Override
			protected void onContentsChanged(int index, ItemStack previousContents) {
				inventoryChanged();
			}
		};
	}

	private ContainerData createIntArray() {
		return new ContainerData()
		{
			@Override
			public int get(int index) {
				return switch (index) {
					case 0 -> CookingPotBlockEntity.this.cookTime;
					case 1 -> CookingPotBlockEntity.this.cookTimeTotal;
					default -> 0;
				};
			}

			@Override
			public void set(int index, int value) {
				switch (index) {
					case 0 -> CookingPotBlockEntity.this.cookTime = value;
					case 1 -> CookingPotBlockEntity.this.cookTimeTotal = value;
				}
			}

			@Override
			public int getCount() {
				return 2;
			}
		};
	}

	@Override
	public void clearContent() {
		ItemUtils.clearItems(inventory);
	}

	// 26.x: the block entity is already gone in Block#affectNeighborsAfterRemoval, so contents are dropped here.
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (level instanceof ServerLevel serverLevel) {
			net.minecraft.world.Containers.dropContents(serverLevel, pos, getDroppableInventory());
			getUsedRecipesAndPopExperience(serverLevel, net.minecraft.world.phys.Vec3.atCenterOf(pos));
		}
	}
}
