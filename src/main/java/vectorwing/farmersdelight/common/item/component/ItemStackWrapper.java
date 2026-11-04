package vectorwing.farmersdelight.common.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class ItemStackWrapper
{
	public static final ItemStackWrapper EMPTY = new ItemStackWrapper(ItemStack.EMPTY);
	public static final Codec<ItemStackWrapper> CODEC = ItemStack.OPTIONAL_CODEC.xmap(ItemStackWrapper::new, ItemStackWrapper::getStack);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackWrapper> STREAM_CODEC =
			ItemStack.OPTIONAL_STREAM_CODEC.map(ItemStackWrapper::new, (itemStackWrapper) -> itemStackWrapper.itemStack);

	private final ItemStack itemStack;
	private final int hashCode;

	/**
	 * Data component values must never change once created, because copies of an item share them. The stack is copied
	 * on the way in and on the way out, so changing a returned stack (like taking a serving) can't leak into other items.
	 */
	public ItemStackWrapper(ItemStack stack) {
		this.itemStack = stack.copy();
		this.hashCode = ItemStack.hashItemAndComponents(stack);
	}

	public ItemStack getStack() {
		return this.itemStack.copy();
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ItemStackWrapper itemStackWrapper) {
			return ItemStack.matches(this.itemStack, itemStackWrapper.itemStack);
		}
		return false;
	}
}
