package org.example.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CustomThreadPool implements CustomExecutor {
    private static final Logger logger = Logger.getLogger(CustomThreadPool.class.getName());

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int minSpareThreads;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;

    private final List<CustomWorker> workers;
    private final List<BlockingQueue<Runnable>> queues;
    private final AtomicInteger activeThreads;
    private final AtomicInteger nextQueueIndex;
    private final AtomicBoolean isShutdown;
    private final CustomThreadFactory threadFactory;

    public CustomThreadPool(
            int corePoolSize,
            int maxPoolSize,
            int minSpareThreads,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueSize
    ) {
        //logger.setLevel(Level.OFF);

        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.minSpareThreads = minSpareThreads;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;

        this.workers = new ArrayList<>();
        this.queues = new ArrayList<>();
        this.activeThreads = new AtomicInteger(0);
        this.nextQueueIndex = new AtomicInteger(0);
        this.isShutdown = new AtomicBoolean(false);
        this.threadFactory = new CustomThreadFactory();

        for (int i = 0; i < corePoolSize; i++) {
            addWorker();
        }
    }

    private synchronized void addWorker() {
        if (workers.size() >= maxPoolSize) {
            return;
        }

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueSize);

        queues.add(queue);

        CustomWorker worker = new CustomWorker(
                queue,
                this.threadFactory,
                workers,
                logger,
                this.corePoolSize,
                this.activeThreads,
                this.keepAliveTime,
                this.timeUnit
        );

        workers.add(worker);
        worker.thread.start();

        logger.info(String.format("[Pool] Created new worker: %s", worker.thread.getName()));
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown.get()) {
            throw new RejectedExecutionException("ThreadPool is shutdown");
        }

        // Подсыпем воркеров по мере надобности
        if (activeThreads.get() < minSpareThreads && workers.size() < maxPoolSize) {
            addWorker();
        }

        // Пока раунд-робин
        // подумать о реализации стратегии сложнее
        int queueIndex = nextQueueIndex.getAndIncrement() % queues.size();
        BlockingQueue<Runnable> queue = queues.get(queueIndex);

        try {
            if (!queue.offer(command, 100, TimeUnit.MILLISECONDS)) {
                logger.warning(String.format("[Rejected] Task %s was rejected due to overload!", command));
                throw new RejectedExecutionException("Queue is full");
            }
            logger.info(String.format("[Pool] Task accepted into queue #%d: %s", queueIndex, command));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RejectedExecutionException("Waiting for queue free space", e);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("[Pool] Initiating shutdown...");
            for (CustomWorker worker : workers) {
                if (worker != null) {
                    worker.shutdownNow();
                }
            }
        }
    }

    @Override
    public void shutdownNow() {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("[Pool] Initiating immediate shutdown...");
            for (CustomWorker worker : workers) {
                if (worker != null) {
                    worker.shutdownNow();
                }
            }
        }
    }
}