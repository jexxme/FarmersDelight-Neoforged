package vectorwing.farmersdelight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.ModDataComponents;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Swings the arm holding a skillet while it flips its ingredient.
 */
public record SkilletArmPoseTransformer() implements IArmPoseTransformer {
	@Override
	public void applyTransform(HumanoidModel<?> model, HumanoidRenderState entity, HumanoidArm arm) {
		ItemStack stack = arm == HumanoidArm.LEFT ? entity.leftHandItemStack : entity.rightHandItemStack;
		Minecraft mc = Minecraft.getInstance();

		if (stack.has(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get())) {
			long time = stack.get(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get());
			float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			float animation = ((mc.level.getGameTime() - time) + partialTicks) / SkilletItem.FLIP_TIME;
			animation = Mth.clamp(animation, 0, 1);

			if (arm == HumanoidArm.LEFT) {
				model.leftArm.xRot = (-Mth.sin(animation * Mth.TWO_PI) * 15 - 20) * (float) (Math.PI / 180.0);
			} else {
				model.rightArm.xRot = (-Mth.sin(animation * Mth.TWO_PI) * 15 - 20) * (float) (Math.PI / 180.0);
			}
		}
	}
}
