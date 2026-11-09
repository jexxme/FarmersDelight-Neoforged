package vectorwing.farmersdelight.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import vectorwing.farmersdelight.common.block.TomatoOnSupportBlock;
import vectorwing.farmersdelight.common.registry.ModBlockEntityTypes;

import javax.annotation.Nullable;

/**
 * The block a {@link TomatoOnSupportBlock} grew into (a trellis, for example), exactly as it was: drawn under the
 * leaves, collided with, and put back when the vine goes.
 */
public class TomatoSupportBlockEntity extends SyncedBlockEntity
{
	private static final String SUPPORT = "support";

	private BlockState support = Blocks.AIR.defaultBlockState();

	public TomatoSupportBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntityTypes.TOMATO_SUPPORT.get(), pos, state);
	}

	/** The block inside, or {@code null} if there is none (a vine placed by a command). */
	@Nullable
	public BlockState getSupport() {
		return support.isAir() ? null : support;
	}

	public void setSupport(BlockState support) {
		support = holdable(support);
		if (support == this.support) {
			return;
		}
		this.support = support;
		inventoryChanged(); // saved and sent to the clients, which draw it
	}

	/** Remembered for this game tick, so the vine block can put the support back once this block entity is gone. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (level != null && !support.isAir()) {
			TomatoOnSupportBlock.stash(level, pos, support);
		}
	}

	/**
	 * Only a plain block can be held: never a tomato vine or anything with a block entity of its own (from a command or
	 * a pasted structure), which would draw and collide through itself without end.
	 */
	private static BlockState holdable(BlockState support) {
		return support.getBlock() instanceof TomatoOnSupportBlock || support.hasBlockEntity() ? Blocks.AIR.defaultBlockState() : support;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		support = holdable(input.read(SUPPORT, BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState()));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!support.isAir()) {
			output.store(SUPPORT, BlockState.CODEC, support);
		}
	}

	@Override
	public void onDataPacket(Connection connection, ValueInput input) {
		super.onDataPacket(connection, input);
		// The support is drawn with the vine: draw the block again now that the client knows it
		if (level != null && level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
		}
	}
}
