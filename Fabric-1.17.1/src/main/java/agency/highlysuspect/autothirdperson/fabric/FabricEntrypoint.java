package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.SeventeenOneMinecraftInteraction;
import net.fabricmc.api.ClientModInitializer;

public class FabricEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AutoThirdPerson.instance = new AutoThirdPerson<>(
			new SeventeenOneMinecraftInteraction(),
			new FabricLoaderInteraction()
		);
		AutoThirdPerson.instance.init();
	}
}
