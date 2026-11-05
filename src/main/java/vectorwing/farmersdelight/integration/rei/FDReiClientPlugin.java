package vectorwing.farmersdelight.integration.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.utility.TextUtils;
import vectorwing.farmersdelight.integration.rei.client.CookingPotCategory;
import vectorwing.farmersdelight.integration.rei.client.CuttingCategory;
import vectorwing.farmersdelight.integration.rei.client.DecompositionCategory;

import java.util.List;

/**
 * REI integration, client side: categories, crafting stations, info pages, the cooking pot's recipe click area and
 * recipe transfer. Mirrors the JEI integration. Adapted from Farmer's Delight Refabricated's REI integration (MIT).
 */
@REIPluginClient
public class FDReiClientPlugin implements REIClientPlugin
{
	@Override
	public void registerCategories(CategoryRegistry registry) {
		registry.add(new CookingPotCategory(), new CuttingCategory(), new DecompositionCategory());
		registry.addWorkstations(REICategoryIdentifiers.COOKING, EntryStacks.of(ModItems.COOKING_POT.get()));
		registry.addWorkstations(REICategoryIdentifiers.CUTTING, EntryStacks.of(ModItems.CUTTING_BOARD.get()));
		registry.addWorkstations(REICategoryIdentifiers.DECOMPOSITION, EntryStacks.of(ModItems.ORGANIC_COMPOST.get()));
		registry.addWorkstations(BuiltinPlugin.CAMPFIRE, EntryStacks.of(ModItems.STOVE.get()), EntryStacks.of(ModItems.SKILLET.get()));
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		info(registry, "item.farmersdelight.wheat_dough", "info.dough", ModItems.WHEAT_DOUGH.get());
		info(registry, "item.farmersdelight.straw", "info.straw", ModItems.STRAW.get());
		info(registry, "item.farmersdelight.ham", "info.ham", ModItems.HAM.get(), ModItems.SMOKED_HAM.get());
		info(registry, "tag.item.farmersdelight.tools.knives", "info.knife", ModItems.FLINT_KNIFE.get(), ModItems.COPPER_KNIFE.get(), ModItems.IRON_KNIFE.get(),
			ModItems.GOLDEN_KNIFE.get(), ModItems.DIAMOND_KNIFE.get(), ModItems.NETHERITE_KNIFE.get());
		info(registry, "item.farmersdelight.cabbage", "info.wild_cabbages", ModItems.WILD_CABBAGES.get(), ModItems.CABBAGE.get(), ModItems.CABBAGE_LEAF.get());
		info(registry, "item.minecraft.beetroot", "info.wild_beetroots", ModItems.WILD_BEETROOTS.get(), Items.BEETROOT);
		info(registry, "item.minecraft.carrot", "info.wild_carrots", ModItems.WILD_CARROTS.get(), Items.CARROT);
		info(registry, "item.farmersdelight.onion", "info.wild_onions", ModItems.WILD_ONIONS.get(), ModItems.ONION.get());
		info(registry, "item.minecraft.potato", "info.wild_potatoes", ModItems.WILD_POTATOES.get(), Items.POTATO);
		info(registry, "item.farmersdelight.tomato", "info.wild_tomatoes", ModItems.WILD_TOMATOES.get(), ModItems.TOMATO.get());
		info(registry, "item.farmersdelight.rice", "info.wild_rice", ModItems.WILD_RICE.get(), ModItems.RICE.get(), ModItems.RICE_PANICLE.get());
	}

	private static void info(DisplayRegistry registry, String titleKey, String infoKey, Item... items) {
		registry.add(DefaultInformationDisplay.createFromEntries(EntryIngredients.ofItems(List.of(items)), Component.translatable(titleKey))
			.lines(TextUtils.JEI(infoKey)));
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerContainerClickArea(new Rectangle(89, 25, 24, 17), CookingPotScreen.class, REICategoryIdentifiers.COOKING);
	}

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		registry.register(SimpleTransferHandler.create(CookingPotMenu.class, REICategoryIdentifiers.COOKING, new SimpleTransferHandler.IntRange(0, 6)));
	}
}
