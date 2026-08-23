package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(ParticleEngine.class)
public class ParticleThrottleMixin {

    @Inject(method = "add(Lnet/minecraft/client/particle/Particle;)V",
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
        var camera = Minecraft.getInstance().gameRenderer.mainCamera();
        if (camera == null || !camera.isInitialized()) {
            return false;
        }
        Vec3 center = particle.getBoundingBox().getCenter();
        return camera.position().distanceToSqr(center) > max * max;
    }
}
