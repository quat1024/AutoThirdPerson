package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.NineteenFourMinecraftInteraction;
import net.fabricmc.api.ClientModInitializer;

public class FabricEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AutoThirdPerson.instance = new AutoThirdPerson<>(
			new NineteenFourMinecraftInteraction(),
			new FabricLoaderInteraction()
		);
		AutoThirdPerson.instance.init();
	}
}
