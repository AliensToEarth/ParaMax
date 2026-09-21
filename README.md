![paramax-banner3](https://cdn.modrinth.com/data/cached_images/f17c79f1caa3c3c705f15c2a12988ea0c812590c_0.webp)

**ParaMax** is a client-side performance mod that cares a bit less about the fps value in your F3 counter and more about the smoothness & actual feeling of the game. Mod goes after the stutters, lag spikes, and uneven pacing that make a gameplay feel rough.

---
# 📖 You're in Control
Everything here is a toggle, all settings are adjustable live from the Mod Menu configuration screen. No need for digging through JSON. You don't like something? Turn it off. Want the whole thing gone for a moment? There's one switch for that too.

## 🔧 Presets
In case you don't want to touch every setting there are three preset buttons at the top of the config screen:
- **Potato PC** - you get fewer particles, shorter view distances for entities, less detail far away. It won't look as nice - that's the trade. This is the only preset which has a governor that watches your FPS and downgrades things further when your PC is struggling, then eases back off once it's not.
- **Balanced preset** - good preset with small tradeoffs. Particles trimmed, less detail on far-away things.
- **Lossless Defaults** - nothing you can see changes, but the invisible work carries on: **frame pacing** is still smoothing out your frames. So this isn't ParaMax doing nothing - it's doing everything that costs you nothing visually. This is how the mod ships, so a fresh install never changes how your game looks, and the button doubles as your config reset for a mod.

Presets aren't modes - they are just set of the same toggles and sliders you can change yourself. Pick one, then tweak whatever you want. Frame pacing stays on in all of them. Comfertable and smooth gameplay is the whole point.

---
## ✅ Every feature overview

### Always on (doesn't worsen vanilla visuals)

| Feature | Default | What it does                                                                                                                  |
|---|---|-------------------------------------------------------------------------------------------------------------------------------|
| ParaMax Enabled | On | Master switch. Turns everything below off in one click.                                                                       |
| Frame Pacing | On | Measures what each frame really costs and holds back the fast ones so frames arrive at a steady cadence instead of juddering. |
| Dynamic FPS | On | Drops to a low framerate the moment the window loses focus.                                                                   |
| Menu FPS Cap | On | Caps the framerate on the pause menu, which vanilla leaves uncapped.                                                          |
| Parallel Entity Visibility | On | Works out which entities are on screen across cores before rendering starts.                                                  |
| Particle Spawn Budget | On | Limits how many particles can appear in one tick. The overflow arrives over the next few ticks rather than being thrown away. |
| Reuse Block Entity States | On | Reuses each chest, sign, beacon etc.'s render data object instead of allocating a new one every frame. Still refreshed every frame, so nothing goes stale. |

### Adaptive

| Feature | Default | What it does                                                                                                                                   |
|---|---|------------------------------------------------------------------------------------------------------------------------------------------------|
| Adaptive FPS Governor | Off | Watches your framerate and frame-time spikes, tightens the settings below when your PC is struggling, then eases off once recovered.           |
| Governor Anticipation | On | Raises pressure the moment a big explosion or particle packet arrives, before the lag lands. Only does anything with the governor switched on. |

### Visual trade-offs - off unless you ask

| Feature | Default | What it does |
|---|---|---|
| Entity Distance Culling | Off | Stops rendering entities past a set distance. |
| Particle Throttling | Off | Keeps only a fraction of particles. |
| Particle Distance Culling | Off | Skips particles spawning beyond a set distance. |
| Temporal Entity LOD | Off | Updates distant entities pose and equipment less often. Position still updates every frame, so movement stays smooth. |
| Half-Rate Texture Animations | Off | Advances animated textures like lava and fire every other tick, so they play at half speed. |
| Reduce Cosmetic Entity Ticks | Off | Ticks paintings, item frames and leash knots every other tick. |

### The numbers behind them

| Setting | Default | What it does |
|---|---|---|
| Target FPS | 60 | The framerate the governor aims for. |
| Governor Base Pressure | 0 | A floor for the governor. Above 0 it never fully relaxes. |
| Unfocused FPS | 10 | Framerate while the window isn't focused. |
| Menu FPS | 60 | Framerate cap on the pause menu. |
| Particles Kept | 60% | How many particles survive when particle throttling is on. |
| Particle Cull Distance | 48 blocks | Particles spawning further away than this are skipped. |
| Particle Spawn Budget | 4000 | The most particles allowed to appear in a single tick. |
| Entity Cull Distance | 64 blocks | Entities beyond this aren't rendered. |
| LOD Near Distance | 16 blocks | Entities closer than this always update every frame. |
| LOD Max Interval | 4 | At the furthest range, distant entities refresh their pose every 4th frame. |
| Parallel Entity Threshold | 128 | How many entities before visibility work is spread across cores. |
---
## 📥 Building from source

ParaMax is a [Fabric Loom](https://fabricmc.net/) project. You'll need **JDK 21**; everything else is pulled in by the Gradle wrapper.

```bash
git clone https://github.com/AliensToEarth/ParaMax
cd ParaMax
./gradlew build
```

The builded mod jar lands in `build/libs/` as `paramax-<mod_version>+<minecraft_version>.jar` (for example `paramax-1.2.1+1.21.11.jar`); the `-sources.jar` beside it is just the source bundle which you wouldn't need.

---

## ❓ Something broke?
If you hit a bug or a crash, please open an [issue](https://github.com/AliensToEarth/ParaMax/issues) with your log and a quick note on what you were up to. If you can, grab a screenshot of the F3 screen - ParaMax adds a line there showing capacity, pacing, and governor state, and that context makes bugs a lot faster to track down.

