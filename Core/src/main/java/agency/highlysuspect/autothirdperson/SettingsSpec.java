package agency.highlysuspect.autothirdperson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A pretty lazily slapped-together description of the options in a config file.
 * Doesn't hold the current settings, just their defaults.
 */
public class SettingsSpec implements Iterable<SettingsSpec.Entry> {
	/// builder api ///
	
	public void section(String name) {
		entries.add(new Section(name));
	}
	
	public void integer(String name, @Nullable String comment, int defaultValue) {
		integer(name, comment, defaultValue, x -> {});
	}
	
	public void integer(String name, @Nullable String comment, int defaultValue, Consumer<IntSetting> configurator) {
		IntSetting setting = new IntSetting(name, comment, defaultValue);
		configurator.accept(setting);
		entries.add(setting);
		allSettings.put(name, setting);
		allIntSettings.put(name, setting);
	}
	
	public void bool(String name, @Nullable String comment, boolean defaultValue) {
		BoolSetting setting = new BoolSetting(name, comment, defaultValue);
		entries.add(setting);
		allSettings.put(name, setting);
		allBoolSettings.put(name, setting);
	}
	
	public void pattern(String name, @Nullable String comment, Pattern defaultValue) {
		PatternSetting setting = new PatternSetting(name, comment, defaultValue);
		entries.add(setting);
		allSettings.put(name, setting);
		allPatternSettings.put(name, setting);
	}
	
	/// visiting ///
	
	@NotNull
	@Override
	public Iterator<Entry> iterator() {
		return entries.iterator();
	}
	
	public void visitEntries(Consumer<Entry> visitor) {
		entries.forEach(visitor);
	}
	
	public IntSetting getIntSetting(String name) {
		return allIntSettings.get(name);
	}
	
	public BoolSetting getBoolSetting(String name) {
		return allBoolSettings.get(name);
	}
	
	public PatternSetting getPatternSetting(String name) {
		return allPatternSettings.get(name);
	}
	
	/// bookkeeping ///
	
	private final List<Entry> entries = new ArrayList<>();
	
	private final Map<String, Setting<?>> allSettings = new HashMap<>();
	private final Map<String, IntSetting> allIntSettings = new HashMap<>();
	private final Map<String, BoolSetting> allBoolSettings = new HashMap<>();
	private final Map<String, PatternSetting> allPatternSettings = new HashMap<>();
	
	public interface Entry {}
	
	public static class Section implements Entry {
		public Section(String name) {
			this.name = name;
		}
		
		public String name;
		
		//N.B.: Matcher.replaceAll(Function) would be very nice, but it's Java 9 only,
		//and i have grand visions of taking this codebase wayyy back in time
		public String getCamelCaseName() {
			Pattern p = Pattern.compile(" ."); //space followed by anything
			
			String work = name.toLowerCase(Locale.ROOT);
			while(true) {
				Matcher m = p.matcher(work);
				if(m.find()) {
					String beforeMatch = work.substring(0, m.start());
					String match = work.substring(m.start(), m.end());
					String afterMatch = work.substring(m.end());
					
					work = beforeMatch + match.trim().toUpperCase(Locale.ROOT) + afterMatch;
				} else break;
			}
			
			return work;
		}
	}
	
	public static class Setting<T> implements Entry {
		public Setting(String name, @Nullable String comment, T defaultValue) {
			this.name = name;
			this.comment = comment;
			this.defaultValue = defaultValue;
		}
		
		public String name;
		public @Nullable String comment;
		public T defaultValue;
	}
	
	public static class IntSetting extends Setting<Integer> {
		public IntSetting(String name, @Nullable String comment, Integer defaultValue) {
			super(name, comment, defaultValue);
		}
		
		public int min = Integer.MIN_VALUE;
		public int max = Integer.MAX_VALUE;
		
		public boolean hasMin() {
			return min != Integer.MIN_VALUE;
		}
		
		public boolean hasMax() {
			return max != Integer.MAX_VALUE;
		}
	}
	
	public static class BoolSetting extends Setting<Boolean> {
		public BoolSetting(String name, @Nullable String comment, Boolean defaultValue) {
			super(name, comment, defaultValue);
		}
	}
	
	public static class PatternSetting extends Setting<Pattern> {
		public PatternSetting(String name, @Nullable String comment, Pattern defaultValue) {
			super(name, comment, defaultValue);
		}
	}
}
