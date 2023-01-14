package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.LoaderInteraction;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
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

public class FabricLoaderInteraction implements LoaderInteraction<FabricClientCommandSource> {
	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
	
	@Override
	public void registerClientTicker(Consumer<Minecraft> action) {
		ClientTickEvents.START_CLIENT_TICK.register(action::accept);
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
	public void registerClientReloadCommand(LiteralArgumentBuilder<FabricClientCommandSource> command) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(command));
	}
	
	@Override
	public LiteralArgumentBuilder<FabricClientCommandSource> literal(String lit) {
		return ClientCommandManager.literal(lit);
	}
	
	@Override
	public Consumer<Component> getFeedbackSender(FabricClientCommandSource cmdSource) {
		return cmdSource::sendFeedback;
	}
}
