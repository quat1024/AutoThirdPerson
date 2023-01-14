package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.NineteenTwoMinecraftInteraction;
import net.fabricmc.api.ClientModInitializer;

public class FabricEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AutoThirdPerson.instance = new AutoThirdPerson<>(
			new NineteenTwoMinecraftInteraction(),
			new FabricLoaderInteraction()
		);
		AutoThirdPerson.instance.init();
	}
}
