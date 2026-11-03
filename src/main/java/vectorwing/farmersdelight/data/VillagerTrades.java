package vectorwing.farmersdelight.data;

import net.neoforged.neoforge.common.conditions.ICondition;
import vectorwing.farmersdelight.common.crafting.condition.ConfigEnabledCondition;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class VillagerTrades
{
    // Keep the trades toggleable through the common config (trades are data-driven since 26.3)
    private static final ICondition FARMER_TRADES = new ConfigEnabledCondition("enableFarmerFDTrades");
    private static final ICondition WANDERING_TRADES = new ConfigEnabledCondition("enableWanderingTraderFDTrades");



    public static final ResourceKey<VillagerTrade> FARMER_1_ONION_EMERALD = resourceKey("farmer/1/onion_emerald");
    public static final ResourceKey<VillagerTrade> FARMER_1_TOMATO_EMERALD = resourceKey("farmer/1/tomato_emerald");
    public static final ResourceKey<VillagerTrade> FARMER_2_CABBAGE_EMERALD = resourceKey("farmer/2/cabbage_emerald");
    public static final ResourceKey<VillagerTrade> FARMER_2_RICE_EMERALD = resourceKey("farmer/2/rice_emerald");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_CABBAGE_EMERALD = resourceKey("wandering_trader/cabbage_emerald");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_TOMATO_EMERALD = resourceKey("wandering_trader/tomato_emerald");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_RICE_EMERALD = resourceKey("wandering_trader/rice_emerald");
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_ONION_EMERALD = resourceKey("wandering_trader/onion_emerald");

    public static void init(final BootstrapContext<VillagerTrade> context) {
        onVillagerTrades(context);
        onWandererTrades(context);
    }

    public static void onVillagerTrades(final BootstrapContext<VillagerTrade> context) {
        context.register(FARMER_1_ONION_EMERALD, emeraldsForItemsTrade(ModItems.ONION.get(), 26, 16, 2), FARMER_TRADES);
        context.register(FARMER_1_TOMATO_EMERALD, emeraldsForItemsTrade(ModItems.TOMATO.get(), 26, 16, 2), FARMER_TRADES);

        context.register(FARMER_2_CABBAGE_EMERALD, emeraldsForItemsTrade(ModItems.CABBAGE.get(), 16, 16, 5), FARMER_TRADES);
        context.register(FARMER_2_RICE_EMERALD, emeraldsForItemsTrade(ModItems.RICE.get(), 20, 16, 5), FARMER_TRADES);
    }

    public static void onWandererTrades(BootstrapContext<VillagerTrade> context) {
        context.register(WANDERING_TRADER_CABBAGE_EMERALD, itemForEmeraldTrade(ModItems.CABBAGE_SEEDS.get(), 1, 12), WANDERING_TRADES);
        context.register(WANDERING_TRADER_TOMATO_EMERALD, itemForEmeraldTrade(ModItems.TOMATO_SEEDS.get(), 1, 12), WANDERING_TRADES);
        context.register(WANDERING_TRADER_RICE_EMERALD, itemForEmeraldTrade(ModItems.RICE.get(), 1, 12), WANDERING_TRADES);
        context.register(WANDERING_TRADER_ONION_EMERALD, itemForEmeraldTrade(ModItems.ONION.get(), 1, 12), WANDERING_TRADES);
    }

    public static VillagerTrade emeraldsForItemsTrade(ItemLike item, int count, int maxTrades, int xp) {
        return VillagerTrade.builder(new TradeCost(item.asItem(), count), new ItemStackTemplate(Items.EMERALD), maxTrades, xp, 0.05f).build();
    }

    public static VillagerTrade itemForEmeraldTrade(ItemLike item, int maxTrades, int xp) {
        return VillagerTrade.builder(new TradeCost(Items.EMERALD, 1), new ItemStackTemplate(item.asItem(), 1), maxTrades, xp, 0.05f).build();
    }

    public static ResourceKey<VillagerTrade> resourceKey(final String path) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, FarmersDelight.id(path));
    }


}