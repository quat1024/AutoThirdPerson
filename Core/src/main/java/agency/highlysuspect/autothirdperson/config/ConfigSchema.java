package agency.highlysuspect.autothirdperson.config;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigSchema {
	private final List<Object> entries = new ArrayList<Object>(); //permits Section or ConfigProperty objects
	private final Map<String, ConfigProperty<?>> propsByName = new HashMap<String, ConfigProperty<?>>();
	
	public ConfigSchema section(String name, @Nullable String sectionComment, ConfigProperty<?>... options) {
		entries.add(new Section(name, sectionComment));
		return opt(options);
	}
	
	public ConfigSchema opt(ConfigProperty<?>... options) {
		for(ConfigProperty<?> opt : options) {
			if(opt == null) continue;
			
			entries.add(opt);
			propsByName.put(opt.name(), opt);
		}
		
		return this;
	}
	
	public static class Section {
		public Section(String name, @Nullable String comment) {
			this.name = name;
			this.comment = comment;
		}
		
		public final String name;
		public final @Nullable String comment;
		
		//N.B.: Matcher.replaceAll(Function) would be very nice, but it's Java 9 only
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
	
	public interface Visitor {
		void visitSection(Section section);
		<T> void visitProperty(ConfigProperty<T> prop);
	}
	
	public void accept(Visitor v) {
		for(Object o : entries) {
			if(o instanceof Section) v.visitSection((Section) o);
			else if(o instanceof ConfigProperty<?>) v.visitProperty((ConfigProperty<?>) o);
			else throw new IllegalStateException("weird thing in config entries: " + v);
		}
	}
	
	public Map<String, ConfigProperty<?>> getPropsByName() {
		return propsByName;
	}
}
