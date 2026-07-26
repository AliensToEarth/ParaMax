package dev.alienstoearth.paramax.config;

public enum ParaMaxPreset {
    POTATO,
    BALANCED,
    LOSSLESS;

    public void apply(ParaMaxConfig c) {
        c.resetToDefaults();
        switch (this) {
            case POTATO -> {
                c.throttleParticles = true;
                c.particleMultiplier = 0.25;
                c.particleCulling = true;
                c.maxParticleDistance = 16.0;
                c.particleSpawnBudget = 200;
                c.entityDistanceCulling = true;
                c.maxEntityRenderDistance = 32.0;
                c.blockEntityDistanceCulling = true;
                c.maxBlockEntityRenderDistance = 24.0;
                c.temporalEntityLod = true;
                c.lodNearDistance = 8.0;
                c.lodMaxInterval = 8;
                c.smartLightmap = true;
                c.halfRateTextureAnimations = true;
                c.skipWeatherRendering = true;
                c.reduceCosmeticEntityTicks = true;
                c.adaptivePerformance = true;
                c.governorBasePressure = 1;
                c.targetFps = 60;
                c.unfocusedFps = 5;
                c.menuFps = 30;
                c.debugHudIntervalMs = 500;
                c.hudCacheIntervalMs = 1000;
                c.pacingMinFps = 20;
            }
            case BALANCED -> {
                c.throttleParticles = true;
                c.particleMultiplier = 0.6;
                c.particleCulling = true;
                c.maxParticleDistance = 32.0;
                c.particleSpawnBudget = 750;
                c.entityDistanceCulling = true;
                c.maxEntityRenderDistance = 48.0;
                c.blockEntityDistanceCulling = true;
                c.maxBlockEntityRenderDistance = 40.0;
                c.temporalEntityLod = true;
                c.smartLightmap = true;
                c.reduceCosmeticEntityTicks = true;
            }
            case LOSSLESS -> {
                c.throttleParticles = false;
                c.particleCulling = false;
                c.particleSpawnBudget = 4000;
                c.entityDistanceCulling = false;
                c.blockEntityDistanceCulling = false;
                c.temporalEntityLod = false;
                c.smartLightmap = false;
                c.halfRateTextureAnimations = false;
                c.skipWeatherRendering = false;
                c.reduceCosmeticEntityTicks = false;
            }
        }
        c.framePacing = true;
        c.clamp();
    }
}
