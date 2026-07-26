package dev.alienstoearth.paramax.governor;

import dev.alienstoearth.paramax.ParaMaxClient;
import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.atomic.AtomicInteger;

public final class PerformanceGovernor {

    public static final int MAX_LEVEL = 4;

    private static final double ESCALATE_BAND = 0.92;
    private static final double RELAX_BAND = 1.10;

    private static final int ESCALATE_DWELL = 40;
    private static final int RELAX_DWELL = 200;

    private static final double[] PARTICLE_FACTOR = {1.0, 0.6, 0.35, 0.25, 0.15};
    private static final double[] ENTITY_DISTANCE_FACTOR = {1.0, 1.0, 0.85, 0.70, 0.55};
    private static final double[] BLOCK_ENTITY_DISTANCE_FACTOR = {1.0, 1.0, 1.0, 0.80, 0.60};
    private static final int HALF_RATE_ANIMATION_LEVEL = 3;
    private static final double NO_LIMIT = Double.MAX_VALUE;

    private static final double SPIKE_RATIO_THRESHOLD = 0.10;
    private static final int FRAME_WINDOW = 128;

    private static final int ANTICIPATION_LEVEL = 2;

    private static final long ANTICIPATION_WINDOW_NANOS = 3_000_000_000L;

    private static final AtomicInteger level = new AtomicInteger();
    private static volatile long anticipateUntilNanos;
    private static int belowTicks;
    private static int aboveTicks;
    private static double smoothedFps;

    private static final long[] frameWork = new long[FRAME_WINDOW];
    private static int frameIndex;
    private static int frameCount;

    private PerformanceGovernor() {
    }

    public static void recordFrame(long workNanos) {
        frameWork[frameIndex] = workNanos;
        frameIndex = (frameIndex + 1) % FRAME_WINDOW;
        if (frameCount < FRAME_WINDOW) {
            frameCount++;
        }
    }

    public static void anticipate() {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (cfg.enabled && cfg.adaptivePerformance && cfg.anticipateSpikes) {
            anticipateUntilNanos = System.nanoTime() + ANTICIPATION_WINDOW_NANOS;
        }
    }

    private static double spikeRatio(int targetFps) {
        if (frameCount < FRAME_WINDOW / 2) {
            return 0.0;
        }
        long spikeBudget = (long) (1_000_000_000.0 / targetFps * 1.5);
        int spikes = 0;
        for (int i = 0; i < frameCount; i++) {
            if (frameWork[i] > spikeBudget) {
                spikes++;
            }
        }
        return (double) spikes / frameCount;
    }

    public static void tick(MinecraftClient client) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.adaptivePerformance) {
            if (level.get() != 0) {
                reset();
            }
            return;
        }
        if (client.world == null) {
            reset();
            return;
        }
        if (!client.isWindowFocused() || client.currentScreen != null || client.isPaused()) {

            belowTicks = 0;
            aboveTicks = 0;
            return;
        }

        int fps = client.getCurrentFps();
        if (fps <= 0) {
            return;
        }
        smoothedFps = smoothedFps <= 0 ? fps : smoothedFps * 0.8 + fps * 0.2;

        int base = Math.min(cfg.governorBasePressure, MAX_LEVEL);
        if (level.get() < base) {
            level.set(base);
        }

        if (System.nanoTime() < anticipateUntilNanos && level.get() < ANTICIPATION_LEVEL) {
            level.set(ANTICIPATION_LEVEL);
            ParaMaxClient.LOGGER.info("Adaptive governor: anticipating spike, pressure level -> {}", level.get());
        }

        int target = cfg.targetFps;

        boolean underPressure = smoothedFps < target * ESCALATE_BAND
                || spikeRatio(target) > SPIKE_RATIO_THRESHOLD;
        if (underPressure) {
            aboveTicks = 0;
            if (++belowTicks >= ESCALATE_DWELL && level.get() < MAX_LEVEL) {
                int now = level.incrementAndGet();
                belowTicks = 0;
                ParaMaxClient.LOGGER.info("Adaptive governor: FPS {} / spikes {}% vs target {}, pressure level -> {}",
                        (int) smoothedFps, (int) (spikeRatio(target) * 100), target, now);
            }
        } else if (smoothedFps > target * RELAX_BAND) {
            belowTicks = 0;
            if (++aboveTicks >= RELAX_DWELL && level.get() > base) {
                int now = level.decrementAndGet();
                aboveTicks = 0;
                ParaMaxClient.LOGGER.info("Adaptive governor: headroom recovered, pressure level -> {}", now);
            }
        } else {
            belowTicks = 0;
            aboveTicks = 0;
        }
    }

    public static void reset() {
        level.set(0);
        anticipateUntilNanos = 0;
        belowTicks = 0;
        aboveTicks = 0;
        smoothedFps = 0;
        frameCount = 0;
        frameIndex = 0;
    }

    public static int level() {
        return level.get();
    }

    private static boolean active(ParaMaxConfig cfg) {
        return level.get() > 0 && cfg.adaptivePerformance;
    }

    public static double particleMultiplier(ParaMaxConfig cfg) {
        if (!cfg.throttleParticles) {
            return 1.0;
        }
        return cfg.particleMultiplier * (active(cfg) ? PARTICLE_FACTOR[level.get()] : 1.0);
    }

    public static boolean halfRateTextureAnimations(ParaMaxConfig cfg) {
        return cfg.halfRateTextureAnimations
                || (active(cfg) && level.get() >= HALF_RATE_ANIMATION_LEVEL);
    }

    public static double maxEntityRenderDistance(ParaMaxConfig cfg) {
        if (!cfg.entityDistanceCulling) {
            return NO_LIMIT;
        }
        return cfg.maxEntityRenderDistance * (active(cfg) ? ENTITY_DISTANCE_FACTOR[level.get()] : 1.0);
    }

    public static double maxBlockEntityRenderDistance(ParaMaxConfig cfg) {
        if (!cfg.blockEntityDistanceCulling) {
            return NO_LIMIT;
        }
        return cfg.maxBlockEntityRenderDistance * (active(cfg) ? BLOCK_ENTITY_DISTANCE_FACTOR[level.get()] : 1.0);
    }
}
