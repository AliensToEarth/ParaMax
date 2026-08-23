package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WeatherRenderSkipMixin {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void paramax$skipWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog,
                                     CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (cfg.enabled && cfg.skipWeatherRendering) {
            ci.cancel();
        }
    }
}
