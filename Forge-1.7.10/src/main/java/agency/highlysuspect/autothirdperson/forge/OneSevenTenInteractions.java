package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.LoaderInteraction;
import agency.highlysuspect.autothirdperson.MinecraftInteraction;
import agency.highlysuspect.autothirdperson.MyCameraType;
import agency.highlysuspect.autothirdperson.MyLogger;
import agency.highlysuspect.autothirdperson.Vehicle;
import agency.highlysuspect.autothirdperson.VehicleClassification;
import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class OneSevenTenInteractions implements LoaderInteraction, MinecraftInteraction {
	public OneSevenTenInteractions(FMLPreInitializationEvent e) {
		this.logger = new Log4jMyLogger(LogManager.getLogger(AutoThirdPerson.MODID));
		this.forgeConfig = new Configuration(new File(e.getModConfigurationDirectory(), "auto_third_person.cfg"));
		this.client = Minecraft.getMinecraft();
		if(client == null) throw new NullPointerException("Minecraft.getMinecraft() == null");
	}
	
	private final MyLogger logger;
	private final Minecraft client;
	private final List<Runnable> clientTickers = new ArrayList<Runnable>();
	private final OneSevenTenState myState = new OneSevenTenState();
	
	private final Configuration forgeConfig;
	private VintageForgeSettings settings;
	
	//tired: making a settings gui for your mod
	//inspired:
	@SubscribeEvent
	public void openGui(GuiOpenEvent e) {
		if(e.gui instanceof GuiIngameModOptions || e.gui instanceof GuiControls) {
			try {
				loadSettings();
			} catch (Exception mmm) { mmm.printStackTrace(); }
		}
	}
	
	@SubscribeEvent
	public void tick(TickEvent.ClientTickEvent e) {
		if(e.phase != TickEvent.Phase.START) return;
		
		//atp core's client tickers
		for(Runnable clientTicker : clientTickers) {
			clientTicker.run();
		}
	}
	
	@SubscribeEvent
	public void frame(TickEvent.RenderTickEvent e) {
		if(e.phase != TickEvent.Phase.START) return;
		
		if(!safeToTick()) {
			myState.reset();
			return;
		}
		
		//Track changes to vehicle (forge doesn't have mount/dismount events yet)
		Entity currentVehicle = client.thePlayer.ridingEntity;
		Entity lastVehicle = myState.lastVehicleWeak.get();
		if(currentVehicle != lastVehicle) { //(object identity compare)
			//If you were riding something last tick, you no longer are
			if(lastVehicle != null) AutoThirdPerson.instance.dismount(new EntityVehicle(lastVehicle));
			
			//If you are riding something this tick, start riding it
			if(currentVehicle != null) AutoThirdPerson.instance.mount(new EntityVehicle(currentVehicle));
			
			//and update the state
			myState.lastVehicleWeak = new WeakReference<Entity>(currentVehicle);
		}
	}
	
	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent e) {
		//terrible mcp name - "check if it's pressed without consuming the click", here
		if(client.gameSettings.keyBindTogglePerspective.getIsKeyPressed()) {
			AutoThirdPerson.instance.manualPress();
		}
		
		//The key input event is fired right after handling keyBindTogglePerspective in vanilla.
		//Instead of using atp core modifyCycle let's handle it here
		if(settings.skipFrontView() && client.gameSettings.thirdPersonView == 2) {
			AutoThirdPerson.instance.debugSpam("Skipping third-person reversed view");
			client.gameSettings.thirdPersonView = 0;
		}
	}
	
	//TODO: readd config option again
	@SubscribeEvent
	public void onRenderHand(RenderHandEvent e) {
		if(client.thePlayer == null) return;
		
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
	
	private void loadSettings() {
		settings = new VintageForgeSettings(forgeConfig, AutoThirdPerson.instance.buildSettingsSpec());
	}
	
	private static class OneSevenTenState {
		WeakReference<Entity> lastVehicleWeak = new WeakReference<Entity>(null);
		
		void reset() {
			lastVehicleWeak.clear();
		}
	}
	
	private static class EntityVehicle implements Vehicle {
		public EntityVehicle(Entity ent) {
			this.ent = new WeakReference<Entity>(ent);
			this.id = ent == null ? "<nothing>" : EntityList.getEntityString(ent);
			
			if(ent instanceof EntityMinecart) this.type = VehicleClassification.MINECART;
			else if(ent instanceof EntityBoat) this.type = VehicleClassification.BOAT;
			else if(ent instanceof EntityAnimal) this.type = VehicleClassification.ANIMAL;
			else this.type = VehicleClassification.OTHER;
		}
		
		private final WeakReference<Entity> ent;
		private final String id;
		private final VehicleClassification type;
		
		@Override
		public boolean stillExists() {
			Entity entity = ent.get();
			return entity != null && entity.isEntityAlive();
		}
		
		@Override
		public @NotNull String id() {
			return id;
		}
		
		@Override
		public @NotNull VehicleClassification classification() {
			return type;
		}
		
		@Override
		public boolean vehicleEquals(Vehicle other) {
			if(!(other instanceof EntityVehicle)) return false;
			Entity myEntity = ent.get();
			Entity otherEntity = ((EntityVehicle) other).ent.get();
			return myEntity != null && myEntity == otherEntity;
		}
	}
	
	//implementing the core:
	
	@Override
	public void init() {
		//"Events silently failing when registered to the wrong bus" is nothing new
		//Party like it's 1.18.2!!!
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		
		loadSettings();
	}
	
	@Override
	public AtpSettings settings() {
		return settings;
	}
	
	@Override
	public void registerClientTicker(Runnable action) {
		clientTickers.add(action);
	}
	
	@Override
	public MyLogger getLogger() {
		return logger;
	}
	
	@Override
	public MyCameraType getCameraType() {
		return MyCameraType.values()[MathHelper.clamp_int(client.gameSettings.thirdPersonView, 0, 2)];
	}
	
	@Override
	public void setCameraType(MyCameraType type) {
		client.gameSettings.thirdPersonView = type.ordinal();
	}
	
	@Override
	public boolean debugScreenUp() {
		return client.gameSettings.showDebugInfo;
	}
	
	@Override
	public boolean safeToTick() {
		return client.thePlayer != null && client.theWorld != null && !client.isGamePaused();
	}
	
	@Override
	public boolean playerIsUnderwater() {
		return safeToTick() && client.thePlayer.isInsideOfMaterial(Material.water);
	}
	
	//unsupported things
	
	@Override
	public boolean hasElytra() {
		return false;
	}
	
	@Override
	public boolean hasSwimmingAnimation() {
		return false;
	}
	
	@Override
	public boolean playerIsElytraFlying() {
		return false;
	}
	
	@Override
	public boolean playerInSwimmingAnimation() {
		return false;
	}
	
	private static class Log4jMyLogger implements MyLogger {
		public Log4jMyLogger(Logger logger) {
			this.logger = logger;
		}
		
		private final Logger logger;
		
		@Override
		public void info(String msg, Object... args) {
			logger.info(msg, args);
		}
		
		@Override
		public void warn(String msg, Object... args) {
			logger.warn(msg, args);
		}
		
		@Override
		public void error(String msg, Throwable err) {
			logger.error(msg, err);
		}
	}
}
