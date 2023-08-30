package agency.highlysuspect.autothirdperson.forge;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class GuiFactory implements IModGuiFactory {
	@Override
	public void initialize(Minecraft minecraftInstance) {
		//Rarrrr!
	}
	
	@Override
	public boolean hasConfigGui() {
		return true;
	}
	
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		//adapted from Choonster's TestMod3, or so the story goes (i found it in my old mod Botania Tweaks)
		Configuration c = ((ForgeImpl) AutoThirdPerson.instance).forgeConfig;
		List<IConfigElement> elements = c.getCategoryNames().stream()
			.filter(name -> !c.getCategory(name).isChild())
			.filter(name -> !"uncategorized".equalsIgnoreCase(name)) //Only the `configVersion` property is in there, which I do not want to expose
			.map(name -> new ConfigElement(c.getCategory(name)))
			.sorted(Comparator.comparing(IConfigElement::getName).reversed()) //reverse-alphabetize (happens to be the ordering i want anyway)
			.collect(Collectors.toList());
		
		return new GuiConfig(parentScreen, elements, AutoThirdPerson.MODID, false, false, AutoThirdPerson.NAME + " Config!");
	}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		//unused by Forge even
		return null;
	}
}
