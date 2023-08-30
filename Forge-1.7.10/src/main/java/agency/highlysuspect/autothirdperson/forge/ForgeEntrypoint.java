package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

import java.util.Map;

@Mod(
	modid = AutoThirdPerson.MODID,
	name = AutoThirdPerson.NAME,
	version = "2.1", //TODO: subst this from gradle somehow (or manually). forge mentioned something about a version.properties file ?
	useMetadata = true,
	acceptableRemoteVersions = "*",
	acceptableSaveVersions = "*"
)
public class ForgeEntrypoint {
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		if(e.getSide() == Side.CLIENT) {
			ClassloadingParanoia.doIt(e);
		}
	}
	
	@NetworkCheckHandler
	public static boolean check(Map<String, String> uh, Side side) {
		return true;
	}
	
	public static class ClassloadingParanoia {
		public static void doIt(FMLPreInitializationEvent e) {
			new ForgeImpl(e).init();
		}
	}
}
