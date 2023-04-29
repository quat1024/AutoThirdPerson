package agency.highlysuspect.autothirdperson;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Handles the mod's main logic
 */
public class AutoThirdPerson<MC extends MinecraftInteraction, LI extends LoaderInteraction> {
	public static final String MODID = "auto_third_person";
	public static final String NAME = "Auto Third Person";
	
	public static AutoThirdPerson<?, ?> instance;
	
	public final MC mc;
	public final LI loader;
	public final MyLogger logger;
	
	public final State state;
	
	public AutoThirdPerson(MC mc, LI loader) {
		this.mc = mc;
		this.loader = loader;
		this.logger = mc.getLogger();
		logger.info("Auto Third Person loading...");
		this.state = new State();
	}
	
	public void init() {
		loader.init();
		loader.registerClientTicker(new Runnable() {
			@Override
			public void run() {
				tickClient();
			}
		});
		
		logger.info("Auto Third Person initialized.");
	}
	
	/// external api ///
	
	public void mount(Vehicle mounting) {
		mountOrDismount(mounting, true);
	}
	
	public void dismount(Vehicle dismounting) {
		mountOrDismount(dismounting, false);
	}
	
	public @Nullable MyCameraType modifyCycle(MyCameraType cycleFrom) {
		if(loader.settings().skipFrontView() && cycleFrom == MyCameraType.THIRD_PERSON) {
			debugSpam("Skipping third-person reversed view");
			return MyCameraType.FIRST_PERSON;
		} else return null;
	}
	
	public void manualPress() {
		if(loader.settings().cancelAutoRestore() && state.isActive()) {
			debugSpam("Cancelling auto-restore, if it was about to happen");
			state.cancel();
		}
	}
	
	/// internal api ///
	
	public void debugSpam(String msg, Object... args) {
		if(mc.debugScreenUp() || loader.settings().logSpam()) logger.info(msg, args);
	}
	
	private void tickClient() {
		if(!mc.safeToTick()) return;
		AtpSettings settings = loader.settings();
		
		boolean isFlying = mc.hasElytra() && mc.playerIsElytraFlying();
		boolean isSwimming = mc.hasSwimmingAnimation() ? mc.playerInSwimmingAnimation() : mc.playerIsUnderwater();
		
		if(settings.elytra() && mc.playerIsElytraFlying()) {
			if(state.elytraFlyingTicks == settings.elytraDelay()) {
				enterThirdPerson(new FlyingReason());
			}
			state.elytraFlyingTicks++;
		} else {
			if(state.elytraFlyingTicks != 0) {
				exitThirdPerson(new FlyingReason());
			}
			state.elytraFlyingTicks = 0;
		}
		
		if(settings.swim() && !(settings.elytra() && isFlying && isSwimming)) { //so swimming rules don't trigger when you dip underwater while flying with elytra
			if(state.wasSwimming && settings.stickySwim()) {
				isSwimming |= mc.playerIsUnderwater();
			}
			
			if(state.wasSwimming != isSwimming) {
				state.swimTicks = 0;
				state.wasSwimming = isSwimming;
			}
			
			if(isSwimming && state.swimTicks == settings.swimmingDelayStart()) enterThirdPerson(new SwimmingReason());
			if(!isSwimming && state.swimTicks == settings.swimmingDelayEnd()) exitThirdPerson(new SwimmingReason());
			
			state.swimTicks++;
		}
	}
	
	private void mountOrDismount(Vehicle vehicle, boolean mounting) {
		if(!mc.safeToTick()) return;
		AtpSettings settings = loader.settings();
		
		debugSpam((mounting ? "Mounting " : "Dismounting ") + vehicle.id());
		
		if(settings.useIgnore() && settings.ignorePattern().matcher(vehicle.id()).matches()) {
			debugSpam("Ignoring, since it matches the ignore pattern '{}'.", settings.ignorePattern());
			return;
		}
		
		boolean doIt = false;
		if(settings.boat() && vehicle.classification() == VehicleClassification.BOAT) {
			debugSpam("This is a boat!");
			doIt = true;
		}
		
		if(settings.cart() && vehicle.classification() == VehicleClassification.MINECART) {
			debugSpam("This is a minecart!");
			doIt = true;
		}
		
		if(settings.animal() && vehicle.classification() == VehicleClassification.ANIMAL) {
			debugSpam("This is an animal!");
			doIt = true;
		}
		
		if(settings.custom() && settings.customPattern().matcher(vehicle.id()).matches()) {
			debugSpam("This matches the pattern '{}'!", settings.customPattern());
			doIt = true;
		}
		
		if(doIt) {
			if(mounting) enterThirdPerson(new MountingReason(vehicle));
			else exitThirdPerson(new MountingReason(vehicle));
		}
	}
	
	private void enterThirdPerson(Reason reason) {
		if(state.reason == null && mc.getCameraType() == MyCameraType.FIRST_PERSON) {
			state.oldPerspective = mc.getCameraType();
			state.reason = reason;
			mc.setCameraType(MyCameraType.THIRD_PERSON);
			debugSpam("Automatically entering third person due to {}", reason);
		} else if(state.isActive()) {
			state.reason = reason;
			debugSpam("Continuing third person into {}", reason);
		}
	}
	
	private void exitThirdPerson(Reason reason) {
		if(!loader.settings().autoRestore()) {
			debugSpam("Not automatically leaving third person due to {} ending - auto restore is turned off", reason);
			return;
		}
		
		if(!state.isActive()) {
			debugSpam("Not automatically leaving third person due to {} ending - cancelled or inactive", reason);
			return;
		}
		
		if(!reason.equals(state.reason)) {
			debugSpam("Not automatically leaving third person due to {} ending - current state is {}", reason, state.reason);
			return;
		}
		
		debugSpam("Automatically leaving third person due to {} ending", reason);
		mc.setCameraType(state.oldPerspective);
		state.cancel();
	}
	
	public static class State {
		public MyCameraType oldPerspective = MyCameraType.FIRST_PERSON;
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
	
	//We have tagged unions at home
	public interface Reason {}
	
	public static final class FlyingReason implements Reason {
		@Override
		public String toString() {
			return "flying";
		}
		
		@Override
		public boolean equals(Object other) {
			return other instanceof FlyingReason;
		}
	}
	
	public static final class SwimmingReason implements Reason {
		@Override
		public String toString() {
			return "swimming";
		}
		
		@Override
		public boolean equals(Object other) {
			return other instanceof SwimmingReason;
		}
	}
	
	@SuppressWarnings("ClassCanBeRecord")
	public static final class MountingReason implements Reason {
		public MountingReason(Vehicle vehicle) {
			this.vehicle = vehicle;
		}
		
		private final Vehicle vehicle;
		
		@Override
		public boolean equals(Object other) {
			return other instanceof MountingReason && ((MountingReason) other).vehicle.vehicleEquals(vehicle); //vehicle
		}
		
		@Override
		public String toString() {
			return "riding " + vehicle.id();
		}
	}
	
	/// settings ///
	
	public SettingsSpec buildSettingsSpec() {
		SettingsSpec spec = new SettingsSpec();
		
		spec.integer("configVersion", null, 6, new MyConsumer<SettingsSpec.IntSetting>() {
			@Override
			public void accept(SettingsSpec.IntSetting thing) {
				thing.writeDefaultComment = false;
			}
		});
		
		spec.section("Scenarios");
		spec.bool("boat", "Automatically go into third person when riding a boat?", true);
		spec.bool("cart", "Automatically go into third person when riding a minecart?", true);
		spec.bool("animal", "Automatically go into third person when riding an animal?", true);
		if(mc.hasElytra()) {
			spec.bool("elytra", "Automatically go into third person when flying an elytra?", true);
		}
		spec.bool("swim", mc.hasSwimmingAnimation() ?
			"Automatically go into third person when doing the swimming animation?" :
			"Automatically go into third person when underwater?",
			false
		);
		spec.bool("custom", "If 'true', the customPattern will be used, and riding anything matching it will toggle third person.", false);
		spec.bool("useIgnore", "If 'true', the ignorePattern will be used, and anything matching it will be ignored.", false);
		
		MyConsumer<SettingsSpec.IntSetting> nonNegative = new MyConsumer<SettingsSpec.IntSetting>() {
			@Override
			public void accept(SettingsSpec.IntSetting thing) {
				thing.min = 0;
			}
		};
		
		spec.section("Scenario Options");
		if(mc.hasElytra()) {
			spec.integer("elytraDelay", "Ticks of elytra flight required before the camera automatically toggles if the 'elytra' option is enabled.", 7, nonNegative);
		}
		spec.integer("swimmingDelayStart", "Ticks of swimming required before the camera automatically toggles if the 'swim' option is enabled.", 0, nonNegative);
		spec.integer("swimmingDelayEnd", "Ticks of not swimming required before the camera restores if the 'swim' option is enabled.", 10, nonNegative);
		if(mc.hasSwimmingAnimation()) {
			spec.bool("stickySwim", "If 'true', your head has to completely exit the water to count as 'not swimming anymore', for the purposes of restoring\nthe camera when you're done swimming. If 'false', you just have to stop doing the swimming animation.", true);
		}
		spec.pattern("customPattern", "Entity IDs that match this regular expression will be considered if the 'custom' option is enabled.", Pattern.compile("^minecraft:(cow|chicken)$"));
		spec.pattern("ignorePattern", "Entity IDs that match this regular expression will be ignored if the 'useIgnore' option is enabled.", Pattern.compile("^examplemod:example$"));
		
		spec.section("Restoration");
		spec.bool("autoRestore", "When the situation that Auto Third Person put you into third person for is over,\nthe camera will be restored back to the way it was.", true);
		spec.bool("cancelAutoRestore", "If 'true', pressing f5 after mounting something will prevent your camera\nfrom being automatically restored to first-person when you dismount.", true);
		
		spec.section("Extras");
		spec.bool("skipFrontView", "Skip the 'third-person front' camera mode when pressing F5.", false);
		spec.bool("logSpam", "Dump a bunch of debug crap into the log.\nMight be handy!", false);
		
		return spec;
	}
}
