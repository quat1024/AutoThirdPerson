package agency.highlysuspect.autothirdperson.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
	modid = "auto_third_person",
	useMetadata = true,
	guiFactory = "agency.highlysuspect.autothirdperson.forge.GuiFactory" //Forge is so weird dude
)
public class ForgeEntrypoint {
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		if(e.getSide() == Side.CLIENT) {
			NiceClientEntrypointDude.niceClientEntrypointDude(e);
		}
	}
	
	private static class NiceClientEntrypointDude {
		private static void niceClientEntrypointDude(FMLPreInitializationEvent e) {
			new ForgeImpl(e).init();
		}
	}
}
