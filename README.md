![banner](https://cdn.modrinth.com/data/cached_images/06629b51516b8817966dcc0b2b5e61f56c6ae76c_0.webp)

**ParaMax** is a client-side performance mod that cares a bit less about the number in your F3 counter and more about how the game actually *feels* to play, **but** doesn't forget about the FPS either. Mod goes after the stutters, hitches, and uneven pacing that make a session feel rough, and it quietly scales its own effort up or down depending on how hard your machine is working at any given moment. You can tweak all settings from inside the game using Mod Menu.

---

### To achieve maximum performance install:

- [Lithium](https://modrinth.com/mod/lithium)
- [Mod Menu](https://modrinth.com/mod/modmenu)
- [Sodium](https://modrinth.com/mod/sodium)
### Dependency:
- [Fabric API](https://modrinth.com/mod/fabric-api)
---

## 🔍 What it actually does
- It's meant to run **alongside** Sodium and Lithium rather than compete with them. ParaMax hooks Minecraft's *vanilla* rendering - entity, particle, lightmap, and HUD paths - but it deliberately leaves the terrain and chunk render pipeline untouched, which is exactly the part Sodium replaces. Lithium optimizes server-side game logic, which ParaMax doesn't go near at all.
- The heart of ParaMax is **frame pacing**. A game that swings between fast and slow frames feels as juddery even when the average framerate looks great, so ParaMax measures what each frame really costs and smooths the delivery out. It does this carefully - with a precise wait and an awareness of Minecraft's 20-per-second logic tick - so it evens things out without quietly stealing framerate from you.
  Sitting on top of that is an **adaptive governor**. Think of it as a helper that keeps an eye on your FPS and your frame-time spikes and only steps in when you're genuinely struggling. When things get heavy it eases off the expensive stuff - particles, distant entities, texture animation rate - and as soon as you have room to breathe again, it hands that back. It can even brace for trouble ahead of time when it sees something like a huge explosion coming.
- Under the hood there's the less glamorous work that tends to matter most for the consistency: reusing render objects instead of throwing them away and rebuilding them every frame (easier on the garbage collector), spreading some particle, entity, and block-entity work across spare CPU cores, and a handful of optional culling and throttling knobs for the truly nasty scenes. There are also the obvious background savings - dropping your framerate when the window isn't focused, not rebuilding the F3 screen every single frame, and caching the scoreboard and player list.

## 📖 You're in control
Everything here is a toggle, and the numbers behind it - target FPS, worker threads, culling distances, particle limits, how twitchy the governor is - are all adjustable live from the Mod Menu screen. No digging through JSON. Don't like something? Turn it off. Want the whole thing gone for a moment? There's one switch for that too.

### 🔧 Presets
Don't want to touch every setting? There are three preset buttons at the top of the config screen:
- **Potato PC** - you get as many frames as possible. Fewer particles, shorter view distances for entities, less detail far away. It won't look as nice - that's the trade. This is the only preset that watches your FPS and tightens things further when your PC is struggling, then eases back off once you're not.
- **Balanced preset** - real savings you'd struggle to spot. Particles trimmed, entities and block entities dropped past a comfortable distance, less detail on far-away things. Rain and texture animations are left exactly as they are.
- **Lossless Defaults** - nothing you can see changes. Culling, particle limits and distant detail reduction are all switched off, but the invisible work carries on: **frame pacing** is still smoothing out your frames, render objects still get reused instead of getting rebuilt every frame, text still gets cached, and work still spreads across spare cores. So this isn't ParaMax doing nothing - it's ParaMax doing everything that costs you nothing visually. This is how the mod ships, so a fresh install never changes how your game looks, and the button doubles as your config reset for a mod.

Presets aren't modes - they just set the same toggles and sliders you can change yourself. Pick one, then tweak whatever you want.

**Frame pacing stays on in all of them. Smooth frames are the whole point.**

---
## ✅ Every feature overview

### Always on (doesn't worsen vanilla visuals)

| Feature | Default | What it does                                                                                                                  |
|---|---|-------------------------------------------------------------------------------------------------------------------------------|
| ParaMax Enabled | On | Master switch. Turns everything below off in one click.                                                                       |
| Frame Pacing | On | Measures what each frame really costs and holds back the fast ones so frames arrive at a steady cadence instead of juddering. |
| Worker Thread Override | On | Lets you override the size of Minecraft's background thread pool. On by default.                                              |
| Dynamic FPS | On | Drops to a low framerate the moment the window loses focus.                                                                   |
| Menu FPS Cap | On | Caps the framerate on the pause menu, which vanilla leaves uncapped.                                                          |
| Parallel Particles | On | Ticks large particle sheets across spare CPU cores.                                                                           |
| Parallel Block Entity States | On | Builds chest, sign and banner render states across cores instead of one at a time.                                            |
| Parallel Entity Visibility | On | Works out which entities are on screen across cores before rendering starts.                                                  |
| Pool Entity States | On | Reuses entity render-state objects between frames instead of allocating fresh ones.                                           |
| Pool Block Entity States | On | The same idea for block entities. Less garbage, fewer collection pauses.                                                      |
| Cache F3 Debug Text | On | Rebuilds the F3 overlay a few times a second instead of every single frame.                                                   |
| Cache HUD Text | On | Caches the scoreboard sidebar and tab-list ordering. Drawing stays live, so pings and names still update.                     |
| Particle Spawn Budget | On | Limits how many particles can appear in one tick. The overflow arrives over the next few ticks rather than being thrown away. |

### Adaptive

| Feature | Default | What it does                                                                                                                                   |
|---|---|------------------------------------------------------------------------------------------------------------------------------------------------|
| Adaptive FPS Governor | Off | Watches your framerate and frame-time spikes, tightens the settings below when your PC is struggling, then eases off once recovered.           |
| Governor Anticipation | On | Raises pressure the moment a big explosion or particle packet arrives, before the lag lands. Only does anything with the governor switched on. |

### Visual trade-offs - off unless you ask

| Feature | Default | What it does |
|---|---|---|
| Entity Distance Culling | Off | Stops rendering entities past a set distance. |
| Block Entity Distance Culling | Off | Stops rendering chests, signs, banners and beacons past a set distance. |
| Particle Throttling | Off | Keeps only a fraction of particles. |
| Particle Distance Culling | Off | Skips particles spawning beyond a set distance. |
| Temporal Entity LOD | Off | Updates distant entities pose and equipment less often. Position still updates every frame, so movement stays smooth. |
| Smart Lightmap | Off | Removes cosmetic light flicker and skips the lightmap rebuild unless something real changed. |
| Half-Rate Texture Animations | Off | Advances animated textures like lava and fire every other tick, so they play at half speed. |
| Skip Weather Rendering | Off | Doesn't draw rain or snow. The weather still happens, you just don't see it. |
| Reduce Cosmetic Entity Ticks | Off | Ticks paintings, item frames and leash knots every other tick. |

### The numbers behind them

| Setting | Default | What it does |
|---|---|---|
| Target FPS | 60 | The framerate the governor aims for. |
| Governor Base Pressure | 0 | A floor for the governor. Above 0 it never fully relaxes. |
| Unfocused FPS | 10 | Framerate while the window isn't focused. |
| Menu FPS | 60 | Framerate cap on the pause menu. |
| Pacing Min FPS | 30 | Frame pacing switches itself off below this, so a struggling game ships frames as fast as it can. |
| Particles Kept | 60% | How many particles survive when particle throttling is on. |
| Particle Cull Distance | 48 blocks | Particles spawning further away than this are skipped. |
| Particle Spawn Budget | 4000 | The most particles allowed to appear in a single tick. |
| Entity Cull Distance | 64 blocks | Entities beyond this aren't rendered. |
| Block Entity Cull Distance | 48 blocks | Chests, signs, banners and beacons beyond this aren't rendered. |
| LOD Near Distance | 16 blocks | Entities closer than this always update every frame. |
| LOD Max Interval | 4 | At the furthest range, distant entities refresh their pose every 4th frame. |
| Worker Threads | auto | Size of ParaMax's own thread pool. Auto means your core count minus one. |
| Parallel Particle Threshold | 512 | How many particles a sheet needs before the work is spread across cores. |
| Parallel Block Entity Threshold | 64 | How many block entities before that work is spread across cores. |
| Parallel Entity Threshold | 128 | How many entities before visibility work is spread across cores. |
| F3 Rebuild Interval | 100 ms | How often the F3 overlay text is rebuilt. |
| HUD Rebuild Interval | 250 ms | How often the scoreboard and tab-list cache is rebuilt. |
---
## 📥 Building from source

ParaMax is a [Fabric Loom](https://fabricmc.net/) project. You'll need **JDK 21**; everything else is pulled in by the Gradle wrapper.

```bash
git clone https://github.com/AliensToEarth/ParaMax
cd ParaMax
./gradlew build
```

The builded mod jar lands in `build/libs/` as `paramax-<mod_version>+<minecraft_version>.jar` (for example `paramax-1.2+1.21.11.jar`); the `-sources.jar` beside it is just the source bundle which you wouldn't need.

---

## ❓ Something broke?
If you hit a bug or a crash, please open an [issue](https://github.com/AliensToEarth/ParaMax/issues) with your log and a quick note on what you were up to. If you can, grab a screenshot of the F3 screen - ParaMax adds a line there showing capacity, pacing, and governor state, and that context makes bugs a lot faster to track down.

