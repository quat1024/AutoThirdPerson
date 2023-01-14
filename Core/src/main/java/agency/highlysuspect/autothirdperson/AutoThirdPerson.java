package agency.highlysuspect.autothirdperson;

import org.jetbrains.annotations.Nullable;

public class AutoThirdPerson<MC extends MinecraftInteraction, LI extends LoaderInteraction> {
	public static final String MODID = "auto_third_person";
	public static final String NAME = "Auto Third Person";
	
	public static AutoThirdPerson<?, ?> instance;
	
	public final MC mc;
	public final LI loader;
	public final MinecraftInteraction.MyLogger logger;
	
	public final State state;
	
	public AutoThirdPerson(MC mc, LI loader) {
		this.mc = mc;
		this.loader = loader;
		this.logger = mc.getLogger();
		
		this.state = new State();
		
		loader.registerClientTicker(this::tickClient);
	}
	
	public @Nullable MinecraftInteraction.MyCameraType modifyCycle(MinecraftInteraction.MyCameraType cycleTo) {
		if(loader.settings().skipFrontView() && cycleTo == MinecraftInteraction.MyCameraType.THIRD_PERSON_REVERSED) {
			debugSpam("Skipping third-person reversed view");
			return MinecraftInteraction.MyCameraType.FIRST_PERSON;
		} else return null;
	}
	
	private void debugSpam(String msg, Object... args) {
		if(loader.settings().logSpam()) logger.info(msg, args);
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
	
	public void mount(MinecraftInteraction.Vehicle mounting) {
		mountOrDismount(mounting, true);
	}
	
	public void dismount(MinecraftInteraction.Vehicle dismounting) {
		mountOrDismount(dismounting, false);
	}
	
	private void mountOrDismount(MinecraftInteraction.Vehicle vehicle, boolean mounting) {
		if(!mc.safeToTick()) return;
		AtpSettings settings = loader.settings();
		
		debugSpam((mounting ? "Mounting " : "Dismounting ") + vehicle.id());
		
		if(settings.useIgnore() && settings.ignorePattern().matcher(vehicle.id()).matches()) {
			debugSpam("Ignoring, since it matches the ignore pattern '{}'.", settings.ignorePattern());
			return;
		}
		
		boolean doIt = false;
		if(settings.boat() && vehicle.classification() == MinecraftInteraction.VehicleClassification.BOAT) {
			debugSpam("This is a boat!");
			doIt = true;
		}
		
		if(settings.cart() && vehicle.classification() == MinecraftInteraction.VehicleClassification.MINECART) {
			debugSpam("This is a minecart!");
			doIt = true;
		}
		
		if(settings.animal() && vehicle.classification() == MinecraftInteraction.VehicleClassification.ANIMAL) {
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
	
	public void manualPress() {
		if(loader.settings().cancelAutoRestore() && state.isActive()) {
			debugSpam("Cancelling auto-restore, if it was about to happen");
			state.cancel();
		}
	}
	
	private void enterThirdPerson(Reason reason) {
		if(state.reason == null && mc.getCameraType() == MinecraftInteraction.MyCameraType.FIRST_PERSON) {
			state.oldPerspective = mc.getCameraType();
			state.reason = reason;
			mc.setCameraType(MinecraftInteraction.MyCameraType.THIRD_PERSON);
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
		public MinecraftInteraction.MyCameraType oldPerspective = MinecraftInteraction.MyCameraType.FIRST_PERSON;
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
		public MountingReason(MinecraftInteraction.Vehicle vehicle) {
			this.vehicle = vehicle;
		}
		
		private final MinecraftInteraction.Vehicle vehicle;
		
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
