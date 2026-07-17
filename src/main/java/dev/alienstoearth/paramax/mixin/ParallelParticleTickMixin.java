package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.DeferredCalls;
import dev.alienstoearth.paramax.parallel.ParallelEngine;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(ParticleRenderer.class)
public abstract class ParallelParticleTickMixin {

    @Shadow @Final protected ParticleManager particleManager;
    @Shadow @Final protected Queue<Particle> particles;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void paramax$parallelTick(CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.parallelParticles) {
            return;
        }

        int size = this.particles.size();
        if (size < cfg.parallelParticleThreshold) {
            return;
        }
        ci.cancel();

        Particle[] snapshot = this.particles.toArray(new Particle[0]);
        int workers = Math.max(1, ParallelEngine.workerCount());
        int stripe = (snapshot.length + workers - 1) / workers;

        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicReference<Particle> failedParticle = new AtomicReference<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>(workers);

        for (int start = 0; start < snapshot.length; start += stripe) {
            final int lo = start;
            final int hi = Math.min(start + stripe, snapshot.length);
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = lo; i < hi; i++) {
                    if (failure.get() != null) {
                        return;
                    }
                    Particle particle = snapshot[i];
                    try {
                        particle.tick();
                    } catch (Throwable t) {
                        if (failure.compareAndSet(null, t)) {
                            failedParticle.set(particle);
                        }
                        return;
                    }
                }
            }, ParallelEngine::execute));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        DeferredCalls.drain();

        Throwable crash = failure.get();
        if (crash != null) {

            CrashReport report = CrashReport.create(crash, "Ticking Particle (ParaMax parallel)");
            CrashReportSection section = report.addElement("Particle being ticked");
            Particle particle = failedParticle.get();
            section.add("Particle", particle::toString);
            section.add("Particle Type", particle.textureSheet()::toString);
            throw new CrashException(report);
        }

        ParticleManagerAccessor manager = (ParticleManagerAccessor) this.particleManager;
        Iterator<Particle> it = this.particles.iterator();
        while (it.hasNext()) {
            Particle particle = it.next();
            if (!particle.isAlive()) {
                particle.getGroup().ifPresent(group -> manager.paramax$addTo(group, -1));
                it.remove();
            }
        }
    }
}
