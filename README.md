![banner](https://cdn.modrinth.com/data/cached_images/06629b51516b8817966dcc0b2b5e61f56c6ae76c_0.webp)

**ParaMax** is a client-side performance mod that cares a bit less about the number in your F3 counter and more about how the game actually *feels* to play, **but** doesn't forget about the FPS either. Mod goes after the stutters, hitches, and uneven pacing that make a session feel rough, and it quietly scales its own effort up or down depending on how hard your machine is working at any given moment. You can tweak all settings from inside the game using Mod Menu.

---

#### To achieve maximum performance install:

- [Lithium](https://modrinth.com/mod/lithium)
- [Mod Menu](https://modrinth.com/mod/modmenu)
- [Sodium](https://modrinth.com/mod/sodium)
#### Dependency:
- [Fabric API](https://modrinth.com/mod/fabric-api)
---

### 🔍 What it actually does
- It's meant to run **alongside** Sodium and Lithium rather than compete with them. ParaMax hooks Minecraft's *vanilla* rendering - entity, particle, lightmap, and HUD paths - but it deliberately leaves the terrain and chunk render pipeline untouched, which is exactly the part Sodium replaces. Lithium optimizes server-side game logic, which ParaMax doesn't go near at all.
- The heart of ParaMax is **frame pacing**. A game that swings between fast and slow frames feels as juddery even when the average framerate looks great, so ParaMax measures what each frame really costs and smooths the delivery out. It does this carefully - with a precise wait and an awareness of Minecraft's 20-per-second logic tick - so it evens things out without quietly stealing framerate from you.
  Sitting on top of that is an **adaptive governor**. Think of it as a helper that keeps an eye on your FPS and your frame-time spikes and only steps in when you're genuinely struggling. When things get heavy it eases off the expensive stuff - particles, distant entities, texture animation rate - and as soon as you have room to breathe again, it hands that back. It can even brace for trouble ahead of time when it sees something like a huge explosion coming.
- Under the hood there's the less glamorous work that tends to matter most for the consistency: reusing render objects instead of throwing them away and rebuilding them every frame (easier on the garbage collector), spreading some particle, entity, and block-entity work across spare CPU cores, and a handful of optional culling and throttling knobs for the truly nasty scenes. There are also the obvious background savings - dropping your framerate when the window isn't focused, not rebuilding the F3 screen every single frame, and caching the scoreboard and player list.

### 📖 You're in control
Everything here is a toggle, and the numbers behind it - target FPS, worker threads, culling distances, particle limits, how twitchy the governor is - are all adjustable live from the Mod Menu screen. No digging through JSON. Don't like something? Turn it off. Want the whole thing gone for a moment? There's one switch for that too.

### 🔧 Presets
Don't want to touch every setting? There are three preset buttons at the top of the config screen:
- **Potato PC** - you get as many frames as possible. Fewer particles, shorter view distances for entities, less detail far away. It won't look as nice - that's the trade. This is the only preset that watches your FPS and tightens things further when your PC is struggling, then eases back off once you're not.
- **Balanced preset** - real savings you'd struggle to spot. Particles trimmed, entities and block entities dropped past a comfortable distance, less detail on far-away things. Rain and texture animations are left exactly as they are.
- **Lossless Defaults** - nothing you can see changes. Culling, particle limits and distant detail reduction are all switched off, but the invisible work carries on: **frame pacing** is still smoothing out your frames, render objects still get reused instead of getting rebuilt every frame, text still gets cached, and work still spreads across spare cores. So this isn't ParaMax doing nothing - it's ParaMax doing everything that costs you nothing visually. This is how the mod ships, so a fresh install never changes how your game looks, and the button doubles as your config reset for a mod.

Presets aren't modes - they just set the same toggles and sliders you can change yourself. Pick one, then tweak whatever you want.

**Frame pacing stays on in all of them. Smooth frames are the whole point.**

---
## Building from source

ParaMax is a [Fabric Loom](https://fabricmc.net/) project. You'll need **JDK 21**; everything else is pulled in by the Gradle wrapper.

```bash
git clone https://github.com/AliensToEarth/ParaMax
cd ParaMax
./gradlew build
```

The builded mod jar lands in `build/libs/` as `paramax-<mod_version>+<minecraft_version>.jar` (for example `paramax-1.2+1.21.11.jar`); the `-sources.jar` beside it is just the source bundle which you wouldn't need.

---

### ❓ Something broke?
If you hit a bug or a crash, please open an [issue](https://github.com/AliensToEarth/ParaMax/issues) with your log and a quick note on what you were up to. If you can, grab a screenshot of the F3 screen - ParaMax adds a line there showing capacity, pacing, and governor state, and that context makes bugs a lot faster to track down.

