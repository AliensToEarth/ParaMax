package dev.alienstoearth.paramax;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import dev.alienstoearth.paramax.parallel.ParallelEngine;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ParaMaxClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("ParaMax");

    @Override
    public void onInitializeClient() {

        ParaMaxConfig cfg = ParaMaxConfig.get();

        ParallelEngine.boot();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ParaMaxState.clientTicks.incrementAndGet();
            PerformanceGovernor.tick(client);
        });

        LOGGER.info("ParaMax initialised (enabled={}, workers={}, dynamicFps={}, entityCull={}, particleThrottle={})",
                cfg.enabled,
                ParallelEngine.workerCount(),
                cfg.dynamicFps,
                cfg.entityDistanceCulling,
                cfg.throttleParticles);
    }
}
