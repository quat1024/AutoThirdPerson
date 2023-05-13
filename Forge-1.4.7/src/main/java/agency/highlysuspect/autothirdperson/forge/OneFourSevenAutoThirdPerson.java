package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.VersionCapabilities;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public abstract class OneFourSevenAutoThirdPerson extends AutoThirdPerson {
	protected final Minecraft client = Minecraft.getMinecraft();
	
	@Override
	public VersionCapabilities.Builder caps(VersionCapabilities.Builder builder) {
		return builder.hasHandGlitch().noSneakDismount();
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
		return client.thePlayer != null && client.theWorld != null && !client.isGamePaused;
	}
	
	@Override
	public boolean playerIsUnderwater() {
		return safeToTick() && client.thePlayer.isInsideOfMaterial(Material.water);
	}
	
	protected static class EntityVehicle implements Vehicle {
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
	
	//Not supported
	
	@Override
	public boolean playerIsElytraFlying() {
		return false;
	}
	
	@Override
	public boolean playerInSwimmingAnimation() {
		return false;
	}
}
