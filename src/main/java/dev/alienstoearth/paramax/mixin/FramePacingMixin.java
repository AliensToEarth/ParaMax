package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.ParaMaxState;
import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(MinecraftClient.class)
public abstract class FramePacingMixin {

    @Unique private static final int WINDOW = 64;

    @Unique private static final int MIN_RENDER_SAMPLES = 24;

    @Unique private static final long SPIN_TAIL_NANOS = 1_500_000L;

    @Unique private static final long TARGET_HYSTERESIS_NANOS = 500_000L;
    @Unique private static final long MAX_WAIT_NANOS = 20_000_000L;

    @Unique private final long[] paramax$work = new long[WINDOW];
    @Unique private final boolean[] paramax$tickFrame = new boolean[WINDOW];
    @Unique private final long[] paramax$scratch = new long[WINDOW];
    @Unique private int paramax$index;
    @Unique private int paramax$count;

    @Unique private long paramax$lastStart;
    @Unique private long paramax$lastTicksSeen;
    @Unique private long paramax$target;
    @Unique private double paramax$workEma;

    @Inject(method = "render", at = @At("HEAD"))
    private void paramax$pace(boolean tick, CallbackInfo ci) {
        ParaMaxState.frames++;

        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled) {
            paramax$lastStart = 0L;
            return;
        }

        MinecraftClient client = (MinecraftClient) (Object) this;
        boolean throttled = !client.isWindowFocused()
                || (client.currentScreen != null && (client.world == null || client.isPaused()));

        long now = System.nanoTime();
        if (throttled) {

            paramax$lastStart = 0L;
            paramax$count = 0;
            paramax$index = 0;
            paramax$target = 0L;
            paramax$workEma = 0.0;
            ParaMaxState.capacityFps = 0;
            ParaMaxState.pacingTargetNanos = 0L;
            return;
        }

        long ticksNow = ParaMaxState.clientTicks.get();
        boolean previousFrameTicked = ticksNow != paramax$lastTicksSeen;
        paramax$lastTicksSeen = ticksNow;

        if (paramax$lastStart != 0L) {
            long work = now - paramax$lastStart;
            if (work > 0L) {
                paramax$work[paramax$index] = work;
                paramax$tickFrame[paramax$index] = previousFrameTicked;
                paramax$index = (paramax$index + 1) % WINDOW;
                if (paramax$count < WINDOW) {
                    paramax$count++;
                }
                PerformanceGovernor.recordFrame(work);

                paramax$workEma = paramax$workEma <= 0.0 ? work : paramax$workEma * 0.9 + work * 0.1;
                ParaMaxState.capacityFps = (int) (1_000_000_000.0 / paramax$workEma);
            }
        }

        if (!cfg.framePacing || client.world == null || paramax$count < WINDOW) {
            paramax$lastStart = now;
            ParaMaxState.pacingTargetNanos = 0L;
            return;
        }

        long candidate = paramax$renderOnlyPercentile();

        if (paramax$target == 0L || Math.abs(candidate - paramax$target) > TARGET_HYSTERESIS_NANOS) {
            paramax$target = candidate;
        }

        long minFpsBudget = 1_000_000_000L / Math.max(10, cfg.pacingMinFps);
        if (paramax$target >= minFpsBudget) {
            paramax$lastStart = now;
            ParaMaxState.pacingTargetNanos = 0L;
            return;
        }
        ParaMaxState.pacingTargetNanos = paramax$target;

        long deadline = paramax$lastStart + paramax$target;
        long wait = deadline - now;
        if (wait > 1_000_000L) {
            if (wait > MAX_WAIT_NANOS) {
                wait = MAX_WAIT_NANOS;
                deadline = now + wait;
            }
            paramax$preciseWait(deadline, wait);
            paramax$lastStart = System.nanoTime();
        } else {
            paramax$lastStart = now;
        }
    }

    @Unique
    private long paramax$renderOnlyPercentile() {
        int n = 0;
        for (int i = 0; i < WINDOW; i++) {
            if (!paramax$tickFrame[i]) {
                paramax$scratch[n++] = paramax$work[i];
            }
        }
        if (n < MIN_RENDER_SAMPLES) {
            System.arraycopy(paramax$work, 0, paramax$scratch, 0, WINDOW);
            n = WINDOW;
        }
        Arrays.sort(paramax$scratch, 0, n);
        return paramax$scratch[(int) (n * 0.85)];
    }

    @Unique
    private static void paramax$preciseWait(long deadline, long wait) {
        long sleepable = wait - SPIN_TAIL_NANOS;
        if (sleepable > 0L) {
            try {
                Thread.sleep(sleepable / 1_000_000L, (int) (sleepable % 1_000_000L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        while (System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
    }
}
