package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.NineteenTwoMinecraftInteraction;
import agency.highlysuspect.autothirdperson.VersionCapabilities;
import agency.highlysuspect.crummyconfig.CookedCrummyConfig;
import agency.highlysuspect.crummyconfig.UncookedCrummyConfig;
import net.fabricmc.api.ClientModInitializer;
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

public class FabricEntrypoint extends NineteenTwoMinecraftInteraction implements ClientModInitializer {
	private UncookedCrummyConfig uncookedConfig;
	private AtpSettings settings = AtpSettings.MISSING;
	
	@Override
	public void onInitializeClient() {
		init();
	}
	
	@Override
	public void init() {
		super.init();
		
		uncookedConfig = new UncookedCrummyConfig(
			FabricLoader.getInstance().getConfigDir().resolve(MODID + ".cfg"),
			buildSettingsSpec()
		);
		
		//Load it once now
		loadConfig();
		
		//Load it on F3+T
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation(MODID, "settings_reloader");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				loadConfig();
			}
		});
		
		//Load it on execution of client command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommandManager.literal(MODID).then(
					ClientCommandManager.literal("reload").executes(s -> {
						loadConfig();
						s.getSource().sendFeedback(Component.literal(NAME + " settings reloaded"));
						return 0;
					}))));
	}
	
	private void loadConfig() {
		try {
			uncookedConfig.load();
		} catch (IOException e) {
			throw new RuntimeException("IOException loading " + NAME + " config", e);
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
