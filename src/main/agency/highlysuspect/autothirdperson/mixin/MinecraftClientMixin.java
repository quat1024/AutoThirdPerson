package agency.highlysuspect.autothirdperson.mixin;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftClient.class})
public class MinecraftClientMixin {
	@Inject(
		method = {"handleInputEvents"},
		at = {@At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/options/GameOptions;setPerspective(Lnet/minecraft/client/options/Perspective;)V"
		)}
	)
	private void onPerspectiveToggle(CallbackInfo ci) {
		AutoThirdPerson.f5Press();
	}
}
