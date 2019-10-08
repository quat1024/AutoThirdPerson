package quaternary.thirdpersonboat;

import me.paulf.wings.server.flight.Flight;
import me.paulf.wings.server.flight.Flights;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WingsCompat {
	public static void preinit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(WingsCompat.class);
	}
	
	private static int wingsFlyingTicks;
	
	@SubscribeEvent
	public static void onTick(TickEvent.ClientTickEvent e) {
		if(e.phase != TickEvent.Phase.START || !AutoThirdPerson.ModConfig.compat.WINGS) return;
		
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.isGamePaused()) return;
		
		EntityPlayer player = mc.player;
		if(player == null) return;
		
		Flight f = Flights.get(player);
		boolean isWingsFlying = f != null && f.isFlying();
		
		if(isWingsFlying) {
			if(wingsFlyingTicks == AutoThirdPerson.ModConfig.compat.wingsFlyingTickDelay) {
				AutoThirdPerson.enterThirdPerson();
			}
			
			wingsFlyingTicks++;
		} else {
			if(wingsFlyingTicks != 0){
				AutoThirdPerson.leaveThirdPerson();
			}
			
			wingsFlyingTicks = 0;
		}
	}
}
