package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.XplatStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Consumer;

@Mod(AutoThirdPerson.MODID)
public class ForgeEntrypoint {
	public ForgeEntrypoint() {
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> NiceClientEntrypointDude::niceClientEntrypointDude);
	}
	
	private static class NiceClientEntrypointDude {
		private static void niceClientEntrypointDude() {
			AutoThirdPerson.INSTANCE = new AutoThirdPerson(new XplatStuff() {
				@Override
				public Path getConfigDir() {
					return FMLPaths.CONFIGDIR.get();
				}
				
				@Override
				public void registerResourceReloadListener(Runnable action) {
					//On Forge this event happens after the initial reload or something.
					//I load the config in this event so without calling it immediately it just doesnt get loaded.
					//When the abstraction is leaky :sus:
					action.run();
					
					MinecraftForge.EVENT_BUS.addListener((RegisterClientReloadListenersEvent e) -> e.registerReloadListener((ResourceManagerReloadListener) mgr -> action.run()));
				}
				
				@Override
				public void registerClientReloadCommand(Runnable leakyAbstraction) {
					MinecraftForge.EVENT_BUS.addListener((RegisterClientCommandsEvent e) ->
						e.getDispatcher().register(
							Commands.literal(AutoThirdPerson.MODID).then(
								Commands.literal("reload").executes(
									ctx -> {
										leakyAbstraction.run();
										ctx.getSource().sendSuccess(new TextComponent("Reloaded config file"), false);
										return 0;
									}))));
				}
				
				@Override
				public void registerClientTicker(Consumer<Minecraft> action) {
					MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
						if(e.phase == TickEvent.Phase.START) { //Nice api dummy
							action.accept(Minecraft.getInstance());
						}
					});
				}
			});
		}
	}
}
