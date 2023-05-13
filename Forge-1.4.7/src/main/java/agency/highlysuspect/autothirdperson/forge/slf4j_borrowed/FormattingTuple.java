package agency.highlysuspect.autothirdperson.forge.slf4j_borrowed;

public class FormattingTuple {
	private final String message;
	private final Throwable throwable;
	
	public FormattingTuple(String message) {
		this(message, null);
	}
	
	public FormattingTuple(String message, Throwable throwable) {
		this.message = message;
		this.throwable = throwable;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Throwable getThrowable() {
		return throwable;
	}
}