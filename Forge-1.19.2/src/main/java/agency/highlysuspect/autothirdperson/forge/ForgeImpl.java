package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AtpSettings;
import agency.highlysuspect.autothirdperson.NineteenTwoAutoThirdPerson;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForgeImpl extends NineteenTwoAutoThirdPerson {
	private UncookedForgeSettings uncookedForgeSettings;
	private AtpSettings cookedForgeSettings = AtpSettings.MISSING;
	
	@Override
	public void init() {
		super.init();
		
		uncookedForgeSettings = new UncookedForgeSettings(buildSettingsSpec());
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, uncookedForgeSettings.forgeSpec);
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::configLoad);
		MinecraftForge.EVENT_BUS.addListener(this::onTick);
		MinecraftForge.EVENT_BUS.addListener(this::onFrame);
		MinecraftForge.EVENT_BUS.addListener(this::onKey);
		MinecraftForge.EVENT_BUS.addListener(this::onMountOrDismount);
	}
	
	@Override
	public AtpSettings settings() {
		return cookedForgeSettings;
	}
	
	public void configLoad(ModConfigEvent e) {
		if(e.getConfig().getModId().equals(MODID)) {
			logger.info("Cooking Auto Third Person config...");
			cookedForgeSettings = new CookedForgeSettings(uncookedForgeSettings);
			logger.info("...done.");
		}
	}
	
	public void onTick(TickEvent.ClientTickEvent e) {
		if(e.phase == TickEvent.Phase.START) tickClient();
	}
	
	public void onFrame(TickEvent.RenderTickEvent e) {
		if(e.phase == TickEvent.Phase.START) renderClient();
	}
	
	public void onKey(InputEvent.Key e) {
		if(client.options.keyTogglePerspective.isDown()) {
			manualPress();
		}
	}
	
	public void onMountOrDismount(EntityMountEvent e) {
		if(e.getEntity() != client.player || e.getEntityMounting() == null) return;
		
		Vehicle v = new EntityVehicle(e.getEntityBeingMounted());
		
		if(e.isDismounting()) dismount(v);
		else mount(v);
	}
}
