package vectorwing.farmersdelight.common.crafting.ingredient;

import java.util.Map;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import vectorwing.farmersdelight.common.registry.ModIngredientTypes;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

/**
 * Ingredient that checks if the given stack can perform a ItemAbility from Forge.
 */
@ParametersAreNonnullByDefault

public class ItemAbilityIngredient implements ICustomIngredient
{
	public static final MapCodec<ItemAbilityIngredient> CODEC = RecordCodecBuilder.mapCodec(inst ->
			inst.group(ItemAbility.CODEC.fieldOf("action").forGetter(ItemAbilityIngredient::getItemAbility)
			).apply(inst, ItemAbilityIngredient::new));

	protected final ItemAbility itemAbility;
	protected java.util.List<Holder<Item>> itemHolders;

	public ItemAbilityIngredient(ItemAbility itemAbility) {
		this.itemAbility = itemAbility;
	}

	/**
	 * 26.x: NeoForge removed the vanilla tool item abilities (axe_strip, axe_dig, pickaxe_dig, shovel_dig, hoe_dig, ...),
	 * since stripping/tilling/flattening became data-driven block transformers. Vanilla tools no longer report them via
	 * canPerformAction, so fall back to the matching vanilla tool tag for those ability names.
	 */
	private static final Map<String, TagKey<Item>> REMOVED_VANILLA_ABILITY_TAGS = Map.ofEntries(
			Map.entry("axe_dig", ItemTags.AXES),
			Map.entry("axe_strip", ItemTags.AXES),
			Map.entry("axe_scrape", ItemTags.AXES),
			Map.entry("axe_wax_off", ItemTags.AXES),
			Map.entry("pickaxe_dig", ItemTags.PICKAXES),
			Map.entry("shovel_dig", ItemTags.SHOVELS),
			Map.entry("shovel_flatten", ItemTags.SHOVELS),
			Map.entry("hoe_dig", ItemTags.HOES),
			Map.entry("hoe_till", ItemTags.HOES),
			Map.entry("sword_dig", ItemTags.SWORDS)
	);

	public static boolean canPerform(ItemStack stack, ItemAbility ability) {
		if (stack.canPerformAction(ability)) {
			return true;
		}
		TagKey<Item> fallback = REMOVED_VANILLA_ABILITY_TAGS.get(ability.name());
		return fallback != null && stack.is(fallback);
	}

	protected void dissolve() {
		if (this.itemHolders == null) {
			itemHolders = BuiltInRegistries.ITEM.stream()
					.filter(item -> canPerform(new ItemStack(item), itemAbility))
					.map(BuiltInRegistries.ITEM::wrapAsHolder)
					.toList();
		}
	}

	@Override
	public boolean test(@Nullable ItemStack stack) {
		return stack != null && canPerform(stack, itemAbility);
	}

	@Override
	public Stream<Holder<Item>> items() {
		dissolve();
		return itemHolders.stream();
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	public ItemAbility getItemAbility() {
		return itemAbility;
	}

	public IngredientType<?> getType() {
		return ModIngredientTypes.ITEM_ABILITY_INGREDIENT.get();
	}
}
