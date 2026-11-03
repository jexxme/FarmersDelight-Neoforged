package vectorwing.farmersdelight.common.mixin;

// Adapted from Farmer's Delight Refabricated (MIT, https://github.com/MehVahdJukaar/FarmersDelightRefabricated)

import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.block.state.CanvasSign;

@Mixin(SignEditScreen.class)
public class SignEditScreenMixin {
	@Shadow
	@Final
	@Mutable
	private Identifier texture;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fdrf$fixSignTextures(SignBlockEntity sign, SignTextSlot slot, boolean shouldFilter, CallbackInfo ci) {
		if (sign.getBlockState().getBlock() instanceof CanvasSign canvas) {
			texture = FarmersDelight.id("textures/gui/signs/canvas" + (canvas.getBackgroundColor() == null ? "" : "_" + canvas.getBackgroundColor().getName()) + ".png");
		}
	}
}
