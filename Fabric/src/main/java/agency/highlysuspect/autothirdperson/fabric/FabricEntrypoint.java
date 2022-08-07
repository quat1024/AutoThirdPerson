package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.XplatStuff;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.file.Path;
import java.util.function.Consumer;

public class FabricEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AutoThirdPerson.INSTANCE = new AutoThirdPerson(new XplatStuff() {
			@Override
			public Path getConfigDir() {
				return FabricLoader.getInstance().getConfigDir();
			}
			
			@Override
			public void registerResourceReloadListener(Runnable action) {
				ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
					@Override
					public ResourceLocation getFabricId() {
						return new ResourceLocation(AutoThirdPerson.MODID, "settings_reloader");
					}
					
					@Override
					public void onResourceManagerReload(ResourceManager resourceManager) {
						action.run();
					}
				});
			}
			
			@Override
			public void registerClientReloadCommand(Runnable leakyAbstraction) {
				ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
					dispatcher.register(
						ClientCommandManager.literal(AutoThirdPerson.MODID).then(
							ClientCommandManager.literal("reload").executes(
								ctx -> {
									leakyAbstraction.run();
									ctx.getSource().sendFeedback(Component.literal("Reloaded config file"));
									return 0;
								})));
				});
			}
			
			@Override
			public void registerClientTicker(Consumer<Minecraft> action) {
				ClientTickEvents.START_CLIENT_TICK.register(action::accept);
			}
		});
	}
}
