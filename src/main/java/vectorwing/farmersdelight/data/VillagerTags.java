package vectorwing.farmersdelight.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.world.item.trading.VillagerTrade;
import vectorwing.farmersdelight.FarmersDelight;

import java.util.concurrent.CompletableFuture;

/**
 * Adds the Farmer's Delight trades to the vanilla trade pools (data-driven since 26.3).
 */
public class VillagerTags extends TagsProvider<VillagerTrade>
{
	public VillagerTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, Registries.VILLAGER_TRADE, lookupProvider, FarmersDelight.MODID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(VillagerTradeTags.FARMER_LEVEL_1)
				.addOptional(VillagerTrades.FARMER_1_ONION_EMERALD)
				.addOptional(VillagerTrades.FARMER_1_TOMATO_EMERALD);
		this.tag(VillagerTradeTags.FARMER_LEVEL_2)
				.addOptional(VillagerTrades.FARMER_2_CABBAGE_EMERALD)
				.addOptional(VillagerTrades.FARMER_2_RICE_EMERALD);
		this.tag(VillagerTradeTags.WANDERING_TRADER_COMMON)
				.addOptional(VillagerTrades.WANDERING_TRADER_CABBAGE_EMERALD)
				.addOptional(VillagerTrades.WANDERING_TRADER_RICE_EMERALD)
				.addOptional(VillagerTrades.WANDERING_TRADER_TOMATO_EMERALD)
				.addOptional(VillagerTrades.WANDERING_TRADER_ONION_EMERALD);
	}
}
