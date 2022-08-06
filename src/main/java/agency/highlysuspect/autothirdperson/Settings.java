package agency.highlysuspect.autothirdperson;

import agency.highlysuspect.autothirdperson.ConfigShape.SkipDefault;
import agency.highlysuspect.autothirdperson.ConfigShape.Section;
import agency.highlysuspect.autothirdperson.ConfigShape.Comment;
import agency.highlysuspect.autothirdperson.ConfigShape.AtLeast;

import java.util.regex.Pattern;

public class Settings {
	private static final int CURRENT_CONFIG_VERSION = 5;
	@SkipDefault
	private int configVersion = CURRENT_CONFIG_VERSION;
	
	/////////////////////
	@Section("Scenarios")
	/////////////////////
	
	@Comment("Automatically go into third person when riding a boat?")
	public boolean boat = true;
	
	@Comment("Automatically go into third person when riding a minecart?")
	public boolean cart = true;
	
	@Comment("Automatically go into third person when riding an animal?")
	public boolean animal = true;
	
	@Comment("Automatically go into third person when flying an elytra?")
	public boolean elytra = true;
	
	@Comment("Automatically go into third person when swimming?")
	public boolean swim = false;
	
	@Comment({
		"If 'true' the customPattern will be used and riding anything",
		"matching it will toggle third person."
	})
	public boolean custom = false;
	
	@Comment("If 'true' the ignorePattern will be used and anything matching it will be ignored.")
	public boolean useIgnore = false;
	
	////////////////////////////
	@Section("Scenario Options")
	////////////////////////////
	
	@Comment({
		"Ticks of elytra flight required before the camera automatically toggles,",
		"if the 'elytra' option is enabled."
	})
	@AtLeast(0)
	public int elytraDelay = 7;
	
	@Comment({
		"Ticks of swimming required before the camera automatically toggles,",
		"if the 'swim' option is enabled."
	})
	@AtLeast(0)
	public int swimmingDelayStart = 0;
	
	@Comment({
		"Ticks of not swimming required before the camera restores,",
		"if the 'swim' option is enabled."
	})
	@AtLeast(0)
	public int swimmingDelayEnd = 10;
	
	@Comment({
		"Entity IDs that match this regular expression will be considered,",
		"if the 'custom' option is enabled."
	})
	public Pattern customPattern = Pattern.compile("^minecraft:(cow|chicken)$");
	
	@Comment({
		"Entity IDs that match this regular expression will be ignored,",
		"if the 'useIgnore' option is enabled."
	})
	public Pattern ignorePattern = Pattern.compile("^examplemod:example$");
	
	///////////////////////
	@Section("Restoration")
	///////////////////////
	
	@Comment("Go back into first-person when dismounting?")
	public boolean autoRestore = true;
	@Comment({
		"If 'true', pressing f5 after mounting something will prevent your camera",
		"from being automatically restored to first-person when you dismount."
	})
	public boolean cancelAutoRestore = true;
	
	/////////////////
	@Section("Extra")
	/////////////////
	
	@Comment("Skip the 'third-person front' camera mode when pressing F5.")
	public boolean skipFrontView = false;
	
	@Comment({
		"Dump a bunch of debug crap into the log.",
		"Might be handy!"
	})
	public boolean logSpam = false;
}
