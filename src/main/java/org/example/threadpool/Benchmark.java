package org.example.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Benchmark {
    private static final int BENCHMARK_ITERATIONS = 10;
    private static final int TASKS_PER_ITERATION = 100;
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 8;
    private static final int QUEUE_SIZE = 100;

    public static void run() throws Exception {
        List<BenchmarkResult> results = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            results.add(runBenchmark());
        }

        printResults(results);
    }

    private static BenchmarkResult runBenchmark() throws Exception {
        // Test CustomThreadPool
        CustomThreadPool customPool = new CustomThreadPool(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                CORE_POOL_SIZE,
                5,
                TimeUnit.SECONDS,
                QUEUE_SIZE
        );

        // Test ThreadPoolExecutor
        ThreadPoolExecutor standardPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                5,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_SIZE)
        );

        long customStart = System.nanoTime();
        runTasks(customPool);
        long customTime = System.nanoTime() - customStart;

        long standardStart = System.nanoTime();
        runTasks(standardPool);
        long standardTime = System.nanoTime() - standardStart;

        customPool.shutdown();
        standardPool.shutdown();

        return new BenchmarkResult(customTime, standardTime);
    }

    private static void runTasks(Executor pool) throws Exception {
        CountDownLatch latch = new CountDownLatch(TASKS_PER_ITERATION);
        AtomicInteger completedTasks = new AtomicInteger(0);

        for (int i = 0; i < TASKS_PER_ITERATION; i++) {
            pool.execute(() -> {
                try {
                    Thread.sleep(100);
                    completedTasks.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
    }

    private static void printResults(List<BenchmarkResult> results) {
        double avgCustomTime = results.stream()
                .mapToLong(r -> r.customTime)
                .average()
                .orElse(0) / 1_000_000.0;

        double avgStandardTime = results.stream()
                .mapToLong(r -> r.standardTime)
                .average()
                .orElse(0) / 1_000_000.0;

        System.out.println("\nBenchmark Results:");
        System.out.println("------------------");
        System.out.printf("Average CustomThreadPool time: %.2f ms\n", avgCustomTime);
        System.out.printf("Average ThreadPoolExecutor time: %.2f ms\n", avgStandardTime);
        System.out.printf("Performance difference: %.2f%%\n",
                ((avgCustomTime - avgStandardTime) / avgStandardTime) * 100);
    }

    private record BenchmarkResult(long customTime, long standardTime) {
    }
} 