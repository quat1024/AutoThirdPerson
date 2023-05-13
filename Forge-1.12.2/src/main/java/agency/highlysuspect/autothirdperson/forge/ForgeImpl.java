package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeImpl extends OneTwelveTwoAutoThirdPerson {
	final Configuration forgeConfig; //public for the gui factory !
	private VintageForgeSettings settings;
	
	public ForgeImpl(FMLPreInitializationEvent e) {
		this.forgeConfig = new Configuration(e.getSuggestedConfigurationFile());
	}
	
	@Override
	public void init() {
		super.init();
		MinecraftForge.EVENT_BUS.register(this);
		settings = new VintageForgeSettings(forgeConfig, buildSettingsSpec());
	}
	
	@Override
	public AtpSettings settings() {
		return settings;
	}
	
	//frog events
	
	//config reloading WITH CONFIG GUI........ I MISS THE CONFIG GUI SO MUCH DUDE....
	@SubscribeEvent
	public void onConfigChange(ConfigChangedEvent e) {
		if(e.getModID().equals(MODID)) {
			//bit of a sloppy call?
			//This event is fired after changing the config in-memory, but before writing it back to disk.
			//VintageForgeSettings constructor then calls config.load(), stomping over all the user's changes.
			//So uh, I can... simply save the file so the subsequent load works? lol
			forgeConfig.save();
			settings = new VintageForgeSettings(forgeConfig, buildSettingsSpec());
		}
	}
	
	@SubscribeEvent
	public void onFrame(TickEvent.RenderTickEvent e) {
		if(e.phase != TickEvent.Phase.START) return;
		
		//We could handle this in the key input event, but the event is fired disconcertingly late.
		//It renders the third-person reversed view for a *handful* of frames before switching. I don't like it.
		if(settings.skipFrontView() && client.gameSettings.thirdPersonView == 2) {
			debugSpam("Skipping third-person reversed view");
			client.gameSettings.thirdPersonView = 0;
		}
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent e) {
		if(e.phase != TickEvent.Phase.START) return;
		
		tickClient();
	}
	
	@SubscribeEvent
	public void onMountOrDismount(EntityMountEvent e) {
		if(e.getEntityMounting() != client.player || e.getEntityBeingMounted() == null) return;
		
		Vehicle v = new EntityVehicle(e.getEntityBeingMounted());
		
		if(e.isDismounting()) dismount(v);
		else mount(v);
	}
	
	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent e) {
		if(client.gameSettings.keyBindTogglePerspective.isKeyDown()) { //"check if it's pressed without consuming the click" method
			manualPress();
		}
	}
}
