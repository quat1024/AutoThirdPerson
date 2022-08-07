package agency.highlysuspect.autothirdperson;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.event.ForgeSubscribe;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.regex.Pattern;

public class AutoThirdPerson {
	public Configuration config;
	public Settings settings;
	public State state;
	
	public void clientPreinit(FMLPreInitializationEvent e) {
		this.config = new Configuration(new File(e.getModConfigurationDirectory(), "auto_third_person.conf"));
		loadSettings();
		this.state = new State();
		
		//odd!
		TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
	}
	
	private void loadSettings() {
		config.load();
		this.settings = new Settings(config);
		config.save();
	}
	
	public class TickHandler implements ITickHandler {
		private final EnumSet<TickType> types = EnumSet.of(TickType.RENDER);
		
		@Override
		public void tickStart(EnumSet<TickType> enumSet, Object... objects) {
			if(Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) {
				state.reset();
				return;
			}
			
			//Handle the "Skip front view" setting
			if(settings.skipFrontView && Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
				Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
			}
			
			//Track whether you manually pressed f5
			if(Minecraft.getMinecraft().gameSettings.thirdPersonView != state.expectedThirdPersonView) {
				f5Press();
			}
			
			//Track changes to your vehicle
			Entity currentVehicle = Minecraft.getMinecraft().thePlayer.ridingEntity;
			Entity lastVehicle = state.lastVehicleWeak.get();
			if(currentVehicle != lastVehicle) { //(object identity compare)
				//If you were riding something last tick, you now no longer count as riding that
				if(lastVehicle != null) mountOrDismount(lastVehicle, false);
				
				//If you are riding something this tick, start riding it
				if(currentVehicle != null) mountOrDismount(currentVehicle, true);
				
				//and update the weak reference
				state.lastVehicleWeak = new WeakReference<Entity>(currentVehicle);
			}
		}
		
		@Override
		public void tickEnd(EnumSet<TickType> enumSet, Object... objects) {
			//Nothing to do. Idk why forge tick events are always so weird
		}
		
		@Override
		public EnumSet<TickType> ticks() {
			return types;
		}
		
		@Override
		public String getLabel() {
			return "Auto Third Person client ticker";
		}
	}
	
	@ForgeSubscribe
	public void renderLast(RenderWorldLastEvent e) {
		if(!settings.fixHandGlitch || Minecraft.getMinecraft().thePlayer == null) return;
		
		//Fired immediately before hand rendering. Search `dispatchRenderLast` in EntityRenderer (took me like
		//5 years to find for some reason). This is our chance to make a move, soooo lucky about this event firing time
		RenderPlayer renderPlayer = (RenderPlayer) RenderManager.instance.getEntityRenderObject(Minecraft.getMinecraft().thePlayer);
		ModelBiped modelBipedMain = ObfuscationReflectionHelper.getPrivateValue(RenderPlayer.class, renderPlayer, 0); //please work
		modelBipedMain.isRiding = false; //Firstperson hand never intended to be drawn in the riding pose.
		//Now see ItemRenderer#renderItemInFirstPerson, which calls RenderPlayer#func_82441_a which actually draws the hand.
		//It will call setRotationAngles which will pick up on the now-false value of `isRiding`.
	}
	
	public void debugSpam(String yeah) {
		if(settings.logSpam) Entrypoint.LOGGER.info(yeah);
	}
	
	public void mountOrDismount(Entity vehicle, boolean mounting) {
		Minecraft client = Minecraft.getMinecraft();
		if(client.theWorld == null || client.thePlayer == null || vehicle == null) return;
		
		String entityId = EntityList.getEntityString(vehicle);
		debugSpam((mounting ? "Mounting " : "Dismounting ") + entityId);
		
		if(settings.useIgnore && settings.ignorePattern.matcher(entityId).matches()) {
			debugSpam("Ignoring, since it matches the ignore pattern '" + settings.ignorePattern + "'.");
			return;
		}
		
		boolean doIt = false;
		if(settings.boat && vehicle instanceof EntityBoat) {
			debugSpam("This is a boat!");
			doIt = true;
		}
		if(settings.cart && vehicle instanceof EntityMinecart) {
			debugSpam("This is a minecart!");
			doIt = true;
		}
		if(settings.animal && vehicle instanceof EntityAnimal) {
			debugSpam("This is an animal!");
			doIt = true;
		}
		if(settings.custom && settings.customPattern.matcher(entityId).matches()) {
			debugSpam("This matches the pattern '" + settings.customPattern + "'!");
			doIt = true;
		}
		
		if(doIt) {
			if(mounting) enterThirdPerson(vehicle);
			else leaveThirdPerson(vehicle);
		}
	}
	
	public void f5Press() {
		if(settings.cancelAutoRestore && state.active) {
			state.active = false;
			debugSpam("Cancelling auto-restore, if it was about to happen");
		}
	}
	
	public void enterThirdPerson(Entity reason) {
		Minecraft client = Minecraft.getMinecraft();
		
		//If you're in first person, enter third person
		if(client.gameSettings.thirdPersonView == 0) {
			state.active = true;
			client.gameSettings.thirdPersonView = 1;
			state.expectedThirdPersonView = 1;
			debugSpam("Automatically entering third person due to mounting " + reason);
		}
	}
	
	public void leaveThirdPerson(Entity previouslyRiding) {
		Minecraft client = Minecraft.getMinecraft();
		
		if(!settings.autoRestore) {
			debugSpam("Not automatically leaving third person - auto restore is turned off");
			return;
		}
		
		if(!state.active) {
			debugSpam("Not automatically leaving third person - cancelled or inactive");
			return;
		}
		
		debugSpam("Automatically leaving third person due to dismounting " + previouslyRiding);
		client.gameSettings.thirdPersonView = 0;
		state.expectedThirdPersonView = 0;
		state.active = false;
	}
	
	public static class State {
		public boolean active = false;
		
		//This version of Forge doesn't have entity mount events. I need to manually check what entity you
		//were riding every tick and watch for when it changes.
		private WeakReference<Entity> lastVehicleWeak = new WeakReference<Entity>(null);
		
		//I'm also not sure what the best way to check whether f5 was pressed is. It's not a real keybind yet.
		public int expectedThirdPersonView;
		
		public void reset() {
			active = false;
			if(lastVehicleWeak.get() != null) lastVehicleWeak = new WeakReference<Entity>(null);
			expectedThirdPersonView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
		}
	}
	
	public static class Settings {
		public Settings(Configuration config) {
			boat = config.get("Scenarios", "boat", true, "Automatically go into third person when riding a boat?").getBoolean(true);
			cart = config.get("Scenarios", "cart", true, "Automatically go into third person when ridign a minecart?").getBoolean(true);
			animal = config.get("Scenarios", "animal", true, "Automatically go into third person when riding an animal?").getBoolean(true);
			
			custom = config.get("Scenarios", "custom", false, "If 'true', the customPattern will be used, and riding anything matching it will toggle third person.").getBoolean(false);
			useIgnore = config.get("Scenarios", "useIgnore", false, "If 'true', the ignorePattern will be used, and anything matching it will be ignored.").getBoolean(false);
			
			customPattern = Pattern.compile(config.get("ScenarioOptions", "customPattern", "^asdfgh$", "Entity IDs that match this regular expression will be considered if the 'custom' option is enabled.").value);
			ignorePattern = Pattern.compile(config.get("ScenarioOptions", "ignorePattern", "^sdaldjalksd$", "Entity IDs that match this regular expression will be ignored if the 'useIgnore' option is enabled.").value);
			
			autoRestore = config.get("Restoration", "autoRestore", true, "When the situation that Auto Third Person put you into third person for is over, the camera will be restored back to the way it was.").getBoolean(true);
			cancelAutoRestore = config.get("Restoration", "cancelAutoRestore", true, "If 'true', pressing f5 after mounting something will prevent your camera from being automatically restored to first-person when you dismount.").getBoolean(true);
			
			skipFrontView = config.get("Extras", "skipFrontView", false, "Skip the 'third-person front' camera mode when pressing F5.").getBoolean(false);
			fixHandGlitch = config.get("Extras", "fixHandGlitch", true, "Fix the annoying 'weirdly rotated first-person hand' rendering error when you ride or look at someone riding a vehicle.").getBoolean(true);
			logSpam = config.get("Extras", "logSpam", false, "Dump a bunch of debug crap into the log. Might be handy!").getBoolean(false);
		}
		
		//Scenarios
		
		public boolean boat;
		public boolean cart;
		public boolean animal;
		//public boolean elytra;
		//public boolean swim;
		
		public boolean custom;
		public boolean useIgnore;
		
		//Scenario options
		
		//public int elytraDelay;
		//public int swimmingDelayStart;
		//public int swimmingDelayEnd;
		//public boolean stickySwim;
		
		public Pattern customPattern;
		public Pattern ignorePattern;
		
		//Restoration
		
		public boolean autoRestore;
		public boolean cancelAutoRestore;
		
		//Extra
		
		public boolean skipFrontView;
		public boolean fixHandGlitch;
		public boolean logSpam;
	}
}
