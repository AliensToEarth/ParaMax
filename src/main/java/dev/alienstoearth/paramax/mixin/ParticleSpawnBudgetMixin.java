package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ParticleManager.class)
public abstract class ParticleSpawnBudgetMixin {

    @Unique private int paramax$spawnedThisTick;

    @Inject(method = "tick", at = @At("HEAD"))
    private void paramax$resetBudget(CallbackInfo ci) {
        this.paramax$spawnedThisTick = 0;
    }

    @Redirect(method = "tick",
            at = @At(value = "INVOKE", target = "Ljava/util/Queue;poll()Ljava/lang/Object;"))
    private Object paramax$budgetedPoll(Queue<Particle> queue) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.budgetParticleSpawns) {
            return queue.poll();
        }
        if (++this.paramax$spawnedThisTick > cfg.particleSpawnBudget) {
            return null;
        }
        return queue.poll();
    }
}
