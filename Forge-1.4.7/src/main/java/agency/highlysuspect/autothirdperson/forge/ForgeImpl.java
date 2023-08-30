package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import agency.highlysuspect.autothirdperson.wrap.MyLogger;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
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
	private final KeyBinding TOGGLE_MOD = new KeyBinding("autothirdperson.toggle", 0);
	
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
				//FMLLogFormatter doesn't support parameters, so manually format the message using a backport of some SLF4J stuff.
				log.log(Level.INFO, SLF4J_MessageFormatter.format(msg, args), SLF4J_MessageFormatter.getThrowableCandidate(args));
			}
			
			@Override
			public void warn(String msg, Object... args) {
				log.log(Level.WARNING, SLF4J_MessageFormatter.format(msg, args), SLF4J_MessageFormatter.getThrowableCandidate(args));
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
	public CookedConfig makeConfig(ConfigSchema s) {
		return new VintageForgeCookedConfig(s, forgeConfig);
	}
	
	@Override
	public boolean modEnableToggleKeyPressed() {
		return TOGGLE_MOD.pressed;
	}
	
	@Override
	public void init() {
		super.init();
		
		MinecraftForge.EVENT_BUS.register(this);
		
		//The old ticking system is completely bizarre
		TickRegistry.registerTickHandler(new WeirdTickerThing(), Side.CLIENT);
		
		LanguageRegistry.instance().loadLocalization("/assets/autothirdperson/lang/en_US.lang", "en_US", false);
		
		//This bizarre incantation is required to get the key onto the options screen without
		//manually System.arraycopying stuff onto GameSettings.keyBindings[]
		KeyBindingRegistry.registerKeyBinding(new KeyBindingRegistry.KeyHandler(new KeyBinding[] { TOGGLE_MOD }) {
			@Override
			public void keyDown(EnumSet<TickType> enumSet, KeyBinding keyBinding, boolean b, boolean b1) {
				//Dont care
			}
			
			@Override
			public void keyUp(EnumSet<TickType> enumSet, KeyBinding keyBinding, boolean b) {
				//Dont care
			}
			
			@Override
			public EnumSet<TickType> ticks() {
				return EnumSet.noneOf(TickType.class);
			}
			
			@Override
			public String getLabel() {
				return "auto third person key handler thing";
			}
		});
	}
	
	//frog events
	
	//subscribed via the tick handler
	@Override
	public void renderClient() {
		if(!safeToTick()) {
			state.reset();
			return;
		}
		
		//TODO: push this up into a generic
		OneFourSevenState myState = (OneFourSevenState) state;
		
		//Track whether you manually pressed F5 (There's no keyboard events yet)
		if(client.gameSettings.thirdPersonView != myState.expectedThirdPersonView) {
			manualPress();
		}
		
		super.renderClient(); //Handles skip-front-view
		
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
		if(config.get(opts.SNEAK_DISMOUNT_BACKPORT) &&
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
		if(!config.get(opts.FIX_HAND_GLITCH) || client.thePlayer == null) return;
		
		//Fired immediately before hand rendering. Search `dispatchRenderLast` in EntityRenderer (took me like
		//5 years to find for some reason). This is our chance to make a move, soooo lucky about this event firing time
		RenderPlayer renderPlayer = (RenderPlayer) RenderManager.instance.getEntityRenderObject(Minecraft.getMinecraft().thePlayer);
		ModelBiped modelBipedMain = ObfuscationReflectionHelper.getPrivateValue(RenderPlayer.class, renderPlayer, 0); //please work
		modelBipedMain.isRiding = false; //Firstperson hand never intended to be drawn in the riding pose.
		//Now see ItemRenderer#renderItemInFirstPerson, which calls RenderPlayer#func_82441_a which actually draws the hand.
		//It will call setRotationAngles which will pick up on the now-false value of `isRiding`.
	}
	
	private Long lastConfigReloadTimeLol = System.currentTimeMillis();
	@ForgeSubscribe
	public void worldLoad(WorldEvent.Load e) {
		if(System.currentTimeMillis() - lastConfigReloadTimeLol > 3000L) {
			lastConfigReloadTimeLol = System.currentTimeMillis();
			refreshConfig();
		}
	}
	
	//Bit of a kludge: there's no keyboard events I can find in this version, so the "manual f5 press"
	//detector uses a mismatch between the current third-person-view setting and this
	//expectedThirdPersonView variable. We need to update it when we automatically change the camera
	//so it doesn't trip that detector
	@Override
	public void setCameraType(int type) {
		super.setCameraType(type);
		((OneFourSevenState) state).expectedThirdPersonView = type;
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
			if(types.contains(TickType.RENDER)) renderClient(); //called every frame
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
