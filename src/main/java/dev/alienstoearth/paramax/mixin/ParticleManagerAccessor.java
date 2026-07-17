package dev.alienstoearth.paramax.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Queue;

@Mixin(ParticleManager.class)
public interface ParticleManagerAccessor {

    @Invoker("addTo")
    void paramax$addTo(ParticleGroup group, int count);

    @Accessor("newParticles")
    Queue<Particle> paramax$getNewParticles();

    @Mutable
    @Accessor("newParticles")
    void paramax$setNewParticles(Queue<Particle> queue);
}
