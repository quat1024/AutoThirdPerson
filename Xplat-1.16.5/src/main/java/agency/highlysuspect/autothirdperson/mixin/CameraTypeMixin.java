package agency.highlysuspect.autothirdperson.mixin;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.SixteenFiveAutoThirdPerson;
import agency.highlysuspect.autothirdperson.wrap.MyCameraType;
import net.minecraft.client.CameraType;
import org.jetbrains.annotations.Nullable;
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
	public void autoThirdPerson$modifyCycle(CallbackInfoReturnable<CameraType> ci) {
		AutoThirdPerson<?, ?> atp = AutoThirdPerson.instance;
		SixteenFiveAutoThirdPerson mcInteraction = (SixteenFiveAutoThirdPerson) atp.mc;
		
		@SuppressWarnings("ConstantConditions") //mixin cast
		@Nullable MyCameraType cycleOverride = atp.modifyCycle(mcInteraction.wrapCameraType((CameraType) (Object) this));
		
		if(cycleOverride != null) {
			ci.setReturnValue(mcInteraction.unwrapCameraType(cycleOverride));
		}
	}
}
