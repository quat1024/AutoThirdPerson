package agency.highlysuspect.autothirdperson;

public class VersionCapabilities {
	public VersionCapabilities(boolean hasElytra, boolean hasSwimmingAnimation, boolean hasHandGlitch, boolean noSneakDismount) {
		this.hasElytra = hasElytra;
		this.hasSwimmingAnimation = hasSwimmingAnimation;
		this.hasHandGlitch = hasHandGlitch;
		this.noSneakDismount = noSneakDismount;
	}
	
	//features
	public final boolean hasElytra;
	public final boolean hasSwimmingAnimation;
	
	//antifeatures
	public final boolean hasHandGlitch;
	public final boolean noSneakDismount;
	
	public static class Builder {
		private boolean hasElytra;
		private boolean hasSwimmingAnimation;
		private boolean hasHandGlitch;
		private boolean noSneakDismount;
		
		public Builder hasElytra() {
			this.hasElytra = true;
			return this;
		}
		
		public Builder hasSwimmingAnimation() {
			this.hasSwimmingAnimation = true;
			return this;
		}
		
		public Builder hasHandGlitch() {
			this.hasHandGlitch = true;
			return this;
		}
		
		public Builder noSneakDismount() {
			this.noSneakDismount = true;
			return this;
		}
		
		public VersionCapabilities build() {
			return new VersionCapabilities(hasElytra, hasSwimmingAnimation, hasHandGlitch, noSneakDismount);
		}
	}
}
