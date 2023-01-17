package agency.highlysuspect.autothirdperson.fabric;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.SettingsSpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CrummyConfig {
	public CrummyConfig(Path path, SettingsSpec spec) {
		this.spec = spec;
		this.path = path;
		
		loadDefaultValues();
	}
	
	private final SettingsSpec spec;
	private final Path path;
	
	public final Map<String, Integer> ints = new HashMap<>();
	public final Map<String, Boolean> bools = new HashMap<>();
	public final Map<String, Pattern> patterns = new HashMap<>();
	
	public void save() throws IOException {
		Files.createDirectories(path.getParent());
		Files.write(path, write(), StandardCharsets.UTF_8);
	}
	
	public void load() throws IOException {
		if(Files.exists(path)) read(Files.readAllLines(path, StandardCharsets.UTF_8));
		else loadDefaultValues();
		save();
	}
	
	public List<String> write() {
		List<String> out = new ArrayList<>();
		
		for(SettingsSpec.Entry entry : spec) {
			if(entry instanceof SettingsSpec.Section sec) {
				String bar = "#".repeat(sec.name.length() + 6);
				out.add(bar);
				out.add("## " + sec.name + " ##");
				out.add(bar);
			} else if(entry instanceof SettingsSpec.Setting<?> genericSetting) {
				if(genericSetting.comment != null) for(String commentLine : genericSetting.comment.split("\n")) out.add("# " + commentLine);
				if(genericSetting.writeDefaultComment) out.add("# Default: " + stringifyValue(genericSetting.defaultValue));
				if(entry instanceof SettingsSpec.IntSetting i) {
					if(i.hasMin()) out.add("# Must be at least " + i.min + ".");
					if(i.hasMax()) out.add("# Must be at most " + i.max + ".");
				}
				
				out.add(genericSetting.name + " = " + stringifyValue(chooseMap(genericSetting).get(genericSetting.name)));
			}
			
			out.add("");
		}
		
		out.remove(out.size() - 1); //remove the final blank line
		
		return out;
	}
	
	public void read(List<String> lines) {
		//so anything missing or that fails to parse will have its default value
		loadDefaultValues();
		
		for(int line = 0; line < lines.size(); line++) {
			String s = lines.get(line).trim();
			if(s.isBlank() || s.startsWith("#")) continue;
			
			String[] split = s.split("=", 2);
			if(split.length != 2) {
				AutoThirdPerson.instance.logger.warn("On line {}, there is no equal sign to split a key/value pair.", line);
				continue;
			}
			String keyStr = split[0].trim();
			String valueStr = split[1].trim();
			
			SettingsSpec.Setting<?> opt = spec.getSetting(keyStr);
			if(opt != null) {
				try {
					readOne(opt, valueStr);
				} catch (Exception e) {
					AutoThirdPerson.instance.logger.warn("On line {}, unable to parse '{}' for option {}. Using default value of '{}'.", line, valueStr, opt.name, stringifyValue(opt.defaultValue), e);
				}
			} else AutoThirdPerson.instance.logger.warn("On line {}, there is no option named '{}'.", line, keyStr);
		}
		
		if(ints.containsKey("configVersion")) ints.put("configVersion", 6);
	}
	
	//Broken into another method in order to name <T>
	@SuppressWarnings({
		"unchecked",
		"RedundantThrows" //Integer.parseInt and Pattern.compile are failable, but their exceptions are unchecked, even in a throws clause >,,<
	})
	private <T> void readOne(SettingsSpec.Setting<T> opt, String valueStr) throws Exception {
		Map<String, T> destination = chooseMap(opt);
		if(opt instanceof SettingsSpec.IntSetting i) {
			Object value = i.clamp(Integer.parseInt(valueStr));
			destination.put(opt.name, (T) value);
		} else if(opt instanceof SettingsSpec.BoolSetting) {
			Object value = Boolean.parseBoolean(valueStr);
			destination.put(opt.name, (T) value);
		} else if(opt instanceof SettingsSpec.PatternSetting) {
			destination.put(opt.name, (T) Pattern.compile(valueStr));
		} else {
			AutoThirdPerson.instance.logger.warn("Missing parser for setting {}. This is a bug!", opt.name);
		}
	}
	
	private void loadDefaultValues() {
		for(SettingsSpec.Entry entry : spec) if(entry instanceof SettingsSpec.Setting<?> genericSetting) loadDefaultValue(genericSetting);
	}
	
	//broken into another method in order to name <T>
	private <T> void loadDefaultValue(SettingsSpec.Setting<T> opt) {
		chooseMap(opt).put(opt.name, opt.defaultValue);
	}
	
	private String stringifyValue(Object o) {
		//happens to work for all types in this config file (integer, bool, and Pattern)
		return o.toString();
	}
	
	@SuppressWarnings("unchecked")
	private <T> Map<String, T> chooseMap(SettingsSpec.Setting<T> opt) {
		if(opt instanceof SettingsSpec.IntSetting) return (Map<String, T>) ints;
		else if(opt instanceof SettingsSpec.BoolSetting) return (Map<String, T>) bools;
		else if(opt instanceof SettingsSpec.PatternSetting) return (Map<String, T>) patterns;
		
		//Should be unreachable...
		AutoThirdPerson.instance.logger.warn("Missing settings map for setting {}. This is a bug!", opt.name);
		return new HashMap<>();
	}
}
