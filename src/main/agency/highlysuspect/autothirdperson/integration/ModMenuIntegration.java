package agency.highlysuspect.autothirdperson.integration;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if(FabricLoader.getInstance().isModLoaded("cloth-config2")) {
			return ClothIntegration::createConfigScreen;
		} else return null;
	}
}
