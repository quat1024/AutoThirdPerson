package agency.highlysuspect.autothirdperson.neo;

import agency.highlysuspect.autothirdperson.TwentyOneFiveAutoThirdPerson;
import agency.highlysuspect.autothirdperson.config.ConfigSchema;
import agency.highlysuspect.autothirdperson.config.CookedConfig;
import agency.highlysuspect.autothirdperson.wrap.Vehicle;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

public class NeoImpl extends TwentyOneFiveAutoThirdPerson {
	private final KeyMapping TOGGLE_MOD = new KeyMapping(
		"autothirdperson.toggle",
		KeyConflictContext.IN_GAME,
		InputConstants.UNKNOWN,
		"key.categories.misc"
	);
	
	public NeoImpl(ModContainer modContainer, IEventBus modBus) {
		this.modContainer = modContainer;
		this.modBus = modBus;
	}
	
	private final ModContainer modContainer;
	private final IEventBus modBus;
	
	public void init() {
		super.init();
		
		modBus.addListener(this::configLoad);
		NeoForge.EVENT_BUS.addListener(this::onTick);
		NeoForge.EVENT_BUS.addListener(this::onFrame);
		NeoForge.EVENT_BUS.addListener(this::onKey);
		NeoForge.EVENT_BUS.addListener(this::onMountOrDismount);
		
		modBus.addListener((RegisterKeyMappingsEvent e) -> e.register(TOGGLE_MOD));
	}
	
	@Override
	public CookedConfig makeConfig(ConfigSchema s) {
		NeoCookedConfig f = new NeoCookedConfig(s);
		modContainer.registerConfig(ModConfig.Type.CLIENT, f.modSpec);
		return f;
	}
	
	@Override
	public boolean modEnableToggleKeyPressed() {
		return TOGGLE_MOD.isDown() && TOGGLE_MOD.getKeyModifier().isActive(KeyConflictContext.IN_GAME);
	}
	
	public void configLoad(ModConfigEvent e) {
		if(e.getConfig().getModId().equals(MODID)) {
			logger.info("Cooking Auto Third Person config...");
			refreshConfig();
			logger.info("...done.");
		}
	}
	
	public void onTick(ClientTickEvent.Pre e) {
		tickClient();
	}
	
	public void onFrame(RenderFrameEvent.Pre e) {
		renderClient();
	}
	
	public void onKey(InputEvent.Key e) {
		if(client().options.keyTogglePerspective.isDown()) {
			manualPress();
		}
	}
	
	public void onMountOrDismount(EntityMountEvent e) {
		if(e.getEntity() != client().player || e.getEntityMounting() == null) return;
		
		Vehicle v = new EntityVehicle(e.getEntityBeingMounted());
		
		if(e.isDismounting()) dismount(v);
		else mount(v);
	}
}
