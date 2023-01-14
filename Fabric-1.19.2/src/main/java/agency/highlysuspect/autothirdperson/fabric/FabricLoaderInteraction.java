package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.LoaderInteraction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FabricLoaderInteraction implements LoaderInteraction {
	@Override
	public AtpSettings settings() {
		return AtpSettings.DEFAULT_TODO;
	}
	
	@Override
	public void registerClientTicker(Runnable action) {
		ClientTickEvents.START_CLIENT_TICK.register(mc -> action.run());
	}
	
	//	@Override
//	public Path getConfigDir() {
//		return FabricLoader.getInstance().getConfigDir();
//	}
//	
//	@Override
//	public void registerClientTicker(Consumer<Minecraft> action) {
//		ClientTickEvents.START_CLIENT_TICK.register(action::accept);
//	}
//	
//	@Override
//	public void registerResourceReloadListener(Runnable action) {
//		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
//			@Override
//			public ResourceLocation getFabricId() {
//				return new ResourceLocation(AutoThirdPersonOld.MODID, "settings_reloader");
//			}
//			
//			@Override
//			public void onResourceManagerReload(ResourceManager resourceManager) {
//				action.run();
//			}
//		});
//	}
//	
//	@Override
//	public void registerClientReloadCommand(LiteralArgumentBuilder<FabricClientCommandSource> command) {
//		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(command));
//	}
//	
//	@Override
//	public LiteralArgumentBuilder<FabricClientCommandSource> literal(String lit) {
//		return ClientCommandManager.literal(lit);
//	}
//	
//	@Override
//	public Consumer<Component> getFeedbackSender(FabricClientCommandSource cmdSource) {
//		return cmdSource::sendFeedback;
//	}
}
