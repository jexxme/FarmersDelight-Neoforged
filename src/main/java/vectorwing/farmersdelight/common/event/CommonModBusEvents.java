package vectorwing.farmersdelight.common.event;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import vectorwing.farmersdelight.common.block.state.CanvasSign;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.FoodValues;
import vectorwing.farmersdelight.common.ItemDefaultComponents;

import java.util.Optional;

@EventBusSubscriber(modid = FarmersDelight.MODID)
public class CommonModBusEvents
{
	@SubscribeEvent
	public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
		ItemDefaultComponents.apply(event);
		if (DatagenModLoader.isRunningDataGen()) {
			return;
		}
		if (Configuration.ENABLE_STACKABLE_SOUP_ITEMS.get()) {
			Configuration.SOUP_ITEM_LIST.get().forEach((key) -> {
				Identifier id = Identifier.tryParse(key);
				Optional<Holder.Reference<Item>> item = id == null ? Optional.empty() : BuiltInRegistries.ITEM.get(id);
				if (item.isEmpty()) {
					FarmersDelight.LOGGER.warn("soupItemList: '{}' is not a known item id, skipping it", key);
				}
				item.ifPresent(itemReference -> event.modify(itemReference.value(), (builder, provider, item2) -> builder.set(DataComponents.MAX_STACK_SIZE, 16)));
			});
		}
		if (Configuration.ENABLE_RABBIT_STEW_BUFF.get()) {
			event.modify(Items.RABBIT_STEW, (builder, provider, item) -> {
				builder.set(DataComponents.FOOD, FoodValues.RABBIT_STEW_BUFF);
				builder.set(DataComponents.CONSUMABLE, FoodValues.ConsumableValues.RABBIT_STEW_BUFF);
			});
		}
	}

	/**
	 * 26.3: Canvas Signs are plain vanilla signs now, so they need to be valid blocks for the vanilla sign block entity types.
	 */
	@SubscribeEvent
	public static void onAddBlockEntityTypeBlocks(BlockEntityTypeAddBlocksEvent event) {
		Block[] signs = BuiltInRegistries.BLOCK.stream()
				.filter(block -> block instanceof CanvasSign && !(block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock))
				.toArray(Block[]::new);
		Block[] hangingSigns = BuiltInRegistries.BLOCK.stream()
				.filter(block -> block instanceof CanvasSign && (block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock))
				.toArray(Block[]::new);
		event.modify(BlockEntityTypes.SIGN, signs);
		event.modify(BlockEntityTypes.HANGING_SIGN, hangingSigns);
	}
}
