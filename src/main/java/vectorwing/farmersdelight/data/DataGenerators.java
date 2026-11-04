package vectorwing.farmersdelight.data;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModDamageTypes;
import vectorwing.farmersdelight.data.advancement.FDAdvancementGenerator;
import vectorwing.farmersdelight.data.loot.FDBlockLoot;
import vectorwing.farmersdelight.data.loot.FDChestLoot;
import vectorwing.farmersdelight.data.tools.StructureUpdater;

import java.util.List;
import java.util.Set;

/**
 * Data generation.
 * <p>
 * Datapack-registry content (damage types, enchantments, villager trades) goes through a {@link RegistrySetBuilder};
 * recipes, loot tables, advancements, tags, loot modifiers, data maps and sounds through classic providers.
 * <p>
 * Static (hand-maintained) resources live in {@code src/main/resources}: block states, block/item models
 * and item model definitions, worldgen features/placed features and NeoForge biome modifiers.
 */
@EventBusSubscriber(modid = FarmersDelight.MODID)
public class DataGenerators
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		PackOutput output = event.getGenerator().getPackOutput();

		RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder()
			.add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrapDamageTypes)
			.add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)
			.add(Registries.VILLAGER_TRADE, VillagerTrades::init);
		event.createDatapackRegistryObjects(registrySetBuilder, VillagerTrades::conditions);

		event.createProvider(Recipes.Runner::new);
		event.createProvider((packOutput, registries) -> new LootTableProvider(packOutput, Set.of(), List.of(
			new LootTableProvider.SubProviderEntry(FDBlockLoot::new, LootContextParamSets.BLOCK),
			new LootTableProvider.SubProviderEntry(FDChestLoot::new, LootContextParamSets.CHEST)
		), registries));
		event.createProvider((packOutput, registries) -> new AdvancementProvider(packOutput, registries, List.of(new FDAdvancementGenerator())));

		event.createBlockAndItemTags(BlockTags::new, ItemTags::new);
		event.createProvider(EntityTags::new);
		event.createProvider(DamageTypeTags::new);
		event.createProvider(EnchantmentTags::new);
		event.createProvider(VillagerTags::new);
		event.createProvider(LootModifiers::new);
		event.createProvider(DataMaps::new);
		event.createProvider(SoundDefinitions::new);
		event.addProvider(new StructureUpdater("structures/village/houses", FarmersDelight.MODID, output, event.getResourceManager(PackType.SERVER_DATA)));
	}
}
