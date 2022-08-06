package agency.highlysuspect.autothirdperson.mixin;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin {
	@Inject(
		method = "cycle",
		at = @At("HEAD"),
		cancellable = true
	)
	public void modifyCycle(CallbackInfoReturnable<CameraType> ci) {
		if(!AutoThirdPerson.INSTANCE.settings.skipFrontView) return;
		
		//noinspection ConstantConditions, RedundantCast
		if((CameraType) (Object) this == CameraType.THIRD_PERSON_BACK) {
			ci.setReturnValue(CameraType.FIRST_PERSON);
			AutoThirdPerson.INSTANCE.debugSpam("Skipping third-person reversed view");
		}
	}
}
