# Auto Third Person but it's for ~~forge~~ ~~fabric~~ fabric and forge ~~1.12~~ ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.19~~ everything???

Puts you in third person when you do certain things.

LGPL 3.0 or later.

## Subprojects guided tour

These subprojects depend on each other with - I dunno if it's a real Gradle term, but with "textual dependencies", where the compilation classpath of the dependent is directly added to the compilation classpath of the dependee. This means there isn't a `Fabric-1.19.2` artifact that depends on `Core` as a separate artifact; it just contains the classes from `Core` instead. That's the goal anyways.

### `Core`

Version-agnostic, loader-agnostic engine of Auto Third Person. This is where the logic lives, as well as where the `MinecraftInteraction` and `LoaderInteraction` API surfaces are defined.

### `Xplat-...`

Loader-agnostic, but not version-agnostic stuff. This project has access to Minecraft's code, and can implement `MinecraftInteraction`.

If there's an artifact for only one loader on a given version, I don't use an `Xplat` artifact to implement `MinecraftInteraction`, i'll just do them both. 

### `[Loader]-[...]`

Code and resources specific to each distributed artifact. This project has access to the modloader, and can implement `LoaderInteraction`. It also plugs everything in to the modloader, initializing the Core with the appropriate Interaction APIs.

### `CrummyConfig`

Lowest-common-denominator config system that only depends on the config intermediate representation in `:Core`, so it can be used on platforms without an ecosystem config system to plug into, thanks Fabric.

Create an `UncookedCrummyConfig` - you supply a `Path` to load the file from - and call `load` whenever you want (probably plug this into F3+T or a client command). Turn this into something `AutoThirdPerson` can use with `CookedCrummyConfig`.

Despite it being basic it produces kinda nice looking config files imo ;)