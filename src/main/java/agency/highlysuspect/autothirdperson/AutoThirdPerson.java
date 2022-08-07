package agency.highlysuspect.autothirdperson;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class AutoThirdPerson {
	public static final String MODID = "auto_third_person";
	public static final Logger LOGGER = LogManager.getLogger("Auto Third Person");
	
	public static AutoThirdPerson INSTANCE;
	
	public Settings settings;
	private final Path settingsPath;
	private final ConfigShape configShape;
	private final XplatStuff services;
	
	public State state;
	
	public AutoThirdPerson(XplatStuff services) {
		this.settings = new Settings();
		this.settingsPath = services.getConfigDir().resolve("auto_third_person.cfg");
		this.configShape = ConfigShape.createFromPojo(settings);
		this.services = services;
		
		this.state = new State();
		
		services.registerResourceReloadListener(this::readConfig);
		services.registerClientReloadCommand(this::readConfig);
		
		services.registerClientTicker((client) -> {
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
					if(state.wasSwimming && settings.stickySwim) {
						isSwimming |= client.player.isUnderWater();
					}
					
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
			settings = configShape.readFromOrCreateFile(settingsPath, new Settings());
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
	
	public static class Settings {
		private static final int CURRENT_CONFIG_VERSION = 5;
		@ConfigShape.SkipDefault
		private int configVersion = CURRENT_CONFIG_VERSION;
		
		/////////////////////////////////
		@ConfigShape.Section("Scenarios")
		/////////////////////////////////
		
		@ConfigShape.Comment("Automatically go into third person when riding a boat?")
		public boolean boat = true;
		@ConfigShape.Comment("Automatically go into third person when riding a minecart?")
		public boolean cart = true;
		@ConfigShape.Comment("Automatically go into third person when riding an animal?")
		public boolean animal = true;
		@ConfigShape.Comment("Automatically go into third person when flying an elytra?")
		public boolean elytra = true;
		@ConfigShape.Comment("Automatically go into third person when doing the swimming animation?")
		public boolean swim = false;
		
		@ConfigShape.Comment("If 'true', the customPattern will be used, and riding anything matching it will toggle third person.")
		public boolean custom = false;
		@ConfigShape.Comment("If 'true', the ignorePattern will be used, and anything matching it will be ignored.")
		public boolean useIgnore = false;
		
		////////////////////////////////////////
		@ConfigShape.Section("Scenario Options")
		////////////////////////////////////////
		
		@ConfigShape.Comment("Ticks of elytra flight required before the camera automatically toggles if the 'elytra' option is enabled.")
		@ConfigShape.AtLeast(0)
		public int elytraDelay = 7;
		
		@ConfigShape.Comment("Ticks of swimming required before the camera automatically toggles if the 'swim' option is enabled.")
		@ConfigShape.AtLeast(0)
		public int swimmingDelayStart = 0;
		
		@ConfigShape.Comment("Ticks of not swimming required before the camera restores if the 'swim' option is enabled.")
		@ConfigShape.AtLeast(0)
		public int swimmingDelayEnd = 10;
		
		@ConfigShape.Comment({
			"If 'true', your head has to completely exit the water to count as 'not swimming anymore', for the purposes of restoring",
			"the camera when you're done swimming. If 'false', you just have to stop doing the swimming animation."
		})
		public boolean stickySwim = true;
		
		@ConfigShape.Comment("Entity IDs that match this regular expression will be considered if the 'custom' option is enabled.")
		public Pattern customPattern = Pattern.compile("^minecraft:(cow|chicken)$");
		
		@ConfigShape.Comment("Entity IDs that match this regular expression will be ignored if the 'useIgnore' option is enabled.")
		public Pattern ignorePattern = Pattern.compile("^examplemod:example$");
		
		///////////////////////////////////
		@ConfigShape.Section("Restoration")
		///////////////////////////////////
		
		@ConfigShape.Comment({
			"When the situation that Auto Third Person put you into third person for is over,",
			"the camera will be restored back to the way it was."
		})
		public boolean autoRestore = true;
		
		@ConfigShape.Comment({
			"If 'true', pressing f5 after mounting something will prevent your camera",
			"from being automatically restored to first-person when you dismount."
		})
		public boolean cancelAutoRestore = true;
		
		/////////////////////////////
		@ConfigShape.Section("Extra")
		/////////////////////////////
		
		@ConfigShape.Comment("Skip the 'third-person front' camera mode when pressing F5.")
		public boolean skipFrontView = false;
		
		@ConfigShape.Comment({
			"Dump a bunch of debug crap into the log.",
			"Might be handy!"
		})
		public boolean logSpam = false;
	}
}
