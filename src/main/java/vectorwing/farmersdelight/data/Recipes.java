package vectorwing.farmersdelight.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import vectorwing.farmersdelight.data.recipe.CookingRecipes;
import vectorwing.farmersdelight.data.recipe.CraftingRecipes;
import vectorwing.farmersdelight.data.recipe.CuttingRecipes;
import vectorwing.farmersdelight.data.recipe.SmeltingRecipes;

import java.util.concurrent.CompletableFuture;

public class Recipes extends RecipeProvider
{
	public Recipes(HolderLookup.Provider registries, RecipeOutput output) {
		super(registries, output);
	}

	@Override
	protected void buildRecipes() {
		CraftingRecipes.register(this.items, this.output);
		SmeltingRecipes.register(this.items, this.output);
		CookingRecipes.register(this.items, this.output);
		CuttingRecipes.register(this.items, this.output);
	}

	public static class Runner extends RecipeProvider.Runner
	{
		public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
			return new Recipes(registries, output);
		}

		@Override
		public String getName() {
			return "Farmer's Delight recipes";
		}
	}
}
