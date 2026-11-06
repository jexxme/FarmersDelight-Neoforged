package vectorwing.farmersdelight.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Dev-only exporter for the documentation site (site/). It is registered only when the system property
 * {@code farmersdelight.docsExport} names an output file (Gradle task {@code runDocsExport}). It writes the real
 * runtime values of every Farmer's Delight item and block: data components (food, consumable, stack size, durability,
 * attributes...), block properties and states, every loaded tag and the loaded recipe ids. The docs site reads this
 * file so that no number on it is typed in by hand.
 */
public final class FDDocsExport
{
	public static final String PROPERTY = "farmersdelight.docsExport";
	private static final Identifier STRUCTURE = FarmersDelight.id("gametest/empty_5x5");
	private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, FarmersDelight.MODID);
	private static final ResourceKey<Consumer<GameTestHelper>> EXPORT = ResourceKey.create(Registries.TEST_FUNCTION, FarmersDelight.id("docs_export"));

	static {
		FUNCTIONS.register("docs_export", () -> FDDocsExport::export);
	}

	private FDDocsExport() {
	}

	public static void register(IEventBus modEventBus) {
		FUNCTIONS.register(modEventBus);
		modEventBus.addListener(FDDocsExport::registerTests);
	}

	private static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(FarmersDelight.id("docs_export"));
		event.registerTest(EXPORT.identifier(), new FunctionGameTestInstance(EXPORT, new TestData<>(environment, STRUCTURE, 200, 0, true)));
	}

	private static void export(GameTestHelper helper) {
		RegistryAccess access = helper.getLevel().registryAccess();
		RegistryOps<JsonElement> ops = access.createSerializationContext(JsonOps.INSTANCE);
		JsonObject root = new JsonObject();

		// Items: full components for FD items, food-related components for every other food
		JsonObject items = new JsonObject();
		for (Item item : BuiltInRegistries.ITEM) {
			Identifier id = BuiltInRegistries.ITEM.getKey(item);
			boolean ours = id.getNamespace().equals(FarmersDelight.MODID);
			ItemStack stack = new ItemStack(item);
			if (!ours && !stack.has(DataComponents.FOOD)) {
				continue;
			}
			JsonObject entry = new JsonObject();
			JsonObject components = new JsonObject();
			for (TypedDataComponent<?> typed : stack.getComponents()) {
				Identifier type = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typed.type());
				if (type == null) {
					continue;
				}
				String key = type.toString();
				if (!ours && !(key.equals("minecraft:food") || key.equals("minecraft:consumable") || key.equals("minecraft:max_stack_size") || key.equals("minecraft:use_remainder"))) {
					continue;
				}
				typed.encodeValue(ops).result().ifPresent(json -> components.add(key, json));
			}
			entry.add("components", components);
			entry.addProperty("class", item.getClass().getName());
			ItemStackTemplate remainder = item.getCraftingRemainder(stack);
			if (remainder != null) {
				entry.addProperty("craftingRemainder", remainder.item().unwrapKey().map(key -> key.identifier().toString()).orElse("?"));
			}
			JsonArray tags = new JsonArray();
			item.builtInRegistryHolder().tags().map(tag -> tag.location().toString()).sorted().forEach(tags::add);
			entry.add("tags", tags);
			items.add(id.toString(), entry);
		}
		root.add("items", items);

		// Creative tab order
		JsonArray creative = new JsonArray();
		ModItems.CREATIVE_TAB_ITEMS.forEach(item -> creative.add(BuiltInRegistries.ITEM.getKey(item.get()).toString()));
		root.add("creativeTab", creative);

		// FD blocks
		JsonObject blocks = new JsonObject();
		for (Block block : BuiltInRegistries.BLOCK) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			if (!id.getNamespace().equals(FarmersDelight.MODID)) {
				continue;
			}
			JsonObject entry = new JsonObject();
			BlockState state = block.defaultBlockState();
			entry.addProperty("class", block.getClass().getName());
			entry.addProperty("hardness", block.defaultDestroyTime());
			entry.addProperty("blastResistance", block.getExplosionResistance());
			entry.addProperty("requiresTool", state.requiresCorrectToolForDrops());
			entry.addProperty("item", BuiltInRegistries.ITEM.getKey(block.asItem()).toString());
			int maxLight = block.getStateDefinition().getPossibleStates().stream().mapToInt(BlockState::getLightEmission).max().orElse(0);
			entry.addProperty("maxLight", maxLight);
			entry.addProperty("friction", block.getFriction());
			entry.addProperty("speedFactor", block.getSpeedFactor());
			entry.addProperty("jumpFactor", block.getJumpFactor());
			JsonObject properties = new JsonObject();
			for (Property<?> property : block.getStateDefinition().getProperties()) {
				JsonArray values = new JsonArray();
				property.getPossibleValues().forEach(value -> values.add(valueName(property, value)));
				properties.add(property.getName(), values);
			}
			entry.add("properties", properties);
			JsonObject defaults = new JsonObject();
			for (Property<?> property : block.getStateDefinition().getProperties()) {
				defaults.addProperty(property.getName(), valueName(property, state.getValue(property)));
			}
			entry.add("defaultState", defaults);
			JsonArray tags = new JsonArray();
			block.builtInRegistryHolder().tags().map(tag -> tag.location().toString()).sorted().forEach(tags::add);
			entry.add("tags", tags);
			blocks.add(id.toString(), entry);
		}
		root.add("blocks", blocks);

		// Mob effects added by FD
		JsonObject effects = new JsonObject();
		Registry<MobEffect> effectRegistry = access.lookupOrThrow(Registries.MOB_EFFECT);
		effectRegistry.listElements().forEach(holder -> {
			Identifier id = holder.key().identifier();
			if (!id.getNamespace().equals(FarmersDelight.MODID)) {
				return;
			}
			JsonObject entry = new JsonObject();
			entry.addProperty("category", holder.value().getCategory().name());
			entry.addProperty("color", holder.value().getColor());
			entry.addProperty("class", holder.value().getClass().getName());
			effects.add(id.toString(), entry);
		});
		root.add("effects", effects);

		// Every loaded tag of the registries the docs care about
		JsonObject tags = new JsonObject();
		tags.add("item", tagsOf(access.lookupOrThrow(Registries.ITEM)));
		tags.add("block", tagsOf(access.lookupOrThrow(Registries.BLOCK)));
		tags.add("mob_effect", tagsOf(effectRegistry));
		tags.add("worldgen/biome", tagsOf(access.lookupOrThrow(Registries.BIOME)));
		tags.add("entity_type", tagsOf(access.lookupOrThrow(Registries.ENTITY_TYPE)));
		tags.add("enchantment", tagsOf(access.lookupOrThrow(Registries.ENCHANTMENT)));
		root.add("tags", tags);

		// Loaded recipe ids (conditional recipes that did not load are missing here)
		JsonArray recipes = new JsonArray();
		helper.getLevel().getServer().getRecipeManager().getRecipes().stream()
			.map(RecipeHolder::id).map(key -> key.identifier().toString())
			.filter(id -> id.startsWith(FarmersDelight.MODID + ":")).sorted().forEach(recipes::add);
		root.add("loadedRecipes", recipes);

		JsonObject biomes = new JsonObject();
		access.lookupOrThrow(Registries.BIOME).listElements().sorted(Comparator.comparing(holder -> holder.key().identifier().toString())).forEach(holder -> {
			JsonObject entry = new JsonObject();
			entry.addProperty("temperature", holder.value().getBaseTemperature());
			entry.addProperty("precipitation", holder.value().hasPrecipitation());
			biomes.add(holder.key().identifier().toString(), entry);
		});
		root.add("biomes", biomes);

		Path out = Path.of(System.getProperty(PROPERTY));
		try {
			Files.createDirectories(out.toAbsolutePath().getParent());
			Files.writeString(out, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Could not write " + out, e);
		}
		FarmersDelight.LOGGER.info("Docs export written to {}", out.toAbsolutePath());
		helper.succeed();
	}

	private static <T> JsonObject tagsOf(Registry<T> registry) {
		JsonObject out = new JsonObject();
		registry.getTags().sorted(Comparator.comparing(set -> set.key().location().toString())).forEach((HolderSet.Named<T> set) -> {
			JsonArray values = new JsonArray();
			set.stream().map(holder -> holder.unwrapKey().map(key -> key.identifier().toString()).orElse("?")).sorted().forEach(values::add);
			out.add(set.key().location().toString(), values);
		});
		return out;
	}

	@SuppressWarnings("unchecked")
	private static <V extends Comparable<V>> String valueName(Property<V> property, Object value) {
		return property.getName((V) value);
	}
}
