package vectorwing.farmersdelight.common.world;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class VillageStructures
{
	public static void addNewVillageBuilding(final ServerAboutToStartEvent event) {
		if (Configuration.GENERATE_VILLAGE_COMPOST_HEAPS.get()) {
			Registry<StructureTemplatePool> templatePools = event.getServer().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
			Registry<StructureProcessorList> processorLists = event.getServer().registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);

			VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/plains/houses"), FarmersDelight.MODID + ":village/houses/plains_compost_pile", 5);
			VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/snowy/houses"), FarmersDelight.MODID + ":village/houses/snowy_compost_pile", 3);
			VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/savanna/houses"), FarmersDelight.MODID + ":village/houses/savanna_compost_pile", 4);
			VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/desert/houses"), FarmersDelight.MODID + ":village/houses/desert_compost_pile", 3);
			VillageStructures.addBuildingToPool(templatePools, processorLists, Identifier.parse("minecraft:village/taiga/houses"), FarmersDelight.MODID + ":village/houses/taiga_compost_pile", 4);
		}

		if (Configuration.GENERATE_VILLAGE_FARM_FD_CROPS.get()) {
			Registry<StructureProcessorList> processorLists = event.getServer().registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);

			StructureProcessor temperateCropProcessor = cropProcessor(List.of(Blocks.WHEAT), List.of(
				new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, ModBlocks.CABBAGE_CROP.get().defaultBlockState()),
				new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, ModBlocks.TOMATO_CROP.get().defaultBlockState()),
				new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, ModBlocks.ONION_CROP.get().defaultBlockState())
			));

			StructureProcessor coldCropProcessor = cropProcessor(List.of(Blocks.WHEAT, Blocks.POTATOES), List.of(
				new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, ModBlocks.CABBAGE_CROP.get().defaultBlockState()),
				new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, ModBlocks.ONION_CROP.get().defaultBlockState()),
				new ProcessorRule(new RandomBlockMatchTest(Blocks.POTATOES, 0.2F), AlwaysTrueTest.INSTANCE, ModBlocks.CABBAGE_CROP.get().defaultBlockState()),
				new ProcessorRule(new RandomBlockMatchTest(Blocks.POTATOES, 0.2F), AlwaysTrueTest.INSTANCE, ModBlocks.ONION_CROP.get().defaultBlockState())
			));

			StructureProcessor aridCropProcessor = cropProcessor(List.of(Blocks.WHEAT), List.of(
				new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, ModBlocks.CABBAGE_CROP.get().defaultBlockState()),
				new ProcessorRule(new RandomBlockMatchTest(Blocks.WHEAT, 0.3F), AlwaysTrueTest.INSTANCE, ModBlocks.TOMATO_CROP.get().defaultBlockState())
			));

			addNewRuleToProcessorList(Identifier.parse("minecraft:farm_plains"), temperateCropProcessor, processorLists);
			addNewRuleToProcessorList(Identifier.parse("minecraft:farm_savanna"), aridCropProcessor, processorLists);
			addNewRuleToProcessorList(Identifier.parse("minecraft:farm_snowy"), coldCropProcessor, processorLists);
			addNewRuleToProcessorList(Identifier.parse("minecraft:farm_taiga"), temperateCropProcessor, processorLists);
			addNewRuleToProcessorList(Identifier.parse("minecraft:farm_desert"), aridCropProcessor, processorLists);
		}
	}

	public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, Identifier poolRL, String nbtPieceRL, int weight) {
		StructureTemplatePool pool = templatePoolRegistry.getValue(poolRL);
		if (pool == null) return;

		Identifier emptyProcessor = Identifier.withDefaultNamespace("empty");
		Holder<StructureProcessorList> processorHolder = processorListRegistry.getOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, emptyProcessor));

		SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, processorHolder).apply(StructureTemplatePool.Projection.RIGID);

		for (int i = 0; i < weight; i++) {
			pool.templates.add(piece);
		}

		List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
		listOfPieceEntries.add(new Pair<>(piece, weight));
		pool.rawTemplates = listOfPieceEntries;
	}

	/**
	 * Vanilla's farm processors draw at most this many random numbers per block.
	 */
	private static final int VANILLA_FARM_DRAWS = 3;

	/**
	 * A RuleProcessor seeds its random from the block position, so a second one on the same farm reuses the numbers
	 * vanilla's farm processor already drew: wheat that vanilla left alone always rolled 0.3 or more first, so a 0.3
	 * cabbage rule could never hit (plains and taiga farms had no cabbages). Leading rules that can never match
	 * (probability 0) draw and discard those numbers for each input block, so FD's rules roll fresh ones.
	 */
	private static StructureProcessor cropProcessor(List<Block> inputs, List<ProcessorRule> rules) {
		List<ProcessorRule> all = new ArrayList<>();
		for (Block input : inputs) {
			for (int i = 0; i < VANILLA_FARM_DRAWS; i++) {
				all.add(new ProcessorRule(new RandomBlockMatchTest(input, 0.0F), AlwaysTrueTest.INSTANCE, input.defaultBlockState()));
			}
		}
		all.addAll(rules);
		return new RuleProcessor(all);
	}

	private static void addNewRuleToProcessorList(Identifier targetProcessorList, StructureProcessor processorToAdd, Registry<StructureProcessorList> processorListRegistry) {
		processorListRegistry.getOptional(targetProcessorList)
			.ifPresent(processorList -> {
				List<StructureProcessor> newSafeList = new ArrayList<>(processorList.list());
				newSafeList.add(processorToAdd);
				processorList.list = newSafeList;
			});
	}
}
