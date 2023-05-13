package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.VersionCapabilities;
import agency.highlysuspect.autothirdperson.wrap.MyCameraType;
import agency.highlysuspect.autothirdperson.wrap.MyLogger;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public abstract class OneTwelveTwoAutoThirdPerson extends AutoThirdPerson {
	protected final Minecraft client = Minecraft.getMinecraft();
	
	@Override
	public MyLogger makeLogger() {
		return new MyLogger() {
			private final Logger log4jlogger = LogManager.getLogger(NAME);
			
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
	public VersionCapabilities.Builder caps(VersionCapabilities.Builder builder) {
		return builder;
	}
	
	@Override
	public MyCameraType getCameraType() {
		return MyCameraType.values()[MathHelper.clamp(client.gameSettings.thirdPersonView, 0, 2)];
	}
	
	@Override
	public void setCameraType(MyCameraType type) {
		client.gameSettings.thirdPersonView = type.ordinal();
	}
	
	@Override
	public boolean debugScreenUp() {
		return client.gameSettings.showDebugInfo;
	}
	
	@Override
	public boolean safeToTick() {
		return client.player != null && client.world != null && !client.isGamePaused();
	}
	
	@Override
	public boolean playerIsUnderwater() {
		return safeToTick() && client.player.isInsideOfMaterial(Material.WATER);
	}
	
	protected static class EntityVehicle implements Vehicle {
		public EntityVehicle(Entity ent) {
			this.ent = new WeakReference<>(ent);
			this.id = ent == null ? "<nothing>" : EntityList.getEntityString(ent);
			
			if(ent instanceof EntityMinecart) this.type = Classification.MINECART;
			else if(ent instanceof EntityBoat) this.type = Classification.BOAT;
			else if(ent instanceof EntityAnimal) this.type = Classification.ANIMAL;
			else this.type = Classification.OTHER;
		}
		
		private final WeakReference<Entity> ent;
		private final String id;
		private final Classification type;
		
		@Override
		public boolean stillExists() {
			Entity entity = ent.get();
			return entity != null && entity.isEntityAlive();
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
			return myEntity != null && myEntity == otherEntity;
		}
	}
	
	//unsupported things
	
	@Override
	public boolean playerIsElytraFlying() {
		return false;
	}
	
	@Override
	public boolean playerInSwimmingAnimation() {
		return false;
	}
}
