package com.shan.concurrency.threadspatterns.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Analysis tool to understand ForkJoinPool thread reuse behavior
 */
public class ThreadPoolAnalysis {

    public static void main(String[] args) throws Exception {
        System.out.println("=== ForkJoinPool Thread Reuse Analysis ===\n");

        // 1. Show pool configuration
        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        System.out.println("ForkJoinPool Configuration:");
        System.out.println("  - Pool Size: " + commonPool.getParallelism());
        System.out.println("  - Available Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  - Formula: processors - 1 = " + (Runtime.getRuntime().availableProcessors() - 1));
        System.out.println();

        // 2. Demonstrate thread reuse with sequential operations
        System.out.println("--- Sequential Operations (thenCompose) ---");
        long start = System.currentTimeMillis();

        CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.printf("[%s] Task 1 STARTED (800ms)\n", threadName);
            sleep(800);
            System.out.printf("[%s] Task 1 COMPLETED\n", threadName);
            return "Result-1";
        }).thenCompose(r1 -> {
            String threadName = Thread.currentThread().getName();
            System.out.printf("[%s] thenCompose callback: submitting Task 2\n", threadName);

            return CompletableFuture.supplyAsync(() -> {
                String thread2 = Thread.currentThread().getName();
                System.out.printf("[%s] Task 2 STARTED (1000ms) - Pool has idle threads!\n", thread2);
                sleep(1000);
                System.out.printf("[%s] Task 2 COMPLETED\n", thread2);
                return "Result-2";
            });
        }).thenCompose(r2 -> {
            String threadName = Thread.currentThread().getName();
            System.out.printf("[%s] thenCompose callback: submitting Task 3\n", threadName);

            return CompletableFuture.supplyAsync(() -> {
                String thread3 = Thread.currentThread().getName();
                System.out.printf("[%s] Task 3 STARTED (600ms) - Reusing idle thread!\n", thread3);
                sleep(600);
                System.out.printf("[%s] Task 3 COMPLETED\n", thread3);
                return "Result-3";
            });
        });

        result.get();
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("\nTotal time: %dms (sequential)\n\n", elapsed);

        // 3. Show thread state timeline
        System.out.println("--- Thread Timeline Explanation ---");
        System.out.println("Time 0ms:    worker-2 picks up Task 1");
        System.out.println("             worker-1, worker-3, ... are IDLE");
        System.out.println();
        System.out.println("Time 800ms:  worker-2 completes Task 1");
        System.out.println("             worker-2 becomes IDLE");
        System.out.println("             Task 2 submitted → worker-1 picks it up (first idle)");
        System.out.println();
        System.out.println("Time 1800ms: worker-1 completes Task 2");
        System.out.println("             worker-1 becomes IDLE");
        System.out.println("             Task 3 submitted → worker-2 picks it up (was idle since 800ms)");
        System.out.println();
        System.out.println("Time 2400ms: worker-2 completes Task 3");
        System.out.println();

        // 4. Demonstrate why new workers aren't created
        System.out.println("--- Why No New Threads? ---");
        System.out.println("1. Pool size is FIXED at " + commonPool.getParallelism() + " threads");
        System.out.println("2. Threads are REUSED from the pool when they become idle");
        System.out.println("3. Sequential operations mean tasks are submitted ONE AT A TIME");
        System.out.println("4. There are always idle threads available to pick up new tasks");
        System.out.println();

        // 5. Show what happens with many parallel tasks
        System.out.println("--- Submitting 10 Parallel Tasks ---");
        CompletableFuture<?>[] parallelTasks = new CompletableFuture[10];

        for (int i = 0; i < 10; i++) {
            final int taskNum = i + 1;
            parallelTasks[i] = CompletableFuture.supplyAsync(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.printf("[%s] Parallel Task %d executing\n", threadName, taskNum);
                sleep(100);
                return "Done-" + taskNum;
            });
        }

        CompletableFuture.allOf(parallelTasks).get();

        System.out.println("\nNotice: Even with 10 tasks, only " + commonPool.getParallelism() +
                           " worker threads are used (pool size limit)");
        System.out.println("Tasks queue up and wait for available threads.");

        // Give time for threads to finish logging
        TimeUnit.MILLISECONDS.sleep(100);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
