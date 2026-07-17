package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(ParticleManager.class)
public class ParticleThrottleMixin {

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V",
            at = @At("HEAD"), cancellable = true)
    private void paramax$throttle(Particle particle, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled) {
            return;
        }

        if (cfg.particleCulling && paramax$beyondCullDistance(particle, cfg.maxParticleDistance)) {
            ci.cancel();
            return;
        }

        double keep = PerformanceGovernor.particleMultiplier(cfg);
        if (keep >= 1.0) {
            return;
        }
        if (keep <= 0.0 || ThreadLocalRandom.current().nextDouble() > keep) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean paramax$beyondCullDistance(Particle particle, double max) {
        var camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (camera == null || !camera.isReady()) {
            return false;
        }
        Vec3d center = particle.getBoundingBox().getCenter();
        return camera.getCameraPos().squaredDistanceTo(center) > max * max;
    }
}
