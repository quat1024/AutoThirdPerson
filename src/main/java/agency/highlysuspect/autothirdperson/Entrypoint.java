package agency.highlysuspect.autothirdperson;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
	modid = Entrypoint.MODID,
	name = Entrypoint.NAME,
	version = Entrypoint.VERSION
	//useMetadata = true //Doesn't seem to work
)
public class Entrypoint {
	public static final String MODID = "auto_third_person";
	public static final String NAME = "Auto Third Person";
	public static final String VERSION = "1.2.1"; //Not pasted from Gradle stuff
	
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		if(e.getSide() == Side.CLIENT) {
			LOGGER.info("Hello, World!");
			ClassloadingParanoia.doIt(e);
		} else {
			LOGGER.info("Not starting, this is a client-only mod but wr're on side " + e.getSide() + ".");
		}
	}
	
	public static class ClassloadingParanoia {
		public static void doIt(FMLPreInitializationEvent e) {
			//Is this enough layers of trying to protect things from the classloader now. Anyway
			new AutoThirdPerson().clientPreInit(e);
		}
	}
}
