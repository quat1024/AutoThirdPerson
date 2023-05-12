package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.SixteenFiveMinecraftInteractions;
import agency.highlysuspect.autothirdperson.VersionCapabilities;
import net.fabricmc.api.ClientModInitializer;

public class FabricEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new AutoThirdPerson<>(
			new SixteenFiveMinecraftInteractions(),
			new FabricLoaderInteraction(),
			new VersionCapabilities.Builder()
				.hasElytra()
				.hasSwimmingAnimation()
		).initLoader();
	}
}
