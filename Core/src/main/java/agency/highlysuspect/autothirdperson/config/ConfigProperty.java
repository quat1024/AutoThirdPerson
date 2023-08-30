package agency.highlysuspect.autothirdperson.config;

import org.jetbrains.annotations.Nullable;

public interface ConfigProperty<T> {
	String name();
	@Nullable String comment();
	
	T defaultValue();
	boolean showDefaultValue();
	
	String write(T thing);
	T read(String s) throws Exception;
	void validate(T thing) throws Exception;
}