package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(AutoThirdPerson.MODID)
public class ForgeEntrypoint {
	public ForgeEntrypoint() {
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> NiceClientEntrypointDude::niceClientEntrypointDude);
	}
	
	private static class NiceClientEntrypointDude {
		private static void niceClientEntrypointDude() {
			AutoThirdPerson.INSTANCE = new AutoThirdPerson<>(new ForgeLoaderInteraction());
		}
	}
}
