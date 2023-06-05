package agency.highlysuspect.autothirdperson;

import agency.highlysuspect.autothirdperson.wrap.MyLogger;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public abstract class SeventeenOneAutoThirdPerson extends AutoThirdPerson {
	protected final Minecraft client = Minecraft.getInstance();
	
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
	public VersionCapabilities.Builder caps(VersionCapabilities.Builder caps) {
		return caps.hasElytra().hasSwimmingAnimation();
	}
	
	@Override
	public int getCameraType() {
		return client.options.getCameraType().ordinal();
	}
	
	@Override
	public void setCameraType(int type) {
		client.options.setCameraType(CameraType.values()[type]);
	}
	
	@Override
	public int numberOfCameraTypes() {
		return CameraType.values().length;
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
	
	@Override
	public void sayEnabled(boolean enabled) {
		assert client.player != null;
		
		client.player.displayClientMessage(new TranslatableComponent("autothirdperson.say_toggle", enabled ? 
			new TranslatableComponent("autothirdperson.enabled").withStyle(ChatFormatting.GREEN) :
			new TranslatableComponent("autothirdperson.disabled").withStyle(ChatFormatting.RED)
		), true);
	}
	
	public static class EntityVehicle implements Vehicle {
		public EntityVehicle(Entity ent) {
			this.ent = new WeakReference<>(ent);
			this.id = ent == null ? "<nothing>" : Registry.ENTITY_TYPE.getKey(ent.getType()).toString();
			
			if(ent instanceof Minecart) this.type = Classification.MINECART;
			else if(ent instanceof Boat) this.type = Classification.BOAT;
			else if(ent instanceof Animal) this.type = Classification.ANIMAL;
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
