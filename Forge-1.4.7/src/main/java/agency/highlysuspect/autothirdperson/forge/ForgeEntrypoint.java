package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
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
	
	@Mod.ServerAboutToStart
	public void serverBoutaStart(FMLServerAboutToStartEvent e) {
		AutoThirdPerson.instance.logger.info("Hello server!!!");
	}
	
	private static class NiceClientEntrypointDude {
		public static void niceClientEntrypointDude(FMLPreInitializationEvent e) {
			new ForgeImpl(e).init();
		}
	}
}
