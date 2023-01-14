package agency.highlysuspect.autothirdperson;

public interface LoaderInteraction {
	default void init() {}
	
	AtpSettings settings();
	
	void registerClientTicker(Runnable action);
}
