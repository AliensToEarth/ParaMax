package dev.alienstoearth.paramax.config;

public enum ParaMaxPreset {
    POTATO,
    BALANCED,
    QUALITY;

    public void apply(ParaMaxConfig c) {
        c.resetToDefaults();
        switch (this) {
            case POTATO -> {
                c.particleMultiplier = 0.2;
                c.maxParticleDistance = 16.0;
                c.particleSpawnBudget = 100;
                c.maxEntityRenderDistance = 24.0;
                c.maxBlockEntityRenderDistance = 16.0;
                c.lodNearDistance = 8.0;
                c.lodMaxInterval = 8;
                c.governorBasePressure = 4;
                c.targetFps = 240;
                c.unfocusedFps = 5;
                c.menuFps = 30;
                c.debugHudIntervalMs = 500;
                c.hudCacheIntervalMs = 1000;
                c.pacingMinFps = 20;
            }
            case BALANCED -> {
                c.particleMultiplier = 0.6;
                c.maxParticleDistance = 32.0;
                c.particleSpawnBudget = 750;
                c.maxEntityRenderDistance = 48.0;
                c.maxBlockEntityRenderDistance = 32.0;
            }
            case QUALITY -> {
                c.particleMultiplier = 1.0;
                c.maxParticleDistance = 64.0;
                c.particleSpawnBudget = 2000;
                c.maxEntityRenderDistance = 96.0;
                c.maxBlockEntityRenderDistance = 64.0;
                c.halfRateTextureAnimations = false;
                c.skipWeatherRendering = false;
                c.reduceCosmeticEntityTicks = false;
                c.lodNearDistance = 32.0;
                c.lodMaxInterval = 2;
            }
        }
        c.framePacing = true;
        c.clamp();
    }
}
