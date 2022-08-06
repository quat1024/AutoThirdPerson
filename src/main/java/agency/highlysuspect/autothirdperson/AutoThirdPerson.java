package agency.highlysuspect.autothirdperson;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class AutoThirdPerson implements ClientModInitializer {
	public static final String MODID = "auto_third_person";
	public static final Path SETTINGS_PATH = FabricLoader.getInstance().getConfigDir().resolve("auto_third_person.cfg");
	public static final Logger LOGGER = LogManager.getLogger("Auto Third Person");
	
	public static AutoThirdPerson INSTANCE;
	
	public Settings settings = new Settings();
	private final ConfigShape configShape = ConfigShape.createFromPojo(settings);
	public State state = new State();
	
	public void onInitializeClient() {
		INSTANCE = this;
		
		//This'll get called during first game startup too (it's a "load listener" as well as "reload", I guess)
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation(MODID, "settings_reloader");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager manager) {
				readConfig();
			}
		});
		
		ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal(MODID).then(ClientCommandManager.literal("reload").executes(c -> {
			readConfig();
			c.getSource().sendFeedback(new TranslatableComponent("auto_third_person.reload"));
			return 0;
		})));
		
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			//Per-frame status checking.
			//I wrote this a long time ago and I don't want to touch it, it's scary, lol.
			if(client.level != null && client.player != null && !client.isPaused()) {
				if(settings.elytra && client.player.isFallFlying()) {
					if(state.elytraFlyingTicks == settings.elytraDelay) enterThirdPerson(new FlyingReason());
					state.elytraFlyingTicks++;
				} else {
					if(state.elytraFlyingTicks != 0) leaveThirdPerson(new FlyingReason());
					state.elytraFlyingTicks = 0;
				}
				
				//I think this is about making sure the swimming rules don't trigger when you are actually flying through the water?
				boolean swimmingAndFlying = client.player.isFallFlying() && client.player.isSwimming();
				if(settings.swim && !(settings.elytra && swimmingAndFlying)) {
					boolean isSwimming = client.player.isSwimming();
					if(state.wasSwimming != isSwimming) {
						state.swimTicks = 0;
						state.wasSwimming = isSwimming;
					}
					
					if(isSwimming && state.swimTicks == settings.swimmingDelayStart) enterThirdPerson(new SwimmingReason());
					if(!isSwimming && state.swimTicks == settings.swimmingDelayEnd) leaveThirdPerson(new SwimmingReason());
					
					state.swimTicks++;
				}
			}
		});
	}
	
	private void readConfig() {
		try {
			settings = configShape.readFromOrCreateFile(SETTINGS_PATH, new Settings());
		} catch (ConfigShape.ConfigParseException e) {
			//Don't bring down the whole game just because the user made a typo in the config file, give them a chance to correct it.
			//Logging e.getCause() will provide a more informative stacktrace than the trace of the ConfigParseException itself.
			//The mod name is restated because some logging configs don't show the name of the logger by default.
			LOGGER.error("[Auto Third Person] Problem parsing config file. Config has not changed.");
			LOGGER.error(e.getMessage(), e.getCause());
		} catch (IOException e) {
			throw new RuntimeException("Severe problem reading config file", e);
		}
	}
	
	public void debugSpam(String yeah) {
		if(settings.logSpam) LOGGER.info(yeah);
	}
	
	//called from LocalPlayerMixin
	public void mountOrDismount(Entity vehicle, boolean mounting) {
		Minecraft client = Minecraft.getInstance();
		if(client.level == null || client.player == null || vehicle == null) return;
		
		String entityId = Registry.ENTITY_TYPE.getKey(vehicle.getType()).toString();
		debugSpam((mounting ? "Mounting " : "Dismounting ") + entityId);
		
		if(settings.useIgnore && settings.ignorePattern.matcher(entityId).matches()) {
			debugSpam("Ignoring, since it matches the ignore pattern '" + settings.ignorePattern + "'.");
			return;
		}
		
		boolean doIt = false;
		if(settings.boat && vehicle instanceof Boat) {
			debugSpam("This is a boat!");
			doIt = true;
		}
		if(settings.cart && vehicle instanceof Minecart) {
			debugSpam("This is a minecart!");
			doIt = true;
		}
		if(settings.animal && vehicle instanceof Animal) {
			debugSpam("This is an animal!");
			doIt = true;
		}
		if(settings.custom && settings.customPattern.matcher(entityId).matches()) {
			debugSpam("This matches the pattern '" + settings.customPattern + "'!");
			doIt = true;
		}
		
		if(doIt) {
			if(mounting) enterThirdPerson(new MountingReason(vehicle));
			else leaveThirdPerson(new MountingReason(vehicle));
		}
	}
	
	//called from MinecraftMixin
	public void f5Press() {
		if(settings.cancelAutoRestore && state.isActive()) {
			cancel();
		}
	}
	
	public void enterThirdPerson(Reason reason) {
		Minecraft client = Minecraft.getInstance();
		
		//TODO Hey this state machine stuff is kind of a mess
		// It might also be a good idea to make e.g. "moving from one mount to another" be atomic, and not actually secretly putting you in FP for less than a frame
		
		if(state.reason == null && client.options.getCameraType().isFirstPerson()) {
			state.oldPerspective = client.options.getCameraType();
			state.reason = reason;
			client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			debugSpam("Automatically entering third person due to " + reason);
		} else if(state.isActive()) {
			state.reason = reason;
			debugSpam("Continuing third person into " + reason + " action");
		}
	}
	
	public void leaveThirdPerson(Reason reason) {
		Minecraft client = Minecraft.getInstance();
		
		if(!settings.autoRestore) {
			debugSpam("Not automatically leaving third person - auto restore is turned off");
			return;
		}
		
		if(!state.isActive()) {
			debugSpam("Not automatically leaving third person - cancelled or inactive");
			return;
		}
		
		if(!reason.equals(state.reason)) {
			debugSpam("Not automatically leaving third person - current state is " + state.reason + ", requested state is " + reason);
			return;
		}
		
		debugSpam("Automatically leaving third person due to " + reason + " ending");
		client.options.setCameraType(state.oldPerspective);
		state.cancel();
	}
	
	public void cancel() {
		state.cancel();
		debugSpam("Cancelling auto-restore, if it was about to happen");
	}
	
	public static class State {
		public CameraType oldPerspective = CameraType.FIRST_PERSON;
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
	
	//We do have tagged unions at home now!!
	public sealed interface Reason permits MountingReason, FlyingReason, SwimmingReason {}
	public static record MountingReason(Entity vehicle) implements Reason {}
	public static record FlyingReason() implements Reason {}
	public static record SwimmingReason() implements Reason {}
}
