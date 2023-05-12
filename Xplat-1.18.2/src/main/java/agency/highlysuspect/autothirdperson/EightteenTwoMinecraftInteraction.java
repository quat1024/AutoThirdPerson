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
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class EightteenTwoMinecraftInteraction implements MinecraftInteraction {
	private final Minecraft client = Minecraft.getInstance();
	private final MyLogger logger = new Log4jMyLogger(LogManager.getLogger(AutoThirdPerson.NAME));
	
	@Override
	public MyLogger getLogger() {
		return logger;
	}
	
	@Override
	public MyCameraType getCameraType() {
		return wrapCameraType(client.options.getCameraType());
	}
	
	@Override
	public void setCameraType(MyCameraType type) {
		client.options.setCameraType(unwrapCameraType(type));
	}
	
	@Override
	public boolean debugScreenUp() {
		return client.options.renderDebug;
	}
	
	@Override
	public boolean safeToTick() {
		return client.player != null && client.level != null && !client.isPaused();
	}
	
	@Override
	public boolean playerIsElytraFlying() {
		assert client.player != null;
		return client.player.isFallFlying();
	}
	
	@Override
	public boolean playerInSwimmingAnimation() {
		assert client.player != null;
		return client.player.isSwimming();
	}
	
	@Override
	public boolean playerIsUnderwater() {
		assert client.player != null;
		return client.player.isUnderWater();
	}
	
	///
	
	public MyCameraType wrapCameraType(CameraType type) {
		return switch(type) {
			case FIRST_PERSON -> MyCameraType.FIRST_PERSON;
			case THIRD_PERSON_BACK -> MyCameraType.THIRD_PERSON;
			case THIRD_PERSON_FRONT -> MyCameraType.THIRD_PERSON_REVERSED;
		};
	}
	
	public CameraType unwrapCameraType(MyCameraType type) {
		return switch(type) {
			case FIRST_PERSON -> CameraType.FIRST_PERSON;
			case THIRD_PERSON -> CameraType.THIRD_PERSON_BACK;
			case THIRD_PERSON_REVERSED -> CameraType.THIRD_PERSON_FRONT;
		};
	}
	
	public Vehicle wrapVehicle(Entity ent) {
		return new EntityVehicle(ent);
	}
	
	private static class EntityVehicle implements Vehicle {
		public EntityVehicle(Entity ent) {
			this.ent = new WeakReference<>(ent);
			this.id = ent == null ? "<nothing>" : Registry.ENTITY_TYPE.getKey(ent.getType()).toString();
			
			if(ent instanceof Minecart) this.type = VehicleClassification.MINECART;
			else if(ent instanceof Boat) this.type = VehicleClassification.BOAT;
			else if(ent instanceof Animal) this.type = VehicleClassification.ANIMAL;
			else this.type = VehicleClassification.OTHER;
		}
		
		private final WeakReference<Entity> ent;
		private final String id;
		private final VehicleClassification type;
		
		@Override
		public boolean stillExists() {
			Entity entity = ent.get();
			return entity != null && entity.isAlive();
		}
		
		@Override
		public @NotNull String id() {
			return id;
		}
		
		@Override
		public @NotNull VehicleClassification classification() {
			return type;
		}
		
		@Override
		public boolean vehicleEquals(Vehicle other) {
			if(!(other instanceof EntityVehicle)) return false;
			Entity myEntity = ent.get();
			Entity otherEntity = ((EntityVehicle) other).ent.get();
			return myEntity != null && myEntity.isAlive() && myEntity == otherEntity;
		}
	}
	
	@SuppressWarnings("ClassCanBeRecord")
	private static class Log4jMyLogger implements MyLogger {
		public Log4jMyLogger(Logger logger) {
			this.logger = logger;
		}
		
		private final Logger logger;
		
		@Override
		public void info(String msg, Object... args) {
			logger.info(msg, args);
		}
		
		@Override
		public void warn(String msg, Object... args) {
			logger.warn(msg, args);
		}
		
		@Override
		public void error(String msg, Throwable err) {
			logger.error(msg, err);
		}
	}
}
