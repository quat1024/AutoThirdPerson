# Auto Third Person but it's for ~~forge~~ ~~fabric~~ fabric and forge ~~1.12~~ ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.19~~ everything???

Puts you in third person when you do certain things. This used to be a simple mod.

LGPL 3.0 or later.

## Warnings

Forge 1.16.5 is broken, no i don't know why. The other Forges work. I commented it out in the settings.gradle so it won't get built.

You need a *lot* of RAM to run `./gradlew build`, which builds every project. `gradle.properties` allows Gradle 4 gigabytes of RAM, which will only grow as I add more projects. Try commenting out some subproject declarations in `settings.gradle` if you're having issues. (As a side effect, if you're moving to a new computer, compiling this project is a great way to populate your Gradle cache.)

Parallel building has been disabled in `gradle.properties`, because there seems to be some bug where if you invoke multiple Looms at the same time they stomp on each other, and I wouldn't be surprised if there was a ForgeGradle bug too. Apologies in advance for the lost performance.

Loom will sometimes explode with some nonsense about being unable to parse the version numbers provided in the `fabricApi.module` calls. If this happens just try again. And again, and again, and again. Just keep trying. (I think it's some sort of race condition, where it fails and downloads the file at the same time, so the next time around it's been downloaded.)

If ForgeGradle explodes with something about "ProjectScopeServices has been closed." that's some forgegradle bug with the daemon. `./gradlew --stop` fixes it. Yes, even if the daemon is already disabled, intellij sync always uses the daemon. It's a good idea to run `--stop` after touching any forgegrade projects (especially adding a new one)

Yes, touching literally anything to do with Gradle will cause it to logspam about configuring every project. This is because most Minecraft plugins don't support the gradle "configure on-demand" stuff.

If you press the intellij Sync button you'll also have to sit through a short wall of `prepareWorkspace` tasks from VanillaGradle. Just roll with it. And then sometimes an unrelated Forge project will do its annoying decompile step in the middle of one of the `prepareWorkspace`s and it takes a million years. Dont worry about it

If you need to `--refresh-dependencies` ITS GONNA DO ALL OF THEM btw. God help you

## Subprojects guided tour

These subprojects depend on each other with - I dunno if it's a real Gradle term, but with "textual dependencies", where the compilation classpath of the dependent is directly added to the compilation classpath of the dependee. This means there isn't a `Fabric-1.19.2` artifact that depends on `Core` as a separate artifact; it just contains the classes from `Core` instead. That's the goal anyways.

### `Core`

Version-agnostic, loader-agnostic engine of Auto Third Person. This is where the logic lives, as well as where the `MinecraftInteraction` and `LoaderInteraction` API surfaces are defined.

Must be compatible with Java 6. (Blame 1.4.7.)

### `Xplat-...`

Loader-agnostic, but not version-agnostic stuff. This project has access to Minecraft's code, and can implement `MinecraftInteraction`.

If there's an artifact for only one loader on a given version, I don't use an `Xplat` artifact to implement `MinecraftInteraction`, i'll just do them both.

Must be compatible with that version's Java version, so, probably Java 17 or 16 or 8.

### `[Loader]-[...]`

Code and resources specific to each distributed artifact. This project has access to the modloader, and can implement `LoaderInteraction`. It also plugs everything in to the modloader, initializing the Core with the appropriate Interaction APIs.

Also must be compatible with that version's Java version.

### `CrummyConfig`

Lowest-common-denominator config system that only depends on the config intermediate representation in `:Core`, so it can be used on platforms without an ecosystem config system to plug into, thanks Fabric.

Must be compatible with Java 8, as it's used by Fabric 1.16.5. (Todo, it should probably be Java 6.)

Create an `UncookedCrummyConfig` - you supply a `Path` to load the file from - and call `load` whenever you want (probably plug this into F3+T or a client command). Turn this into something `AutoThirdPerson` can use with `CookedCrummyConfig`.  Despite it being basic it produces kinda nice looking config files imo ;)

## todo

Evaluate [dropbox/focus](https://github.com/dropbox/focus) - i doubt it'll work because minecraft plugins do their expensive work in afterEvaluate, but who knows