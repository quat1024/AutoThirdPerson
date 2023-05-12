package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.SeventeenOneMinecraftInteraction;
import agency.highlysuspect.autothirdperson.VersionCapabilities;
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
			new AutoThirdPerson<>(
				new SeventeenOneMinecraftInteraction(),
				new ForgeLoaderInteraction(),
				new VersionCapabilities.Builder()
					.hasElytra()
					.hasSwimmingAnimation()
			).initLoader();
		}
	}
}
