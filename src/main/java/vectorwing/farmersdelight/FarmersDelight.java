package vectorwing.farmersdelight;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vectorwing.farmersdelight.client.event.ClientSetupEvents;
import vectorwing.farmersdelight.common.CommonSetup;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.registry.*;
import vectorwing.farmersdelight.common.world.VillageStructures;

@Mod(FarmersDelight.MODID)
public class FarmersDelight
{
	public static final String MODID = "farmersdelight";
	public static final Logger LOGGER = LogManager.getLogger();

	public static Identifier id(String name) {
		return Identifier.fromNamespaceAndPath(MODID, name);
	}

	public FarmersDelight(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(CommonSetup::init);
		if (FMLEnvironment.getDist().isClient()) {
			// TODO may be unnecessary?
			//modEventBus.addListener(ClientSetupEvents::init);
			modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}

		// Same file name as the 26.3 build (which uses ModConfig.Type.LOCAL), so configs carry over.
		modContainer.registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG, MODID + "-common.toml");
		modContainer.registerConfig(ModConfig.Type.CLIENT, Configuration.CLIENT_CONFIG);

		ModSounds.SOUNDS.register(modEventBus);
		ModEffects.EFFECTS.register(modEventBus);
		ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
		ModDataComponents.DATA_COMPONENTS.register(modEventBus);
		ModDataComponents.ENCHANTMENT_EFFECT_COMPONENTS.register(modEventBus);
		ModEntityTypes.ENTITIES.register(modEventBus);
		ModBlockEntityTypes.TILES.register(modEventBus);
		ModMenuTypes.MENU_TYPES.register(modEventBus);
		ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
		ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
		ModRecipeSerializers.RECIPE_DISPLAYS.register(modEventBus);
		ModBiomeFeatures.FEATURES.register(modEventBus);
		ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
		ModPlacementModifiers.PLACEMENT_MODIFIERS.register(modEventBus);
		ModBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
		ModLootFunctions.LOOT_FUNCTIONS.register(modEventBus);
		ModLootFunctions.LOOT_CONDITIONS.register(modEventBus);
		ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
		ModConditionCodecs.CONDITION_CODECS.register(modEventBus);
		ModIngredientTypes.INGREDIENT_TYPES.register(modEventBus);
		ModAdvancements.TRIGGERS.register(modEventBus);
		ModConsumeEffectTypes.CONSUME_EFFECTS.register(modEventBus);
		ModRecipeCategories.RECIPE_BOOK_CATEGORIES.register(modEventBus);

		// GameTests only in the dev environment (or with -Dfarmersdelight.gametests=true), not in the released jar
		if (!net.neoforged.fml.loading.FMLEnvironment.isProduction() || Boolean.getBoolean("farmersdelight.gametests")) {
			vectorwing.farmersdelight.gametest.FDGameTests.register(modEventBus);
			vectorwing.farmersdelight.gametest.FDStressTests.register(modEventBus);
			vectorwing.farmersdelight.gametest.FDConservationTests.register(modEventBus);
			vectorwing.farmersdelight.gametest.FDRegressionTests.register(modEventBus);
			vectorwing.farmersdelight.gametest.FDPortAuditTests.register(modEventBus);
			vectorwing.farmersdelight.gametest.FDTomatoSupportTests.register(modEventBus);
		}

		RegistryAliases.addRegistryAliases();

		NeoForge.EVENT_BUS.addListener(VillageStructures::addNewVillageBuilding);
	}
}
