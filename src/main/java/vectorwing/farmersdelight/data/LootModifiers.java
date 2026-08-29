package vectorwing.farmersdelight.data;

import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.world.item.enchantment.Enchantments;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.RicePaniclesBlock;
import vectorwing.farmersdelight.common.loot.modifier.AddItemModifier;
import vectorwing.farmersdelight.common.loot.modifier.FDAddTableLootModifier;
import vectorwing.farmersdelight.common.loot.modifier.PastrySlicingModifier;
import vectorwing.farmersdelight.common.loot.modifier.ReplaceItemModifier;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModChestLootTables;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;

import vectorwing.farmersdelight.common.loot.condition.BlockTagCondition;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LootModifiers extends GlobalLootModifierProvider
{
	public LootModifiers(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, FarmersDelight.MODID);
	}

	@Override
	protected void start() {
		HolderGetter<Item> items = this.registries.lookupOrThrow(Registries.ITEM);
		HolderGetter<EntityType<?>> entityTypes = this.registries.lookupOrThrow(Registries.ENTITY_TYPE);
		HolderGetter<Block> blocks = this.registries.lookupOrThrow(Registries.BLOCK);
		// Chest Loot
		this.add("add_loot_abandoned_mineshaft", this.addNewLootPool(BuiltInLootTables.ABANDONED_MINESHAFT, ModChestLootTables.ABANDONED_MINESHAFT));
		this.add("add_loot_bastion_hoglin_stable", this.addNewLootPool(BuiltInLootTables.BASTION_HOGLIN_STABLE, ModChestLootTables.BASTION_HOGLIN_STABLE));
		this.add("add_loot_bastion_treasure", this.addNewLootPool(BuiltInLootTables.BASTION_TREASURE, ModChestLootTables.BASTION_TREASURE));
		this.add("add_loot_end_city_treasure", this.addNewLootPool(BuiltInLootTables.END_CITY_TREASURE, ModChestLootTables.END_CITY_TREASURE));
		this.add("add_loot_pillager_outpost", this.addNewLootPool(BuiltInLootTables.PILLAGER_OUTPOST, ModChestLootTables.PILLAGER_OUTPOST));
		this.add("add_loot_ruined_portal", this.addNewLootPool(BuiltInLootTables.RUINED_PORTAL, ModChestLootTables.RUINED_PORTAL));
		this.add("add_loot_shipwreck_supply", this.addNewLootPool(BuiltInLootTables.SHIPWRECK_SUPPLY, ModChestLootTables.SHIPWRECK_SUPPLY));
		this.add("add_loot_simple_dungeon", this.addNewLootPool(BuiltInLootTables.SIMPLE_DUNGEON, ModChestLootTables.SIMPLE_DUNGEON));
		this.add("add_loot_village_butcher", this.addNewLootPool(BuiltInLootTables.VILLAGE_BUTCHER, ModChestLootTables.VILLAGE_BUTCHER));
		this.add("add_loot_village_desert_house", this.addNewLootPool(BuiltInLootTables.VILLAGE_DESERT_HOUSE, ModChestLootTables.VILLAGE_DESERT_HOUSE));
		this.add("add_loot_village_plains_house", this.addNewLootPool(BuiltInLootTables.VILLAGE_PLAINS_HOUSE, ModChestLootTables.VILLAGE_PLAINS_HOUSE));
		this.add("add_loot_village_savanna_house", this.addNewLootPool(BuiltInLootTables.VILLAGE_SAVANNA_HOUSE, ModChestLootTables.VILLAGE_SAVANNA_HOUSE));
		this.add("add_loot_village_snowy_house", this.addNewLootPool(BuiltInLootTables.VILLAGE_SNOWY_HOUSE, ModChestLootTables.VILLAGE_SNOWY_HOUSE));
		this.add("add_loot_village_taiga_house", this.addNewLootPool(BuiltInLootTables.VILLAGE_TAIGA_HOUSE, ModChestLootTables.VILLAGE_TAIGA_HOUSE));

		// Entity Loot
		this.add("add_onion_to_illagers", this.addItemOnPlayerKill(entityTypes, ModItems.ONION.get(), 0.02F, EntityType.PILLAGER, EntityType.EVOKER, EntityType.VINDICATOR));
		this.add("add_onion_to_zombies", this.addItemOnPlayerKill(entityTypes, ModItems.ONION.get(), 0.02F, EntityType.ZOMBIE, EntityType.HUSK, EntityType.ZOMBIE_VILLAGER));

		// Knife Scavenging
		this.add("scavenging_feather", this.addItemOnKnifeKill(items, entityTypes, Items.FEATHER, EntityType.CHICKEN));
		this.add("scavenging_ham_from_hoglin", this.addItemOnKnifeKill(items, entityTypes, ModItems.HAM.get(), false, 1.0F, EntityType.HOGLIN));
		this.add("scavenging_ham_from_pig", this.addItemOnKnifeKill(items, entityTypes, ModItems.HAM.get(), false, 0.5F, EntityType.PIG));
		this.add("scavenging_leather", this.addItemOnKnifeKill(items, entityTypes, Items.LEATHER, EntityType.COW, EntityType.MOOSHROOM, EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.LLAMA, EntityType.TRADER_LLAMA));
		this.add("scavenging_rabbit_hide", this.addItemOnKnifeKill(items, entityTypes, Items.RABBIT_HIDE, EntityType.RABBIT));
		this.add("scavenging_shulker_shell", this.addItemOnKnifeKill(items, entityTypes, Items.SHULKER_SHELL, EntityType.SHULKER));
		this.add("scavenging_smoked_ham_from_hoglin", this.addItemOnKnifeKill(items, entityTypes, ModItems.SMOKED_HAM.get(), true, 1.0F, EntityType.HOGLIN));
		this.add("scavenging_smoked_ham_from_pig", this.addItemOnKnifeKill(items, entityTypes, ModItems.SMOKED_HAM.get(), true, 0.5F, EntityType.PIG));
		this.add("scavenging_string", this.addItemOnKnifeKill(items, entityTypes, Items.STRING, EntityType.SPIDER, EntityType.CAVE_SPIDER));
		this.add("scavenging_pumpkin", new ReplaceItemModifier(all(
			LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.PUMPKIN),
			MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, ModTags.Items.KNIVES)),
			hasSilkTouch().invert()
		), Items.PUMPKIN, ModItems.PUMPKIN_SLICE.get(), 4));

		// Pastry Slicing
		this.add("slicing_apple_pie", this.pastrySlicing(items, blocks, ModItems.APPLE_PIE_SLICE.get(), ModBlocks.APPLE_PIE.get()));
		this.add("slicing_cake", this.pastrySlicing(items, blocks, ModItems.CAKE_SLICE.get(), Blocks.CAKE));
		this.add("slicing_chocolate_pie", this.pastrySlicing(items, blocks, ModItems.CHOCOLATE_PIE_SLICE.get(), ModBlocks.CHOCOLATE_PIE.get()));
		this.add("slicing_pumpkin_pie", this.pastrySlicing(items, blocks, ModItems.PUMPKIN_PIE_SLICE.get(), ModBlocks.PUMPKIN_PIE.get()));
		this.add("slicing_sweet_berry_cheesecake", this.pastrySlicing(items, blocks, ModItems.SWEET_BERRY_CHEESECAKE_SLICE.get(), ModBlocks.SWEET_BERRY_CHEESECAKE.get()));
		this.add("slicing_candle_cake", this.candleCakeSlicing(items, blocks));

		// Straw Harvesting
		this.add("straw_from_grass", this.strawHarvesting(items, BlockTagCondition.blockInTag(ModTags.Blocks.DROPS_STRAW), 0.2F));
		this.add("straw_from_mature_rice", this.strawHarvesting(items, LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.RICE_CROP_PANICLES.get()).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(RicePaniclesBlock.RICE_AGE, 3)), 1.0F));
		this.add("straw_from_mature_wheat", this.strawHarvesting(items, LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7)), 1.0F));
		this.add("straw_from_sandy_shrub", this.strawHarvesting(items, LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.SANDY_SHRUB.get()), 0.3F));
	}

	private static LootItemCondition[] all(LootItemCondition.Builder... conditions) {
		return Arrays.stream(conditions).map(LootItemCondition.Builder::build).toArray(LootItemCondition[]::new);
	}

	private AddTableLootModifier addNewLootPool(ResourceKey<LootTable> lootToAddTo, ResourceKey<LootTable> newPool) {
		return new FDAddTableLootModifier(all(LootTableIdCondition.builder(lootToAddTo.identifier())), newPool);
	}

	private LootItemCondition.Builder anyEntity(HolderGetter<EntityType<?>> entityTypes, EntityType<?>... entity) {
		LootItemCondition.Builder[] entityConditions = new LootItemCondition.Builder[entity.length];
		for (int i = 0; i < entity.length; i++) {
			entityConditions[i] = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(entityTypes, entity[i]));
		}
		return entityConditions.length > 1 ? AnyOfCondition.anyOf(entityConditions) : entityConditions[0];
	}

	private AddItemModifier addItemOnPlayerKill(HolderGetter<EntityType<?>> entityTypes, Item item, float chance, EntityType<?>... entity) {
		List<LootItemCondition.Builder> conditions = new ArrayList<>();
		conditions.add(anyEntity(entityTypes, entity));
		conditions.add(LootItemKilledByPlayerCondition.killedByPlayer());
		if (chance < 1.0F) {
			conditions.add(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.registries, chance, 0.01F));
		}
		return new AddItemModifier(all(conditions.toArray(LootItemCondition.Builder[]::new)), item, 1);
	}

	private AddItemModifier addItemOnKnifeKill(HolderGetter<Item> items, HolderGetter<EntityType<?>> entityTypes, Item item, EntityType<?>... entity) {
		return this.addItemOnKnifeKill(items, entityTypes, item, null, 1.0F, entity);
	}

	private AddItemModifier addItemOnKnifeKill(HolderGetter<Item> items, HolderGetter<EntityType<?>> entityTypes, Item item, Boolean onFire, float chance, EntityType<?>... entity) {
		List<LootItemCondition.Builder> conditions = new ArrayList<>();
		conditions.add(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.ATTACKER,
				EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().of(items, ModTags.Items.KNIVES)))));
		conditions.add(anyEntity(entityTypes, entity));
		if (onFire != null) {
			conditions.add(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(onFire))));
		}
		if (chance < 1.0F) {
			conditions.add(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.registries, chance, 0.1F));
		}
		return new AddItemModifier(all(conditions.toArray(LootItemCondition.Builder[]::new)), item, 1);
	}

	private AddItemModifier candleCakeSlicing(HolderGetter<Item> items, HolderGetter<Block> blocks) {
		List<Block> cakes = new ArrayList<>();
		cakes.add(Blocks.CANDLE_CAKE);
		for (DyeColor color : DyeColor.values()) {
			cakes.add(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(color.getName() + "_candle_cake")));
		}
		LootItemCondition.Builder[] conditions = cakes.stream().map(cake -> LootItemBlockStatePropertyCondition.hasBlockStateProperties(cake)).toArray(LootItemCondition.Builder[]::new);
		return new AddItemModifier(all(
			MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, ModTags.Items.KNIVES)),
			AnyOfCondition.anyOf(conditions)
		), ModItems.CAKE_SLICE.get(), 7);
	}

	private PastrySlicingModifier pastrySlicing(HolderGetter<Item> items, HolderGetter<Block> blocks, Item receivedItem, Block slicedBlock) {
		return new PastrySlicingModifier(all(
			MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, ModTags.Items.KNIVES)),
			LootItemBlockStatePropertyCondition.hasBlockStateProperties(slicedBlock)
		), receivedItem);
	}

	private AddItemModifier strawHarvesting(HolderGetter<Item> items, LootItemCondition.Builder harvestedBlock, float chance) {
		List<LootItemCondition.Builder> conditions = new ArrayList<>();
		conditions.add(MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, ModTags.Items.STRAW_HARVESTERS)));
		if (chance < 1.0F) {
			conditions.add(LootItemRandomChanceCondition.randomChance(chance));
		}
		conditions.add(harvestedBlock);
		return new AddItemModifier(all(conditions.toArray(LootItemCondition.Builder[]::new)), ModItems.STRAW.get(), 1);
	}

	private LootItemCondition.Builder hasSilkTouch() {
		return MatchTool.toolMatches(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(
				DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(
						this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1))))).build()));
	}
}
