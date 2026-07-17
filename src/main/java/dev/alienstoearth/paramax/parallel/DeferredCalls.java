package dev.alienstoearth.paramax.parallel;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class DeferredCalls {

    private static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<>();

    private DeferredCalls() {
    }

    public static void defer(Runnable call) {
        QUEUE.add(call);
    }

    public static void drain() {
        Runnable call;
        while ((call = QUEUE.poll()) != null) {
            call.run();
        }
    }
}
