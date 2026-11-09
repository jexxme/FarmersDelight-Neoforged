package vectorwing.farmersdelight.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.entity.TomatoSupportBlockEntity;
import vectorwing.farmersdelight.common.tag.ModTags;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A tomato vine on a block of {@code #farmersdelight:tomato_supports} that is not a rope (another mod's trellis, for
 * example). It grows, ripens and is picked like the vine on a rope; the block it climbed is kept exactly as it was
 * (its whole block state) in a {@link TomatoSupportBlockEntity}: drawn under the leaves (the client model wrapper),
 * bumped into like before, and put back whenever the vine goes: broken by a player, blown up, burnt, or gone because
 * the plant below it or the light went.
 * <p>
 * Ropes keep their own vine ({@link HangingTomatoBlock}), so worlds with tomatoes on ropes don't change.
 */
public class TomatoOnSupportBlock extends HangingTomatoBlock implements EntityBlock
{
	private static final VoxelShape LEAVES = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
	/** The support of a vine that just went, for {@link #affectNeighborsAfterRemoval} (its block entity is gone by then). */
	private static final Map<Level, Map<BlockPos, Stashed>> STASH = new WeakHashMap<>();

	public TomatoOnSupportBlock(Properties properties) {
		super(properties);
	}

	/**
	 * True for a block a tomato vine can grow into and hold: in {@code #farmersdelight:tomato_supports}, not a rope
	 * (ropes have their own vine), not a tomato vine, no block entity of its own, and not waterlogged (the vine
	 * would drain it).
	 */
	public static boolean canHold(BlockState state) {
		return state.is(ModTags.Blocks.TOMATO_SUPPORTS)
				&& !state.is(ModTags.Blocks.ROPES)
				&& !(state.getBlock() instanceof TomatoBlock)
				&& !state.hasBlockEntity()
				&& !(state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED));
	}

	/** Puts {@code vine} (this block, any age) at {@code pos}, holding {@code support}, the block that stood there. */
	public static void place(Level level, BlockPos pos, BlockState vine, BlockState support) {
		level.setBlock(pos, vine, Block.UPDATE_ALL);
		if (level.getBlockEntity(pos) instanceof TomatoSupportBlockEntity entity) {
			entity.setSupport(support);
		}
	}

	/** The block a vine at {@code pos} holds, or {@code null}. */
	@Nullable
	public static BlockState supportAt(BlockGetter level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof TomatoSupportBlockEntity entity ? entity.getSupport() : null;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TomatoSupportBlockEntity(pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		BlockState support = supportAt(level, pos);
		return support == null ? LEAVES : Shapes.or(support.getShape(level, pos, context), LEAVES);
	}

	/** You bump into the support as before; the leaves let you through. */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		BlockState support = supportAt(level, pos);
		return support == null ? Shapes.empty() : support.getCollisionShape(level, pos, context);
	}

	/** Mobs walk around it, as around the support. */
	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	/** Climbable if the support is. */
	@Override
	public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
		BlockState support = supportAt(level, pos);
		return support != null && support.isLadder(level, pos, entity);
	}

	/** Burns like the support (the support itself is put back once the vine has burnt away). */
	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		BlockState support = supportAt(level, pos);
		return support == null ? 0 : support.getFlammability(level, pos, direction);
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		BlockState support = supportAt(level, pos);
		return support == null ? 0 : support.getFireSpreadSpeed(level, pos, direction);
	}

	/** A player breaks the vine: the support stays (the vine's own drops still drop). */
	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
		// The game already called playerWillDestroy (sound, particles, game event) just before this
		BlockState support = supportAt(level, pos);
		return level.setBlock(pos, support != null ? support : fluid.createLegacyBlock(), level.isClientSide() ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
	}

	/** Gone by anything but a player (blown up, burnt, no plant below, no light, a command): the support comes back. */
	@Override
	protected void restoreAfterRemoval(ServerLevel level, BlockPos pos, boolean movedByPiston) {
		BlockState support = takeStash(level, pos);
		BlockState now = level.getBlockState(pos);
		// Burnt away: the support puts the fire out
		if (support != null && !movedByPiston && (now.isAir() || now.is(BlockTags.FIRE))) {
			level.setBlock(pos, support, Block.UPDATE_ALL);
		}
	}

	/** Kept for this game tick by the block entity just before it goes (see {@link TomatoSupportBlockEntity#preRemoveSideEffects}). */
	public static void stash(Level level, BlockPos pos, BlockState support) {
		if (level.isClientSide()) {
			return; // only the server puts supports back (and the client must not share this map with the server thread)
		}
		Map<BlockPos, Stashed> stashed = STASH.computeIfAbsent(level, l -> new HashMap<>());
		long now = level.getGameTime();
		stashed.values().removeIf(entry -> entry.gameTime() != now); // never taken (the player-break path places it itself)
		stashed.put(pos.immutable(), new Stashed(support, now));
	}

	/** The support stashed for {@code pos} in this game tick (anything older is stale), removed either way. */
	@Nullable
	private static BlockState takeStash(Level level, BlockPos pos) {
		Map<BlockPos, Stashed> stashed = STASH.get(level);
		Stashed entry = stashed == null ? null : stashed.remove(pos);
		return entry != null && entry.gameTime() == level.getGameTime() ? entry.support() : null;
	}

	private record Stashed(BlockState support, long gameTime)
	{
	}
}
