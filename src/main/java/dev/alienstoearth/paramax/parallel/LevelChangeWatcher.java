package dev.alienstoearth.paramax.parallel;

import java.lang.ref.WeakReference;
import java.util.List;

public final class LevelChangeWatcher {

    private final List<LevelBoundCache> caches;
    private WeakReference<Object> lastLevel = new WeakReference<>(null);

    public LevelChangeWatcher(LevelBoundCache... caches) {
        this.caches = List.of(caches);
    }

    public void observe(Object currentLevel) {
        if (lastLevel.get() == currentLevel) {
            return;
        }
        lastLevel = new WeakReference<>(currentLevel);
        caches.forEach(LevelBoundCache::clear);
    }
}
