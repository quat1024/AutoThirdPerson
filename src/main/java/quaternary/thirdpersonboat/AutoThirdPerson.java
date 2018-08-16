package quaternary.thirdpersonboat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

@Mod(modid = AutoThirdPerson.MODID, name = AutoThirdPerson.NAME, version = AutoThirdPerson.VERSION, clientSideOnly = true)
@Mod.EventBusSubscriber
public class AutoThirdPerson {
	public static final String MODID = "auto_third_person";
	public static final String NAME = "Auto Third Person";
	public static final String VERSION = "GRADLE:VERSION";
	
	static int oldCameraMode = 0;
	static boolean wasElytraFlying = false;
	static Pattern[] whitelistPatterns = null;
	static boolean didLog = false;
	
	@SubscribeEvent
	public static void mountEvent(EntityMountEvent e) {
		Minecraft mc = Minecraft.getMinecraft();
		
		if(e.getEntity() == mc.player) {
			Entity mounting = e.getEntityBeingMounted();
			boolean doIt = false;
			
			if(whitelistPatterns == null) parseConfigPatterns();
			
			if(ModConfig.entities.MINECART && mounting instanceof EntityMinecart) {
				doIt = true;
			} else if(ModConfig.entities.BOAT && mounting instanceof EntityBoat) {
				doIt = true;
			} else if(ModConfig.entities.ANIMAL && mounting instanceof EntityLiving) {
				doIt = true;
			} else if(ModConfig.entities.OTHER && whitelistPatterns.length == 0) {
				doIt = true;
			}
			
			if(ModConfig.entities.OTHER && whitelistPatterns.length > 0) {
				ResourceLocation entityLocation = EntityList.getKey(mounting);
				if(entityLocation == null) return;
				String entityType = entityLocation.toString();
				for(Pattern p : whitelistPatterns) {
					if(p.matcher(entityType).matches()) {
						doIt = true;
						break;
					}
				}
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
		
		if(ModConfig.LOG_FIRST_TIME && !didLog) {
			Logger log = LogManager.getLogger(NAME);
			log.info("You were automatically put into third person mode!");
			log.info("If you don't like this behavior, please see the in-game configuration screen.");
			log.info("This message will only display once. *dissolves*");
			didLog = true;
		}
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
	
	private static void parseConfigPatterns() {
		whitelistPatterns = new Pattern[ModConfig.entities.otherSettings.whitelist.length];
		for(int i = 0; i < ModConfig.entities.otherSettings.whitelist.length; i++) {
			whitelistPatterns[i] = Pattern.compile(ModConfig.entities.otherSettings.whitelist[i]);
		}
	}
	
	@Config(modid = MODID)
	@Mod.EventBusSubscriber
	public static class ModConfig {
		@Config.Comment("Which entities cause automatic third person behavior?")
		public static Entities entities = new Entities();
		@Config.Comment("Special bonus settings!")
		public static Extras extras = new Extras();
		
		public static class Entities {
			@Config.Name("Minecarts")
			@Config.Comment({
							"Should Minecraft go into third person when you enter a minecart?",
							"Technical note: this works on all EntityMinecarts, vanilla or not."
			})
			public boolean MINECART = true;
			
			@Config.Name("Boats")
			@Config.Comment({
							"Should Minecraft go into third person when you enter a boat?",
							"Technical note: this works on all EntityBoats, vanilla or not."
			})
			public boolean BOAT = true;
			
			@Config.Name("Animals")
			@Config.Comment({
							"Should Minecraft go into third person when you ride an animal?",
							"Technical note: this works on all EntityLivings, vanilla or not."
			})
			public boolean ANIMAL = true;
			
			@Config.Name("ElytraFlying")
			@Config.Comment({
							"Should Minecraft go into third person when you fly an elytra?",
							"Technical note: this works on anything that sets \"player.isElytraFlying\", vanilla or not."
			})
			public boolean ELYTRA = true;
			
			@Config.Name("Other")
			@Config.Comment("Should Minecraft go into third person when you start riding something else?")
			public boolean OTHER = true;
			
			@Config.Comment("Settings for the \"Other\" config option. Nothing in here will have any effect if \"Other\" is false.")
			public OtherSettings otherSettings = new OtherSettings();
			
			public static class OtherSettings {
				@Config.Name("OtherEntitiesWhitelist")
				@Config.Comment({
								"If this is blank, every entity that's not a boat, minecart, or animal will cause third-person behavior.",
								"If it's not blank, only entities that appear in this list will cause third-person behavior.",
								"This setting will override the general Animals, Boat, Minecart settings, but only if it's not blank.",
								"",
								"Please write entries in the \"modid:name\" format, similar to what you would enter into /summon.",
								"If you are not sure how to find an entity ID, please contact its developers!",
								"Feel free to use regular expressions to match many entity IDs on one line!"
				})
				public String[] whitelist = new String[]{
								"botania:player_mover",
								"jurassicraft:.*"
				};
			}
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
		@Config.Comment({
						"If you manually toggle the camera view while riding, what should happen when you leave your vehicle?",
						"If true: Nothing, your camera view will be left as-is.",
						"If false: You will be put back in the camera view you chose before entering the vehicle."
		})
		public static boolean CANCEL_AUTO_RESTORE = true;
		
		@Config.Name("LogFirstTime")
		@Config.Comment({
						"Should Auto Third Person log a prominent message the first time it triggers, per game session?",
						"This is intended to unobtrusively reduce confusion for mod-developers receiving",
						"bug reports of the form \"Hmm? I randomly went in to third person?\""
		})
		public static boolean LOG_FIRST_TIME = true;
		
		@SubscribeEvent
		public static void configChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
			if(e.getModID().equals(MODID)) {
				ConfigManager.sync(MODID, Config.Type.INSTANCE);
				whitelistPatterns = null;
			}
		}
	}
}
