package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class DynamicFpsMixin {

    @Unique
    private long paramax$lastFrameNanos;

    @Inject(method = "render", at = @At("HEAD"))
    private void paramax$throttleUnfocused(boolean tick, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.dynamicFps) {
            return;
        }

        MinecraftClient client = (MinecraftClient) (Object) this;
        int targetFps;
        if (!client.isWindowFocused()) {
            targetFps = cfg.unfocusedFps;
        } else if (cfg.throttleMenus && client.currentScreen != null
                && (client.world == null || client.isPaused())) {
            targetFps = cfg.menuFps;
        } else {
            paramax$lastFrameNanos = 0L;
            return;
        }

        long frameBudgetNanos = 1_000_000_000L / Math.max(1, targetFps);
        long now = System.nanoTime();
        if (paramax$lastFrameNanos != 0L) {
            long elapsed = now - paramax$lastFrameNanos;
            long sleepNanos = frameBudgetNanos - elapsed;
            if (sleepNanos > 0L) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        paramax$lastFrameNanos = System.nanoTime();
    }
}
