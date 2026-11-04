package vectorwing.farmersdelight.common.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/**
 * Passes when the broken block is in a block tag. Minecraft 26.3 has this built in ({@code minecraft:match_block});
 * this version still needs it so that {@code #farmersdelight:drops_straw} can drive straw drops (upstream #1411).
 */
public record BlockTagCondition(TagKey<Block> tag) implements LootItemCondition
{
	public static final MapCodec<BlockTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			TagKey.hashedCodec(Registries.BLOCK).fieldOf("tag").forGetter(BlockTagCondition::tag)
	).apply(instance, BlockTagCondition::new));

	@Override
	public MapCodec<BlockTagCondition> codec() {
		return CODEC;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.BLOCK_STATE);
	}

	@Override
	public boolean test(LootContext context) {
		BlockState state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
		return state != null && state.is(this.tag);
	}

	public static LootItemCondition.Builder blockInTag(TagKey<Block> tag) {
		return () -> new BlockTagCondition(tag);
	}
}
