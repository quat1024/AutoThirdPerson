package agency.highlysuspect.autothirdperson;

import com.google.common.base.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Settings {
	@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"}) // >:(
	private int configVersion = 0;
	
	@Section("Scenarios")
	@Comment("Automatically go into third person when riding a boat?")
	public boolean boat = true;
	@Comment("Automatically go into third person when riding a minecart?")
	public boolean cart = true;
	@Comment("Automatically go into third person when riding an animal?")
	public boolean animal = true;
	@Comment("Automatically go into third person when flying an elytra?")
	public boolean elytra = true;
	@LineBreak
	@Section("Scenario Options")
	@Comment("Ticks of elytra flight required before the camera automatically toggles.")
	public int elytraDelay = 7;
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
				s.update();
				return s;
			} catch (ParseException | IOException var3) {
				AutoThirdPerson.LOGGER.info("Could not read or parse config file", var3);
				AutoThirdPerson.LOGGER.info("Using default config");
				return new Settings();
			}
		} else {
			Settings s = new Settings();
			
			try {
				AutoThirdPerson.LOGGER.info("Creating default settings...");
				s.write(path);
			} catch (IOException var4) {
				AutoThirdPerson.LOGGER.info("Could not write default config file", var4);
			}
			
			return s;
		}
	}
	
	private void update() {
		if(configVersion > 0) {
			throw new ParseException("This config file is from the future!");
		}
	}
	
	public static Settings read(Path path) throws IOException, ParseException {
		Settings settings = new Settings();
		int lineCount = 0;
		
		for(String line : Files.readAllLines(path)) {
			++lineCount;
			
			line = line.trim();
			if(line.startsWith("#") || line.isEmpty()) continue;
			
			String[] split = line.split("=", 2);
			if(split.length != 2) {
				continue;
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
			else throw new ParseException("Should be impossible but if you see this quat's a dork and forgot to add a deserializer, go yell at it to add one!!");
			
			try {
				field.set(settings, yahoo);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Can't set field for some reason", e);
			}
		}
		
		return settings;
	}
	
	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException var2) {
			throw new Settings.ParseException("Can't parse this integer", var2);
		}
	}
	
	public void write(Path path) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		
		for(Field f : Settings.class.getDeclaredFields()) {
			if(f.isAnnotationPresent(LineBreak.class)) {
				writer.newLine();
			}
			
			if(f.isAnnotationPresent(Section.class)) {
				String header = f.getAnnotation(Section.class).value();
				String hashes = Strings.repeat("#", header.length() + 4);
				writer.write(hashes);
				writer.newLine();
				writer.write("# " + header + " #");
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
				writer.write(f.get(this).toString());
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Can't read field to serialize to config file, for some reason", e);
			}
			
			writer.newLine();
		}
		
		writer.flush();
	}
	
	private static class ParseException extends RuntimeException {
		public ParseException(String message) {
			super(message);
		}
		
		public ParseException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface LineBreak {}
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Comment {
		String[] value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Section {
		String value();
	}
}
