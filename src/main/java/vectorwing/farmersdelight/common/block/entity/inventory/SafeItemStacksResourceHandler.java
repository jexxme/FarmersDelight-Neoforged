package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import vectorwing.farmersdelight.FarmersDelight;

/**
 * An {@link ItemStacksResourceHandler} that loads slot by slot, like vanilla containers do.
 * <p>
 * The NeoForge default decodes the whole slot list in one go: a single unreadable stack (an item from a removed mod,
 * a broken component) throws the whole inventory away, and a saved list of another length resizes the handler.
 * Here an unreadable slot is left empty, every other slot keeps its items, and the size never changes.
 */
public class SafeItemStacksResourceHandler extends ItemStacksResourceHandler
{
	public SafeItemStacksResourceHandler(int size) {
		super(size);
	}

	@Override
	public void deserialize(ValueInput input) {
		int index = 0;
		for (ValueInput entry : input.childrenListOrEmpty(StacksResourceHandler.VALUE_IO_KEY)) {
			if (index >= stacks.size()) {
				FarmersDelight.LOGGER.warn("Ignoring saved items beyond slot {} (inventory has {} slots)", index, stacks.size());
				break;
			}
			ItemStack stack = entry.read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY);
			stacks.set(index++, stack);
		}
		while (index < stacks.size()) {
			stacks.set(index++, ItemStack.EMPTY);
		}
	}
}
