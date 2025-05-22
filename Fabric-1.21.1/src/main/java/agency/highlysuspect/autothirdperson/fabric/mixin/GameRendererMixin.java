package agency.highlysuspect.autothirdperson.fabric.mixin;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(method = "renderLevel", at = @At("HEAD"))
	void beforeCameraSetup(DeltaTracker __, CallbackInfo ci) {
		AutoThirdPerson.instance.renderClient();
	}
}
