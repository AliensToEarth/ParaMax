package dev.alienstoearth.paramax.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ParaMaxConfig {

    public boolean enabled = true;

    public boolean tuneWorkerThreads = true;

    public int workerThreadCount = 0;

    public boolean dynamicFps = true;

    public int unfocusedFps = 10;

    public boolean entityDistanceCulling = true;

    public double maxEntityRenderDistance = 64.0;

    public boolean throttleParticles = true;

    public double particleMultiplier = 1.0;

    public boolean parallelParticles = true;

    public int parallelParticleThreshold = 512;

    public boolean blockEntityDistanceCulling = true;

    public double maxBlockEntityRenderDistance = 48.0;

    public boolean skipWeatherRendering = true;

    public boolean parallelBlockEntityStates = true;

    public int parallelBlockEntityThreshold = 64;

    public boolean throttleDebugHud = true;

    public int debugHudIntervalMs = 100;

    public boolean halfRateTextureAnimations = true;

    public boolean reduceCosmeticEntityTicks = true;

    public boolean throttleMenus = true;

    public int menuFps = 60;

    public boolean parallelEntityVisibility = true;

    public int parallelEntityThreshold = 128;

    public boolean smartLightmap = true;

    public boolean cacheHudText = true;

    public int hudCacheIntervalMs = 250;

    public boolean poolBlockEntityStates = true;

    public boolean adaptivePerformance = false;

    public int targetFps = 60;

    public int governorBasePressure = 0;

    public boolean framePacing = true;

    public int pacingMinFps = 30;

    public boolean particleCulling = true;

    public double maxParticleDistance = 48.0;

    public boolean poolEntityStates = true;

    public boolean temporalEntityLod = true;

    public double lodNearDistance = 16.0;

    public int lodMaxInterval = 4;

    public boolean anticipateSpikes = true;

    public boolean budgetParticleSpawns = true;

    public int particleSpawnBudget = 1000;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile ParaMaxConfig instance;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("paramax.json");
    }

    public static ParaMaxConfig get() {
        ParaMaxConfig local = instance;
        if (local != null) {
            return local;
        }
        synchronized (ParaMaxConfig.class) {
            if (instance == null) {
                instance = load();
            }
            return instance;
        }
    }

    private static ParaMaxConfig load() {
        Path p = path();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                ParaMaxConfig cfg = GSON.fromJson(r, ParaMaxConfig.class);
                if (cfg != null) {
                    cfg.clamp();
                    return cfg;
                }
            } catch (Exception e) {

                System.err.println("[ParaMax] Failed to read config, using defaults: " + e);
            }
        }
        ParaMaxConfig cfg = new ParaMaxConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        clamp();
        Path p = path();
        try {
            Files.createDirectories(p.getParent());
            try (Writer w = Files.newBufferedWriter(p)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            System.err.println("[ParaMax] Failed to write config: " + e);
        }
    }

    public void resetToDefaults() {
        ParaMaxConfig defaults = new ParaMaxConfig();
        try {
            for (java.lang.reflect.Field field : ParaMaxConfig.class.getFields()) {
                field.set(this, field.get(defaults));
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public void clamp() {
        if (workerThreadCount < 0) workerThreadCount = 0;
        if (unfocusedFps < 1) unfocusedFps = 1;
        if (unfocusedFps > 260) unfocusedFps = 260;
        if (maxEntityRenderDistance < 8.0) maxEntityRenderDistance = 8.0;
        if (particleMultiplier < 0.0) particleMultiplier = 0.0;
        if (particleMultiplier > 1.0) particleMultiplier = 1.0;
        if (parallelParticleThreshold < 64) parallelParticleThreshold = 64;
        if (maxBlockEntityRenderDistance < 8.0) maxBlockEntityRenderDistance = 8.0;
        if (parallelBlockEntityThreshold < 16) parallelBlockEntityThreshold = 16;
        if (debugHudIntervalMs < 16) debugHudIntervalMs = 16;
        if (debugHudIntervalMs > 1000) debugHudIntervalMs = 1000;
        if (menuFps < 5) menuFps = 5;
        if (menuFps > 260) menuFps = 260;
        if (parallelEntityThreshold < 32) parallelEntityThreshold = 32;
        if (hudCacheIntervalMs < 50) hudCacheIntervalMs = 50;
        if (hudCacheIntervalMs > 2000) hudCacheIntervalMs = 2000;
        if (targetFps < 20) targetFps = 20;
        if (targetFps > 240) targetFps = 240;
        if (governorBasePressure < 0) governorBasePressure = 0;
        if (governorBasePressure > 4) governorBasePressure = 4;
        if (pacingMinFps < 10) pacingMinFps = 10;
        if (pacingMinFps > 120) pacingMinFps = 120;
        if (maxParticleDistance < 8.0) maxParticleDistance = 8.0;
        if (maxParticleDistance > 512.0) maxParticleDistance = 512.0;
        if (lodNearDistance < 8.0) lodNearDistance = 8.0;
        if (lodNearDistance > 64.0) lodNearDistance = 64.0;
        if (lodMaxInterval < 1) lodMaxInterval = 1;
        if (lodMaxInterval > 8) lodMaxInterval = 8;
        if (particleSpawnBudget < 100) particleSpawnBudget = 100;
        if (particleSpawnBudget > 16384) particleSpawnBudget = 16384;
    }
}
