package vectorwing.farmersdelight.common.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import vectorwing.farmersdelight.common.utility.RecipeUtils;

public class RegistryAliases
{
	public static void addRegistryAliases() {
		addBlockAlias("basket", "bamboo_basket");
		addItemAlias("basket", "bamboo_basket");
		// 26.x: Canvas Signs now use the vanilla sign block entities (sign bodies are rendered from block models).
		addBlockEntityAlias("canvas_sign", "sign");
		addBlockEntityAlias("hanging_canvas_sign", "hanging_sign");
	}

	private static void addBlockEntityAlias(String oldName, String vanillaName) {
		BuiltInRegistries.BLOCK_ENTITY_TYPE.addAlias(RecipeUtils.FDLocation(oldName), Identifier.withDefaultNamespace(vanillaName));
	}

	public static void addBlockAlias(String oldName, String newName) {
		BuiltInRegistries.BLOCK.addAlias(RecipeUtils.FDLocation(oldName), RecipeUtils.FDLocation(newName));
	}

	public static void addItemAlias(String oldName, String newName) {
		BuiltInRegistries.ITEM.addAlias(RecipeUtils.FDLocation(oldName), RecipeUtils.FDLocation(newName));
	}
}
