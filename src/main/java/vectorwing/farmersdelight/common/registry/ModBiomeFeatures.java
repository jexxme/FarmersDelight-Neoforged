package vectorwing.farmersdelight.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.world.configuration.InOrderFeatureConfiguration;
import vectorwing.farmersdelight.common.world.feature.InOrderFeature;
import vectorwing.farmersdelight.common.world.feature.WildRiceFeature;

import java.util.function.Supplier;

public class ModBiomeFeatures
{
	// Configured features carry the block predicates; placed features only position them.
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, FarmersDelight.MODID);

	public static final Supplier<Feature<InOrderFeatureConfiguration>> IN_ORDER = FEATURES.register("in_order", () -> new InOrderFeature(InOrderFeatureConfiguration.CODEC));
	public static final Supplier<Feature<NoneFeatureConfiguration>> WILD_RICE = FEATURES.register("wild_rice", () -> new WildRiceFeature(NoneFeatureConfiguration.CODEC));
}
