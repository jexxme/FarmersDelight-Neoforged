package vectorwing.farmersdelight.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.NullMarked;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.entity.*;

import java.util.function.Supplier;

@NullMarked
public class ModBlockEntityTypes
{
	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FarmersDelight.MODID);

	public static final Supplier<BlockEntityType<StoveBlockEntity>> STOVE = TILES.register("stove",
		() -> new BlockEntityType<>(StoveBlockEntity::new, ModBlocks.STOVE.get()));
	public static final Supplier<BlockEntityType<CookingPotBlockEntity>> COOKING_POT = TILES.register("cooking_pot",
		() -> new BlockEntityType<>(CookingPotBlockEntity::new, ModBlocks.COOKING_POT.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasketBlockEntity>> BASKET = TILES.register("basket",
		() -> new BlockEntityType<>(BasketBlockEntity::new, ModBlocks.WOODEN_BASKET.get(), ModBlocks.BAMBOO_BASKET.get()));
	public static final Supplier<BlockEntityType<CuttingBoardBlockEntity>> CUTTING_BOARD = TILES.register("cutting_board",
		() -> new BlockEntityType<>(CuttingBoardBlockEntity::new, ModBlocks.CUTTING_BOARD.get()));
	public static final Supplier<BlockEntityType<SkilletBlockEntity>> SKILLET = TILES.register("skillet",
		() -> new BlockEntityType<>(SkilletBlockEntity::new, ModBlocks.SKILLET.get()));
	public static final Supplier<BlockEntityType<TomatoSupportBlockEntity>> TOMATO_SUPPORT = TILES.register("tomato_support",
		() -> new BlockEntityType<>(TomatoSupportBlockEntity::new, ModBlocks.TOMATO_CROP_ON_SUPPORT.get()));
	public static final Supplier<BlockEntityType<CabinetBlockEntity>> CABINET = TILES.register("cabinet",
		() -> new BlockEntityType<>(CabinetBlockEntity::new,
			ModBlocks.OAK_CABINET.get(),
			ModBlocks.BIRCH_CABINET.get(),
			ModBlocks.SPRUCE_CABINET.get(),
			ModBlocks.JUNGLE_CABINET.get(),
			ModBlocks.ACACIA_CABINET.get(),
			ModBlocks.DARK_OAK_CABINET.get(),
			ModBlocks.MANGROVE_CABINET.get(),
			ModBlocks.BAMBOO_CABINET.get(),
			ModBlocks.CHERRY_CABINET.get(),
			ModBlocks.PALE_OAK_CABINET.get(),
			ModBlocks.CRIMSON_CABINET.get(),
			ModBlocks.WARPED_CABINET.get())
	);
}
