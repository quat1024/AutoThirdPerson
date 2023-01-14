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
			return 69420;
		}
		
		@Override
		public boolean boat() {
			return true;
		}
		
		@Override
		public boolean cart() {
			return true;
		}
		
		@Override
		public boolean animal() {
			return true;
		}
		
		@Override
		public boolean elytra() {
			return true;
		}
		
		@Override
		public boolean swim() {
			return true;
		}
		
		@Override
		public boolean custom() {
			return true;
		}
		
		@Override
		public boolean useIgnore() {
			return false;
		}
		
		@Override
		public int elytraDelay() {
			return 5;
		}
		
		@Override
		public int swimmingDelayStart() {
			return 10;
		}
		
		@Override
		public int swimmingDelayEnd() {
			return 10;
		}
		
		@Override
		public boolean stickySwim() {
			return true;
		}
		
		@Override
		public Pattern customPattern() {
			return Pattern.compile("lkajsdklajsldksa");
		}
		
		@Override
		public Pattern ignorePattern() {
			return Pattern.compile("lkajsdklajsldksa");
		}
		
		@Override
		public boolean autoRestore() {
			return true;
		}
		
		@Override
		public boolean cancelAutoRestore() {
			return true;
		}
		
		@Override
		public boolean skipFrontView() {
			return true;
		}
		
		@Override
		public boolean logSpam() {
			return true;
		}
	};
}
