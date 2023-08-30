package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.NineteenTwoAutoThirdPerson;
import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import agency.highlysuspect.crummyconfig.CrummyConfig2;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;

public class FabricEntrypoint extends NineteenTwoAutoThirdPerson implements ClientModInitializer {
	private final KeyMapping TOGGLE_MOD = KeyBindingHelper.registerKeyBinding(new KeyMapping(
		"autothirdperson.toggle",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_UNKNOWN,
		"key.categories.misc"
	));
	
	@Override
	public void onInitializeClient() {
		init();
	}
	
	@Override
	public void init() {
		super.init();
		
		ClientTickEvents.START_CLIENT_TICK.register(__ -> tickClient());
		
		//Load it on F3+T
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation(MODID, "settings_reloader");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				refreshConfig();
			}
		});
		
		//Load it on execution of client command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommandManager.literal(MODID).then(
					ClientCommandManager.literal("reload").executes(s -> {
						refreshConfig();
						s.getSource().sendFeedback(Component.literal(NAME + " settings reloaded"));
						return 0;
					}))));
	}
	
	@Override
	public CookedConfig makeConfig(ConfigSchema s) {
		return new CrummyConfig2(s, FabricLoader.getInstance().getConfigDir().resolve(MODID + ".cfg"));
	}
	
	@Override
	public boolean modEnableToggleKeyPressed() {
		return TOGGLE_MOD.isDown();
	}
}
