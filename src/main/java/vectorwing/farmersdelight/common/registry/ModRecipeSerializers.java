package vectorwing.farmersdelight.common.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import vectorwing.farmersdelight.client.recipe.CookingPotRecipeDisplay;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.crafting.*;

import java.util.function.Supplier;

public class ModRecipeSerializers
{
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, FarmersDelight.MODID);

	public static final Supplier<RecipeSerializer<? extends Recipe<RecipeInput>>> COOKING = RECIPE_SERIALIZERS.register("cooking", fromCodecs(CookingPotRecipe.CODEC, CookingPotRecipe.STREAM_CODEC));
	public static final Supplier<RecipeSerializer<? extends Recipe<CuttingBoardRecipeInput>>> CUTTING = RECIPE_SERIALIZERS.register("cutting", fromCodecs(CuttingBoardRecipe.CODEC, CuttingBoardRecipe.STREAM_CODEC));

	public static final Supplier<RecipeSerializer<?>> FOOD_SERVING =
			RECIPE_SERIALIZERS.register("food_serving", () -> FoodServingRecipe.SERIALIZER);
	public static final Supplier<RecipeSerializer<?>> DOUGH =
			RECIPE_SERIALIZERS.register("dough", () -> DoughRecipe.SERIALIZER);

	// Recipe book display type of cooking pot recipes; must be registered so the server can send it to clients.
	public static final DeferredRegister<RecipeDisplay.Type<?>> RECIPE_DISPLAYS = DeferredRegister.create(Registries.RECIPE_DISPLAY, FarmersDelight.MODID);
	public static final Supplier<RecipeDisplay.Type<CookingPotRecipeDisplay>> COOKING_DISPLAY = RECIPE_DISPLAYS.register("cooking", () -> CookingPotRecipeDisplay.TYPE);

	private static <T extends Recipe<?>> Supplier<RecipeSerializer<T>> fromCodecs(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
		return () -> new RecipeSerializer<T>(codec, streamCodec);
	}
}
