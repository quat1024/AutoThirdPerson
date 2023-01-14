package agency.highlysuspect.autothirdperson;

import org.jetbrains.annotations.NotNull;

/**
 * Facade that abstracts away the differences between Minecraft versions.
 */
public interface MinecraftInteraction {
	MyLogger getLogger();
	
	/** Whether this Minecraft version has an elytra in it (post-1.9) */
	boolean hasElytra();
	
	/** Whether this Minecraft version has an special swimming animation (post-1.13) */
	boolean hasSwimmingAnimation();
	
	MyCameraType getCameraType();
	void setCameraType(MyCameraType type);
	
	/** @return Player exists, level exists, game is not paused, etc */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean safeToTick();
	
	/** or `false` if this game doesn't have an elytra */
	boolean playerIsElytraFlying();
	
	/** or `false` if this game doesn't have the swimming animation */
	boolean playerInSwimmingAnimation();
	
	/** Whether the player's head/camera/whatever is underwater, used for `stickySwim` and for the swim setting on pre-1.13 */
	boolean playerIsUnderwater();
	
	/** We have Log4j at home */
	interface MyLogger {
		void info(String msg, Object... args);
		
		void error(String msg, Throwable err);
	}
	
	/** Wrapper around the vanilla camera type enum */
	enum MyCameraType {
		/** First person */
		FIRST_PERSON,
		
		/** usual third person, facing the back of the player's head */
		THIRD_PERSON,
		
		/** flipped third person, facing the front */
		THIRD_PERSON_REVERSED,
	}
	
	/** Something you can mount. */
	interface Vehicle {
		/**
		 * Vehicles are backed by Entities, and entities can despawn, so return `false` if the entity is gone.
		 * Usually you'd do this using a WeakReference, and checking that the entity's not dead.
		 */
		boolean stillExists();
		
		/**
		 * Would return a ResourceLocation if I had access to them. This gets passed to the user's regex options.
		 */
		@NotNull String id();
		
		/**
		 * General vehicle category, or `OTHER` if it doesn't fit in one. Used for the generic category options.
		 */
		@NotNull VehicleClassification classification();
		
		/**
		 * Whether these two `Vehicle`s refer to the same vehicle.
		 */
		boolean vehicleEquals(Vehicle other);
	}
	
	enum VehicleClassification {
		BOAT,
		MINECART,
		ANIMAL,
		OTHER
	}
}