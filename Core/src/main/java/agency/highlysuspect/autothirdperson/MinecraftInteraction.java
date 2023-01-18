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
	
	/** Wrap the current Minecraft camera type */
	MyCameraType getCameraType();
	
	/** Unwrap and set the current Minecraft camera type */
	void setCameraType(MyCameraType type);
	
	/** Whether the player has the f3 menu up */
	boolean debugScreenUp();
	
	/** @return Player exists, level exists, game is not paused, etc */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean safeToTick();
	
	/** or `false` if this game doesn't have an elytra */
	boolean playerIsElytraFlying();
	
	/** or `false` if this game doesn't have the swimming animation */
	boolean playerInSwimmingAnimation();
	
	/** Whether the player's head/camera/whatever is underwater, used for `stickySwim` and for the swim setting on pre-1.13 */
	boolean playerIsUnderwater();
}