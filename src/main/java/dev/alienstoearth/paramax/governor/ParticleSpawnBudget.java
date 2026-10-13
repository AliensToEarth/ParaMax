package dev.alienstoearth.paramax.governor;

import java.util.concurrent.atomic.AtomicInteger;

public final class ParticleSpawnBudget {

    private final AtomicInteger spawnedThisTick = new AtomicInteger();

    public void reset() {
        spawnedThisTick.set(0);
    }

    public boolean tryAcquire(int limit) {
        return spawnedThisTick.incrementAndGet() <= limit;
    }
}
