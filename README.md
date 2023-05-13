# Auto Third Person but it's for ~~forge~~ ~~fabric~~ fabric and forge ~~1.12~~ ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.19~~ everything???

Puts you in third person when you do certain things. This used to be a simple mod.

LGPL 3.0 or later.

## Config notes

The config file is loaded once at startup, then...

|version||
|---|---|
|1.4.7 Forge|reloaded every time you load a world. (log out/log in to refresh it)|
|1.7.10 Forge|reloaded when you open the controls screen, or that weird unfinished "Mod Options" screen|
|1.12.2 Forge|it's plugged in to the standard Forge config GUI!|
|1.16.5 Forge|automatically after you save the config file|
|1.16.5 Fabric|reloaded when you `F3+T` or use the `/auto_third_person reload` client command|
|1.17.1 Forge|automatically after you save the config file|
|1.17.1 Fabric|reloaded when you `F3+T` or use the `/auto_third_person reload` client command|
|1.18.2 Forge|automatically after you save the config file|
|1.18.2 Fabric|reloaded when you `F3+T` or use the `/auto_third_person reload` client command|
|1.19.2 Forge|automatically after you save the config file|
|1.19.2 Fabric|reloaded when you `F3+T` or use the `/auto_third_person reload` client command|
|1.19.4 Forge|automatically after you save the config file|
|1.19.4 Fabric|reloaded when you `F3+T` or use the `/auto_third_person reload` client command|

It's a mess out there! Different loaders provide different services for configuration files.

## Layout

The core is `Core/.../AutoThirdPerson.java`, and is a singleton with a bunch of abstract methods. I generally use a pattern where I implement as much as possible without touching the modloader (in classes named like `OneSixteenFiveAutoThirdPerson`) in a still-abstract class, then finish the rest in a modloader-specific class. The core also contains some thin wrappers over bits shared across all Minecraft versions just so i can refer to them. It must remain compatible with Java 6.

* modern versions with official mappings (1.17, 1.18, 1.19, 1.19 again) - using [VanillaGradle](https://github.com/SpongePowered/VanillaGradle/) in the `xplat` modules to write code against vanilla minecraft using official names, then using the modloader-specific gradle plugins to complete the mod, also using official names
* 1.16 is an odd duck because it does have official mappings but ForgeGradle's `"official"` mappings channel is a big fat lie, so the mc-specific classes are moved into the main project
* 1.12 is simply good old ForgeGradle, using MCP names
* 1.7 and 1.4 ("vintage" Forge) use [Voldeloom](https://github.com/CrackedPolishedBlackstoneBricksMC/voldeloom/) projects and MCP names
  * Feel free to request ports for Forge 1.3, 1.5, and 1.6 if you want em, voldeloom works on those too

Forge versions generally use events, my needs happened to expertly dodge all available Fabric events though lmao, so mixin is used there.

`CrummyConfig` is a lowest-common-denominator configuration loading system, used on Fabric which doesn't come with a config API. Written against Java 8.

### dirty secret

The mod isn't usually tested very well because there's 13 versions to try :joy:

## Warnings

You need a *lot* of RAM to run `./gradlew build`, which builds every project. `gradle.properties` allows Gradle 6 gigabytes of RAM, which will only grow as I add more projects. Try commenting out some subproject declarations in `settings.gradle` if you're having issues. (As a side effect, if you're moving to a new computer, compiling this project is a great way to populate your Gradle cache.)

Parallel building has been disabled in `gradle.properties`, because there seems to be some bug where if you invoke multiple Looms at the same time they stomp on each other, and I wouldn't be surprised if there was a ForgeGradle bug too. Apologies in advance for the lost performance.

Loom will sometimes explode with some nonsense about being unable to parse the version numbers provided in the `fabricApi.module` calls. If this happens just try again. And again, and again, and again. Just keep trying. (I think it's some sort of race condition, where it fails and downloads the file at the same time, so the next time around it's been downloaded.)

If ForgeGradle explodes with something about "ProjectScopeServices has been closed." that's some forgegradle bug with the daemon. `./gradlew --stop` fixes it. Yes, even if the daemon is already disabled, intellij sync always uses the daemon. It's a good idea to run `--stop` after touching any forgegrade projects (especially adding a new one)

Yes, touching literally anything to do with Gradle will cause it to logspam about configuring every project. This is because most Minecraft plugins don't support the gradle "configure on-demand" stuff.

If you press the intellij Sync button you'll also have to sit through a short wall of `prepareWorkspace` tasks from VanillaGradle. Just roll with it. And then sometimes an unrelated Forge project will do its annoying decompile step in the middle of one of the `prepareWorkspace`s and it takes a million years. Dont worry about it

If you need to `--refresh-dependencies` ITS GONNA DO ALL OF THEM btw. God help you