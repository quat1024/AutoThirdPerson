package agency.highlysuspect.autothirdperson;

public interface LoaderInteraction {
	AtpSettings settings();
	
	void registerClientTicker(Runnable action);
}
