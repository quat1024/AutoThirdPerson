package agency.highlysuspect.autothirdperson;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
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
		LOGGER.warn("Hello 1.7.10!!!!!!!");
		LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");LOGGER.warn("Hello 1.7.10!!!!!!!");
	}
}
