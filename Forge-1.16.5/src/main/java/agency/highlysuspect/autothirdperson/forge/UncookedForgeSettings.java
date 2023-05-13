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
				if(!noSectionsYet) builder.pop();
				builder.push(((SettingsSpec.Section) e).getCamelCaseName());
				noSectionsYet = false;
			} else if(e instanceof SettingsSpec.Setting<?>) {
				SettingsSpec.Setting<?> s = (SettingsSpec.Setting<?>) e;
				if(s.comment != null) builder.comment(s.comment);
				
				if(s instanceof SettingsSpec.IntSetting) {
					SettingsSpec.IntSetting i = (SettingsSpec.IntSetting) s;
					if(i.hasMin() || i.hasMax()) {
						ints.put(s.name, builder.defineInRange(s.name, i.defaultValue, i.min, i.max));
					} else {
						ints.put(s.name, builder.define(s.name, i.defaultValue));
					}
				} else if(s instanceof SettingsSpec.BoolSetting) {
					SettingsSpec.BoolSetting b = (SettingsSpec.BoolSetting) s;
					bools.put(s.name, builder.define(s.name, b.defaultValue));
				} else if(s instanceof SettingsSpec.PatternSetting) {
					SettingsSpec.PatternSetting p = (SettingsSpec.PatternSetting) s;
					patterns.put(s.name, builder.define(s.name, p.defaultValue.pattern())); //pattern will be compiled later
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
