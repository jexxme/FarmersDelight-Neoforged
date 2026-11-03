package vectorwing.farmersdelight.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import vectorwing.farmersdelight.data.recipe.CookingRecipes;
import vectorwing.farmersdelight.data.recipe.CraftingRecipes;
import vectorwing.farmersdelight.data.recipe.CuttingRecipes;
import vectorwing.farmersdelight.data.recipe.SmeltingRecipes;

import java.util.Set;

/**
 * 26.3: recipes (and their unlock advancements) are generated as datapack registry entries.
 */
public class Recipes extends RecipeProvider
{
	public Recipes(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
		super(recipeOutput, advancementOutput);
	}

	public static MultiRegistryBootstrap bootstrap() {
		return new MultiRegistryBootstrap()
		{
			@Override
			public Set<ResourceKey<? extends Registry<?>>> requestedRegistries() {
				return Set.of(Registries.RECIPE, Registries.ADVANCEMENT);
			}

			@Override
			public void run(MultiRegistryBootstrap.BootstrapGetter registries) {
				new Recipes(registries.get(Registries.RECIPE), registries.get(Registries.ADVANCEMENT)).buildRecipes();
			}
		};
	}

	@Override
	protected void buildRecipes() {
		CraftingRecipes.register(this.items, this.output);
		SmeltingRecipes.register(this.items, this.output);
		CookingRecipes.register(this.items, this.output);
		CuttingRecipes.register(this.items, this.output);
	}
}
