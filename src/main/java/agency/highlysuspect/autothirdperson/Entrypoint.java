package agency.highlysuspect.autothirdperson;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

import java.util.logging.Logger;

@Mod(
	modid = Entrypoint.MODID,
	useMetadata = true
)
public class Entrypoint {
	public static final String MODID = "auto_third_person";
	public static final Logger LOGGER = Logger.getLogger("Auto Third Person");
	
	@Mod.PreInit
	public void preinit(FMLPreInitializationEvent e) {
		LOGGER.setParent(FMLLog.getLogger());
		
		if(e.getSide() == Side.CLIENT) {
			LOGGER.info("[Auto Third Person] Hello, World!");
			ClassloadingParanoia.doIt(e);
		} else {
			LOGGER.info("[Auto Third Person] Not starting, this is a client-only mod but we're on side " + e.getSide() + ".");
		}
	}
	
	public static class ClassloadingParanoia {
		public static void doIt(FMLPreInitializationEvent e) {
			//Is this enough layers of trying to protect things from the classloader now. Anyway
			AutoThirdPerson atp = new AutoThirdPerson();
			atp.clientPreinit(e);
			MinecraftForge.EVENT_BUS.register(atp);
		}
	}
}
