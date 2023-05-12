package agency.highlysuspect.autothirdperson;

public class VersionCapabilities {
	public VersionCapabilities(boolean hasElytra, boolean hasSwimmingAnimation, boolean hasHandGlitch, boolean hasSneakDismount) {
		this.hasElytra = hasElytra;
		this.hasSwimmingAnimation = hasSwimmingAnimation;
		this.hasHandGlitch = hasHandGlitch;
		this.hasSneakDismount = hasSneakDismount;
	}
	
	public final boolean hasElytra;
	public final boolean hasSwimmingAnimation;
	public final boolean hasHandGlitch;
	public final boolean hasSneakDismount;
	
	public static class Builder {
		private boolean hasElytra;
		private boolean hasSwimmingAnimation;
		private boolean hasHandGlitch;
		private boolean hasSneakDismount;
		
		public Builder hasElytra(boolean hasElytra) {
			this.hasElytra = hasElytra;
			return this;
		}
		
		public Builder hasSwimmingAnimation(boolean hasSwimmingAnimation) {
			this.hasSwimmingAnimation = hasSwimmingAnimation;
			return this;
		}
		
		public Builder hasHandGlitch(boolean hasHandGlitch) {
			this.hasHandGlitch = hasHandGlitch;
			return this;
		}
		
		public Builder hasSneakDismount(boolean hasSneakDismount) {
			this.hasSneakDismount = hasSneakDismount;
			return this;
		}
		
		public VersionCapabilities build() {
			return new VersionCapabilities(hasElytra, hasSwimmingAnimation, hasHandGlitch, hasSneakDismount);
		}
	}
}
