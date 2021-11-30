package agency.highlysuspect.autothirdperson.mixin;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(
		method = "startRiding",
		at = @At("TAIL")
	)
	private void onStartRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
		AutoThirdPerson.mountOrDismount(vehicle, true);
	}
	
	@Inject(
		method = "removeVehicle",
		at = @At("HEAD")
	)
	private void onStopRiding(CallbackInfo ci) {
		@SuppressWarnings("ConstantConditions")
		Entity vehicle = ((Entity) (Object) this).getVehicle();
		AutoThirdPerson.mountOrDismount(vehicle, false);
	}
}
