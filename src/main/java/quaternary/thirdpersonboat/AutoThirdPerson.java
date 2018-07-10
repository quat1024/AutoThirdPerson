package quaternary.thirdpersonboat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = AutoThirdPerson.MODID, name = AutoThirdPerson.NAME, version = AutoThirdPerson.VERSION, clientSideOnly = true)
@Mod.EventBusSubscriber
public class AutoThirdPerson {
	public static final String MODID = "auto_third_person";
	public static final String NAME = "Auto Third Person";
	public static final String VERSION = "GRADLE:VERSION";
	
	static int oldCameraMode = 0;
	static boolean wasElytraFlying = false;
	
	@SubscribeEvent
	public static void mountEvent(EntityMountEvent e) {
		Minecraft mc = Minecraft.getMinecraft();
		
		if(e.getEntity() == mc.player) {
			Entity mounting = e.getEntityBeingMounted();
			boolean doIt = false;
			
			if(ModConfig.entities.MINECART && mounting instanceof EntityMinecart) {
				doIt = true;
			} else if(ModConfig.entities.BOAT && mounting instanceof EntityBoat) {
				doIt = true;
			} else if(ModConfig.entities.ANIMAL && mounting instanceof EntityLiving) {
				doIt = true;
			}
			
			if(doIt) {
				if(e.isMounting()) enterThirdPerson();
				else leaveThirdPerson();
			}
		}
	}
	
	@SubscribeEvent
	public static void tickEvent(TickEvent.PlayerTickEvent e) {
		if(ModConfig.extras.SKIP_FRONT_VIEW && getCameraMode() == 2) {
			setCameraMode(0);
		}
		
		if(ModConfig.entities.ELYTRA) {
			Minecraft mc = Minecraft.getMinecraft();
			if(e.player == mc.player) {
				if(!wasElytraFlying && mc.player.isElytraFlying()) {
					enterThirdPerson();
				} else if(wasElytraFlying && !mc.player.isElytraFlying()) {
					leaveThirdPerson();
				}
				
				wasElytraFlying = mc.player.isElytraFlying();
			}
		}
	}
	
	@SubscribeEvent
	public static void keyPressEvent(InputEvent.KeyInputEvent e) {
		if(ModConfig.CANCEL_AUTO_RESTORE && Minecraft.getMinecraft().gameSettings.keyBindTogglePerspective.isKeyDown()) {
			oldCameraMode = -1;
		}
	}
	
	private static void enterThirdPerson() {
		oldCameraMode = Minecraft.getMinecraft().gameSettings.thirdPersonView;
		setCameraMode(1);
	}
	
	private static void leaveThirdPerson() {
		if(oldCameraMode == -1) {
			oldCameraMode = 0;
		} else if(ModConfig.AUTO_RESTORE) {
			setCameraMode(oldCameraMode);
		}
	}
	
	private static void setCameraMode(int mode) {
		Minecraft.getMinecraft().gameSettings.thirdPersonView = mode;
	}
	
	private static int getCameraMode() {
		return Minecraft.getMinecraft().gameSettings.thirdPersonView;
	}
	
	@Config(modid = MODID)
	@Mod.EventBusSubscriber
	public static class ModConfig {
		public static Entities entities = new Entities();
		public static Extras extras = new Extras();
		
		private static class Entities {
			@Config.Name("Minecarts")
			@Config.Comment("Should Minecraft go into third person when you enter a minecart?")
			public boolean MINECART = true;
			
			@Config.Name("Boats")
			@Config.Comment("Should Minecraft go into third person when you enter a boat?")
			public boolean BOAT = true;
			
			@Config.Name("Animals")
			@Config.Comment("Should Minecraft go into third person when you ride an animal?")
			public boolean ANIMAL = true;
			
			@Config.Name("ElytraFlying")
			@Config.Comment("Should Minecraft go into third person when you fly an elytra?")
			public boolean ELYTRA = true;
		}
		
		private static class Extras {
			@Config.Name("SkipFrontView")
			@Config.Comment("Should Minecraft never go into \"first-person reversed\" view?")
			public boolean SKIP_FRONT_VIEW = false;
		}
		
		@Config.Name("AutoRestoreView")
		@Config.Comment("Should Minecraft return to your previous camera mode when you leave your vehicle?")
		public static boolean AUTO_RESTORE = true;
		
		@Config.Name("CancelAutoRestore")
		@Config.Comment({"If you manually toggle the camera view while riding, what should happen when you leave your vehicle?", "If true: Nothing, your camera view will be left as-is.", "If false: You will be put back in the camera view you chose before entering the vehicle."})
		public static boolean CANCEL_AUTO_RESTORE = true;
		
		@SubscribeEvent
		public static void configChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
			if(e.getModID().equals(MODID)) {
				ConfigManager.sync(MODID, Config.Type.INSTANCE);
			}
		}
	}
}
