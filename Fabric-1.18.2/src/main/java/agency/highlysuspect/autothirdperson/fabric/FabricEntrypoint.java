package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.EightteenTwoMinecraftInteraction;
import net.fabricmc.api.ClientModInitializer;

public class FabricEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AutoThirdPerson.instance = new AutoThirdPerson<>(
			new EightteenTwoMinecraftInteraction(),
			new FabricLoaderInteraction()
		);
		AutoThirdPerson.instance.init();
	}
}
