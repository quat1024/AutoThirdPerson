# Auto Third Person but it's for ~~forge~~ ~~fabric~~ ~~fabric and forge~~ just forge again ~~1.12~~ ~~1.16~~ ~~1.17~~ ~~1.18~~ ~~1.19~~ ~~1.4.7~~ 1.7.10

Puts you in third person when you do certain things. Well, less things than the recent versions, anyways.

LGPL 3.0 or later, with my usual "I don't care if you are looking at the buildscript to learn how to compile things" exception.

## Note

Buildscript is loosely based off of some of the buildscript work done by the GregTech New Horizons team, they have [a forgegradlefork](https://github.com/GTNewHorizons/ForgeGradle), etc.

They have a quite... [overengineered Gradle setup](https://github.com/GTNewHorizons/ExampleMod1.7.10) - the goal seems to have a unified buildscript across all GTNH projects, so every script has everything any project could ever need - everything from Maven publishing to Mixin support (!) to Scala support to... a buildscript self-updater? It seems kinda wild but I guess they know what they're doing.

I'm trying to always keep this mod light, so I used it only as a reference for "how to compile 1.7.10 stuff in 2022" but didn't copy the things I didn't need.

Trying to figure things out:

* Gradle 7 removed the "maven" plugin in favor of "maven-publish", but this forgegradle fork still seems to expect a `maven` plugin, so you must use gradle 6.x. The latest version is 6.9.3 and it still seems to be somewhat actively maintained.
* Greg's website has been a Maven server this whole time! Comments in GTNH projects that say it's "a Forge maven mirror" are slightly wrong, it contains some scala crap that's apparently required to run setupDecompWorkspace.
* You still need the `metadataSources { artifact() }` trick on Forge's maven for artifacts from this time period (i.e. it's not just a 1.4.7 thing lol)
* The Forge version checker will fail to parse json, it's harmless.
* If you get `java.lang.NoClassDefFoundError: com/mojang/authlib/exceptions/AuthenticationException` immediately after pressing the IDEA run button, smack that mf gradle refresh button 🔄🔄🔄🔄
* Compiling fails with cryptic "unsupported class file version" errors on Java 17, despite the toolchain declaration stuff on Gradle. I'm using Java 8 on my PC and in github actions, it seems to work alright.

## IDEA integration is a fuck

IDEA integration is quite rough compared to modern times. Running the `idea` task will poop out a bunch of idea project files in the root of the project. You'll have to close the project and specifically open the project through one of those .ipr files in order to get run configs. To regenerate them, delete the run configs, delete all the project files, then run `./gradlew setupDecompWorkspace idea` from a terminal.

I transitioned this project from an `.idea` folder-based project to one of these. IJ seems to not like it when you have both, I think.

Asset loading (or at least mcmod.info loading) is still broken for some reason, even after pasting in that "inheritOutputDirs" incantation. Not sure how to debug this...