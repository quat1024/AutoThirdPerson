package agency.highlysuspect.crummyconfig;

import agency.highlysuspect.autothirdperson.AtpSettings;

import java.util.regex.Pattern;

public class CookedCrummyConfig implements AtpSettings {
	public CookedCrummyConfig(UncookedCrummyConfig raw) {
		//mainly using getOrDefault here to catch config options that don't *exist* on this version
		//like fixHandGlitch on not-ancient versions
		
		configVersion = raw.ints.getOrDefault("configVersion", 0);
		boat = raw.bools.getOrDefault("boat", false);
		cart = raw.bools.getOrDefault("cart", false);
		animal = raw.bools.getOrDefault("animal", false);
		elytra = raw.bools.getOrDefault("elytra", false);
		swim = raw.bools.getOrDefault("swim", false);
		custom = raw.bools.getOrDefault("custom", false);
		useIgnore = raw.bools.getOrDefault("useIgnore", false);
		elytraDelay = raw.ints.getOrDefault("elytraDelay", 0);
		swimmingDelayStart = raw.ints.getOrDefault("swimmingDelayStart", 0);
		swimmingDelayEnd = raw.ints.getOrDefault("swimmingDelayEnd", 0);
		stickySwim = raw.bools.getOrDefault("stickySwim", false);
		customPattern = raw.patterns.getOrDefault("customPattern", DEFAULT_PATTERN);
		ignorePattern = raw.patterns.getOrDefault("ignorePattern", DEFAULT_PATTERN);
		autoRestore = raw.bools.getOrDefault("autoRestore", false);
		cancelAutoRestore = raw.bools.getOrDefault("cancelAutoRestore", false);
		skipFrontView = raw.bools.getOrDefault("skipFrontView", false);
		logSpam = raw.bools.getOrDefault("logSpam", false);
		fixHandGlitch = raw.bools.getOrDefault("fixHandGlitch", false);
		sneakDismount = raw.bools.getOrDefault("sneakDismount", false);
	}
	
	private static final Pattern DEFAULT_PATTERN = Pattern.compile("-empty pattern-");
	
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
	private final boolean fixHandGlitch;
	private final boolean sneakDismount;
	
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
	
	@Override
	public boolean fixHandGlitch() {
		return fixHandGlitch;
	}
	
	@Override
	public boolean sneakDismount() {
		return sneakDismount;
	}
}
