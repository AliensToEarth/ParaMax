package dev.alienstoearth.paramax.parallel;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;

import java.util.IdentityHashMap;
import java.util.Map;

public final class TemporalLodCache {

    private static final long SWEEP_INTERVAL = 600;
    private static final long STALE_AGE = 120;

    public static final class Entry {
        public EntityRenderState state;
        public long lastUpdateFrame;
        public long lastTouchedFrame;
    }

    private static final Map<Entity, Entry> CACHE = new IdentityHashMap<>();
    private static long lastSweep;

    private TemporalLodCache() {
    }

    public static Entry get(Entity entity, long frame) {
        maybeSweep(frame);
        Entry entry = CACHE.get(entity);
        if (entry != null) {
            entry.lastTouchedFrame = frame;
        }
        return entry;
    }

    public static void put(Entity entity, EntityRenderState state, long frame) {
        Entry entry = new Entry();
        entry.state = state;
        entry.lastUpdateFrame = frame;
        entry.lastTouchedFrame = frame;
        CACHE.put(entity, entry);
    }

    public static void clear() {
        CACHE.clear();
    }

    public static boolean isEmpty() {
        return CACHE.isEmpty();
    }

    private static void maybeSweep(long frame) {
        if (frame - lastSweep < SWEEP_INTERVAL) {
            return;
        }
        lastSweep = frame;
        CACHE.values().removeIf(entry -> frame - entry.lastTouchedFrame > STALE_AGE);
    }
}
