package agency.highlysuspect.autothirdperson.neo;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = AutoThirdPerson.MODID, dist = Dist.CLIENT)
public class NeoEntrypoint {
	public NeoEntrypoint(ModContainer mc, IEventBus modBus) {
		new NeoImpl(mc, modBus).init();
	}
}
