package dev.alienstoearth.paramax;

import java.util.concurrent.atomic.AtomicLong;

public final class ParaMaxState {

    public static final AtomicLong clientTicks = new AtomicLong();

    public static long frames;

    public static volatile int capacityFps;

    public static volatile long pacingTargetNanos;

    private ParaMaxState() {
    }
}
