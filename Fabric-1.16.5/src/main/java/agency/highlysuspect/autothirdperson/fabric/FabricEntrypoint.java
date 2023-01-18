package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.SixteenFiveMinecraftInteractions;
import net.fabricmc.api.ClientModInitializer;

public class FabricEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AutoThirdPerson.instance = new AutoThirdPerson<>(
			new SixteenFiveMinecraftInteractions(),
			new FabricLoaderInteraction()
		);
		AutoThirdPerson.instance.init();
	}
}
