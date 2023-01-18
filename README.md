# Auto Third Person but it's for ~~forge~~ ~~fabric~~ fabric and forge ~~1.12~~ ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.19~~ everything???

Puts you in third person when you do certain things.

LGPL 3.0 or later.

## Warning

You need a *lot* of RAM to run `build`, which builds every project at the same time. `gradle.properties` allows Gradle 4 gigabytes of RAM, which will only grow as I add more projects. Try commenting out some subproject declarations in `settings.gradle` if you're having issues.

Parallel building has been disabled in `gradle.properties`, because there seems to be some bug where if you invoke multiple Looms at the same time, they stomp on each other. Apologies in advance for the lost performance.

Loom will sometimes explode with some nonsense about being unable to parse the version numbers provided in the `fabricApi.module` calls. If this happens just try again. (this might be related to the race condition) 

## Subprojects guided tour

These subprojects depend on each other with - I dunno if it's a real Gradle term, but with "textual dependencies", where the compilation classpath of the dependent is directly added to the compilation classpath of the dependee. This means there isn't a `Fabric-1.19.2` artifact that depends on `Core` as a separate artifact; it just contains the classes from `Core` instead. That's the goal anyways.

### `Core`

Version-agnostic, loader-agnostic engine of Auto Third Person. This is where the logic lives, as well as where the `MinecraftInteraction` and `LoaderInteraction` API surfaces are defined.

Must be compatible with Java 6. (Blame 1.4.7.)

### `Xplat-...`

Loader-agnostic, but not version-agnostic stuff. This project has access to Minecraft's code, and can implement `MinecraftInteraction`.

If there's an artifact for only one loader on a given version, I don't use an `Xplat` artifact to implement `MinecraftInteraction`, i'll just do them both.

Must be compatible with that version's Java version, so, probably Java 17 or Java 8.

### `[Loader]-[...]`

Code and resources specific to each distributed artifact. This project has access to the modloader, and can implement `LoaderInteraction`. It also plugs everything in to the modloader, initializing the Core with the appropriate Interaction APIs.

Also must be compatible with that version's Java version.

### `CrummyConfig`

Lowest-common-denominator config system that only depends on the config intermediate representation in `:Core`, so it can be used on platforms without an ecosystem config system to plug into, thanks Fabric.

Must be compatible with Java 8.

Create an `UncookedCrummyConfig` - you supply a `Path` to load the file from - and call `load` whenever you want (probably plug this into F3+T or a client command). Turn this into something `AutoThirdPerson` can use with `CookedCrummyConfig`.  Despite it being basic it produces kinda nice looking config files imo ;)