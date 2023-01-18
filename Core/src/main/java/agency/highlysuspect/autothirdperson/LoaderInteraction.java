package agency.highlysuspect.autothirdperson;

/**
 * Facade that abstracts the differences between mod loaders.
 */
public interface LoaderInteraction {
	/**
	 * called right after setting up AutoThirdPerson. atp.instance is available.
	 * It's ok to not use this, but :Core uses java 6 and default methods are not supported ;) 
	 */
	void init();
	
	/**
	 * @return The current settings.
	 *         If this loader can automatically reload settings, this should return the most up-to-date copy of them.
	 *         If it can't, this should return the most up-to-date copy after a manual reload step, or after game startup, or whatever.
	 *         Don't return `null`; there's a default setting object in AtpSettings.
	 */
	AtpSettings settings();
	
	/**
	 * Tell the loader to call this Runnable every frame.
	 */
	void registerClientTicker(Runnable action);
}
