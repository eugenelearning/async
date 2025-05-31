package org.example.threadpool;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CustomWorker implements Runnable {
    private final Logger logger;
    private final BlockingQueue<Runnable> queue;
    private final int corePoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    final Thread thread;
    private final AtomicInteger activeThreads;
    private final List workers;
    private volatile boolean running = true;

    CustomWorker(
            BlockingQueue<Runnable> queue,
            CustomThreadFactory threadFactory,
            List workers,
            Logger logger,
            int corePoolSize,
            AtomicInteger activeThreads,
            long keepAliveTime,
            TimeUnit timeUnit
    ) {
        this.queue = queue;
        this.corePoolSize = corePoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.logger = logger;
        this.workers = workers;
        this.thread = threadFactory.newThread(this);
        this.activeThreads = activeThreads;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Runnable task = queue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    activeThreads.incrementAndGet();
                    try {
                        logger.info(String.format("[Worker] %s р-р-р-работаем %s",
                                Thread.currentThread().getName(), task));
                        task.run();
                    } finally {
                        activeThreads.decrementAndGet();
                    }
                } else if (workers.size() > corePoolSize) {
                    logger.info(String.format("[Worker] %s делать нечего, ждемс..",
                            Thread.currentThread().getName()));
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            workers.remove(this);
            logger.info(String.format("[Worker] %s отваливаюсь.",
                    Thread.currentThread().getName()));
        }
    }

    void shutdown() {
        running = false;
    }

    void shutdownNow() {
        running = false;
        thread.interrupt();
    }
}
