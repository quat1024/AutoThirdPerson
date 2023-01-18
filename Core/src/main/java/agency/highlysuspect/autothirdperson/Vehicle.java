package agency.highlysuspect.autothirdperson;

import org.jetbrains.annotations.NotNull;

/** Something you can mount. */
public interface Vehicle {
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
