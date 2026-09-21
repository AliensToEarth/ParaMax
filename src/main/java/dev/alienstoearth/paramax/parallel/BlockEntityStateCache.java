package dev.alienstoearth.paramax.parallel;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

import java.util.IdentityHashMap;
import java.util.Map;

public final class BlockEntityStateCache {

    private static final long SWEEP_INTERVAL = 600;
    private static final long STALE_AGE = 120;

    private static final Map<BlockEntity, Entry> CACHE = new IdentityHashMap<>();
    private static long lastSweep;

    public static final class Entry {
        public BlockEntityRenderState state;
        public long lastTouchedFrame;
    }

    private BlockEntityStateCache() {
    }

    public static BlockEntityRenderState get(BlockEntity blockEntity, long frame) {
        maybeSweep(frame);
        Entry entry = CACHE.get(blockEntity);
        if (entry == null) {
            return null;
        }
        entry.lastTouchedFrame = frame;
        return entry.state;
    }

    public static void put(BlockEntity blockEntity, BlockEntityRenderState state, long frame) {
        Entry entry = new Entry();
        entry.state = state;
        entry.lastTouchedFrame = frame;
        CACHE.put(blockEntity, entry);
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
