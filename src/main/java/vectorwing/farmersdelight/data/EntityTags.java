package vectorwing.farmersdelight.data;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
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
		fdTag(ModTags.EntityTypes.DOG_FOOD_USERS).add(EntityTypes.WOLF);
		fdTag(ModTags.EntityTypes.HORSE_FEED_USERS).add(
			EntityTypes.HORSE,
			EntityTypes.SKELETON_HORSE,
			EntityTypes.ZOMBIE_HORSE,
			EntityTypes.DONKEY,
			EntityTypes.MULE,
			EntityTypes.LLAMA);
		fdTag(ModTags.EntityTypes.HORSE_FEED_TEMPTED).add(
			EntityTypes.HORSE,
			EntityTypes.DONKEY,
			EntityTypes.MULE);
	}

	private vectorwing.farmersdelight.data.tools.FDTagAppender<EntityType<?>> fdTag(net.minecraft.tags.TagKey<EntityType<?>> tag) {
		return new vectorwing.farmersdelight.data.tools.FDTagAppender<>(this.tag(tag), BuiltInRegistries.ENTITY_TYPE);
	}
}
