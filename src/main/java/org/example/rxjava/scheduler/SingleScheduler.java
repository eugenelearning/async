package org.example.rxjava.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SingleScheduler implements Scheduler {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

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
