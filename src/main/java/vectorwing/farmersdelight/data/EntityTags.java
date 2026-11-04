package vectorwing.farmersdelight.data;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.concurrent.CompletableFuture;

public class EntityTags extends EntityTypeTagsProvider
{
	public EntityTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, FarmersDelight.MODID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		fdTag(ModTags.EntityTypes.DOG_FOOD_USERS).add(EntityType.WOLF);
		fdTag(ModTags.EntityTypes.HORSE_FEED_USERS).add(
			EntityType.HORSE,
			EntityType.SKELETON_HORSE,
			EntityType.ZOMBIE_HORSE,
			EntityType.DONKEY,
			EntityType.MULE,
			EntityType.LLAMA);
		fdTag(ModTags.EntityTypes.HORSE_FEED_TEMPTED).add(
			EntityType.HORSE,
			EntityType.DONKEY,
			EntityType.MULE);
	}

	private vectorwing.farmersdelight.data.tools.FDTagAppender<EntityType<?>> fdTag(net.minecraft.tags.TagKey<EntityType<?>> tag) {
		return new vectorwing.farmersdelight.data.tools.FDTagAppender<>(this.tag(tag), BuiltInRegistries.ENTITY_TYPE);
	}
}
