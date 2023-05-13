package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.forge.slf4j_borrowed.FormattingTuple;
import agency.highlysuspect.autothirdperson.forge.slf4j_borrowed.MessageFormatter;
import agency.highlysuspect.autothirdperson.wrap.MyCameraType;
import agency.highlysuspect.autothirdperson.wrap.MyLogger;
import cpw.mods.fml.common.FMLLog;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForgeImpl extends OneFourSevenAutoThirdPerson {
	private final Configuration forgeConfig;
	private AtpSettings settings;
	
	public ForgeImpl(FMLPreInitializationEvent e) {
		this.forgeConfig = new Configuration(e.getSuggestedConfigurationFile());
	}
	
	@Override
	public MyLogger makeLogger() {
		final Logger log = Logger.getLogger(NAME);
		log.setParent(FMLLog.getLogger()); //Required or nothing will log !
		
		return new MyLogger() {
			@Override
			public void info(String msg, Object... args) {
				//FMLLogFormatter doesn't support parameters. Manually format the message using a backport of some SLF4J stuff.
				FormattingTuple fmt = MessageFormatter.format(msg, args);
				log.log(Level.INFO, fmt.getMessage(), fmt.getThrowable());
			}
			
			@Override
			public void warn(String msg, Object... args) {
				FormattingTuple fmt = MessageFormatter.format(msg, args);
				log.log(Level.WARNING, fmt.getMessage(), fmt.getThrowable());
			}
			
			@Override
			public void error(String msg, Throwable err) {
				log.log(Level.SEVERE, msg, err);
			}
		};
	}
	
	@Override
	public State makeState() {
		return new OneFourSevenState();
	}
	
	@Override
	public void init() {
		super.init();
		
		MinecraftForge.EVENT_BUS.register(this);
		
		settings = new VintageForgeSettings(forgeConfig, buildSettingsSpec());
		
		//The old ticking system is completely bizarre
		TickRegistry.registerTickHandler(new WeirdTickerThing(), Side.CLIENT);
	}
	
	@Override
	public AtpSettings settings() {
		return settings;
	}
	
	//frog events
	
	//subscribed via the tick handler
	private void frame() {
		if(!safeToTick()) {
			state.reset();
			return;
		}
		
		//TODO: push this up into a generic
		OneFourSevenState myState = (OneFourSevenState) state;
		
		//Handle the "Skip front view" setting (I don't think there's keyboard events?)
		if(settings().skipFrontView() && client.gameSettings.thirdPersonView == 2) {
			debugSpam("Skipping third-person reversed view");
			client.gameSettings.thirdPersonView = 0;
		}
		
		//Track whether you manually pressed F5 (same, I don't think there's keyboard events yet)
		if(client.gameSettings.thirdPersonView != myState.expectedThirdPersonView) {
			manualPress();
		}
		
		//Track changes to vehicle (forge doesn't have mount/dismount events yet)
		Entity currentVehicle = client.thePlayer.ridingEntity;
		Entity lastVehicle = myState.lastVehicleWeak.get();
		if(currentVehicle != lastVehicle) { //(object identity compare)
			//If you were riding something last tick, you no longer are
			if(lastVehicle != null) dismount(new EntityVehicle(lastVehicle));
			
			//If you are riding something this tick, start riding it
			if(currentVehicle != null) mount(new EntityVehicle(currentVehicle));
			
			//and update the state
			myState.lastVehicleWeak = new WeakReference<Entity>(currentVehicle);
		}
		
		//Handle "sneak-to-dismount"
		if(true && //TODO: add config option
			myState.wasSneaking &&
			client.thePlayer.isSneaking() &&
			currentVehicle != null &&
			//Kludgy timer fixes bugs while holding shift?
			//Not happy about using a timer to fix this, but I'm really not sure what causes it
			//Adding a clientside unmountEntity call makes it worse lol
			(System.currentTimeMillis() - myState.lastSneakDismountTime > 750))
		{
			//func_78768_b -> "interactEntity" or something. Sends a packet like you right clicked on the entity
			Minecraft.getMinecraft().playerController.func_78768_b(client.thePlayer, currentVehicle);
			myState.lastSneakDismountTime = System.currentTimeMillis();
		}
		myState.wasSneaking = client.thePlayer.isSneaking();
	}
	
	@ForgeSubscribe
	public void renderLast(RenderWorldLastEvent e) {
		if(!settings().fixHandGlitch() || client.thePlayer == null) return;
		
		//Fired immediately before hand rendering. Search `dispatchRenderLast` in EntityRenderer (took me like
		//5 years to find for some reason). This is our chance to make a move, soooo lucky about this event firing time
		RenderPlayer renderPlayer = (RenderPlayer) RenderManager.instance.getEntityRenderObject(Minecraft.getMinecraft().thePlayer);
		ModelBiped modelBipedMain = ObfuscationReflectionHelper.getPrivateValue(RenderPlayer.class, renderPlayer, 0); //please work
		modelBipedMain.isRiding = false; //Firstperson hand never intended to be drawn in the riding pose.
		//Now see ItemRenderer#renderItemInFirstPerson, which calls RenderPlayer#func_82441_a which actually draws the hand.
		//It will call setRotationAngles which will pick up on the now-false value of `isRiding`.
	}
	
	//In 1.7 i cheat this with GuiOpenEvent but that doesn't exist on 1.4 actually lmao.
	//This might be okay, it will catch logging out and back in
	private Long lastConfigReloadTimeLol = System.currentTimeMillis();
	@ForgeSubscribe
	public void worldLoad(WorldEvent.Load e) {
		if(System.currentTimeMillis() - lastConfigReloadTimeLol > 3000L) {
			lastConfigReloadTimeLol = System.currentTimeMillis();
			logger.info(NAME + ": hackily reloading config");
			settings = new VintageForgeSettings(forgeConfig, buildSettingsSpec());
		}
	}
	
	//Bit of a kludge: there's no keyboard events I can find in this version, so the "manual f5 press"
	//detector uses a mismatch between the current third-person-view setting and this
	//expectedThirdPersonView variable. We need to update it when we automatically change the camera
	//so it doesn't trip that detector
	@Override
	public void setCameraType(MyCameraType type) {
		super.setCameraType(type);
		((OneFourSevenState) state).expectedThirdPersonView = type.ordinal();
	}
	
	private static class OneFourSevenState extends State {
		WeakReference<Entity> lastVehicleWeak = new WeakReference<Entity>(null);
		int expectedThirdPersonView;
		boolean wasSneaking;
		long lastSneakDismountTime;
		
		@Override
		public void reset() {
			super.reset();
			
			lastVehicleWeak.clear();
			expectedThirdPersonView = Minecraft.getMinecraft().gameSettings.thirdPersonView;
			wasSneaking = false;
			lastSneakDismountTime = 0;
		}
	}
	
	private class WeirdTickerThing implements ITickHandler {
		private final EnumSet<TickType> ticks = EnumSet.of(TickType.CLIENT, TickType.RENDER);
		
		@Override
		public void tickStart(EnumSet<TickType> types, Object... args) {
			if(types.contains(TickType.RENDER)) frame(); //called every frame
			else if(types.contains(TickType.CLIENT)) tickClient(); //called 20 times per second
		}
		
		@Override
		public void tickEnd(EnumSet<TickType> types, Object... args) {
			//Don't care.
		}
		
		@Override
		public EnumSet<TickType> ticks() {
			return ticks;
		}
		
		@Override
		public String getLabel() {
			return "Auto Third Person client ticker";
		}
	}
}
