# ParaMax

ParaMax is a free, client-side performance mod that cares less about the number in your F3 counter and more about how the game actually *feels* to play. It goes after the stutters, hitches, and uneven pacing that make a session feel rough, and it quietly scales its own effort up or down depending on how hard your machine is working at any given moment.

It's client-side only, it doesn't change how anything looks, and you can tweak all of it from inside the game.

## What it actually does

The heart of ParaMax is **frame pacing**. A game that swings between fast and slow frames feels as juddery even when the average framerate looks great, so ParaMax measures what each frame really costs and smooths the delivery out. It does this carefully - with a precise wait and an awareness of Minecraft's 20-per-second logic tick - so it evens things out without quietly stealing framerate from you.

Sitting on top of that is an **adaptive governor**. Think of it as a helper that keeps an eye on your FPS and your frame-time spikes and only steps in when you're genuinely struggling. When things get heavy it eases off the expensive stuff - particles, distant entities, texture animation rate - and as soon as you have room to breathe again, it hands that back. It can even brace for trouble ahead of time when it sees something like a huge explosion coming.

Under the hood there's the less glamorous work that tends to matter most for consistency: **reusing render objects** instead of throwing them away and rebuilding them every frame (easier on the garbage collector), spreading some particle, entity, and block-entity work across **spare CPU cores**, and a handful of optional **culling and throttling** knobs for the truly nasty scenes. There are also the obvious background savings - dropping your framerate when the window isn't focused, not rebuilding the F3 screen every single frame, and caching the scoreboard and player list.

## You're in control

Everything here is a toggle, and the numbers behind it - target FPS, worker threads, culling distances, particle limits, how twitchy the governor is - are all adjustable live from the **Mod Menu** screen. No digging through JSON. Don't like something? Turn it off. Want the whole thing gone for a moment? There's one switch for that too.

## Presets

Don't want to touch every setting? There are three preset buttons at the top of the
config screen, plus a reset:

- **Potato PC** - you get as many frames as possible. Fewer particles, shorter view distances
  for entities, less detail far away. It won't look as nice - that's the trade.
- **Balanced preset** - the defaults, with particles and block entities toned down a bit.
  Good savings, hard to notice.
- **Lossless** - if looks come first. Everything you can see stays on. ParaMax still
  helps out if the game starts to struggle.
- **Set Defaults** - puts everything back to normal.

Presets aren't modes - they just set the same toggles and sliders you can change
yourself. Pick one, then tweak whatever you want on top.

Frame pacing stays on in all of them. Smooth frames are the whole point.

## Playing nice with others

ParaMax is **client-side only** - servers neither know nor care whether you're running it. It needs [Fabric API](https://modrinth.com/mod/fabric-api).

It's meant to run **alongside** [Sodium](https://modrinth.com/mod/sodium) and [Lithium](https://modrinth.com/mod/lithium) rather than compete with them. ParaMax hooks Minecraft's *vanilla* rendering - entity, particle, lightmap, and HUD paths - but it deliberately leaves the terrain and chunk render pipeline untouched, which is exactly the part Sodium replaces. Lithium optimizes server-side game logic, which ParaMax doesn't go near at all.

## Being honest about it

Results depend a lot on your hardware and what you're doing. Where ParaMax earns its keep is **steadier frame times and much better 1% lows when the game gets busy**: clouds of particles, packed-out entity scenes. If what you're really after is raw FPS in GPU-bound situations, pair it with a rendering optimizer like Sodium.

And to be clear: ParaMax doesn't collect or send off any of your data.

## Building from source

ParaMax is a [Fabric Loom](https://fabricmc.net/) project. You'll need **JDK 21**; everything else is pulled in by the Gradle wrapper.

```bash
git clone https://github.com/AliensToEarth/ParaMax.git
cd ParaMax
./gradlew build
```
or
```
gradle build
```

The builded mod jar lands in `build/libs/` as `paramax-<mod_version>+<minecraft_version>.jar` (for example `paramax-1.0+1.21.11.jar`); the `-sources.jar` beside it is just the source bundle which you wouldn't need.

## Something broke?

If you hit a bug or a crash, please open an [issue](https://github.com/AliensToEarth/ParaMax/issues) with your log and a quick note on what you were up to. If you can, grab a shot of the F3 screen - ParaMax adds a line there showing capacity, pacing, and governor state, and that context makes bugs a lot faster to track down.