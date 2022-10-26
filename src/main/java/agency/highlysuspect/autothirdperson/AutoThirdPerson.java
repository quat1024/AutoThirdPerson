package agency.highlysuspect.autothirdperson;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

public class AutoThirdPerson {
	public Configuration config;
	public Settings settings;
	public State state;
	
	public void clientPreInit(FMLPreInitializationEvent e) {
		this.config = new Configuration(new File(e.getModConfigurationDirectory(), "auto_third_person.cfg"));
		loadSettings();
		this.state = new State();
		
		//"Events silently failing when registered to the wrong bus" is nothing new
		//Party like it's 1.18.2!!!
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}
	
	private void loadSettings() {
		config.load();
		this.settings = new Settings(config);
		config.save();
	}
	
	public void debugSpam(String yeah) {
		if(settings.logSpam) Entrypoint.LOGGER.info(yeah);
	}
	
	//ClientTickEvent is fired twenty times a second. This has disappointing performance when clicking on a boat.
	//Instead I will use this event, fired at the start of every frame before the camera is even set up.
	//This looks and feels great. I should go back in time and tell 1.12-era me about this event...
	@SubscribeEvent 
	public void frame(TickEvent.RenderTickEvent e) {
		if(e.phase != TickEvent.Phase.START) return;
		
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.thePlayer == null || mc.theWorld == null) {
			state.reset();
			return;
		}
		
		//Track changes to your vehicle
		Entity currentVehicle = mc.thePlayer.ridingEntity;
		Entity lastVehicle = state.lastVehicleWeak.get();
		if(currentVehicle != lastVehicle) { //(object identity compare)
			//If you were riding something last tick, you now no longer count as riding that
			if(lastVehicle != null) mountOrDismount(lastVehicle, false);
			
			//If you are riding something this tick, start riding it
			if(currentVehicle != null) mountOrDismount(currentVehicle, true);
			
			//and update the weak reference
			state.lastVehicleWeak = new WeakReference<>(currentVehicle);
		}
	}
	
	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent e) {
		GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
		
		//mcp moment: getIsKeyPressed is the "check if it's pressed without consuming the click" method
		if(gameSettings.keyBindTogglePerspective.getIsKeyPressed()) {
			if(settings.cancelAutoRestore && state.active) {
				state.active = false;
				debugSpam("Cancelling auto-restore, if it was about to happen");
			}
			
			//The key input event is fired right after handling keyBindTogglePerspective in vanilla.
			//Perfect place to handle it
			if(settings.skipFrontView && gameSettings.thirdPersonView == 2) {
				gameSettings.thirdPersonView = 0;
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderHand(RenderHandEvent e) {
		if(!settings.fixHandGlitch || Minecraft.getMinecraft().thePlayer == null) return;
		
		//The 1.4.7 build uses RenderWorldLastEvent, which is conveniently fired right before hand rendering.
		//That event timing still happens in 1.7.10, but this new hand event exists now; doesn't hurt to use the right one.
		Render entityRenderer = RenderManager.instance.getEntityRenderObject(Minecraft.getMinecraft().thePlayer);
		if(entityRenderer instanceof RenderPlayer) {
			//The hand glitch is caused by some entity renderer setting this to "true", but hand rendering code doesn't
			//reset it to "false" ever. If the last rendered ModelBiped was riding something, you get the hand glitch.
			//This is why pressing F5 in a boat causes the hand glitch (you render yourself riding the boat), and in singleplayer
			//leaving the boat doesn't fix the hand glitch until you press F5 or flash your inventory screen, which are both things
			//that render a standing-up player.
			ModelBiped biped = ((RenderPlayer) entityRenderer).modelBipedMain; //Not public in 1.4.7, but its public here ayoo 
			if(biped != null) biped.isRiding = false;
		}
	}
	
	private void mountOrDismount(Entity vehicle, boolean mounting) {
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
	
	public void enterThirdPerson(Entity reason) {
		Minecraft client = Minecraft.getMinecraft();
		
		//If you're in first person, enter third person
		if(client.gameSettings.thirdPersonView == 0) {
			state.active = true;
			client.gameSettings.thirdPersonView = 1;
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
		state.active = false;
	}
	
	public static class State {
		public boolean active = false;
		
		//This version of Forge doesn't have entity mount events. I need to manually check what entity you
		//were riding every tick and watch for when it changes.
		public WeakReference<Entity> lastVehicleWeak = new WeakReference<>(null);
		
		public void reset() {
			active = false;
			if(lastVehicleWeak.get() != null) lastVehicleWeak = new WeakReference<>(null);
		}
	}
	
	public static class Settings {
		public Settings(Configuration config) {
			boat = config.get("Scenarios", "boat", true, "Automatically go into third person when riding a boat?").getBoolean(true);
			cart = config.get("Scenarios", "cart", true, "Automatically go into third person when ridign a minecart?").getBoolean(true);
			animal = config.get("Scenarios", "animal", true, "Automatically go into third person when riding an animal?").getBoolean(true);
			
			custom = config.get("Scenarios", "custom", false, "If 'true', the customPattern will be used, and riding anything matching it will toggle third person.").getBoolean(false);
			useIgnore = config.get("Scenarios", "useIgnore", false, "If 'true', the ignorePattern will be used, and anything matching it will be ignored.").getBoolean(false);
			
			customPattern = Pattern.compile(config.get("ScenarioOptions", "customPattern", "^asdfgh$", "Entity IDs that match this regular expression will be considered if the 'custom' option is enabled.").getString());
			ignorePattern = Pattern.compile(config.get("ScenarioOptions", "ignorePattern", "^sdaldjalksd$", "Entity IDs that match this regular expression will be ignored if the 'useIgnore' option is enabled.").getString());
			
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
		
		public boolean custom;
		public boolean useIgnore;
		
		//Scenario options
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
