package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeImpl extends OneTwelveTwoAutoThirdPerson {
	public Configuration forgeConfig; //public for the gui factory !
	
	private final KeyBinding TOGGLE_MOD = new KeyBinding(
		"autothirdperson.toggle",
		KeyConflictContext.IN_GAME,
		0,
		"key.categories.misc"
	);
	
	public ForgeImpl(FMLPreInitializationEvent e) {
		this.forgeConfig = new Configuration(e.getSuggestedConfigurationFile());
	}
	
	@Override
	public void init() {
		super.init();
		MinecraftForge.EVENT_BUS.register(this);
		
		ClientRegistry.registerKeyBinding(TOGGLE_MOD);
	}
	
	@Override
	public CookedConfig makeConfig(ConfigSchema s) {
		return new VintageForgeCookedConfig(s, forgeConfig);
	}
	
	@Override
	public boolean modEnableToggleKeyPressed() {
		return TOGGLE_MOD.isKeyDown();
	}
	
	//frog events
	
	//config reloading WITH CONFIG GUI........ I MISS THE CONFIG GUI SO MUCH DUDE....
	@SubscribeEvent
	public void onConfigChange(ConfigChangedEvent e) {
		if(e.getModID().equals(MODID)) {
			refreshConfig();
		}
	}
	
	@SubscribeEvent
	public void onFrame(TickEvent.RenderTickEvent e) {
		if(e.phase != TickEvent.Phase.START) return;
		
		renderClient();
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
