package agency.highlysuspect.autothirdperson;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface LoaderInteraction<CMDSOURCE> {
	Path getConfigDir();
	void registerClientTicker(Consumer<Minecraft> action);
	
	void registerResourceReloadListener(Runnable action);
	void registerClientReloadCommand(LiteralArgumentBuilder<CMDSOURCE> command);
	
	//So Fabric has a FabricClientCommandSource, but it doesn't actually extend CommandSource, so it's different and bad.
	//We need to interact with commands at arm's length, then.
	LiteralArgumentBuilder<CMDSOURCE> literal(String lit);
	Consumer<Component> getFeedbackSender(CMDSOURCE cmdSource);
}
