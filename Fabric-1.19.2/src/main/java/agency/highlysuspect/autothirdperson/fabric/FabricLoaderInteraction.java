package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.LoaderInteraction;
import agency.highlysuspect.crummyconfig.CookedCrummyConfig;
import agency.highlysuspect.crummyconfig.UncookedCrummyConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class FabricLoaderInteraction implements LoaderInteraction {
	private UncookedCrummyConfig uncookedConfig;
	private AtpSettings settings = AtpSettings.MISSING;
	
	@Override
	public void initLoader() {
		uncookedConfig = new UncookedCrummyConfig(
			FabricLoader.getInstance().getConfigDir().resolve(AutoThirdPerson.MODID + ".cfg"),
			AutoThirdPerson.instance.buildSettingsSpec()
		);
		
		//Load it once now
		loadConfig();
		
		//Load it on F3+T
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation(AutoThirdPerson.MODID, "settings_reloader");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				loadConfig();
			}
		});
		
		//Load it on execution of client command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> 
			dispatcher.register(
				ClientCommandManager.literal(AutoThirdPerson.MODID).then(
					ClientCommandManager.literal("reload").executes(s -> {
						loadConfig();
						s.getSource().sendFeedback(Component.literal(AutoThirdPerson.NAME + " settings reloaded"));
						return 0;
					}))));
	}
	
	private void loadConfig() {
		try {
			uncookedConfig.load();
		} catch (IOException e) {
			throw new RuntimeException("IOException loading " + AutoThirdPerson.NAME + " config", e);
		}
		
		settings = new CookedCrummyConfig(uncookedConfig);
	}
	
	@Override
	public AtpSettings settings() {
		return settings;
	}
	
	@Override
	public void registerClientTicker(Runnable action) {
		ClientTickEvents.START_CLIENT_TICK.register(mc -> action.run());
	}
}
