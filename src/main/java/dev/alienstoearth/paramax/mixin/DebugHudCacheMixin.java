package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugHudCacheMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow protected abstract void renderLines(GuiGraphics context, List<String> text, boolean left);

    @Shadow public abstract boolean showProfilerChart();

    @Shadow public abstract boolean showFpsCharts();

    @Unique private List<String> paramax$cachedLeft;
    @Unique private List<String> paramax$cachedRight;
    @Unique private long paramax$lastBuildMs;
    @Unique private boolean paramax$drawingCached;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void paramax$serveCached(GuiGraphics context, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.throttleDebugHud) {
            return;
        }

        if (this.showProfilerChart() || this.showFpsCharts()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.paramax$cachedLeft == null || now - this.paramax$lastBuildMs >= cfg.debugHudIntervalMs) {
            return;
        }

        if (!this.minecraft.isGameLoadFinished()
                || (this.minecraft.options.hideGui && this.minecraft.screen == null)
                || this.minecraft.debugEntries.getCurrentlyEnabled().isEmpty()) {
            this.paramax$cachedLeft = null;
            this.paramax$cachedRight = null;
            return;
        }

        ci.cancel();
        context.nextStratum();
        this.paramax$drawingCached = true;
        try {
            this.renderLines(context, this.paramax$cachedLeft, true);
            this.renderLines(context, this.paramax$cachedRight, false);
        } finally {
            this.paramax$drawingCached = false;
        }
    }

    @Inject(method = "renderLines", at = @At("HEAD"))
    private void paramax$capture(GuiGraphics context, List<String> text, boolean left, CallbackInfo ci) {
        if (this.paramax$drawingCached) {
            return;
        }
        if (left) {

            if (!text.isEmpty()) {
                paramax$appendStatusLine(text);
            }
            this.paramax$cachedLeft = new ArrayList<>(text);
        } else {
            this.paramax$cachedRight = new ArrayList<>(text);
            this.paramax$lastBuildMs = System.currentTimeMillis();
        }
    }

    @Unique
    private static void paramax$appendStatusLine(List<String> text) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled) {
            return;
        }
        StringBuilder line = new StringBuilder("ParaMax:");
        int capacity = dev.alienstoearth.paramax.ParaMaxState.capacityFps;
        if (capacity > 0) {
            line.append(" capacity ~").append(capacity).append(" fps");
        }
        long pacing = dev.alienstoearth.paramax.ParaMaxState.pacingTargetNanos;
        if (pacing > 0L) {
            line.append(String.format(java.util.Locale.ROOT, ", pacing %.1f ms", pacing / 1_000_000.0));
        } else if (cfg.framePacing) {
            line.append(", pacing idle");
        }
        line.append(", governor L")
                .append(dev.alienstoearth.paramax.governor.PerformanceGovernor.level());
        text.add(line.toString());
    }
}
