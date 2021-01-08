package agency.highlysuspect.autothirdperson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class Settings {
	@Settings.Comment("Automatically go into third person when riding a boat?")
	public boolean boat = true;
	@Settings.Comment("Automatically go into third person when riding a minecart?")
	public boolean cart = true;
	@Settings.Comment("Automatically go into third person when riding an animal?")
	public boolean animal = true;
	@Settings.Comment("Automatically go into third person when flying an elytra?")
	public boolean elytra = true;
	@Settings.LineBreak
	@Settings.Comment("Ticks of elytra flight required before the camera automatically toggles.")
	public int elytraDelay = 7;
	@Settings.LineBreak
	@Settings.Comment("Go back into first-person when dismounting?")
	public boolean autoRestore = true;
	@Settings.Comment(
		"Manually toggling camera perspective won't automatically return the camera to first-person next time you dismount.")
	public boolean cancelAutoRestore = true;
	@Settings.LineBreak
	@Settings.Comment("Skip the 'third-person front' camera mode when pressing F5?")
	public boolean skipFrontView = false;
	@Settings.Comment("Dump a bunch of debug crap into the log, might be handy.")
	public boolean logSpam = false;
	
	public static Settings readOrDefault(Path path) {
		AutoThirdPerson.LOGGER.info("Loading settings...");
		if(Files.exists(path, new LinkOption[0])) {
			try {
				return read(path);
			} catch (Settings.ParseException | IOException var3) {
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
	
	public static Settings read(Path path) throws IOException, Settings.ParseException {
		Settings settings = new Settings();
		int lineCount = 0;
		Iterator var3 = Files.readAllLines(path).iterator();
		
		while(var3.hasNext()) {
			String line = (String) var3.next();
			++lineCount;
			line = line.trim();
			if(!line.startsWith("#") && !line.isEmpty()) {
				String[] split = line.split("=", 2);
				if(split.length != 2) {
					throw new Settings.ParseException("Can't parse line " + lineCount + " in the config file: " + line);
				}
				
				split[0] = split[0].trim();
				split[1] = split[1].trim();
				Field field = getFieldOrNull(split[0]);
				if(field == null) {
					throw new Settings.ParseException("No config field named " + split[0] + " referenced on line " + lineCount);
				}
				
				Object yahoo;
				if(field.getType() == Boolean.TYPE) {
					yahoo = Boolean.parseBoolean(split[1]);
				} else {
					if(field.getType() != Integer.TYPE) {
						throw new Settings.ParseException("Should be impossible but if you see this quat's a dork and forgot to add a deserializer, go yell at it to add one");
					}
					
					yahoo = parseInt(split[1]);
				}
				
				setFieldVerySafe(settings, field, yahoo);
			}
		}
		
		return settings;
	}
	
	public void write(Path path) throws IOException {
		BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		Field[] var3 = Settings.class.getDeclaredFields();
		int var4 = var3.length;
		
		for(int var5 = 0; var5 < var4; ++var5) {
			Field f = var3[var5];
			if(f.isAnnotationPresent(Settings.LineBreak.class)) {
				writer.newLine();
			}
			
			if(f.isAnnotationPresent(Settings.Comment.class)) {
				String comment = ((Settings.Comment) f.getAnnotation(Settings.Comment.class)).value();
				writer.write("# ");
				writer.write(comment);
				writer.newLine();
			}
			
			writer.write(f.getName());
			writer.write(" = ");
			
			try {
				writer.write(f.get(this).toString());
			} catch (ReflectiveOperationException var8) {
				throw new RuntimeException(var8);
			}
			
			writer.newLine();
		}
		
		writer.flush();
	}
	
	private static Field getFieldOrNull(String name) {
		try {
			return Settings.class.getField(name);
		} catch (ReflectiveOperationException var2) {
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
		} catch (ReflectiveOperationException var4) {
			throw new RuntimeException("Oops", var4);
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
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface LineBreak {
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Comment {
		String value();
	}
}
