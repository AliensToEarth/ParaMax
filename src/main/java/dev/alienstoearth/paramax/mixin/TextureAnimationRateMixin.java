package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.ParaMaxState;
import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.texture.SpriteContents$Animator")
public abstract class TextureAnimationRateMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void paramax$halfRate(CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (cfg.enabled && PerformanceGovernor.halfRateTextureAnimations(cfg)
                && (ParaMaxState.clientTicks.get() & 1L) == 1L) {
            ci.cancel();
        }
    }
}
