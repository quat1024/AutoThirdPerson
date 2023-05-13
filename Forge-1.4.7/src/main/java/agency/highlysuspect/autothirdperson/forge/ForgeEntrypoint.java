package agency.highlysuspect.autothirdperson.forge;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;

@Mod(
	modid = "auto_third_person",
	useMetadata = true
)
public class ForgeEntrypoint {
	@Mod.PreInit
	public void preinit(FMLPreInitializationEvent e) {
		if(e.getSide() == Side.CLIENT) {
			NiceClientEntrypointDude.niceClientEntrypointDude(e);
		}
	}
	
	private static class NiceClientEntrypointDude {
		public static void niceClientEntrypointDude(FMLPreInitializationEvent e) {
			new ForgeImpl(e).init();
		}
	}
}
