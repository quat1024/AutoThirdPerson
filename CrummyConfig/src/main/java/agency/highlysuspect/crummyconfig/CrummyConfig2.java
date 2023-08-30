package agency.highlysuspect.crummyconfig;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.config.ConfigProperty;
import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CrummyConfig2 implements CookedConfig {
	public CrummyConfig2(ConfigSchema schema, Path path) {
		this.schema = schema;
		this.path = path;
	}
	
	private final ConfigSchema schema;
	private final Path path;
	
	private final Map<ConfigProperty<?>, Object> parsedValues = new IdentityHashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigProperty<T> prop) {
		return (T) parsedValues.computeIfAbsent(prop, ConfigProperty::defaultValue);
	}
	
	@Override
	public <T> T getOr(ConfigProperty<T> prop, T def) {
		if(prop == null) return def;
		else return get(prop);
	}
	
	@Override
	public void refresh() throws Exception {
		load();
		save();
	}
	
	public void load() throws IOException {
		//"get" has get-or-default semantics, it's fine to leave the map empty
		if(Files.notExists(path)) return;
		
		parsedValues.clear();
		
		Map<String, ConfigProperty<?>> propsByName = schema.getPropsByName();
		
		Iterator<String> lineserator = Files.lines(path, StandardCharsets.UTF_8).iterator();
		int lineNo = 0;
		while(lineserator.hasNext()) {
			lineNo++;
			
			String line = lineserator.next().trim();
			if(line.isEmpty() || line.startsWith("#")) continue;
			
			String[] split = line.split("=", 2);
			if(split.length != 2) {
				AutoThirdPerson.instance.logger.warn("On line {} of {}, there is no equal sign to split a key/value pair.", line, path);
				continue;
			}
			
			ConfigProperty<?> key = propsByName.get(split[0].trim());
			if(key == null) {
				AutoThirdPerson.instance.logger.warn("On line {} of {}, there's no known option named '{}'.", lineNo, path, split[0].trim());
				continue;
			}
			parsedValues.put(key, read(key, split[1].trim(), lineNo));
		}
	}
	
	private <T> T read(ConfigProperty<T> key, String valueStr, int lineNo) {
		T value;
		try {
			value = key.read(valueStr);
		} catch (Exception e) {
			//TODO: use Error, my shitty logger facade doesnt expose the right one
			AutoThirdPerson.instance.logger.warn("On line {} of {}, option '{}' failed to parse. Defaulting to {}.", lineNo, path, key.name(), key.write(key.defaultValue()));
			return key.defaultValue();
		}
		
		try {
			key.validate(value);
		} catch (Exception e) {
			AutoThirdPerson.instance.logger.warn("On line {} of {}, option '{}' failed validation. Defaulting to {}.", lineNo, path, key.name(), key.write(key.defaultValue()));
			return key.defaultValue();
		}
		
		return value;
	}
	
	public void save() throws IOException {
		List<String> out = new ArrayList<>();
		
		schema.accept(new ConfigSchema.Visitor() {
			@Override
			public void visitSection(ConfigSchema.Section section) {
				String bar = repeat("#", section.name.length() + 6);
				out.add(bar);
				out.add("## " + section.name + " ##");
				out.add(bar);
				if(section.comment != null) for(String line : section.comment.split("\n")) out.add("# " + line);
				
				out.add("");
			}
			
			@Override
			public <T> void visitProperty(ConfigProperty<T> prop) {
				@Nullable String comment = prop.comment();
				if(comment != null) for(String line : comment.split("\n")) out.add("# " + line);
				if(prop.showDefaultValue()) out.add("# Default: " + prop.write(prop.defaultValue()));
				out.add(prop.name() + " = " + prop.write(get(prop)));
				out.add("");
			}
		});
		
		out.remove(out.size() - 1); //remove the final blank line
		
		if(path.getParent() != null) Files.createDirectories(path.getParent());
		Files.write(path, out, StandardCharsets.UTF_8);
	}
	
	//Functionally the same as String#repeat, but Java 8 compatible.
	private static String repeat(String in, int count) {
		StringBuilder bob = new StringBuilder(in.length() * count);
		for(int i = 0; i < count; i++) {
			bob.append(in);
		}
		return bob.toString();
	}
}
