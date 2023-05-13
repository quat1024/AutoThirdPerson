package agency.highlysuspect.autothirdperson.wrap;

/** Wrapper around the vanilla camera type enum */
public enum MyCameraType {
	/** First person */
	FIRST_PERSON,
	
	/** usual third person, facing the back of the player's head */
	THIRD_PERSON,
	
	/** flipped third person, facing the front */
	THIRD_PERSON_REVERSED;
	
	//Generally i'm worried about like, a hypothetical mod that adds a fourth camera type?
	//which is why im not throwing IllegalArgumentException or something.
	public static MyCameraType wrapFromInt(int i) {
		if(i == 1) return THIRD_PERSON;
		else if(i == 2) return THIRD_PERSON_REVERSED;
		else return FIRST_PERSON;
	}
	
	//Minecraft's enum "just so happens" to be in the same order as mine
	public static MyCameraType wrapFromEnum(Enum<?> e) {
		return wrapFromInt(e.ordinal());
	}
}
