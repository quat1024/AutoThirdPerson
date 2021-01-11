package agency.highlysuspect.autothirdperson;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AutoThirdPerson implements ClientModInitializer {
	public static State STATE = new State();
	public static Settings SETTINGS;
	public static final Logger LOGGER = LogManager.getLogger("Auto Third Person");
	
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			public Identifier getFabricId() {
				return new Identifier("auto_third_person", "settings_reloader");
			}
			
			public void apply(ResourceManager manager) {
				SETTINGS = Settings.load(FabricLoader.getInstance().getConfigDir().resolve("auto_third_person.cfg"));
			}
		});
		
		ClientTickEvents.START_CLIENT_TICK.register(AutoThirdPerson::clientTick);
	}
	
	public static void clientTick(MinecraftClient client) {
		if(client.world != null && client.player != null && !client.isPaused()) {
			if(SETTINGS.elytra && client.player.isFallFlying()) {
				if(STATE.elytraFlyingTicks == SETTINGS.elytraDelay) enterThirdPerson(client);
				STATE.elytraFlyingTicks++;
			} else {
				if(STATE.elytraFlyingTicks != 0) leaveThirdPerson(client);
				STATE.elytraFlyingTicks = 0;
			}
		}
	}
	
	public static void mountOrDismount(Entity vehicle, boolean mounting) {
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.world == null || client.player == null) return;
		
		String entityId = Registry.ENTITY_TYPE.getId(vehicle.getType()).toString();
		if(SETTINGS.logSpam) LOGGER.info((mounting ? "Mounting " : "Dismounting ") + entityId);
		
		if(SETTINGS.useIgnore && SETTINGS.ignorePattern.matcher(entityId).matches()) {
			if(SETTINGS.logSpam) LOGGER.info("Ignoring, since it matches the ignore pattern '" + SETTINGS.ignorePattern + "'.");
			return;
		}
		
		boolean doIt = false;
		if(SETTINGS.boat && vehicle instanceof BoatEntity) {
			if(SETTINGS.logSpam) LOGGER.info("This is a boat!");
			doIt = true;
		}
		if(SETTINGS.cart && vehicle instanceof MinecartEntity) {
			if(SETTINGS.logSpam) LOGGER.info("This is a minecart!");
			doIt = true;
		}
		if(SETTINGS.animal && vehicle instanceof AnimalEntity) {
			if(SETTINGS.logSpam) LOGGER.info("This is an animal!");
			doIt = true;
		}
		if(SETTINGS.custom && SETTINGS.customPattern.matcher(entityId).matches()) {
			if(SETTINGS.logSpam) LOGGER.info("This matches the pattern '" + SETTINGS.customPattern + "'!");
			doIt = true;
		}
		
		if(doIt) {
			if(mounting) enterThirdPerson(client);
			else leaveThirdPerson(client);
		}
	}
	
	public static void f5Press() {
		if(SETTINGS.cancelAutoRestore && !STATE.cancelled) {
			STATE.cancelled = true;
			
			if(SETTINGS.logSpam) LOGGER.info("Cancelling auto-restore, if it was about to happen");
		}
	}
	
	private static void enterThirdPerson(MinecraftClient client) {
		STATE.oldPerspective = client.options.getPerspective();
		STATE.cancelled = false;
		client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
		
		if(SETTINGS.logSpam) LOGGER.info("Automatically entering third person");
	}
	
	private static void leaveThirdPerson(MinecraftClient client) {
		if(SETTINGS.autoRestore && !STATE.cancelled) {
			client.options.setPerspective(STATE.oldPerspective);
			
			if(SETTINGS.logSpam) LOGGER.info("Automatically leaving third person");
		}
	}
	
	public static class State {
		public Perspective oldPerspective = Perspective.FIRST_PERSON;
		public boolean cancelled = false;
		public int elytraFlyingTicks = 0;
	}
}
