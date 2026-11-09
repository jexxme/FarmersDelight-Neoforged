package vectorwing.farmersdelight.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import vectorwing.farmersdelight.common.block.TomatoOnSupportBlock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A tomato vine on a support ({@link TomatoOnSupportBlock}): its leaves (the block state's own model), plus the block
 * it holds, drawn with that block's own model and read from the vine's block entity. So a trellis stays that trellis
 * under the leaves, whichever mod it comes from.
 */
public final class SupportedTomatoModel extends DelegateBlockStateModel
{
	private SupportedTomatoModel(BlockStateModel leaves) {
		super(leaves);
	}

	/** Wraps the baked model of every tomato-on-support state. */
	public static void wrap(ModelEvent.ModifyBakingResult event) {
		for (Map.Entry<BlockState, BlockStateModel> entry : event.getBakingResult().blockStateModels().entrySet()) {
			if (entry.getKey().getBlock() instanceof TomatoOnSupportBlock) {
				entry.setValue(new SupportedTomatoModel(entry.getValue()));
			}
		}
	}

	@Nullable
	private static BlockStateModel supportModel(@Nullable BlockState support) {
		return support == null ? null : Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(support);
	}

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		delegate.collectParts(level, pos, state, random, parts);
		BlockState support = TomatoOnSupportBlock.supportAt(level, pos);
		BlockStateModel model = supportModel(support);
		if (model != null) {
			model.collectParts(level, pos, support, random, parts);
		}
	}

	@Override
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		BlockState support = TomatoOnSupportBlock.supportAt(level, pos);
		BlockStateModel model = supportModel(support);
		int leaves = delegate.materialFlags(level, pos, state);
		return model == null ? leaves : leaves | model.materialFlags(level, pos, support);
	}

	/** Drawn from the block entity each time: no geometry to share between positions. */
	@Nullable
	@Override
	public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return null;
	}
}
