# things to do later

Ive worked a lot on old froge versions today, need to retest that the new forge+fabric versions work too

check that CrummyConfig error handling is the way i want it

# tracking what the player is riding

|version||
|---|---|
|forge 1.4.7|watch every frame for player mounting things|
|forge 1.7.10|watch every frame for player mounting things|
|forge 1.12.2|`EntityMountEvent`, check the mounter == client.player|
|forge 1.16.5|`EntityMountEvent`|
|fabric 1.16.5|mixin to `LocalPlayer`, check startRiding/removeVehicle|
|forge 1.17.1|`EntityMountEvent`|
|fabric 1.17.1|mixin to `LocalPlayer`|
|forge 1.18.2|`EntityMountEvent`|
|fabric 1.18.2|mixin to `LocalPlayer`|
|forge 1.19.2|`EntityMountEvent`|
|fabric 1.19.2|mixin to `LocalPlayer`|
|forge 1.19.4|`EntityMountEvent`|
|fabric 1.19.4|mixin to `LocalPlayer`|

# detecting manual F5 key presses

|version||
|---|---|
|forge 1.4.7|keep track of expected third-person state<br>assume current != expected -> manual press|
|forge 1.7.10|`InputEvent.KeyInputEvent`|
|forge 1.12.2|`InputEvent.KeyInputEvent`|
|forge 1.16.5|`InputEvent.KeyInputEvent`|
|fabric 1.16.5|mixin to `Minecraft`|
|forge 1.17.1|`InputEvent.KeyInputEvent`|
|fabric 1.17.1|mixin to `Minecraft`|
|forge 1.18.2|`InputEvent.KeyInputEvent`|
|fabric 1.18.2|mixin to `Minecraft`|
|forge 1.19.2|`InputEvent.Key`|
|fabric 1.19.2|mixin to `Minecraft`|
|forge 1.19.4|`InputEvent.Key`|
|fabric 1.19.4|mixin to `Minecraft`|

# skipping front-view

|version||
|---|---|
|forge 1.4.7|ticker (`TickType.RENDER`)|
|forge 1.7.10|ticker (`TickEvent.RenderTickEvent`)|
|forge 1.12.2|ticker (`TickEvent.RenderTickEvent`)|
|forge 1.16.5|ticker (`TickEvent.RenderTickEvent`)|
|fabric 1.16.5|mixin to `GameRenderer`<br>available events in fabric-rendering-v1 are too late|
|forge 1.17.1|ticker (`TickEvent.RenderTickEvent`)|
|fabric 1.17.1|mixin to `GameRenderer`|
|forge 1.18.2|ticker (`TickEvent.RenderTickEvent`)|
|fabric 1.18.2|mixin to `GameRenderer`|
|forge 1.19.2|ticker (`TickEvent.RenderTickEvent`)|
|fabric 1.19.2|mixin to `GameRenderer`|
|forge 1.19.4|ticker (`TickEvent.RenderTickEvent`)|
|fabric 1.19.4|mixin to `GameRenderer`|

Forge `CameraSetupEvent`/`ViewportEvent.ComputeCameraAngles` would be perfect but it's fired one line too late

# architecture

previous version: kinda botania style, two "service" classes are required in the constructor: one for interacting with minecraft & one for interacting with the loader. A little bit wrong (like "logging" requires interacting with FML on 1.4.7, so it's not really a vanilla-only capability) and I don't really think the separation proves useful.

better approach imo: one mod class full of abstract methods, extended abstractly in the xplat module with whatever is possible to implement there, and further extended in the loader module