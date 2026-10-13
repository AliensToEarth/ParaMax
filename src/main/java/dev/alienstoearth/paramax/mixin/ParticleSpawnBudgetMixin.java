package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.ParticleSpawnBudget;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleSpawnBudgetMixin {

    @Unique
    private final ParticleSpawnBudget paramax$budget = new ParticleSpawnBudget();

    @Inject(method = "tick", at = @At("HEAD"))
    private void paramax$resetBudget(CallbackInfo ci) {
        paramax$budget.reset();
    }

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V",
            at = @At("HEAD"), cancellable = true, order = 1100)
    private void paramax$budgetedAdd(Particle particle, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.budgetParticleSpawns) {
            return;
        }
        if (!paramax$budget.tryAcquire(cfg.particleSpawnBudget)) {
            ci.cancel();
        }
    }
}
