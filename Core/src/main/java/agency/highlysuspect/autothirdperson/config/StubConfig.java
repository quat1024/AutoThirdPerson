package agency.highlysuspect.autothirdperson.config;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;

public class StubConfig implements CookedConfig {
	@Override
	public <T> T get(ConfigProperty<T> prop) {
		AutoThirdPerson.instance.logger.error("Accessing stub config - this is a bug, config access happened before loading it!", new Throwable());
		return prop.defaultValue();
	}
	
	@Override
	public <T> T getOr(ConfigProperty<T> prop, T def) {
		if(prop == null) return def;
		else return get(prop);
	}
	
	@Override
	public void refresh() throws Exception {
	
	}
}
