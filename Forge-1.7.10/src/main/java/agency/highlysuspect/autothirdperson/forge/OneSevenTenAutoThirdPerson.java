package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.VersionCapabilities;
import agency.highlysuspect.autothirdperson.wrap.MyLogger;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

//No touching Forge, imagine writing code in an Xplat-1.7.10 arm.
public abstract class OneSevenTenAutoThirdPerson extends AutoThirdPerson {
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
		return builder.hasHandGlitch();
	}
	
	@Override
	public int getCameraType() {
		return client.gameSettings.thirdPersonView;
	}
	
	@Override
	public void setCameraType(int type) {
		client.gameSettings.thirdPersonView = type;
	}
	
	@Override
	public boolean f3ScreenUp() {
		return client.gameSettings.showDebugInfo;
	}
	
	@Override
	public boolean safeToTick() {
		return client.thePlayer != null && client.theWorld != null && !client.isGamePaused();
	}
	
	@Override
	public boolean playerIsUnderwater() {
		return safeToTick() && client.thePlayer.isInsideOfMaterial(Material.water);
	}
	
	@Override
	public void sayEnabled(boolean enabled) {
		assert client.thePlayer != null;
		
		client.thePlayer.addChatComponentMessage(new ChatComponentTranslation("autothirdperson.say_toggle", enabled ?
			new ChatComponentTranslation("autothirdperson.enabled").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)) :
			new ChatComponentTranslation("autothirdperson.disabled").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
		));
	}
	
	public static class EntityVehicle implements Vehicle {
		public EntityVehicle(Entity ent) {
			this.ent = new WeakReference<Entity>(ent);
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
