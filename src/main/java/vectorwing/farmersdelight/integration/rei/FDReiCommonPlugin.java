package vectorwing.farmersdelight.integration.rei;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.forge.REIPluginCommon;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapelessDisplay;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.integration.rei.display.CookingPotDisplay;
import vectorwing.farmersdelight.integration.rei.display.CuttingDisplay;
import vectorwing.farmersdelight.integration.rei.display.DecompositionDisplay;

import java.util.List;

/**
 * REI integration, common side: turns cooking pot and cutting board recipes into REI displays (REI syncs them to
 * clients). Adapted from Farmer's Delight Refabricated's REI integration (MIT).
 */
@REIPluginCommon
public class FDReiCommonPlugin implements REICommonPlugin
{
	@Override
	public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
		registry.register(REICategoryIdentifiers.COOKING.getIdentifier(), CookingPotDisplay.SERIALIZER);
		registry.register(REICategoryIdentifiers.CUTTING.getIdentifier(), CuttingDisplay.SERIALIZER);
		registry.register(REICategoryIdentifiers.DECOMPOSITION.getIdentifier(), DecompositionDisplay.SERIALIZER);
	}

	@Override
	public void registerDisplays(ServerDisplayRegistry registry) {
		registry.beginRecipeFiller(CookingPotRecipe.class)
			.filterType(ModRecipeTypes.COOKING.get())
			.fill(CookingPotDisplay::new);
		registry.beginRecipeFiller(CuttingBoardRecipe.class)
			.filterType(ModRecipeTypes.CUTTING.get())
			.fill(CuttingDisplay::new);
		registry.add(new DecompositionDisplay());
		// The dough recipe is special (dynamic), so REI cannot read its ingredients: show it as wheat + water bucket.
		registry.add(new DefaultShapelessDisplay(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, FarmersDelight.id("wheat_dough_from_water")),
			new ShapelessRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "fd_dough"),
				ItemStackTemplate.fromNonEmptyStack(new ItemStack(ModItems.WHEAT_DOUGH.get())),
				List.of(Ingredient.of(Items.WHEAT), Ingredient.of(Items.WATER_BUCKET))))));
	}
}
