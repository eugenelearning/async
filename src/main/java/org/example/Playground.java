package org.example;

import org.example.threadpool.Benchmark;
import org.example.threadpool.CustomThreadPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Playground {
    private static final Logger logger = Logger.getLogger(Playground.class.getName());

    public void benchmark() {
        try {
            Benchmark.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void demo() {
        CustomThreadPool pool = new CustomThreadPool(
                2,
                2,
                1,
                5,
                TimeUnit.SECONDS,
                5
        );

        // Submit some tasks
        for (int i = 0; i < 100; i++) {
            int taskId = i;

            pool.submit(() -> {
                logger.info(String.format("Task %d started", taskId));

                TimeUnit.SECONDS.sleep(2);

                logger.info(String.format("Task %d completed", taskId));

                return null;
            });
        }

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("Initiating shutdown...");
        pool.shutdown();
    }

    public void run() {
        benchmark();
    }
}
