package agency.highlysuspect.autothirdperson.mixin;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.NineteenTwoAutoThirdPerson;
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
	private void autoThirdPerson$onStartRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
		AutoThirdPerson<?, ?> atp = AutoThirdPerson.instance;
		NineteenTwoAutoThirdPerson mcInteraction = (NineteenTwoAutoThirdPerson) atp.mc;
		
		atp.mount(mcInteraction.wrapVehicle(vehicle));
	}
	
	@Inject(
		method = "removeVehicle",
		at = @At("HEAD")
	)
	private void autoThirdPerson$onStopRiding(CallbackInfo ci) {
		@SuppressWarnings("ConstantConditions")
		Entity vehicle = ((Entity) (Object) this).getVehicle();
		if(vehicle != null) {
			AutoThirdPerson<?, ?> atp = AutoThirdPerson.instance;
			NineteenTwoAutoThirdPerson mcInteraction = (NineteenTwoAutoThirdPerson) atp.mc;
			
			atp.dismount(mcInteraction.wrapVehicle(vehicle));
		}
	}
}
