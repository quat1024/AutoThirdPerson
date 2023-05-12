package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.SettingsSpec;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;

public class UncookedForgeSettings {
	public UncookedForgeSettings(SettingsSpec spec) {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		boolean noSectionsYet = true;
		
		for(SettingsSpec.Entry e : spec) {
			if(e instanceof SettingsSpec.Section) {
				SettingsSpec.Section s = (SettingsSpec.Section) e;
				
				if(!noSectionsYet) builder.pop();
				builder.push(s.getCamelCaseName());
				noSectionsYet = false;
			} else if(e instanceof SettingsSpec.Setting<?>) {
				SettingsSpec.Setting<?> settingGeneric = (SettingsSpec.Setting<?>) e;
				if(settingGeneric.comment != null) builder.comment(settingGeneric.comment);
				
				if(settingGeneric instanceof SettingsSpec.IntSetting) {
					SettingsSpec.IntSetting setting = (SettingsSpec.IntSetting) settingGeneric;
					
					if(setting.hasMin() || setting.hasMax()) {
						ints.put(setting.name, builder.defineInRange(setting.name, setting.defaultValue, setting.min, setting.max));
					} else {
						ints.put(setting.name, builder.define(setting.name, setting.defaultValue));
					}
				} else if(settingGeneric instanceof SettingsSpec.BoolSetting) {
					SettingsSpec.BoolSetting setting = (SettingsSpec.BoolSetting) settingGeneric;
					bools.put(setting.name, builder.define(setting.name, setting.defaultValue));
				} else if(settingGeneric instanceof SettingsSpec.PatternSetting) {
					SettingsSpec.PatternSetting setting = (SettingsSpec.PatternSetting) settingGeneric;
					patterns.put(setting.name, builder.define(setting.name, setting.defaultValue.pattern())); //pattern will be compiled later
				}
			}
		}
		
		forgeSpec = builder.build();
	}
	
	public final ForgeConfigSpec forgeSpec;
	public final Map<String, ForgeConfigSpec.ConfigValue<Integer>> ints = new HashMap<>();
	public final Map<String, ForgeConfigSpec.ConfigValue<Boolean>> bools = new HashMap<>();
	public final Map<String, ForgeConfigSpec.ConfigValue<String>> patterns = new HashMap<>();
}
