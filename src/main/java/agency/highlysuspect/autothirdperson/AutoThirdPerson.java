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
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public class AutoThirdPerson implements ClientModInitializer {
	public static State STATE = new State();
	public static Settings SETTINGS;
	public static Path SETTINGS_PATH;
	public static final Logger LOGGER = LogManager.getLogger("Auto Third Person");
	
	public void onInitializeClient() {
		SETTINGS_PATH = FabricLoader.getInstance().getConfigDir().resolve("auto_third_person.cfg");
		
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			public Identifier getFabricId() {
				return new Identifier("auto_third_person", "settings_reloader");
			}
			
			public void apply(ResourceManager manager) {
				SETTINGS = Settings.load(SETTINGS_PATH);
			}
		});
		
		ClientTickEvents.START_CLIENT_TICK.register(AutoThirdPerson::clientTick);
	}
	
	//called from the above event
	public static void clientTick(MinecraftClient client) {
		if(client.world != null && client.player != null && !client.isPaused()) {
			if(SETTINGS.elytra && client.player.isFallFlying()) {
				if(STATE.elytraFlyingTicks == SETTINGS.elytraDelay) enterThirdPerson(Reason.flying());
				STATE.elytraFlyingTicks++;
			} else {
				if(STATE.elytraFlyingTicks != 0) leaveThirdPerson(Reason.flying());
				STATE.elytraFlyingTicks = 0;
			}
			
			boolean swimmingAndFlying = client.player.isFallFlying() && client.player.isSwimming();
			
			if(SETTINGS.swim && !(SETTINGS.elytra && swimmingAndFlying)) {
				boolean isSwimming = client.player.isSwimming();
				if(STATE.wasSwimming != isSwimming) {
					STATE.swimTicks = 0;
					STATE.wasSwimming = isSwimming;
				}
				
				if(isSwimming && STATE.swimTicks == SETTINGS.swimmingDelayStart) enterThirdPerson(Reason.swimming());
				if(!isSwimming && STATE.swimTicks == SETTINGS.swimmingDelayEnd) leaveThirdPerson(Reason.swimming());
				
				STATE.swimTicks++;
			}
		}
	}
	
	//called from a mixin
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
			if(mounting) enterThirdPerson(Reason.mounting(vehicle));
			else leaveThirdPerson(Reason.mounting(vehicle));
		}
	}
	
	public static void f5Press() {
		if(SETTINGS.cancelAutoRestore && STATE.isActive()) {
			cancel();
		}
	}
	
	public static void enterThirdPerson(Reason reason) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		//TODO Hey this state machine stuff is kind of a mess
		// It might also be a good idea to make e.g. "moving from one mount to another" be atomic, and not actually secretly putting you in FP for less than a frame
		
		if(STATE.reason == null && client.options.getPerspective().isFirstPerson()) {
			STATE.oldPerspective = client.options.getPerspective();
			STATE.reason = reason;
			client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
			
			if(SETTINGS.logSpam) LOGGER.info("Automatically entering third person due to " + reason);
		} else if(STATE.isActive()) {
			STATE.reason = reason;
			if(SETTINGS.logSpam) LOGGER.info("Continuing third person into " + reason + " action");
		}
	}
	
	public static void leaveThirdPerson(Reason reason) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		if(!SETTINGS.autoRestore) {
			if(SETTINGS.logSpam) LOGGER.info("Not automatically leaving third person - auto restore is turned off");
			return;
		}
		
		if(!STATE.isActive()) {
			if(SETTINGS.logSpam) LOGGER.info("Not automatically leaving third person - cancelled or inactive");
			return;
		}
		
		if(!reason.equals(STATE.reason)) {
			LOGGER.info("Not automatically leaving third person - current state is " + STATE.reason + ", requested state is " + reason);
			return;
		}
		
		if(SETTINGS.logSpam) LOGGER.info("Automatically leaving third person due to " + reason + " ending");
		client.options.setPerspective(STATE.oldPerspective);
		STATE.cancel();
	}
	
	public static void cancel() {
		STATE.cancel();
		if(SETTINGS.logSpam) LOGGER.info("Cancelling auto-restore, if it was about to happen");
	}
	
	public static class State {
		public Perspective oldPerspective = Perspective.FIRST_PERSON;
		public @Nullable Reason reason;
		
		public int elytraFlyingTicks = 0;
		public boolean wasSwimming = false;
		public int swimTicks = 0;
		
		public boolean isActive() {
			return reason != null;
		}
		
		public void cancel() {
			reason = null;
		}
	}
	
	public static class Reason {
		//We have tagged unions at home.
		private Reason(String magic, Object extra) {
			this.magic = magic;
			this.extra = extra;
		}
		
		private final String magic;
		private final Object extra;
		
		public static Reason mounting(Entity vehicle) {
			return new Reason("mounting", vehicle.getUuid());
		}
		
		public static Reason flying() {
			return new Reason("flying", null);
		}
		
		public static Reason swimming() {
			return new Reason("swimming", null);
		}
		
		public String toString() {
			if("mounting".equals(magic)) {
				return "mounting entity " + extra.toString();
			}
			return magic;
		}
		
		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;
			
			Reason reason = (Reason) o;
			
			if(!magic.equals(reason.magic)) return false;
			return Objects.equals(extra, reason.extra);
		}
	}
}
