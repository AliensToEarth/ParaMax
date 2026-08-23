package dev.alienstoearth.paramax.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WeatherRenderSkipMixin {

    @Inject(method = "addWeatherPass", at = @At("HEAD"), cancellable = true)
    private void paramax$skipWeather(FrameGraphBuilder frameGraphBuilder, GpuBufferSlice fogBuffer,
                                     CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (cfg.enabled && cfg.skipWeatherRendering) {
            ci.cancel();
        }
    }
}
