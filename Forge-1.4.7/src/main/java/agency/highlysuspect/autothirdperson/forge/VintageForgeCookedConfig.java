package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.config.ConfigProperties;
import agency.highlysuspect.autothirdperson.config.ConfigProperty;
import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import net.minecraftforge.common.Configuration;

import java.util.HashMap;
import java.util.Map;

public class VintageForgeCookedConfig implements CookedConfig {
	public VintageForgeCookedConfig(ConfigSchema schema, Configuration forge) {
		this.schema = schema;
		this.forge = forge;
		
		try {
			refresh();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private final ConfigSchema schema;
	private final Configuration forge;
	
	private final Map<ConfigProperty<?>, Object> parsedValues = new HashMap<ConfigProperty<?>, Object>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigProperty<T> prop) {
		Object result = parsedValues.get(prop);
		if(result == null) {
			parsedValues.put(prop, prop.defaultValue()); //compute-if-absent semantics in java 6
			return prop.defaultValue();
		}
		else return (T) result;
	}
	
	@Override
	public <T> T getOr(ConfigProperty<T> prop, T def) {
		if(prop == null) return def;
		else return get(prop);
	}
	
	@Override
	public void refresh() throws Exception {
		forge.load();
		
		schema.accept(new ConfigSchema.Visitor() {
			String currentSection = "Uncategorized";
			
			@Override
			public void visitSection(ConfigSchema.Section section) {
				currentSection = section.getCamelCaseName();
				
				if(section.comment != null) forge.addCustomCategoryComment(currentSection, section.comment);
			}
			
			@Override
			public <T> void visitProperty(ConfigProperty<T> prop) {
				T defaultValue = prop.defaultValue();
				
				String comment = prop.comment();
				if(comment != null && prop.showDefaultValue()) {
					comment += "\nDefault: " + prop.write(defaultValue);
				}
				
				if(defaultValue instanceof Integer) {
					int defaultInt = (Integer) defaultValue;
					int frogInt = forge.get(currentSection, prop.name(), defaultInt, comment).getInt(defaultInt);
					
					try {
						ConfigProperties.validateErased(prop, frogInt);
					} catch (Exception e) {
						AutoThirdPerson.instance.logger.error("prop " + prop.name() + " failed validation", e);
					}
					
					parsedValues.put(prop, frogInt);
				} else if(defaultValue instanceof Boolean) {
					boolean defaultBool = (Boolean) defaultValue;
					boolean frogBool = forge.get(currentSection, prop.name(), defaultBool, comment).getBoolean(defaultBool);
					parsedValues.put(prop, frogBool);
				} else {
					//use a string and the read/write methods
					String defaultString = prop.write(defaultValue);
					String frogString = forge.get(currentSection, prop.name(), defaultString, comment).value;
					if(frogString == null) frogString = defaultString;
					
					try {
						Object value = prop.read(frogString);
						ConfigProperties.validateErased(prop, value);
						parsedValues.put(prop, value);
					} catch (Exception e) {
						AutoThirdPerson.instance.logger.error("prop " + prop.name() + " failed validation", e);
					}
				}
			}
		});
		
		forge.save(); //unconditionally
	}
}
