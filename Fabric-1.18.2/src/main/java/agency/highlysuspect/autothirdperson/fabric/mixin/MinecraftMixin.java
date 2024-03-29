package agency.highlysuspect.autothirdperson.fabric.mixin;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(
		method = "handleKeybinds",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"
		)
	)
	private void autoThirdPerson$onPerspectiveToggle(CallbackInfo ci) {
		AutoThirdPerson.instance.manualPress();
	}
}
