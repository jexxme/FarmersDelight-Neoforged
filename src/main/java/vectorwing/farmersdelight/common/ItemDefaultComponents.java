package vectorwing.farmersdelight.common;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.VillagerFood;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import vectorwing.farmersdelight.FarmersDelight;

/**
 * 26.3 port: furnace fuels, compostables and villager food used to be NeoForge data maps / static maps.
 * In 26.3 they are item data components ({@link DataComponents#COOKING_FUEL}, {@link DataComponents#COMPOSTABLE},
 * {@link DataComponents#VILLAGER_FOOD}), so they are applied here as default components.
 * Values were taken from the former data/neoforge/data_maps/item/*.json files.
 */
public class ItemDefaultComponents
{
	public static void apply(ModifyDefaultComponentsEvent event) {
		// Furnace fuels
		fuel(event, "bamboo_basket", 300);
		fuel(event, "canvas", 400);
		fuel(event, "canvas_rug", 200);
		fuel(event, "cutting_board", 200);
		fuel(event, "full_tatami_mat", 200);
		fuel(event, "half_tatami_mat", 100);
		fuel(event, "rope", 200);
		fuel(event, "rope_fence", 200);
		fuel(event, "rope_fence_gate", 200);
		fuel(event, "safety_net", 200);
		fuel(event, "straw", 100);
		fuel(event, "straw_bale", 1000);
		fuel(event, "tatami", 400);
		fuel(event, "tree_bark", 200);
		fuel(event, "wooden_basket", 300);
		BuiltInRegistries.ITEM.keySet().stream()
				.filter(id -> id.getNamespace().equals(FarmersDelight.MODID) && id.getPath().endsWith("_cabinet"))
				.filter(id -> !id.getPath().startsWith("crimson") && !id.getPath().startsWith("warped"))
				.forEach(id -> fuel(event, id.getPath(), 300));

		// Compostables
		compostable(event, "apple_pie", ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
		compostable(event, "apple_pie_slice", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "brown_mushroom_colony", ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
		compostable(event, "cabbage", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "cabbage_leaf", ContextIntProviders.COMPOSTABLE_LOW_MEDIUM);
		compostable(event, "cabbage_seeds", ContextIntProviders.COMPOSTABLE_LOW);
		compostable(event, "cake_slice", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "chocolate_pie", ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
		compostable(event, "chocolate_pie_slice", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "dumplings", ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
		compostable(event, "honey_cookie", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "kelp_roll", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "kelp_roll_slice", ContextIntProviders.COMPOSTABLE_LOW_MEDIUM);
		compostable(event, "onion", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "pie_crust", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "pumpkin_slice", ContextIntProviders.COMPOSTABLE_LOW_MEDIUM);
		compostable(event, "raw_pasta", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "red_mushroom_colony", ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
		compostable(event, "rice", ContextIntProviders.COMPOSTABLE_LOW);
		compostable(event, "rice_bale", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "rice_panicle", ContextIntProviders.COMPOSTABLE_LOW);
		compostable(event, "rotten_tomato", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "sandy_shrub", ContextIntProviders.COMPOSTABLE_LOW);
		compostable(event, "straw", ContextIntProviders.COMPOSTABLE_LOW);
		compostable(event, "stuffed_pumpkin_block", ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
		compostable(event, "sweet_berry_cheesecake", ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE);
		compostable(event, "sweet_berry_cheesecake_slice", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "sweet_berry_cookie", ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH);
		compostable(event, "tomato", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "tomato_seeds", ContextIntProviders.COMPOSTABLE_LOW);
		compostable(event, "tree_bark", ContextIntProviders.COMPOSTABLE_LOW);
		compostable(event, "wild_beetroots", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "wild_cabbages", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "wild_carrots", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "wild_onions", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "wild_potatoes", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "wild_rice", ContextIntProviders.COMPOSTABLE_MEDIUM);
		compostable(event, "wild_tomatoes", ContextIntProviders.COMPOSTABLE_MEDIUM);

		// Villager food (formerly Villager.FOOD_POINTS)
		villagerFood(event, "cabbage", 1);
		villagerFood(event, "tomato", 1);
		villagerFood(event, "onion", 1);
		villagerFood(event, "rice", 2);
	}

	private static Item item(String path) {
		return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, path));
	}

	private static void fuel(ModifyDefaultComponentsEvent event, String path, int burnTime) {
		event.modify(item(path), (builder, provider, item) -> builder.set(DataComponents.COOKING_FUEL,
				new CookingFuel(new ResolvableInt.Constant(burnTime), ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER))));
	}

	private static void compostable(ModifyDefaultComponentsEvent event, String path, ResourceKey<ContextIntProvider> layers) {
		event.modify(item(path), (builder, provider, item) -> builder.set(DataComponents.COMPOSTABLE, new Compostable(layers)));
	}

	private static void villagerFood(ModifyDefaultComponentsEvent event, String path, int nutrition) {
		event.modify(item(path), (builder, provider, item) -> builder.set(DataComponents.VILLAGER_FOOD, new VillagerFood(nutrition)));
	}
}
