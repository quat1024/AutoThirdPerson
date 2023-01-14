package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.LoaderInteraction;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ForgeLoaderInteraction implements LoaderInteraction<CommandSourceStack> {
	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}
	
	@Override
	public void registerClientTicker(Consumer<Minecraft> action) {
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
			if(e.phase == TickEvent.Phase.START) { //Nice api dummy
				action.accept(Minecraft.getInstance());
			}
		});
	}
	
	@Override
	public void registerResourceReloadListener(Runnable action) {
		//On Forge this event happens after the initial reload or something. Fabric happens before.
		//so to achieve the same behavior i need to run it immediately.
		action.run();
		MinecraftForge.EVENT_BUS.addListener((RegisterClientReloadListenersEvent e) -> e.registerReloadListener((ResourceManagerReloadListener) mgr -> action.run()));
	}
	
	@Override
	public void registerClientReloadCommand(LiteralArgumentBuilder<CommandSourceStack> command) {
		MinecraftForge.EVENT_BUS.addListener((RegisterClientCommandsEvent e) -> e.getDispatcher().register(command));
	}
	
	@Override
	public LiteralArgumentBuilder<CommandSourceStack> literal(String lit) {
		return Commands.literal(lit);
	}
	
	@Override
	public Consumer<Component> getFeedbackSender(CommandSourceStack cmdSource) {
		return c -> cmdSource.sendSuccess(c, false);
	}
}
