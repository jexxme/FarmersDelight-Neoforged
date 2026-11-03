package vectorwing.farmersdelight.data.recipe;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;
import vectorwing.farmersdelight.data.builder.CookingPotRecipeBuilder;

public class CookingRecipes
{
	public static final int FAST_COOKING = 100;      // 5 seconds
	public static final int NORMAL_COOKING = 200;    // 10 seconds
	public static final int SLOW_COOKING = 400;      // 20 seconds

	public static final float SMALL_EXP = 0.35F;
	public static final float MEDIUM_EXP = 1.0F;
	public static final float LARGE_EXP = 2.0F;

	public static void register(HolderGetter<Item> holderGetter, RecipeOutput output) {
		cookMiscellaneous(holderGetter, output);
		cookMinecraftSoups(holderGetter, output);
		cookMeals(holderGetter, output);
	}

	private static void cookMiscellaneous(HolderGetter<Item> holderGetter, RecipeOutput output) {
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.HOT_COCOA.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(Tags.Items.DRINKS_MILK)
			.addIngredient(Items.SUGAR)
			.addIngredient(Items.COCOA_BEANS)
			.addIngredient(Items.COCOA_BEANS)
			.unlockedByAnyIngredient(Items.COCOA_BEANS, Items.MILK_BUCKET, ModItems.MILK_BOTTLE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.DRINKS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.APPLE_CIDER.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(Items.APPLE)
			.addIngredient(Items.APPLE)
			.addIngredient(Items.SUGAR)
			.unlockedByItems("has_apple", Items.APPLE)
			.setRecipeBookCategory(CookingPotRecipeBookTab.DRINKS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.TOMATO_SAUCE.get(), 1, FAST_COOKING, SMALL_EXP)
			.addIngredient(CommonTags.Items.CROPS_TOMATO)
			.addIngredient(CommonTags.Items.CROPS_TOMATO)
			.unlockedByItems("has_tomato", ModItems.TOMATO.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MISC)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.DOG_FOOD.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(Items.ROTTEN_FLESH)
			.addIngredient(Items.BONE_MEAL)
			.addIngredient(Tags.Items.FOODS_RAW_MEAT)
			.addIngredient(CommonTags.Items.CROPS_RICE)
			.unlockedByAnyIngredient(Items.ROTTEN_FLESH, Items.BONE_MEAL, ModItems.RICE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MISC)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.GLOW_BERRY_CUSTARD.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(Items.GLOW_BERRIES)
			.addIngredient(Tags.Items.DRINKS_MILK)
			.addIngredient(ItemTags.EGGS)
			.addIngredient(Items.SUGAR)
			.unlockedByAnyIngredient(Items.GLOW_BERRIES, Items.MILK_BUCKET, ModItems.MILK_BOTTLE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MISC)
			.save(output);
	}

	private static void cookMinecraftSoups(HolderGetter<Item> holderGetter, RecipeOutput output) {
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, Items.MUSHROOM_STEW, 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
			.addIngredient(ItemTags.MUSHROOMS)
			.addIngredient(ItemTags.MUSHROOMS)
			.unlockedByAnyIngredient(Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.SHELF_MUSHROOM)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.saveToFD(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, Items.BEETROOT_SOUP, 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
			.addIngredient(Tags.Items.CROPS_BEETROOT)
			.addIngredient(Tags.Items.CROPS_BEETROOT)
			.addIngredient(Tags.Items.CROPS_BEETROOT)
			.unlockedByItems("has_beetroot", Items.BEETROOT)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.saveToFD(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, Items.RABBIT_STEW, 1, NORMAL_COOKING, MEDIUM_EXP, Items.BOWL)
			.addIngredient(Tags.Items.CROPS_POTATO)
			.addIngredient(Items.RABBIT)
			.addIngredient(Tags.Items.CROPS_CARROT)
			.addIngredient(ItemTags.MUSHROOMS)
			.unlockedByAnyIngredient(Items.RABBIT, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.SHELF_MUSHROOM, Items.CARROT, Items.POTATO)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.saveToFD(output);
	}

	private static void cookMeals(HolderGetter<Item> holderGetter, RecipeOutput output) {
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.BAKED_COD_STEW.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.FOODS_RAW_COD)
			.addIngredient(Tags.Items.CROPS_POTATO)
			.addIngredient(ItemTags.EGGS)
			.addIngredient(CommonTags.Items.CROPS_TOMATO)
			.unlockedByAnyIngredient(Items.COD, Items.POTATO, ModItems.TOMATO.get(), Items.EGG)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.BEEF_STEW.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.FOODS_RAW_BEEF)
			.addIngredient(Tags.Items.CROPS_CARROT)
			.addIngredient(Tags.Items.CROPS_POTATO)
			.unlockedByAnyIngredient(Items.BEEF, Items.CARROT, Items.POTATO)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.BONE_BROTH.get(), 1, NORMAL_COOKING, SMALL_EXP)
			.addIngredient(Tags.Items.BONES)
			.addIngredient(CompoundIngredient.of(
				Ingredient.of(Items.GLOW_BERRIES),
				Ingredient.of(holderGetter.getOrThrow(Tags.Items.MUSHROOMS)),
				Ingredient.of(Items.HANGING_ROOTS),
				Ingredient.of(Items.GLOW_LICHEN)
			))
			.unlockedByItems("has_bone", Items.BONE)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.CABBAGE_ROLLS.get(), 1, FAST_COOKING, SMALL_EXP)
			.addIngredient(CommonTags.Items.CROPS_CABBAGE)
			.addIngredient(CompoundIngredient.of(
				Ingredient.of(holderGetter.getOrThrow(Tags.Items.FOODS_RAW_MEAT)),
				Ingredient.of(holderGetter.getOrThrow(CommonTags.Items.FOODS_SAFE_RAW_FISH)),
				Ingredient.of(holderGetter.getOrThrow(Tags.Items.FOODS_VEGETABLE)),
				Ingredient.of(holderGetter.getOrThrow(Tags.Items.MUSHROOMS))
			))
			.unlockedByAnyIngredient(ModItems.CABBAGE.get(), ModItems.CABBAGE_LEAF.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MISC)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.CHICKEN_SOUP.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.FOODS_RAW_CHICKEN)
			.addIngredient(Tags.Items.CROPS_CARROT)
			.addIngredient(CommonTags.Items.FOODS_LEAFY_GREEN)
			.addIngredient(Tags.Items.FOODS_VEGETABLE)
			.unlockedByAnyIngredient(Items.CHICKEN, Items.CARROT)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.COOKED_RICE.get(), 1, FAST_COOKING, SMALL_EXP)
			.addIngredient(CommonTags.Items.CROPS_RICE)
			.unlockedByItems("has_rice", ModItems.RICE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MISC)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.DUMPLINGS.get(), 2, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.FOODS_DOUGH)
			.addIngredient(CommonTags.Items.CROPS_CABBAGE)
			.addIngredient(CommonTags.Items.CROPS_ONION)
			.addIngredient(CompoundIngredient.of(
				Ingredient.of(holderGetter.getOrThrow(CommonTags.Items.FOODS_RAW_CHICKEN)),
				Ingredient.of(holderGetter.getOrThrow(CommonTags.Items.FOODS_RAW_PORK)),
				Ingredient.of(holderGetter.getOrThrow(CommonTags.Items.FOODS_RAW_BEEF)),
				Ingredient.of(Items.BROWN_MUSHROOM)
			))
			.unlockedByAnyIngredient(ModItems.WHEAT_DOUGH.get(), ModItems.CABBAGE.get(), ModItems.ONION.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MISC)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.FISH_STEW.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.FOODS_SAFE_RAW_FISH)
			.addIngredient(ModItems.TOMATO_SAUCE.get())
			.addIngredient(CommonTags.Items.CROPS_ONION)
			.unlockedByAnyIngredient(Items.SALMON, Items.COD, Items.TROPICAL_FISH, ModItems.TOMATO_SAUCE.get(), ModItems.ONION.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.FRIED_RICE.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.CROPS_RICE)
			.addIngredient(ItemTags.EGGS)
			.addIngredient(Tags.Items.CROPS_CARROT)
			.addIngredient(CommonTags.Items.CROPS_ONION)
			.unlockedByAnyIngredient(ModItems.RICE.get(), Items.EGG, Items.CARROT, ModItems.ONION.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.MUSHROOM_RICE.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(ItemTags.MUSHROOMS)
			.addIngredient(ItemTags.MUSHROOMS)
			.addIngredient(CommonTags.Items.CROPS_RICE)
			.addIngredient(Ingredient.of(Items.CARROT, Items.POTATO))
			.unlockedByAnyIngredient(Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.SHELF_MUSHROOM, ModItems.RICE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.NOODLE_SOUP.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.FOODS_PASTA)
			.addIngredient(ItemTags.EGGS)
			.addIngredient(Items.DRIED_KELP)
			.addIngredient(CommonTags.Items.FOODS_RAW_PORK)
			.unlockedByAnyIngredient(ModItems.RAW_PASTA.get(), Items.DRIED_KELP, Items.PORKCHOP)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.ONION_SOUP.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.CROPS_ONION)
			.addIngredient(CommonTags.Items.CROPS_ONION)
			.addIngredient(Tags.Items.FOODS_BREAD)
			.addIngredient(Tags.Items.DRINKS_MILK)
			.unlockedByAnyIngredient(ModItems.ONION.get(), Items.BREAD, Items.MILK_BUCKET, ModItems.MILK_BOTTLE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.PASTA_WITH_MEATBALLS.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(ModItems.MINCED_BEEF.get())
			.addIngredient(CommonTags.Items.FOODS_PASTA)
			.addIngredient(ModItems.TOMATO_SAUCE.get())
			.unlockedByAnyIngredient(ModItems.RAW_PASTA.get(), Items.BEEF, ModItems.TOMATO_SAUCE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.PASTA_WITH_MUTTON_CHOP.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.FOODS_RAW_MUTTON)
			.addIngredient(CommonTags.Items.FOODS_PASTA)
			.addIngredient(ModItems.TOMATO_SAUCE.get())
			.unlockedByAnyIngredient(ModItems.RAW_PASTA.get(), Items.MUTTON, ModItems.TOMATO_SAUCE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.PUMPKIN_SOUP.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(ModItems.PUMPKIN_SLICE.get())
			.addIngredient(CommonTags.Items.FOODS_LEAFY_GREEN)
			.addIngredient(CommonTags.Items.FOODS_RAW_PORK)
			.addIngredient(Tags.Items.DRINKS_MILK)
			.unlockedByAnyIngredient(Items.PUMPKIN, ModItems.PUMPKIN_SLICE.get(), Items.PORKCHOP, Items.MILK_BUCKET, ModItems.MILK_BOTTLE.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.RATATOUILLE.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.CROPS_TOMATO)
			.addIngredient(CommonTags.Items.CROPS_ONION)
			.addIngredient(Tags.Items.CROPS_BEETROOT)
			.addIngredient(Tags.Items.FOODS_VEGETABLE)
			.unlockedByAnyIngredient(ModItems.TOMATO.get(), ModItems.ONION.get(), Items.BEETROOT)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.SQUID_INK_PASTA.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(CommonTags.Items.FOODS_SAFE_RAW_FISH)
			.addIngredient(CommonTags.Items.FOODS_PASTA)
			.addIngredient(CommonTags.Items.CROPS_TOMATO)
			.addIngredient(Items.INK_SAC)
			.unlockedByAnyIngredient(ModItems.RAW_PASTA.get(), Items.INK_SAC, ModItems.TOMATO.get())
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.STUFFED_PUMPKIN_BLOCK.get(), 1, SLOW_COOKING, LARGE_EXP, Items.PUMPKIN)
			.addIngredient(CommonTags.Items.CROPS_RICE)
			.addIngredient(CommonTags.Items.CROPS_ONION)
			.addIngredient(Items.BROWN_MUSHROOM)
			.addIngredient(Tags.Items.CROPS_POTATO)
			.addIngredient(Tags.Items.FOODS_BERRY)
			.addIngredient(Tags.Items.FOODS_VEGETABLE)
			.unlockedByItems("has_pumpkin", Blocks.PUMPKIN)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.VEGETABLE_NOODLES.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(Tags.Items.CROPS_CARROT)
			.addIngredient(Tags.Items.MUSHROOMS)
			.addIngredient(CommonTags.Items.FOODS_PASTA)
			.addIngredient(CommonTags.Items.FOODS_LEAFY_GREEN)
			.addIngredient(Tags.Items.FOODS_VEGETABLE)
			.unlockedByAnyIngredient(ModItems.RAW_PASTA.get(), Items.BROWN_MUSHROOM, Items.CARROT)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
		CookingPotRecipeBuilder.cookingPotRecipe(holderGetter, ModItems.VEGETABLE_SOUP.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
			.addIngredient(Tags.Items.CROPS_CARROT)
			.addIngredient(Tags.Items.CROPS_POTATO)
			.addIngredient(Tags.Items.CROPS_BEETROOT)
			.addIngredient(CommonTags.Items.FOODS_LEAFY_GREEN)
			.unlockedByAnyIngredient(Items.CARROT, ModItems.ONION.get(), Items.BEETROOT)
			.setRecipeBookCategory(CookingPotRecipeBookTab.MEALS)
			.save(output);
	}
}