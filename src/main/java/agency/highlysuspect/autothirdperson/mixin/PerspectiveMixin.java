package agency.highlysuspect.autothirdperson.mixin;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import net.minecraft.client.options.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Perspective.class})
public class PerspectiveMixin {
	@Inject(
		method = "next",
		at = @At("HEAD"),
		cancellable = true
	)
	public void modifyNext(CallbackInfoReturnable<Perspective> ci) {
		if(!AutoThirdPerson.SETTINGS.skipFrontView) return;
		
		//noinspection ConstantConditions, RedundantCast
		if((Perspective) (Object) this == Perspective.THIRD_PERSON_BACK) {
			ci.setReturnValue(Perspective.FIRST_PERSON);
			if(AutoThirdPerson.SETTINGS.logSpam) {
				AutoThirdPerson.LOGGER.info("Skipping third-person reversed view");
			}
		}
	}
}
