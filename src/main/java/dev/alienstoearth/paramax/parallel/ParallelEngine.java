package dev.alienstoearth.paramax.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

public final class ParallelEngine {

    private static volatile ForkJoinPool pool;
    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    private ParallelEngine() {
    }

    public static synchronized void boot() {
        if (pool != null) {
            return;
        }
        int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

        ForkJoinPool.ForkJoinWorkerThreadFactory factory = p -> {
            ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
            t.setName("ParaMax-Worker-" + THREAD_ID.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        };

        pool = new ForkJoinPool(
                parallelism,
                factory,
                (thread, throwable) ->
                        System.err.println("[ParaMax] Uncaught in " + thread.getName() + ": " + throwable),
                 true);
    }

    private static ForkJoinPool pool() {
        ForkJoinPool p = pool;
        if (p == null) {
            boot();
            p = pool;
        }
        return p;
    }

    public static int workerCount() {
        return pool == null ? 0 : pool.getParallelism();
    }

    public static boolean isWorkerThread() {
        return Thread.currentThread().getName().startsWith("ParaMax-Worker-");
    }

    public static void execute(Runnable task) {
        pool().execute(task);
    }
}
