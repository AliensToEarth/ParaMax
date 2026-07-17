package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.parallel.DeferredCalls;
import dev.alienstoearth.paramax.parallel.ParallelEngine;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleSpawnDeferMixin {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void paramax$deferEffectSpawn(ParticleEffect parameters, double x, double y, double z,
                                          double velocityX, double velocityY, double velocityZ,
                                          CallbackInfoReturnable<Particle> cir) {
        if (ParallelEngine.isWorkerThread()) {
            ParticleManager self = (ParticleManager) (Object) this;
            DeferredCalls.defer(() -> self.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ));
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V",
            at = @At("HEAD"), cancellable = true)
    private void paramax$deferDirectSpawn(Particle particle, CallbackInfo ci) {
        if (ParallelEngine.isWorkerThread()) {
            ParticleManager self = (ParticleManager) (Object) this;
            DeferredCalls.defer(() -> self.addParticle(particle));
            ci.cancel();
        }
    }

    @Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V",
            at = @At("HEAD"), cancellable = true)
    private void paramax$deferEmitter(Entity entity, ParticleEffect parameters, CallbackInfo ci) {
        if (ParallelEngine.isWorkerThread()) {
            ParticleManager self = (ParticleManager) (Object) this;
            DeferredCalls.defer(() -> self.addEmitter(entity, parameters));
            ci.cancel();
        }
    }

    @Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V",
            at = @At("HEAD"), cancellable = true)
    private void paramax$deferTimedEmitter(Entity entity, ParticleEffect parameters, int maxAge, CallbackInfo ci) {
        if (ParallelEngine.isWorkerThread()) {
            ParticleManager self = (ParticleManager) (Object) this;
            DeferredCalls.defer(() -> self.addEmitter(entity, parameters, maxAge));
            ci.cancel();
        }
    }
}
