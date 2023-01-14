package agency.highlysuspect.autothirdperson;

import java.util.regex.Pattern;

public interface AtpSettings {
	int configVersion();
	
	//scenarios
	boolean boat();
	boolean cart();
	boolean animal();
	boolean elytra();
	boolean swim();
	boolean custom();
	boolean useIgnore();
	
	//scenario options
	int elytraDelay();
	int swimmingDelayStart();
	int swimmingDelayEnd();
	boolean stickySwim();
	Pattern customPattern();
	Pattern ignorePattern();
	
	//restoration
	boolean autoRestore();
	boolean cancelAutoRestore();
	
	//extra
	boolean skipFrontView();
	boolean logSpam();
	
	AtpSettings DEFAULT_TODO = new AtpSettings() {
		@Override
		public int configVersion() {
			return 0;
		}
		
		@Override
		public boolean boat() {
			return false;
		}
		
		@Override
		public boolean cart() {
			return false;
		}
		
		@Override
		public boolean animal() {
			return false;
		}
		
		@Override
		public boolean elytra() {
			return false;
		}
		
		@Override
		public boolean swim() {
			return false;
		}
		
		@Override
		public boolean custom() {
			return false;
		}
		
		@Override
		public boolean useIgnore() {
			return false;
		}
		
		@Override
		public int elytraDelay() {
			return 0;
		}
		
		@Override
		public int swimmingDelayStart() {
			return 0;
		}
		
		@Override
		public int swimmingDelayEnd() {
			return 0;
		}
		
		@Override
		public boolean stickySwim() {
			return false;
		}
		
		@Override
		public Pattern customPattern() {
			return Pattern.compile("default auto third person settings object - custom pattern");
		}
		
		@Override
		public Pattern ignorePattern() {
			return Pattern.compile("default auto third person settings object - ignore pattern");
		}
		
		@Override
		public boolean autoRestore() {
			return false;
		}
		
		@Override
		public boolean cancelAutoRestore() {
			return false;
		}
		
		@Override
		public boolean skipFrontView() {
			return false;
		}
		
		@Override
		public boolean logSpam() {
			return false;
		}
	};
}
