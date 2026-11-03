package vectorwing.farmersdelight.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.VillagerCompostable;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.concurrent.CompletableFuture;

/**
 * NeoForge data maps. Fuels and compostables are item components in 26.3 (see ItemDefaultComponents);
 * this only covers villager-specific behaviour.
 */
public class DataMaps extends DataMapProvider
{
	public DataMaps(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider);
	}

	@Override
	protected void gather(HolderLookup.Provider provider) {
		// Farmers pick up Farmer's Delight seeds, so they must also be able to compost them (upstream #1404).
		builder(NeoForgeDataMaps.VILLAGER_COMPOSTABLES)
			.add(ModItems.CABBAGE_SEEDS.get().builtInRegistryHolder(), new VillagerCompostable(true), false)
			.add(ModItems.TOMATO_SEEDS.get().builtInRegistryHolder(), new VillagerCompostable(true), false);
	}
}
