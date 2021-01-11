package agency.highlysuspect.autothirdperson;

import com.google.common.base.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Settings {
	private static final int CURRENT_CONFIG_VERSION = 2;
	@Hidden
	private int configVersion = CURRENT_CONFIG_VERSION;
	@LineBreak
	@Section("Scenarios")
	@Comment("Automatically go into third person when riding a boat?")
	public boolean boat = true;
	@Comment("Automatically go into third person when riding a minecart?")
	public boolean cart = true;
	@Comment("Automatically go into third person when riding an animal?")
	public boolean animal = true;
	@Comment("Automatically go into third person when flying an elytra?")
	public boolean elytra = true;
	@Comment({
		"If 'true' the customPattern will be used and riding anything",
		"matching it will toggle third person."
	})
	public boolean custom = false;
	@Comment("If 'true' the ignorePattern will be used and anything matching it will be ignored.")
	public boolean useIgnore = false;
	@LineBreak
	@Section("Scenario Options")
	@Comment({
		"Ticks of elytra flight required before the camera automatically toggles,",
		"if the 'elytra' option is enabled."
	})
	public int elytraDelay = 7;
	@Comment({
		"Entity IDs that match this regular expression will be considered,",
		"if the 'custom' option is enabled."
	})
	public Pattern customPattern = Pattern.compile("^minecraft:(cow|chicken)$");
	@Comment({
		"Entity IDs that match this regular expression will be ignored,",
		"if the 'useIgnore' option is enabled."
	})
	public Pattern ignorePattern = Pattern.compile("^examplemod:example$");
	@LineBreak
	@Section("Restoration")
	@Comment("Go back into first-person when dismounting?")
	public boolean autoRestore = true;
	@Comment({
		"If 'true', pressing f5 after mounting something will prevent your camera",
		"from being automatically restored to first-person when you dismount."
	})
	public boolean cancelAutoRestore = true;
	@LineBreak
	@Section("Extra")
	@Comment("Skip the 'third-person front' camera mode when pressing F5.")
	public boolean skipFrontView = false;
	@Comment({
		"Dump a bunch of debug crap into the log.",
		"Might be handy!"
	})
	public boolean logSpam = false;
	
	//////////////////////////////////////////////////////
	
	public static Settings load(Path path) {
		AutoThirdPerson.LOGGER.info("Loading settings...");
		
		if(Files.exists(path)) {
			try {
				AutoThirdPerson.LOGGER.info("File exists, reading...");
				Settings s = read(path);
				
				boolean needsRewrite = s.update();
				
				if(needsRewrite) {
					AutoThirdPerson.LOGGER.info("Saving over the config file because it is old");
					try {
						s.write(path);
					} catch (IOException e) {
						AutoThirdPerson.LOGGER.info("Could not write over config file", e);
					}
				}
				
				return s;
			} catch (ParseException | IOException e) {
				AutoThirdPerson.LOGGER.info("Could not read or parse config file", e);
				AutoThirdPerson.LOGGER.info("Using default config");
				return new Settings();
			}
		} else {
			Settings s = new Settings();
			
			try {
				AutoThirdPerson.LOGGER.info("Creating default settings...");
				s.write(path);
			} catch (IOException e) {
				AutoThirdPerson.LOGGER.info("Could not write default config file", e);
			}
			
			return s;
		}
	}
	
	//returns 'true' if the config file should be written over
	//We have DFU at home lol
	private boolean update() throws ParseException {
		if(configVersion > CURRENT_CONFIG_VERSION) {
			throw new ParseException("This config file is from the future!");
		}
		
		boolean isOld = configVersion < CURRENT_CONFIG_VERSION;
		
		configVersion = CURRENT_CONFIG_VERSION;
		return isOld;
	}
	
	public static Settings read(Path path) throws IOException, ParseException {
		Settings settings = new Settings();
		int lineCount = 0;
		
		for(String line : Files.readAllLines(path)) {
			++lineCount;
			
			line = line.trim();
			if(line.startsWith("#") || line.isEmpty()) continue;
			
			String[] split = line.split("=", 2);
			if(split.length == 1) {
				split = new String[] { split[0], "" };
			}
			
			split[0] = split[0].trim();
			split[1] = split[1].trim();
			Field field;
			try {
				field = Settings.class.getDeclaredField(split[0]);
			} catch (ReflectiveOperationException e) {
				throw new ParseException("No config field named " + split[0] + " referenced on line " + lineCount, e);
			}
			
			Object yahoo;
			
			if(field.getType() == Boolean.TYPE) yahoo = Boolean.parseBoolean(split[1]);
			else if(field.getType() == Integer.TYPE) yahoo = parseInt(split[1]);
			else if(field.getType() == Pattern.class) yahoo = compilePattern(split[1]);
			else throw new ParseException("Should be impossible but if you see this quat's a dork and forgot to add a deserializer for " + split[0] + " go yell at it to add one!!");
			
			try {
				field.set(settings, yahoo);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Can't set field for some reason", e);
			}
		}
		
		return settings;
	}
	
	private static int parseInt(String s) throws ParseException {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new ParseException("Can't parse this integer", e);
		}
	}
	
	private static Pattern compilePattern(String s) throws ParseException {
		try {
			return Pattern.compile(s);
		} catch (PatternSyntaxException e) {
			throw new ParseException("Can't compile this regular expression", e);
		}
	}
	
	public void write(Path path) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		
		for(Field f : Settings.class.getDeclaredFields()) {
			int mod = f.getModifiers();
			if(Modifier.isStatic(mod) || Modifier.isTransient(mod) || Modifier.isFinal(mod)) continue;
			
			if(f.isAnnotationPresent(LineBreak.class)) {
				writer.newLine();
			}
			
			if(f.isAnnotationPresent(Section.class)) {
				String sect = f.getAnnotation(Section.class).value();
				String hashes = Strings.repeat("#", sect.length() + 4);
				writer.write(hashes);
				writer.newLine();
				writer.write("# " + sect + " #");
				writer.newLine();
				writer.write(hashes);
				writer.newLine();
				writer.newLine();
			}
			
			if(f.isAnnotationPresent(Comment.class)) {
				for(String line : f.getAnnotation(Comment.class).value()) {
					writer.write("# ");
					writer.write(line);
					writer.newLine();
				}
			}
			
			writer.write(f.getName());
			writer.write(" = ");
			
			try {
				//So far this works for ints, booleans, and Patterns
				writer.write(f.get(this).toString());
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Can't read field to serialize to config file, for some reason", e);
			}
			
			writer.newLine();
		}
		
		writer.flush();
	}
	
	private static class ParseException extends Exception {
		public ParseException(String message) {
			super(message);
		}
		
		public ParseException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface LineBreak {}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Comment {
		String[] value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Section {
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Hidden {}
	
	private static final Settings DEFAULT_SETTINGS = new Settings();
	public static <T> T getDefaultValue(Field f) {
		try {
			//noinspection unchecked,
			return (T) f.get(DEFAULT_SETTINGS);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
