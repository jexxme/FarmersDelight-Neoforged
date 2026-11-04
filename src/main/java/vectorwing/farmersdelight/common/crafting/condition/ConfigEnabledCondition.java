package vectorwing.farmersdelight.common.crafting.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.Configuration;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Load condition that checks a boolean option of the common config.
 * Used for data that became data-driven in 26.x (e.g. villager trades) so the old config toggles keep working.
 * <p>
 * JSON: {@code {"type": "farmersdelight:config_enabled", "option": "enableFarmerFDTrades"}}
 */
public record ConfigEnabledCondition(String option) implements ICondition
{
	private static final Map<String, Supplier<Supplier<Boolean>>> OPTIONS = Map.of(
			"enableFarmerFDTrades", () -> Configuration.ENABLE_FARMERS_BUY_FD_CROPS,
			"enableWanderingTraderFDTrades", () -> Configuration.ENABLE_WANDERING_TRADER_SELLS_FD_ITEMS,
			"enableVanillaCropCrates", () -> Configuration.ENABLE_VANILLA_CROP_CRATES
	);

	public static final MapCodec<ConfigEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.STRING.validate(option -> OPTIONS.containsKey(option)
					? com.mojang.serialization.DataResult.success(option)
					: com.mojang.serialization.DataResult.error(() -> "Unknown Farmer's Delight config option: " + option + ", expected one of " + OPTIONS.keySet())
			).fieldOf("option").forGetter(ConfigEnabledCondition::option)
	).apply(inst, ConfigEnabledCondition::new));

	@Override
	public boolean test(@NotNull IContext context) {
		return OPTIONS.get(option).get().get();
	}

	@Override
	public @NotNull MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
