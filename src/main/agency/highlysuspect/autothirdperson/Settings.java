package agency.highlysuspect.autothirdperson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Settings {
	@Comment("Automatically go into third person when riding a boat?")
	public boolean boat = true;
	@Comment("Automatically go into third person when riding a minecart?")
	public boolean cart = true;
	@Comment("Automatically go into third person when riding an animal?")
	public boolean animal = true;
	@Comment("Automatically go into third person when flying an elytra?")
	public boolean elytra = true;
	@LineBreak
	@Comment("Ticks of elytra flight required before the camera automatically toggles.")
	public int elytraDelay = 7;
	@LineBreak
	@Comment("Go back into first-person when dismounting?")
	public boolean autoRestore = true;
	@Comment("Manually toggling camera perspective won't automatically return the camera to first-person next time you dismount.")
	public boolean cancelAutoRestore = true;
	@LineBreak
	@Comment("Skip the 'third-person front' camera mode when pressing F5?")
	public boolean skipFrontView = false;
	@Comment("Dump a bunch of debug crap into the log, might be handy.")
	public boolean logSpam = false;
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface LineBreak {}
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Comment {
		String value();
	}
	
	public static Settings readOrDefault(Path path) {
		AutoThirdPerson.LOGGER.info("Loading settings...");
		if(Files.exists(path)) {
			try {
				return read(path);
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
	
	public static Settings read(Path path) throws IOException, ParseException {
		Settings settings = new Settings();
		int lineCount = 0;
		
		for(String line : Files.readAllLines(path)) {
			++lineCount;
			line = line.trim();
			if(!line.startsWith("#") && !line.isEmpty()) {
				String[] split = line.split("=", 2);
				if(split.length != 2) {
					throw new ParseException("Can't parse line " + lineCount + " in the config file: " + line);
				}
				
				split[0] = split[0].trim();
				split[1] = split[1].trim();
				Field field = getFieldOrNull(split[0]);
				if(field == null) {
					throw new ParseException("No config field named " + split[0] + " referenced on line " + lineCount);
				}
				
				Object yahoo;
				
				if(field.getType() == Boolean.TYPE) yahoo = Boolean.parseBoolean(split[1]);
				else if(field.getType() == Integer.TYPE) yahoo = parseInt(split[1]);
				else throw new ParseException("Should be impossible but if you see this quat's a dork and forgot to add a deserializer, go yell at it to add one");
				
				setFieldVerySafe(settings, field, yahoo);
			}
		}
		
		return settings;
	}
	
	public void write(Path path) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		
		for(Field f : Settings.class.getDeclaredFields()) {
			if(f.isAnnotationPresent(LineBreak.class)) {
				writer.newLine();
			}
			
			if(f.isAnnotationPresent(Comment.class)) {
				writer.write("# ");
				writer.write(f.getAnnotation(Comment.class).value());
				writer.newLine();
			}
			
			writer.write(f.getName());
			writer.write(" = ");
			
			try {
				writer.write(f.get(this).toString());
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
			
			writer.newLine();
		}
		
		writer.flush();
	}
	
	private static Field getFieldOrNull(String name) {
		try {
			return Settings.class.getField(name);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}
	
	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException var2) {
			throw new Settings.ParseException("Can't parse this integer", var2);
		}
	}
	
	private static void setFieldVerySafe(Settings settings, Field field, Object value) {
		try {
			field.set(settings, value);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Oops", e);
		}
	}
	
	private static class ParseException extends RuntimeException {
		public ParseException(String message) {
			super(message);
		}
		
		public ParseException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
