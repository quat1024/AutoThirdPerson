package agency.highlysuspect.autothirdperson;

import agency.highlysuspect.autothirdperson.config.ConfigProperties;
import agency.highlysuspect.autothirdperson.config.ConfigProperty;
import agency.highlysuspect.autothirdperson.config.ConfigSchema;

import java.util.regex.Pattern;

public class AtpOpts {
	public AtpOpts(VersionCapabilities version) {
		CONFIG_VERSION = ConfigProperties.integer("configVersion", null, 7);
		
		BOAT = ConfigProperties.bool("boat", "Automatically go into third person when riding a boat" + (version.hasRafts ? " or raft" : "") + "?", true);
		CART = ConfigProperties.bool("cart", "Automatically go into third person when riding a minecart?", true);
		ANIMAL = ConfigProperties.bool("animal", "Automatically go into third person when riding an animal?", true);
		SWIM = ConfigProperties.bool("swim", version.hasSwimmingAnimation ? "Automatically go into third person when doing the swimming animation?" : "Automatically go into third person when underwater?", false);
		CUSTOM = ConfigProperties.bool("custom", "If 'true', the customPattern will be used, and riding anything matching it will toggle third person.", false);
		USEIGNORE = ConfigProperties.bool("useIgnore", "If 'true', the ignorePattern will be used, and anything matching it will be ignored.", false);
		
		SWIMMING_DELAY_START = ConfigProperties.nonNegativeInteger("swimmingDelayStart", "Ticks of swimming required before the camera automatically toggles if the 'swim' option is enabled.", version.hasSwimmingAnimation ? 0 : 10); //default value of 0 is too odd-looking without the swimming animation
		SWIMMING_DELAY_END = ConfigProperties.nonNegativeInteger("swimmingDelayEnd", "Ticks of not swimming required before the camera restores if the 'swim' option is enabled.", 10);
		CUSTOM_PATTERN = ConfigProperties.pattern("customPattern", "Entity IDs that match this regular expression will be considered if the 'custom' option is enabled.", Pattern.compile("^minecraft:(cow|chicken)$"));
		IGNORE_PATTERN = ConfigProperties.pattern("ignorePattern", "Entity IDs that match this regular expression will be ignored if the 'useIgnore' option is enabled.", Pattern.compile("^examplemod:example$"));
		
		AUTO_RESTORE = ConfigProperties.bool("autoRestore", "When the situation that Auto Third Person put you into third person for is over,\nthe camera will be restored back to the way it was.", true);
		CANCEL_AUTO_RESTORE = ConfigProperties.bool("cancelAutoRestore", "If 'true', pressing f5 after mounting something will prevent your camera\nfrom being automatically restored to first-person when you dismount.", true);
		
		SKIP_FRONT_VIEW = ConfigProperties.bool("skipFrontView", "Skip the 'third-person front' camera mode when pressing F5.", false);
		LOG_SPAM = ConfigProperties.bool("logSpam", "Dump a bunch of debug crap into the log.\nMight be handy!", false);
		
		if(version.hasElytra) {
			ELYTRA = ConfigProperties.bool("elytra", "Automatically go into third person when flying an elytra?", true);
			ELYTRA_DELAY = ConfigProperties.nonNegativeInteger("elytraDelay", "Ticks of elytra flight required before the camera automatically toggles if the 'elytra' option is enabled.", 7);
		} else {
			ELYTRA = null;
			ELYTRA_DELAY = null;
		}
		
		if(version.hasSwimmingAnimation) {
			STICKY_SWIM = ConfigProperties.bool("stickySwim", "If 'true', your head has to completely exit the water to count as 'not swimming anymore', for the purposes of restoring\nthe camera when you're done swimming. If 'false', you just have to stop doing the swimming animation.", true);
		} else {
			STICKY_SWIM = null;
		}
		
		if(version.hasHandGlitch) {
			FIX_HAND_GLITCH = ConfigProperties.bool("fixHandGlitch", "Fix the annoying 'weirdly rotated first-person hand' rendering error when you ride or look at someone riding a vehicle.", true);
		} else {
			FIX_HAND_GLITCH = null;
		}
		
		if(version.noSneakDismount) {
			SNEAK_DISMOUNT_BACKPORT = ConfigProperties.bool("sneakDismount", "Pressing sneak will remove you from the vehicle, instead of requiring a click on the vehicle, like in modern versions.", false);
		} else {
			SNEAK_DISMOUNT_BACKPORT = null;
		}
	}
	
	public final ConfigProperty<Integer> CONFIG_VERSION;
	
	public final ConfigProperty<Boolean> BOAT;
	public final ConfigProperty<Boolean> CART;
	public final ConfigProperty<Boolean> ANIMAL;
	public final ConfigProperty<Boolean> ELYTRA;
	public final ConfigProperty<Boolean> SWIM;
	public final ConfigProperty<Boolean> CUSTOM;
	public final ConfigProperty<Boolean> USEIGNORE;
	
	public final ConfigProperty<Integer> ELYTRA_DELAY;
	public final ConfigProperty<Integer> SWIMMING_DELAY_START;
	public final ConfigProperty<Integer> SWIMMING_DELAY_END;
	public final ConfigProperty<Boolean> STICKY_SWIM;
	public final ConfigProperty<Pattern> CUSTOM_PATTERN;
	public final ConfigProperty<Pattern> IGNORE_PATTERN;
	
	public final ConfigProperty<Boolean> AUTO_RESTORE;
	public final ConfigProperty<Boolean> CANCEL_AUTO_RESTORE;
	
	public final ConfigProperty<Boolean> SKIP_FRONT_VIEW;
	public final ConfigProperty<Boolean> LOG_SPAM;
	public final ConfigProperty<Boolean> FIX_HAND_GLITCH;
	public final ConfigProperty<Boolean> SNEAK_DISMOUNT_BACKPORT;
	
	public ConfigSchema makeSchema() {
		ConfigSchema s = new ConfigSchema();
		
		s.opt(CONFIG_VERSION);
		s.section("Scenarios", "Things that might get you into third person.", BOAT, CART, ANIMAL, ELYTRA, SWIM, CUSTOM, USEIGNORE);
		s.section("Scenario Options", "Scenario configuration.\nHas no effect if the corresponding scenario is turned off.", ELYTRA_DELAY, SWIMMING_DELAY_START, SWIMMING_DELAY_END, STICKY_SWIM, CUSTOM_PATTERN, IGNORE_PATTERN);
		s.section("Restoration", "Automatically exiting from third person.", AUTO_RESTORE, CANCEL_AUTO_RESTORE);
		s.section("Extras", "Other stuff I threw in the mod.", SKIP_FRONT_VIEW, LOG_SPAM, FIX_HAND_GLITCH, SNEAK_DISMOUNT_BACKPORT);
		
		return s;
	}
}
