package vectorwing.farmersdelight.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModDamageTypes;
import vectorwing.farmersdelight.data.advancement.FDAdvancementGenerator;
import vectorwing.farmersdelight.data.loot.FDBlockLoot;
import vectorwing.farmersdelight.data.loot.FDChestLoot;
import vectorwing.farmersdelight.data.tools.StructureUpdater;

import java.util.List;
import java.util.Set;

/**
 * Data generation for Minecraft 26.3.
 * <p>
 * Datapack-registry content (recipes, loot tables, advancements, villager trades, damage types, enchantments)
 * is generated through {@link GatherDataRegistryEntriesEvent}; tags, loot modifiers and sounds through
 * classic providers in {@link GatherDataEvent}.
 * <p>
 * Static (hand-maintained) resources live in {@code src/main/resources}: block states, block/item models
 * and item model definitions, worldgen features/placed features and NeoForge biome modifiers.
 */
@EventBusSubscriber(modid = FarmersDelight.MODID)
public class DataGenerators
{
	@SubscribeEvent
	public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
		event.add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrapDamageTypes)
			.add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)
			.add(Registries.VILLAGER_TRADE, VillagerTrades::init)
			.add(Registries.LOOT_TABLE, new LootTableProvider(Set.of(), List.of(
				new LootTableProvider.SubProviderEntry(FDBlockLoot::new, LootContextParamSets.BLOCK),
				new LootTableProvider.SubProviderEntry(FDChestLoot::new, LootContextParamSets.CHEST)
			)))
			.add(Registries.ADVANCEMENT, new AdvancementProvider(List.of(FDAdvancementGenerator::new)))
			.add(Recipes.bootstrap());
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		event.createBlockAndItemTags(BlockTags::new, ItemTags::new);
		event.createProvider(EntityTags::new);
		event.createProvider(DamageTypeTags::new);
		event.createProvider(EnchantmentTags::new);
		event.createProvider(VillagerTags::new);
		event.createProvider(LootModifiers::new);
		event.createProvider(DataMaps::new);
		event.createProvider(SoundDefinitions::new);
		event.addProvider(new StructureUpdater("structures/village/houses", FarmersDelight.MODID, event.getGenerator().getPackOutput(), event.getResourceManager(PackType.SERVER_DATA)));
	}
}
