package vectorwing.farmersdelight.common.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.world.feature.InOrderFeature;
import vectorwing.farmersdelight.common.world.feature.WildRiceFeature;

import java.util.function.Supplier;

public class ModBiomeFeatures
{
	// 26.3: Features are now codec-dispatched records; configured features were merged into them.
	public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, FarmersDelight.MODID);

	public static final Supplier<MapCodec<InOrderFeature>> IN_ORDER = FEATURES.register("in_order", () -> InOrderFeature.CODEC);
	public static final Supplier<MapCodec<WildRiceFeature>> WILD_RICE = FEATURES.register("wild_rice", () -> WildRiceFeature.CODEC);
}
