package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.MyConsumer;
import agency.highlysuspect.autothirdperson.SettingsSpec;
import net.minecraftforge.common.config.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class VintageForgeSettings implements AtpSettings {
	public VintageForgeSettings(final Configuration config, SettingsSpec settings) {
		config.load();
		
		final Map<String, Integer> intValues = new HashMap<String, Integer>();
		final Map<String, Boolean> boolValues = new HashMap<String, Boolean>();
		final Map<String, Pattern> patternValues = new HashMap<String, Pattern>();
		
		settings.visitEntries(new MyConsumer<SettingsSpec.Entry>() {
			String currentCategory = "Uncategorized";
			
			@Override
			public void accept(SettingsSpec.Entry thing) {
				if(thing instanceof SettingsSpec.Section) {
					currentCategory = ((SettingsSpec.Section) thing).getCamelCaseName();
				} else if(thing instanceof SettingsSpec.Setting<?>) {
					SettingsSpec.Setting<?> s = (SettingsSpec.Setting<?>) thing;
					
					if(thing instanceof SettingsSpec.IntSetting) {
						SettingsSpec.IntSetting i = (SettingsSpec.IntSetting) thing;
						intValues.put(s.name, config.getInt(s.name, currentCategory, i.defaultValue, i.min, i.max, s.comment));
					} else if(thing instanceof SettingsSpec.BoolSetting) {
						SettingsSpec.BoolSetting b = (SettingsSpec.BoolSetting) thing;
						boolValues.put(s.name, config.getBoolean(s.name, currentCategory, b.defaultValue, s.comment));
					} else if(thing instanceof SettingsSpec.PatternSetting) {
						SettingsSpec.PatternSetting p = (SettingsSpec.PatternSetting) thing;
						
						String unparsedPattern = config.getString(s.name, currentCategory, p.defaultValue.toString(), s.comment);
						
						Pattern parsedPattern;
						try {
							parsedPattern = Pattern.compile(unparsedPattern);
						} catch (Exception e) {
							AutoThirdPerson.instance.logger.error("Exception loading pattern " + s.name + ": " + e.getMessage(), e);
							parsedPattern = p.defaultValue;
						}
						
						patternValues.put(s.name, parsedPattern);
					}
				}
			}
		});
		
		if(config.hasChanged()) config.save();
		
		configVersion = intValues.get("configVersion");
		boat = boolValues.get("boat");
		cart = boolValues.get("cart");
		animal = boolValues.get("animal");
		swim = boolValues.get("swim");
		custom = boolValues.get("custom");
		useIgnore = boolValues.get("useIgnore");
		swimmingDelayStart = intValues.get("swimmingDelayStart");
		swimmingDelayEnd = intValues.get("swimmingDelayEnd");
		customPattern = patternValues.get("customPattern");
		ignorePattern = patternValues.get("ignorePattern");
		autoRestore = boolValues.get("autoRestore");
		cancelAutoRestore = boolValues.get("cancelAutoRestore");
		skipFrontView = boolValues.get("skipFrontView");
		logSpam = boolValues.get("logSpam");
	}
	
	private final int configVersion;
	private final boolean boat;
	private final boolean cart;
	private final boolean animal;
	private final boolean swim;
	private final boolean custom;
	private final boolean useIgnore;
	private final int swimmingDelayStart;
	private final int swimmingDelayEnd;
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
	public int swimmingDelayStart() {
		return swimmingDelayStart;
	}
	
	@Override
	public int swimmingDelayEnd() {
		return swimmingDelayEnd;
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
	
	//Unsupported options
	
	@Override
	public int elytraDelay() {
		return 0;
	}
	
	@Override
	public boolean elytra() {
		return false;
	}
	
	@Override
	public boolean stickySwim() {
		return false;
	}
}
