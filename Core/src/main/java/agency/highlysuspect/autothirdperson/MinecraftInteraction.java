package agency.highlysuspect.autothirdperson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface MinecraftInteraction {
	//logging
	MyLogger getLogger();
	
	//capabilities of this minecraft version
	boolean hasElytra();
	boolean hasSwimmingAnimation();
	
	//camera type setting
	MyCameraType getCameraType();
	void setCameraType(MyCameraType type);
	
	//interactions with the world and game
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean safeToTick(); //the player exists, game is not paused, etc
	boolean playerIsElytraFlying();
	boolean playerInSwimmingAnimation();
	boolean playerIsUnderwater();
	
	interface MyLogger {
		void info(String msg, Object... args);
	}
	
	enum MyCameraType {
		FIRST_PERSON,
		THIRD_PERSON,
		THIRD_PERSON_REVERSED,
	}
	
	interface Vehicle {
		boolean stillExists();
		
		@NotNull String id();
		@NotNull VehicleClassification classification();
		boolean vehicleEquals(Vehicle other);
	}
	
	enum VehicleClassification {
		BOAT,
		MINECART,
		ANIMAL,
		OTHER
	}
}
