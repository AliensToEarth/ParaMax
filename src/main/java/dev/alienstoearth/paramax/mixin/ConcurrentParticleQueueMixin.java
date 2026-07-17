package dev.alienstoearth.paramax.mixin;

import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(ParticleManager.class)
public abstract class ConcurrentParticleQueueMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void paramax$useConcurrentQueue(CallbackInfo ci) {
        ((ParticleManagerAccessor) this).paramax$setNewParticles(new ConcurrentLinkedQueue<>());
    }
}
