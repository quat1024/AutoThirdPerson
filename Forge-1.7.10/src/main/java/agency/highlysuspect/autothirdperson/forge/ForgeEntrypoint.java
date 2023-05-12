package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.VersionCapabilities;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;

@Mod(
	modid = AutoThirdPerson.MODID,
	name = AutoThirdPerson.NAME,
	version = "1.2.3", //TODO: subst this from gradle somehow (or manually). forge mentioned something about a version.properties file ?
	useMetadata = true
)
public class ForgeEntrypoint {
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		if(e.getSide() == Side.CLIENT) {
			ClassloadingParanoia.doIt(e);
		}
	}
	
	public static class ClassloadingParanoia {
		public static void doIt(FMLPreInitializationEvent e) {
			new ForgeImpl(e).init();
		}
	}
}
