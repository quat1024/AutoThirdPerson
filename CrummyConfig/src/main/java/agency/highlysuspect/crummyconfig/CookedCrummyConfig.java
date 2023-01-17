package agency.highlysuspect.crummyconfig;

import agency.highlysuspect.autothirdperson.AtpSettings;

import java.util.regex.Pattern;

public class CookedCrummyConfig implements AtpSettings {
	public CookedCrummyConfig(UncookedCrummyConfig raw) {
		configVersion = raw.ints.get("configVersion");
		boat = raw.bools.get("boat");
		cart = raw.bools.get("cart");
		animal = raw.bools.get("animal");
		elytra = raw.bools.get("elytra");
		swim = raw.bools.get("swim");
		custom = raw.bools.get("custom");
		useIgnore = raw.bools.get("useIgnore");
		elytraDelay = raw.ints.get("elytraDelay");
		swimmingDelayStart = raw.ints.get("swimmingDelayStart");
		swimmingDelayEnd = raw.ints.get("swimmingDelayEnd");
		stickySwim = raw.bools.get("stickySwim");
		autoRestore = raw.bools.get("autoRestore");
		cancelAutoRestore = raw.bools.get("cancelAutoRestore");
		skipFrontView = raw.bools.get("skipFrontView");
		logSpam = raw.bools.get("logSpam");
		
		customPattern = raw.patterns.get("customPattern");
		ignorePattern = raw.patterns.get("ignorePattern");
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
}
