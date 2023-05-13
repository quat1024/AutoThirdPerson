package agency.highlysuspect.autothirdperson.forge;

/**
 * Class originally from SLF4J. Its license is reproduced in SLF4J_LICENSE.txt.
 * 
 * Things have been inlined and cut-down significantly.
 */
public class SLF4J_MessageFormatter {
	static final char DELIM_START = '{';
	static final String DELIM_STR = "{}";
	private static final char ESCAPE_CHAR = '\\';
	
	public static String format(String messagePattern, Object... args) {
		if(messagePattern == null) return null;
		
		int i = 0;
		int j;
		StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);
		
		for(int arg = 0; arg < args.length; arg++) {
			
			j = messagePattern.indexOf(DELIM_STR, i);
			
			if (j == -1) {
				// no more variables
				if (i == 0) { // this is a simple string
					return messagePattern;
				} else { // add the tail string which contains no variables and return
					// the result.
					sbuf.append(messagePattern, i, messagePattern.length());
					return sbuf.toString();
				}
			} else {
				if (isEscapedDelimeter(messagePattern, j)) {
					if (!isDoubleEscaped(messagePattern, j)) {
						arg--; // DELIM_START was escaped, thus should not be incremented
						sbuf.append(messagePattern, i, j - 1);
						sbuf.append(DELIM_START);
						i = j + 1;
					} else {
						// The escape character preceding the delimiter start is
						// itself escaped: "abc x:\\{}"
						// we have to consume one backward slash
						sbuf.append(messagePattern, i, j - 1);
						sbuf.append(args[arg] == null ? "null" : args[arg]);
						i = j + 2;
					}
				} else {
					// normal case
					sbuf.append(messagePattern, i, j);
					sbuf.append(args[arg] == null ? "null" : args[arg]);
					i = j + 2;
				}
			}
		}
		// append the characters following the last {} pair.
		sbuf.append(messagePattern, i, messagePattern.length());
		return sbuf.toString();
	}
	
	static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {
		if (delimeterStartIndex == 0) {
			return false;
		}
		char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
		return potentialEscape == ESCAPE_CHAR;
	}
	
	static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
		return delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR;
	}
	
	//Original SLF4J class: NormalizedParameters
	public static Throwable getThrowableCandidate(final Object[] argArray) {
		if (argArray == null || argArray.length == 0) return null;
		
		final Object lastEntry = argArray[argArray.length - 1];
		return lastEntry instanceof Throwable ? (Throwable) lastEntry : null;
	}
}
