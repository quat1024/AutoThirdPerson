Auto Third Person but it's for fabric ~~1.16~~ 1.17
===================================================

Puts you in third person when you do certain things.

LGPL 3.0 or later.

# Compiling

Requires [NaCL](https://github.com/quat1024/nacl) in your `mavenLocal` (it's not on any internet mavens right now)

* Clone `nacl` somewhere.
* Run `./gradlew publishToMavenLocal` in `nacl`.
* Now, this mod will compile.

## A note on dependencies and config

~~This mod uses Cloth Config for its config screen, but for filesize reasons, doesn't bundle it. If you have both ModMenu and Cloth Config (and in a big enough modpack, you probably do), a config screen is available from ModMenu.~~

~~The ModMenu and Cloth Config integrations have been disabled for this quick-and-dirty 1.17 port. I'll get them in later.~~

I'll add these to NaCl some time.

The mod can always be configured through its config file. Reload resources (F3+T), or use the client-command `/auto_third_person reload`, to reread the file.