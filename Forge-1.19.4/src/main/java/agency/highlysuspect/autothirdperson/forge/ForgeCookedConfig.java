package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.config.ConfigProperties;
import agency.highlysuspect.autothirdperson.config.ConfigProperty;
import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ForgeCookedConfig implements CookedConfig {
	public ForgeCookedConfig(ConfigSchema schema) {
		ForgeConfigSpec.Builder forge = new ForgeConfigSpec.Builder();
		
		schema.accept(new ConfigSchema.Visitor() {
			boolean noSectionsYet = true;
			
			@Override
			public void visitSection(ConfigSchema.Section section) {
				if(noSectionsYet) noSectionsYet = false;
				else forge.pop();
				
				if(section.comment != null) forge.comment(section.comment);
				
				forge.push(section.getCamelCaseName());
			}
			
			@Override
			public <T> void visitProperty(ConfigProperty<T> prop) {
				T defaultValue = prop.defaultValue();
				
				String comment = prop.comment();
				if(comment != null) {
					if(prop.showDefaultValue()) comment += "\nDefault: " + prop.write(defaultValue);
					if(comment.trim().isEmpty()) comment = " ";
					forge.comment(comment);
				}
				
				if(defaultValue instanceof Integer) {
					int defaultInt = (Integer) defaultValue;
					ForgeConfigSpec.ConfigValue<Integer> forgeInt = forge.define(prop.name(), defaultInt, mkValidator(prop));
					forgeProps.put(prop, forgeInt);
				} else if(defaultValue instanceof Boolean) {
					boolean defaultBool = (Boolean) defaultValue;
					ForgeConfigSpec.ConfigValue<Boolean> forgeBool = forge.define(prop.name(), defaultBool, mkValidator(prop));
					forgeProps.put(prop, forgeBool);
				} else {
					String defaultString = prop.write(defaultValue); //using my own serializer
					ForgeConfigSpec.ConfigValue<String> forgeString = forge.define(prop.name(), defaultString, mkValidator(prop));
					forgeProps.put(prop, forgeString);
				}
			}
		});
		
		forgeSpec = forge.build();
		
		try {
			refresh();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Predicate<Object> mkValidator(ConfigProperty<?> prop) {
		return (obj) -> {
			if(obj == null) {
				System.err.println("FORGE ASKED ME TO VALIDATE A NULL VALUE FOR " + prop.name() + ", is the filewatcher acting up again");
				return false; //Idk why forge throws nulls at me, maybe related to their glitchy af filewatcher
			}
			
			//TODO kludge for manually-parsed options
			if(obj instanceof String) {
				try {
					//using my deserializer
					obj = prop.read((String) obj);
				} catch (Exception e) {
					AutoThirdPerson.instance.logger.warn("Option '" + prop.name() + "' failed to parse: ", e);
					return false;
				}
			}
			
			try {
				ConfigProperties.validateErased(prop, obj);
			} catch (Exception e) {
				AutoThirdPerson.instance.logger.error("Option " + prop.name() + " failed validation: ", e);
				return false;
			}
			
			return true;
		};
	}
	
	public final ForgeConfigSpec forgeSpec;
	
	private final Map<ConfigProperty<?>, ForgeConfigSpec.ConfigValue<?>> forgeProps = new HashMap<>();
	private final Map<ConfigProperty<?>, Object> parsedValues = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigProperty<T> prop) {
		return (T) parsedValues.computeIfAbsent(prop, this::load);
	}
	
	@Override
	public <T> T getOr(ConfigProperty<T> prop, T def) {
		if(prop == null) return def;
		else return get(prop);
	}
	
	@Override
	public void refresh() throws Exception {
		parsedValues.clear();
	}
	
	public <T> T load(ConfigProperty<T> prop) {
		Object frog = forgeProps.get(prop).get();
		
		//TODO: kludge for manually-parsed options
		if(frog instanceof String) {
			try {
				//using my deserializer
				frog = prop.read((String) frog);
			} catch (Exception e) {
				AutoThirdPerson.instance.logger.warn("Option '{}' failed to parse. Using default value of {}.", prop.name(), prop.write(prop.defaultValue()));
				return prop.defaultValue();
			}
		}
		
		//by this point the forge option & my option should unify
		@SuppressWarnings("unchecked") T frog2 = (T) frog;
		
		try {
			prop.validate(frog2);
		} catch (Exception e) {
			AutoThirdPerson.instance.logger.warn("Option '{}' failed validation. Using default value of {}.", prop.name(), prop.write(prop.defaultValue()));
			return prop.defaultValue();
		}
		
		return frog2;
	}
}
