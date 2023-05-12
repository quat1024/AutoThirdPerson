package agency.highlysuspect.autothirdperson;

/**
 * Facade that abstracts away the differences between Minecraft versions.
 */
@Deprecated //merge into AutoThirdPerson
public interface MinecraftInteraction {
	MyLogger makeLogger();
	
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