package agency.highlysuspect.autothirdperson.forge.slf4j_borrowed;

public class NormalizedParameters {
	
	final String message;
	final Object[] arguments;
	final Throwable throwable;
	
	public NormalizedParameters(String message, Object[] arguments, Throwable throwable) {
		this.message = message;
		this.arguments = arguments;
		this.throwable = throwable;
	}
	
	public NormalizedParameters(String message, Object[] arguments) {
		this(message, arguments, null);
	}
	
	public String getMessage() {
		return message;
	}
	
	public Object[] getArguments() {
		return arguments;
	}
	
	public Throwable getThrowable() {
		return throwable;
	}
	
	public static Throwable getThrowableCandidate(final Object[] argArray) {
		if (argArray == null || argArray.length == 0) {
			return null;
		}
		
		final Object lastEntry = argArray[argArray.length - 1];
		if (lastEntry instanceof Throwable) {
			return (Throwable) lastEntry;
		}
		
		return null;
	}
	
	public static Object[] trimmedCopy(final Object[] argArray) {
		if (argArray == null || argArray.length == 0) {
			throw new IllegalStateException("non-sensical empty or null argument array");
		}
		
		final int trimmedLen = argArray.length - 1;
		
		Object[] trimmed = new Object[trimmedLen];
		
		if (trimmedLen > 0) {
			System.arraycopy(argArray, 0, trimmed, 0, trimmedLen);
		}
		
		return trimmed;
	}
	
	public static NormalizedParameters normalize(String msg, Object[] arguments, Throwable t) {
		
		if (t != null) {
			return new NormalizedParameters(msg, arguments, t);
		}
		
		if (arguments == null || arguments.length == 0) {
			return new NormalizedParameters(msg, arguments, t);
		}
		
		Throwable throwableCandidate = NormalizedParameters.getThrowableCandidate(arguments);
		if (throwableCandidate != null) {
			Object[] trimmedArguments = trimmedCopy(arguments);
			return new NormalizedParameters(msg, trimmedArguments, throwableCandidate);
		} else {
			return new NormalizedParameters(msg, arguments);
		}
		
	}
//	
//	public static NormalizedParameters normalize(LoggingEvent event) {
//		return normalize(event.getMessage(), event.getArgumentArray(), event.getThrowable());
//	}
//	
}