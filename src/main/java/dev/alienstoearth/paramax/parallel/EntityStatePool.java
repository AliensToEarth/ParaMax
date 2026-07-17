package dev.alienstoearth.paramax.parallel;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class EntityStatePool {

    private static final Map<EntityRenderer<?, ?>, Queue<EntityRenderState>> AVAILABLE =
            new ConcurrentHashMap<>();
    private static final Queue<PooledState> IN_FLIGHT = new ConcurrentLinkedQueue<>();

    private record PooledState(EntityRenderer<?, ?> renderer, EntityRenderState state) {
    }

    private EntityStatePool() {
    }

    public static EntityRenderState acquire(EntityRenderer<?, ?> renderer) {
        Queue<EntityRenderState> pool = AVAILABLE.get(renderer);
        EntityRenderState state = pool != null ? pool.poll() : null;
        if (state != null) {
            IN_FLIGHT.add(new PooledState(renderer, state));
        }
        return state;
    }

    public static void track(EntityRenderer<?, ?> renderer, EntityRenderState state) {
        IN_FLIGHT.add(new PooledState(renderer, state));
    }

    public static void recycle() {
        PooledState pooled;
        while ((pooled = IN_FLIGHT.poll()) != null) {
            AVAILABLE.computeIfAbsent(pooled.renderer(), r -> new ConcurrentLinkedQueue<>())
                    .add(pooled.state());
        }
    }

    public static void clear() {
        IN_FLIGHT.clear();
        AVAILABLE.clear();
    }

    public static boolean isEmpty() {
        return IN_FLIGHT.isEmpty() && AVAILABLE.isEmpty();
    }
}
