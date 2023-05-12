package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.AutoThirdPerson;

import java.util.regex.Pattern;

public class CookedForgeSettings implements AtpSettings {
	public CookedForgeSettings(UncookedForgeSettings raw) {
		configVersion = raw.ints.get("configVersion").get();
		boat = raw.bools.get("boat").get();
		cart = raw.bools.get("cart").get();
		animal = raw.bools.get("animal").get();
		elytra = raw.bools.get("elytra").get();
		swim = raw.bools.get("swim").get();
		custom = raw.bools.get("custom").get();
		useIgnore = raw.bools.get("useIgnore").get();
		elytraDelay = raw.ints.get("elytraDelay").get();
		swimmingDelayStart = raw.ints.get("swimmingDelayStart").get();
		swimmingDelayEnd = raw.ints.get("swimmingDelayEnd").get();
		stickySwim = raw.bools.get("stickySwim").get();
		autoRestore = raw.bools.get("autoRestore").get();
		cancelAutoRestore = raw.bools.get("cancelAutoRestore").get();
		skipFrontView = raw.bools.get("skipFrontView").get();
		logSpam = raw.bools.get("logSpam").get();
		
		Pattern cust;
		try {
			cust = Pattern.compile(raw.patterns.get("customPattern").get());
		} catch (Exception e) {
			AutoThirdPerson.instance.logger.error("Exception loading customPattern: ", e);
			//cust = Pattern.compile(raw.patterns.get("customPattern").getDefault());
			cust = Pattern.compile("exception loading customPattern");
		}
		this.customPattern = cust;
		
		Pattern ignore;
		try {
			ignore = Pattern.compile(raw.patterns.get("ignorePattern").get());
		} catch (Exception e) {
			AutoThirdPerson.instance.logger.error("Exception loading ignorePattern: ", e);
			//ignore = Pattern.compile(raw.patterns.get("ignorePattern").getDefault());
			ignore = Pattern.compile("exception loading ignorePattern");
		}
		this.ignorePattern = ignore;
	}
	
	private final int configVersion;
	private final boolean boat;
	private final boolean cart;
	private final boolean animal;
	private final boolean elytra;
	private final boolean swim;
	private final boolean custom;
	private final boolean useIgnore;
	private final int elytraDelay;
	private final int swimmingDelayStart;
	private final int swimmingDelayEnd;
	private final boolean stickySwim;
	private final Pattern customPattern;
	private final Pattern ignorePattern;
	private final boolean autoRestore;
	private final boolean cancelAutoRestore;
	private final boolean skipFrontView;
	private final boolean logSpam;
	
	@Override
	public int configVersion() {
		return configVersion;
	}
	
	@Override
	public boolean boat() {
		return boat;
	}
	
	@Override
	public boolean cart() {
		return cart;
	}
	
	@Override
	public boolean animal() {
		return animal;
	}
	
	@Override
	public boolean elytra() {
		return elytra;
	}
	
	@Override
	public boolean swim() {
		return swim;
	}
	
	@Override
	public boolean custom() {
		return custom;
	}
	
	@Override
	public boolean useIgnore() {
		return useIgnore;
	}
	
	@Override
	public int elytraDelay() {
		return elytraDelay;
	}
	
	@Override
	public int swimmingDelayStart() {
		return swimmingDelayStart;
	}
	
	@Override
	public int swimmingDelayEnd() {
		return swimmingDelayEnd;
	}
	
	@Override
	public boolean stickySwim() {
		return stickySwim;
	}
	
	@Override
	public Pattern customPattern() {
		return customPattern;
	}
	
	@Override
	public Pattern ignorePattern() {
		return ignorePattern;
	}
	
	@Override
	public boolean autoRestore() {
		return autoRestore;
	}
	
	@Override
	public boolean cancelAutoRestore() {
		return cancelAutoRestore;
	}
	
	@Override
	public boolean skipFrontView() {
		return skipFrontView;
	}
	
	@Override
	public boolean logSpam() {
		return logSpam;
	}
	
	//Not supported
	
	@Override
	public boolean fixHandGlitch() {
		return false;
	}
}
