package agency.highlysuspect.autothirdperson;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ConfigShape {
	public sealed interface Element permits Heading, Option {}
	public static record Heading(String name) implements Element {}
	public static record Option<T>(String key, T defaultValue, List<String> comment, Function<String, T> parser, Function<T, String> writer, Field field) implements Element {
		@SuppressWarnings("unchecked")
		String getAndWriteErased(Object pojo) {
			try {
				return writer.apply((T) field.get(pojo));
			} catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
		}
	}
	
	public static class ConfigParseException extends RuntimeException {
		public ConfigParseException(String message) { super(message); }
		public ConfigParseException(String message, Throwable cause) { super(message, cause); }
	}
	
	private final List<Element> elements = new ArrayList<>();
	private final Map<String, Option<?>> optionsByName = new HashMap<>();
	
	public void add(Element element) {
		elements.add(element);
		if(element instanceof Option opt) optionsByName.put(opt.key, opt);
	}
	
	// writing and reading into a pojo //
	
	@SuppressWarnings("DuplicateExpressions")
	public List<String> write(Object pojo) {
		List<String> lines = new ArrayList<>();
		for(Element e : elements) {
			if(e instanceof Heading h) {
				lines.add("#".repeat(h.name.length() + 6));
				lines.add("## " + h.name + " ##");
				lines.add("#".repeat(h.name.length() + 6));
			} else if(e instanceof Option<?> opt) {
				opt.comment.forEach(commentLine -> lines.add("# " + commentLine));
				lines.add(opt.key + " = " + opt.getAndWriteErased(pojo));
			}
			lines.add("");
		}
		lines.remove(lines.size() - 1); //snip extra blank line
		return lines;
	}
	
	public <P> P readInto(List<String> file, P pojo) {
		for(int line = 0; line < file.size(); line++) {
			try {
				String s = file.get(line).trim();
				if(s.isBlank() || s.startsWith("#")) continue;
				
				//Split
				int eqIndex = s.indexOf('=');
				if(eqIndex == -1) throw new ConfigParseException("there is no equal sign to split a key/value pair.");
				
				//Parse key
				String keyStr = s.substring(0, eqIndex).trim();
				Option<?> opt = optionsByName.get(keyStr);
				//Good place to extend if you want more functionality: config upgrade system instead of crashing lol
				if(opt == null) throw new ConfigParseException("there is no option named '" + keyStr + "'.");
				
				//Parse value
				String valueStr = s.substring(eqIndex + 1).trim();
				Object result;
				try {
					result = opt.parser.apply(valueStr);
				} catch (RuntimeException e) {
					throw new ConfigParseException("unable to parse '" + valueStr + "' for option " + keyStr + ".", e);
				}
				
				//Store
				opt.field.set(pojo, result);
			} catch (ConfigParseException e) {
				//Include the line number, also hopefully funge into something that will be of more value to nonprogrammers
				throw new ConfigParseException("On line " + line + ", " + e.getMessage(), e.getCause());
			} catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
		}
		return pojo;
	}
	
	// file handling //
	
	public void writeFile(Path path, Object pojo) throws IOException {
		Files.createDirectories(path.getParent());
		Files.write(path, write(pojo));
	}
	
	public <P> P readFromOrCreateFile(Path path, P pojoContainingDefaultValues) throws IOException {
		P result = Files.exists(path) ? readInto(Files.readAllLines(path), pojoContainingDefaultValues) : pojoContainingDefaultValues;
		writeFile(path, result); //always write the file back. ensures all the comments etc are updated
		return result;
	}
	
	// annotation magic //
	
	//Easily the most kludgy part. You could (theoretically) create a ConfigShape by repeatedly calling `add` yourself.
	//A bunch of people, including me, like being able to make them from annotations though.
	//This could be made less messy/special-casey but it'd be more code.
	
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Section { String value(); }
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface SkipDefault {}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Comment { String[] value(); }
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface AtLeast { int value(); }
	
	public static ConfigShape createFromPojo(Object pojoContainingDefaultSettings) {
		ConfigShape shape = new ConfigShape();
		try {
			for(Field field : pojoContainingDefaultSettings.getClass().getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
				field.setAccessible(true);
				String key = field.getName();
				Object defaultValue = field.get(pojoContainingDefaultSettings);
				
				//Writer (good place to extend if you want more functionality)
				Function<Object, String> writer = Object::toString;
				
				//Section headings. With this annotation scheme, they go before the first field of the section.
				//Headings are purely for decoration and do not create a new namespace of config values
				Section headingAnnotation = field.getAnnotation(Section.class);
				if(headingAnnotation != null) shape.add(new Heading(headingAnnotation.value()));
				
				//Comment, including mention of the default value
				List<String> comment = new ArrayList<>();
				Comment commentAnnotation = field.getAnnotation(Comment.class);
				if(commentAnnotation != null) comment.addAll(List.of(commentAnnotation.value()));
				if(field.getAnnotation(SkipDefault.class) == null) comment.add("Default: " + writer.apply(defaultValue));
				
				//Parser (good place to extend if you want more functionality)
				Function<String, ?> parser;
				if(field.getType() == String.class) parser = String::toString; //Function.identity() doesn't typecheck lol
				else if(field.getType() == Pattern.class) parser = Pattern::compile;
				else if(field.getType() == Boolean.class || field.getType() == boolean.class) parser = Boolean::parseBoolean;
				else if(field.getType() == Integer.class || field.getType() == int.class) parser = Integer::parseInt;
				else throw new RuntimeException("Missing parser for type " + field.getType());
				
				//Constraints and parse-time validation (another good place to extend)
				AtLeast atLeastAnnotation = field.getAnnotation(AtLeast.class);
				if(atLeastAnnotation != null) comment.add("Must be at least " + atLeastAnnotation.value() + ".");
				
				if(atLeastAnnotation != null) {
					int min = atLeastAnnotation.value();
					//noinspection unchecked
					parser = ((Function<String, Integer>) parser).andThen(x -> {
						if(min > x) throw new ConfigParseException("The value for this option must be at least " + min + ", but it is set to " + x + ".");
						return x;
					});
				}
				
				//noinspection unchecked
				shape.add(new Option<>(key, defaultValue, comment, (Function<String, Object>) parser, writer, field));
			}
		} catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
		return shape;
	}
}
