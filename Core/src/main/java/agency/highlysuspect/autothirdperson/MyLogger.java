package agency.highlysuspect.autothirdperson;

/** We have Log4j at home */
public interface MyLogger {
	void info(String msg, Object... args);
	
	void warn(String msg, Object... args);
	
	void error(String msg, Throwable err);
}
