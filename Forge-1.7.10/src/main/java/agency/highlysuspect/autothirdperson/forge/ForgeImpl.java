package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;

import java.lang.ref.WeakReference;

public class ForgeImpl extends OneSevenTenAutoThirdPerson {
	private final Configuration forgeConfig;
	private final KeyBinding TOGGLE_MOD = new KeyBinding(
		"autothirdperson.toggle",
		0,
		"key.categories.misc"
	);
	
	public ForgeImpl(FMLPreInitializationEvent e) {
		this.forgeConfig = new Configuration(e.getSuggestedConfigurationFile());
	}
	
	@Override
	public State makeState() {
		return new OneSevenTenState();
	}
	
	@Override
	public CookedConfig makeConfig(ConfigSchema s) {
		return new VintageForgeCookedConfig(s, forgeConfig);
	}
	
	@Override
	public void init() {
		super.init();
		
		//"Events silently failing when registered to the wrong bus" is nothing new
		//Party like it's 1.18.2!!!
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		
		ClientRegistry.registerKeyBinding(TOGGLE_MOD);
	}
	
	@Override
	public boolean modEnableToggleKeyPressed() {
		return TOGGLE_MOD.getIsKeyPressed();
	}
	
	//frog events
	
	//tired: making a settings gui for your mod
	//wired:
	private Long lastConfigReloadTimeLol = System.currentTimeMillis();
	@SubscribeEvent
	public void loadWorld(WorldEvent.Load e) {
		if(System.currentTimeMillis() - lastConfigReloadTimeLol > 3000L) {
			lastConfigReloadTimeLol = System.currentTimeMillis();
			refreshConfig();
		}
	}
	
	//inspired:
	@SubscribeEvent
	public void openGui(GuiOpenEvent e) {
		if(e.gui instanceof GuiIngameModOptions || e.gui instanceof GuiControls) {
			try {
				refreshConfig();
			} catch (Exception mmm) { mmm.printStackTrace(); }
		}
	}
	
	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent e) {
		if(e.phase != TickEvent.Phase.START) return;
		
		tickClient();
	}
	
	@SubscribeEvent
	public void frame(TickEvent.RenderTickEvent e) {
		if(e.phase != TickEvent.Phase.START) return;
		
		if(!safeToTick()) {
			state.reset();
			return;
		}
		
		renderClient();
		
		//TODO: push this up into a generic
		OneSevenTenState myState = (OneSevenTenState) state;
		
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
	}
	
	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent e) {
		//terrible mcp name - "check if it's pressed without consuming the click", here
		if(client.gameSettings.keyBindTogglePerspective.getIsKeyPressed()) {
			manualPress();
		}
	}
	
	@SubscribeEvent
	public void onRenderHand(RenderHandEvent e) {
		if(client.thePlayer == null || !config.get(opts.FIX_HAND_GLITCH)) return;
		
		Render entityRenderer = RenderManager.instance.getEntityRenderObject(client.thePlayer);
		if(entityRenderer instanceof RenderPlayer) {
			//The hand glitch is caused by some entity renderer setting this to "true", but hand rendering code doesn't
			//reset it to "false" ever. If the last rendered ModelBiped was riding something, you get the hand glitch.
			//This is why pressing F5 in a boat causes the hand glitch (you render yourself riding the boat), and in singleplayer
			//leaving the boat doesn't fix the hand glitch until you press F5 or flash your inventory screen, which are both things
			//that render a standing-up player.
			ModelBiped biped = ((RenderPlayer) entityRenderer).modelBipedMain;
			if(biped != null) biped.isRiding = false;
		}
	}
	
	private static class OneSevenTenState extends State {
		WeakReference<Entity> lastVehicleWeak = new WeakReference<Entity>(null);
		
		@Override
		public void reset() {
			super.reset();
			lastVehicleWeak.clear();
		}
	}
}
