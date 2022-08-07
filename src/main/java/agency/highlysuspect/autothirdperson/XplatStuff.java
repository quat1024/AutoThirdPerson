package agency.highlysuspect.autothirdperson;

import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface XplatStuff {
	Path getConfigDir();
	
	void registerResourceReloadListener(Runnable action);
	
	//So Fabric has a FabricClientCommandSource but it doesn't actually extend CommandSource, so it's different and bad.
	//I can't figure out how to apply a mapping function to the output of a Command so anything that requires touching it,
	//such as sending command feedback, will have to be duplicated. Sorry.
	void registerClientReloadCommand(Runnable leakyAbstraction);
	
	void registerClientTicker(Consumer<Minecraft> action);
}
