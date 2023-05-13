package agency.highlysuspect.autothirdperson;

import agency.highlysuspect.autothirdperson.wrap.MyLogger;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public abstract class SixteenFiveAutoThirdPerson_MCP extends AutoThirdPerson {
	protected final Minecraft client = Minecraft.getInstance();
	
	@Override
	public MyLogger makeLogger() {
		return new MyLogger() {
			private final Logger log4jlogger = LogManager.getLogger(AutoThirdPerson.NAME);
			
			@Override
			public void info(String msg, Object... args) {
				log4jlogger.info(msg, args);
			}
			
			@Override
			public void warn(String msg, Object... args) {
				log4jlogger.warn(msg, args);
			}
			
			@Override
			public void error(String msg, Throwable err) {
				log4jlogger.error(msg, err);
			}
		};
	}
	
	@Override
	public VersionCapabilities.Builder caps(VersionCapabilities.Builder caps) {
		return caps.hasElytra().hasSwimmingAnimation();
	}
	
	@Override
	public int getCameraType() {
		return client.options.getCameraType().ordinal();
	}
	
	@Override
	public void setCameraType(int type) {
		client.options.setCameraType(PointOfView.values()[type]);
	}
	
	@Override
	public int numberOfCameraTypes() {
		return PointOfView.values().length;
	}
	
	@Override
	public boolean f3ScreenUp() {
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
	
	public static class EntityVehicle implements Vehicle {
		@SuppressWarnings("deprecation") //Vanilla registries are fine, Forge, shut up
		public EntityVehicle(Entity ent) {
			this.ent = new WeakReference<>(ent);
			this.id = ent == null ? "<nothing>" : Registry.ENTITY_TYPE.getKey(ent.getType()).toString();
			
			if(ent instanceof MinecartEntity) this.type = Classification.MINECART;
			else if(ent instanceof BoatEntity) this.type = Classification.BOAT;
			else if(ent instanceof AnimalEntity) this.type = Classification.ANIMAL;
			else this.type = Classification.OTHER;
		}
		
		private final WeakReference<Entity> ent;
		private final String id;
		private final Classification type;
		
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
		public @NotNull Classification classification() {
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
}
