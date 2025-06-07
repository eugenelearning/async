package org.example.rxjava.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IoScheduler implements Scheduler {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

    @Override
    public Worker createWorker() {
        return new Worker() {
            @Override
            public void execute(Runnable task) {
                executor.execute(task);
            }

            @Override
            public void dispose() {
                executor.shutdown();
            }
        };
    }
}
