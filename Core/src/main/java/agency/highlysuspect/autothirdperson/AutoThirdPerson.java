package agency.highlysuspect.autothirdperson;

import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import agency.highlysuspect.autothirdperson.config.StubConfig;
import agency.highlysuspect.autothirdperson.wrap.MyLogger;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import org.jetbrains.annotations.Nullable;

public abstract class AutoThirdPerson {
	public static final String MODID = "auto_third_person";
	public static final String NAME = "Auto Third Person";
	
	public static AutoThirdPerson instance;
	
	public final MyLogger logger;
	public final VersionCapabilities version = caps(new VersionCapabilities.Builder()).build();
	
	//config
	public final AtpOpts opts = new AtpOpts(version);
	public CookedConfig config = new StubConfig();
	
	public final State state;
	
	//Well-known camera types.
	//Note that a mod *might* enum-extend and add a new camera type, so this isn't the complete universe. 
	public static final int FIRST_PERSON = 0;
	public static final int THIRD_PERSON = 1;
	public static final int THIRD_PERSON_REVERSED = 2;
	
	public AutoThirdPerson() {
		if(instance == null) {
			instance = this;
		} else {
			RuntimeException e = new IllegalStateException(NAME + " instantiated twice!");
			e.printStackTrace();
			makeLogger().error(NAME + " instantiated twice!", e);
			throw e;
		}
		
		this.logger = makeLogger();
		this.state = makeState();
	}
	
	public abstract VersionCapabilities.Builder caps(VersionCapabilities.Builder builder);
	public abstract MyLogger makeLogger();
	public State makeState() { return new State(); }
	public abstract CookedConfig makeConfig(ConfigSchema s);
	
	public abstract int getCameraType();
	public abstract void setCameraType(int type);
	public int numberOfCameraTypes() {
		//It makes sense to me that a mod might add additional camera types.
		//On versions that use an `int` to manage camera types, it's not possible to check how many types there are.
		//But, like, maybe a mod enum-extends new types? Then we can check with CameraType.values().length, at least.
		//I dunno, this feels like a bit of a reach.
		return 3;
	}
	
	public abstract boolean f3ScreenUp();
	/** Player exists, level exists, game is not paused, etc */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public abstract boolean safeToTick();
	/** or `false` if this game doesn't have an elytra */
	public abstract boolean playerIsElytraFlying();
	/** or `false` if this game doesn't have the swimming animation */
	public abstract boolean playerInSwimmingAnimation();
	/** Whether the player's head/camera/whatever is underwater, used for `stickySwim`, and for the swim setting on pre-1.13 */
	public abstract boolean playerIsUnderwater();
	public abstract boolean modEnableToggleKeyPressed();
	public abstract void sayEnabled(boolean enabled);
	
	public void init() {
		logger.info(NAME + " initializing...");
		
		config = makeConfig(opts.makeSchema());
		refreshConfig();
	}
	
	public void refreshConfig() {
		try {
			config.refresh();
		} catch (Exception e) {
			logger.error("Problem with " + NAME + " config load", e);
		}
	}
	
	/// external api ///
	
	public void debugSpam(String msg, Object... args) {
		if(f3ScreenUp() || config.get(opts.LOG_SPAM)) logger.info(msg, args);
	}
	
	public void mount(Vehicle mounting) {
		if(!state.modEnabled) return;
		
		mountOrDismount(mounting, true);
	}
	
	public void dismount(Vehicle dismounting) {
		if(!state.modEnabled) return;
		
		mountOrDismount(dismounting, false);
	}
	
	public void manualPress() {
		if(!state.modEnabled) return;
		
		if(config.get(opts.CANCEL_AUTO_RESTORE) && state.isActive()) {
			debugSpam("Cancelling auto-restore, if it was about to happen");
			state.cancel();
		}
	}
	
	public void renderClient() {
		boolean modEnableToggleKeyPressed = modEnableToggleKeyPressed();
		if(modEnableToggleKeyPressed && !state.modEnableToggleKeyWasPressed) {
			state.modEnabled ^= true;
			sayEnabled(state.modEnabled);
			debugSpam("Auto Third Person is now " + (state.modEnabled ? "ENABLED" : "DISABLED"));
		}
		state.modEnableToggleKeyWasPressed = modEnableToggleKeyPressed;
		
		if(!safeToTick() || !state.modEnabled) return;
		
		if(config.get(opts.SKIP_FRONT_VIEW)) {
			int currentCameraType = getCameraType();
			if(currentCameraType == THIRD_PERSON_REVERSED) {
				debugSpam("Skipping third-person reversed view");
				setCameraType((currentCameraType + 1) % numberOfCameraTypes());
			}
		}
	}
	
	public void tickClient() {
		if(!state.modEnabled || !safeToTick()) return;
		
		boolean elytraEnabled = config.getOr(opts.ELYTRA, false);
		int elytraDelay = config.getOr(opts.ELYTRA_DELAY, 10);
		boolean swimEnabled = config.get(opts.SWIM);
		
		boolean isFlying = version.hasElytra && playerIsElytraFlying();
		boolean isSwimming = version.hasSwimmingAnimation ? playerInSwimmingAnimation() : playerIsUnderwater();
		
		if(elytraEnabled && isFlying) {
			if(state.elytraFlyingTicks == elytraDelay) enterThirdPerson(new FlyingReason());
			state.elytraFlyingTicks++;
		} else {
			if(state.elytraFlyingTicks != 0) exitThirdPerson(new FlyingReason());
			state.elytraFlyingTicks = 0;
		}
		
		if(swimEnabled && !(elytraEnabled && isFlying && isSwimming)) { //so swimming rules don't trigger when you dip underwater while flying with elytra
			boolean stickySwim = config.getOr(opts.STICKY_SWIM, false);
			int swimmingDelayStart = config.get(opts.SWIMMING_DELAY_START);
			int swimmingDelayEnd = config.get(opts.SWIMMING_DELAY_END);
			
			if(state.wasSwimming && stickySwim) isSwimming |= playerIsUnderwater();
			
			if(state.wasSwimming != isSwimming) {
				state.swimTicks = 0;
				state.wasSwimming = isSwimming;
			}
			
			if(isSwimming && state.swimTicks == swimmingDelayStart) enterThirdPerson(new SwimmingReason());
			if(!isSwimming && state.swimTicks == swimmingDelayEnd) exitThirdPerson(new SwimmingReason());
			
			state.swimTicks++;
		}
	}
	
	//internal api
	
	private void mountOrDismount(Vehicle vehicle, boolean mounting) {
		if(!safeToTick()) return;
		
		debugSpam((mounting ? "Mounting " : "Dismounting ") + vehicle.id());
		
		if(config.get(opts.USEIGNORE) && config.get(opts.IGNORE_PATTERN).matcher(vehicle.id()).matches()) {
			debugSpam("Ignoring, since it matches the ignore pattern '{}'.", config.get(opts.IGNORE_PATTERN));
			return;
		}
		
		boolean doIt = false;
		if(config.get(opts.BOAT) && vehicle.classification() == Vehicle.Classification.BOAT) {
			debugSpam("This is a boat!");
			doIt = true;
		}
		
		if(config.get(opts.CART) && vehicle.classification() == Vehicle.Classification.MINECART) {
			debugSpam("This is a minecart!");
			doIt = true;
		}
		
		if(config.get(opts.ANIMAL) && vehicle.classification() == Vehicle.Classification.ANIMAL) {
			debugSpam("This is an animal!");
			doIt = true;
		}
		
		if(config.get(opts.CUSTOM) && config.get(opts.CUSTOM_PATTERN).matcher(vehicle.id()).matches()) {
			debugSpam("This matches the pattern '{}'!", config.get(opts.CUSTOM_PATTERN));
			doIt = true;
		}
		
		if(doIt) {
			if(mounting) enterThirdPerson(new MountingReason(vehicle));
			else exitThirdPerson(new MountingReason(vehicle));
		}
	}
	
	private void enterThirdPerson(Reason reason) {
		if(state.reason == null && getCameraType() == FIRST_PERSON) {
			state.oldPerspective = getCameraType();
			state.reason = reason;
			setCameraType(THIRD_PERSON);
			debugSpam("Automatically entering third person due to {}", reason);
		} else if(state.isActive()) {
			state.reason = reason;
			debugSpam("Continuing third person into {}", reason);
		}
	}
	
	private void exitThirdPerson(Reason reason) {
		if(!config.get(opts.AUTO_RESTORE)) {
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
		setCameraType(state.oldPerspective);
		state.cancel();
	}
	
	public static class State {
		public boolean modEnabled = true;
		public boolean modEnableToggleKeyWasPressed = false;
		
		public int oldPerspective = FIRST_PERSON;
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
		
		public void reset() {
			oldPerspective = FIRST_PERSON;
			reason = null;
			elytraFlyingTicks = swimTicks = 0;
			wasSwimming = false;
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
}
