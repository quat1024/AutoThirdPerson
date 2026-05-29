package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.TwentySixOneTwoAutoThirdPerson;
import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import agency.highlysuspect.crummyconfig.CrummyConfig2;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;

public class FabricEntrypoint extends TwentySixOneTwoAutoThirdPerson implements ClientModInitializer {
	private final KeyMapping TOGGLE_MOD = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"autothirdperson.toggle",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_UNKNOWN,
		KeyMapping.Category.MISC
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
			public Identifier getFabricId() {
				return Identifier.fromNamespaceAndPath(MODID, "settings_reloader");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				refreshConfig();
			}
		});
		
		//Load it on execution of client command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommands.literal(MODID).then(
					ClientCommands.literal("reload").executes(s -> {
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
