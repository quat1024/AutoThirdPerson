package agency.highlysuspect.autothirdperson.config;

public interface CookedConfig {
	<T> T get(ConfigProperty<T> prop);
	<T> T getOr(ConfigProperty<T> prop, T def);
	void refresh() throws Exception;
}
