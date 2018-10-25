package quaternary.thirdpersonboat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
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
import java.util.regex.PatternSyntaxException;

@Mod(modid = AutoThirdPerson.MODID, name = AutoThirdPerson.NAME, version = AutoThirdPerson.VERSION, clientSideOnly = true)
@Mod.EventBusSubscriber
public class AutoThirdPerson {
	public static final String MODID = "auto_third_person";
	public static final String NAME = "Auto Third Person";
	public static final String VERSION = "GRADLE:VERSION";
	
	static int oldCameraMode = 0;
	static boolean wasElytraFlying = false;
	static Pattern[] whitelistPatterns = null;
	static Pattern[] blacklistPatterns = null;
	static boolean didLog = false;
	
	@SubscribeEvent
	public static void mountEvent(EntityMountEvent e) {
		Minecraft mc = Minecraft.getMinecraft();
		
		if(e.getEntity() == mc.player) {
			Entity mounting = e.getEntityBeingMounted();
			boolean doIt = false;
			
			if(whitelistPatterns == null || blacklistPatterns == null) parseConfigPatterns();
			
			//General categories
			if(ModConfig.entities.MINECART && mounting instanceof EntityMinecart) {
				doIt = true;
			} else if(ModConfig.entities.BOAT && mounting instanceof EntityBoat) {
				doIt = true;
			} else if(ModConfig.entities.ANIMAL && mounting instanceof EntityAnimal) {
				doIt = true;
			}
			
			//Additional whitelist
			if(whitelistPatterns.length > 0) {
				ResourceLocation entityLocation = EntityList.getKey(mounting);
				if(entityLocation != null) {
					String entityType = entityLocation.toString();
					for(Pattern p : whitelistPatterns) {
						if(p.matcher(entityType).matches()) {
							doIt = true;
							break;
						}
					}
				}
			}
			
			//Additional blacklist
			if(blacklistPatterns.length > 0) {
				ResourceLocation entityLocation = EntityList.getKey(mounting);
				if(entityLocation != null) {
					String entityType = entityLocation.toString();
					for(Pattern p : blacklistPatterns) {
						if(p.matcher(entityType).matches()) {
							doIt = false;
							break;
						}
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
	
	private static final String error = "There was a problem parsing a regex in the Auto Third Person %s\nSpecifically, the error was on the %s expression, '%s'.\nHere are more details of the error.";
	
	private static void parseConfigPatterns() {
		whitelistPatterns = new Pattern[ModConfig.entities.whitelist.length];
		for(int i = 0; i < ModConfig.entities.whitelist.length; i++) {
			try {
				whitelistPatterns[i] = Pattern.compile(ModConfig.entities.whitelist[i]);
			} catch(PatternSyntaxException e) {
				throw new RuntimeException(String.format(error, "extra entities whitelist", numberToEnglishOrdinal(i + 1), ModConfig.entities.whitelist[i]));
			}
		}
		
		blacklistPatterns = new Pattern[ModConfig.entities.blacklist.length];
		for(int i = 0; i < ModConfig.entities.blacklist.length; i++) {
			try {
				blacklistPatterns[i] = Pattern.compile(ModConfig.entities.blacklist[i]);
			} catch(PatternSyntaxException e) {
				throw new RuntimeException(String.format(error, "entity blacklist", numberToEnglishOrdinal(i + 1), ModConfig.entities.blacklist[i]));
			}
		}
	}
	
	private static String numberToEnglishOrdinal(int i) {
		//numbers ending in 1: "st"
		//numbers ending in 2: "nd"
		//numbers ending in 3: "rd"
		//everything else: "th"
		//exception: numbers ending in 11, 12, 13 are all "th"
		String suffix;
		if(i <= 0 || (i % 100) / 10 == 1 || i % 10 >= 4) {
			suffix = "th";
		} else if(i % 10 == 1) {
			suffix = "st";
		} else if(i % 10 == 2) {
			suffix = "nd";
		} else if(i % 10 == 3){
			suffix = "rd";
		} else {
			suffix = "th"; //Impossible to reach xd
		}
		
		return i + suffix;
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
							"Technical note: this works on all EntityAnimals, vanilla or not."
			})
			public boolean ANIMAL = true;
			
			@Config.Name("ElytraFlying")
			@Config.Comment({
							"Should Minecraft go into third person when you fly an elytra?",
							"Technical note: this works on anything that sets \"player.isElytraFlying\", vanilla or not."
			})
			public boolean ELYTRA = true;
			
			@Config.Name("_Others")
			@Config.Comment({
							"Additional entity IDs that will trigger special third person behavior.",
							"This option supplements the broader category configuration options, e.g.",
							"if \"animals\" is false, but you put an animal in here, it will still trigger third person behavior.",
							"",
							"Please write entries in the \"modid:name\" format, similar to what you would enter into /summon.",
							"If you are not sure how to find an entity ID, please contact its developers!",
							"Feel free to use regular expressions to match many entity IDs on one line!"
			})
			public String[] whitelist = new String[]{
							"botania:player_mover",
							"jurassicraft:.*"
			};
			
			//Underscore just makes it sort to the bottom...
			@Config.Name("_Blacklist")
			@Config.Comment({
							"Entity IDs that will *never* trigger special third person behavior.",
							"This option overrides the broader category configuration options,",
							"and it additionally overrides \"Others\". Blacklisting an entity listed there ultimately blacklists it.",
							"",
							"Please write entries in the \"modid:name\" format, similar to what you would enter into /summon.",
							"If you are not sure how to find an entity ID, please contact its developers!",
							"Feel free to use regular expressions to match many entity IDs on one line!"
			})
			public String[] blacklist = new String[]{
							
			};
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
