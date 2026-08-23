package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WeatherRenderSkipMixin {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void paramax$skipWeather(LightmapTextureManager lightmap, float tickDelta,
                                     double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (cfg.enabled && cfg.skipWeatherRendering) {
            ci.cancel();
        }
    }
}
