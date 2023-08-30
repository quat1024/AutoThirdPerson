package agency.highlysuspect.autothirdperson.config;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigProperties {
	@SuppressWarnings("unchecked")
	public static <T> void validateErased(ConfigProperty<T> prop, Object thing) throws Exception {
		prop.validate((T) thing);
	}
	
	public static abstract class PropertyBase<T> implements ConfigProperty<T> {
		public PropertyBase(String name, @Nullable String comment, T defaultValue) {
			this.name = name;
			this.comment = comment;
			this.defaultValue = defaultValue;
		}
		
		protected String name;
		protected @Nullable String comment;
		protected T defaultValue;
		
		@Override
		public String name() {
			return name;
		}
		
		@Override
		public @Nullable String comment() {
			return comment;
		}
		
		@Override
		public T defaultValue() {
			return defaultValue;
		}
		
		@Override
		public boolean showDefaultValue() {
			//TODO: api
			return !"configVersion".equals(name);
		}
		
		@Override
		public void validate(T thing) {
			//seems good
		}
	}
	
	public static class IntProperty extends PropertyBase<Integer> {
		public IntProperty(String name, @Nullable String comment, Integer defaultValue) {
			this(name, comment, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		
		public IntProperty(String name, @Nullable String comment, Integer defaultValue, int min, int max) {
			super(name, comment, defaultValue);
			this.min = min;
			this.max = max;
			
			if(this.comment != null) {
				if(min != Integer.MIN_VALUE) this.comment += "\nMin: " + min;
				if(max != Integer.MAX_VALUE) this.comment += "\nMax: " + max;
			}
		}
		
		private final int min, max;
		
		@Override
		public String write(Integer thing) {
			return Integer.toString(thing);
		}
		
		@Override
		public Integer read(String s) throws RuntimeException {
			return Integer.parseInt(s);
		}
		
		@Override
		public void validate(Integer thing) {
			if(thing < min) throw new RuntimeException("Number " + thing + " is smaller than minimum " + min);
			if(thing > max) throw new RuntimeException("Number " + thing + " is larger than maximum " + max);
		}
	}
	
	public static class BoolProperty extends PropertyBase<Boolean> {
		public BoolProperty(String name, @Nullable String comment, Boolean defaultValue) {
			super(name, comment, defaultValue);
		}
		
		@Override
		public String write(Boolean thing) {
			return Boolean.toString(thing);
		}
		
		@Override
		public Boolean read(String s) throws Exception {
			return Boolean.parseBoolean(s);
		}
	}
	
	public static class PatternProperty extends PropertyBase<Pattern> {
		public PatternProperty(String name, @Nullable String comment, Pattern defaultValue) {
			super(name, comment, defaultValue);
		}
		
		@Override
		public String write(Pattern thing) {
			return thing.pattern();
		}
		
		@Override
		public Pattern read(String s) throws PatternSyntaxException {
			return Pattern.compile(s);
		}
	}
	
	public static IntProperty integer(String name, @Nullable String comment, int defaultValue) {
		return new IntProperty(name, comment, defaultValue);
	}
	
	public static IntProperty integer(String name, @Nullable String comment, int defaultValue, int min, int max) {
		return new IntProperty(name, comment, defaultValue, min, max);
	}
	
	public static IntProperty nonNegativeInteger(String name, @Nullable String comment, int defaultValue) {
		return integer(name, comment, defaultValue, 0, Integer.MAX_VALUE);
	}
	
	public static IntProperty nonNegativeInteger(String name, @Nullable String comment, int defaultValue, int max) {
		return integer(name, comment, defaultValue, 0, max);
	}
	
	public static BoolProperty bool(String name, @Nullable String comment, boolean defaultValue) {
		return new BoolProperty(name, comment, defaultValue);
	}
	
	public static PatternProperty pattern(String name, @Nullable String comment, Pattern defaultValue) {
		return new PatternProperty(name, comment, defaultValue);
	}
}
