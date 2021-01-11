package agency.highlysuspect.autothirdperson.integration;

import agency.highlysuspect.autothirdperson.AutoThirdPerson;
import agency.highlysuspect.autothirdperson.Settings;
import joptsimple.internal.Strings;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.FieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextFieldBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ClothIntegration {
	public static Screen createConfigScreen(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create();
		builder.setParentScreen(parent);
		builder.setTitle(new LiteralText("Auto Third Person"));
		
		Supplier<ConfigCategory> cat = () -> builder.getOrCreateCategory(new LiteralText("Uncategorized"));
		
		for(Field f : Settings.class.getDeclaredFields()) {
			int mod = f.getModifiers();
			if(Modifier.isStatic(mod) || Modifier.isTransient(mod) || Modifier.isFinal(mod) || Modifier.isPrivate(mod)) continue;
			if(f.isAnnotationPresent(Settings.Hidden.class)) continue;
			
			if(f.isAnnotationPresent(Settings.Section.class)) {
				String sect = f.getAnnotation(Settings.Section.class).value();
				cat = () -> builder.getOrCreateCategory(new LiteralText(sect));
			}
			
			FieldBuilder<?, ?> fieldBuilder;
			Consumer<Text> tooltipSetter;
			
			if(f.getType() == Boolean.TYPE) {
				boolean defValue;
				try {
					defValue = f.getBoolean(AutoThirdPerson.SETTINGS);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
				
				BooleanToggleBuilder a = builder.entryBuilder()
					.startBooleanToggle(new LiteralText(f.getName()), defValue)
					.setDefaultValue((Boolean) Settings.getDefaultValue(f))
					.setSaveConsumer(b -> {
						try {
							f.set(AutoThirdPerson.SETTINGS, b);
						} catch (ReflectiveOperationException e) {
							throw new RuntimeException(e);
						}
					});
				
				fieldBuilder = a;
				tooltipSetter = a::setTooltip;
			} else if(f.getType() == Integer.TYPE) {
				int defValue;
				try {
					defValue = f.getInt(AutoThirdPerson.SETTINGS);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
				
				IntFieldBuilder a = builder.entryBuilder()
					.startIntField(new LiteralText(f.getName()), defValue)
					.setDefaultValue((Integer) Settings.getDefaultValue(f))
					.setSaveConsumer(b -> {
						try {
							f.set(AutoThirdPerson.SETTINGS, b);
						} catch (ReflectiveOperationException e) {
							throw new RuntimeException(e);
						}
					});
				
				fieldBuilder = a;
				tooltipSetter = a::setTooltip;
			} else if(f.getType() == Pattern.class) {
				Pattern defValue;
				try {
					defValue = (Pattern) f.get(AutoThirdPerson.SETTINGS);
				} catch (ReflectiveOperationException | ClassCastException e) {
					throw new RuntimeException(e);
				}
				
				TextFieldBuilder a = builder.entryBuilder()
					.startTextField(new LiteralText(f.getName()), defValue.pattern())
					.setDefaultValue(((Pattern) Settings.getDefaultValue(f)).pattern())
					.setErrorSupplier(s -> {
						try {
							Pattern.compile(s);
						} catch (PatternSyntaxException e) {
							return Optional.of(new LiteralText("Invalid regular expression"));
						}
						return Optional.empty();
					})
					.setSaveConsumer(s -> {
						try {
							f.set(AutoThirdPerson.SETTINGS, Pattern.compile(s));
						} catch (ReflectiveOperationException e) {
							throw new RuntimeException(e);
						}
					});
				
				fieldBuilder = a;
				tooltipSetter = a::setTooltip;
			} else {
				throw new RuntimeException("If you see this quat forgot a serializer, go holler at it on the bug tracker");
			}
			
			if(f.isAnnotationPresent(Settings.Comment.class)) {
				tooltipSetter.accept(new LiteralText(Strings.join(f.getAnnotation(Settings.Comment.class).value(), " ")));
			}
			
			cat.get().addEntry(fieldBuilder.build());
		}
		
		builder.setSavingRunnable(() -> {
			try {
				AutoThirdPerson.SETTINGS.write(AutoThirdPerson.SETTINGS_PATH);
			} catch (IOException e) {
				throw new RuntimeException("Cannot save file from cloth saving runnable", e);
			}
		});
		
		return builder.build();
	}
}
