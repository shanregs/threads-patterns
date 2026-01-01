package com.shan.concurrency.threadspatterns.custompoolpatterns;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CustomPoolBasicDemo - Basic usage of custom ExecutorService with CompletableFuture
 *
 * Demonstrates:
 * 1. Creating a custom thread pool
 * 2. Using custom pool with supplyAsync()
 * 3. Comparing with common pool behavior
 * 4. Proper shutdown of custom pool
 */
@Slf4j
public class CustomPoolBasicDemo {

    public void demonstrate() {
        log.info("=== Custom Pool Basic Demo ===\n");

        // Create custom thread pool with 4 threads
        ExecutorService customPool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setName("CustomPool-" + t.getId());
            return t;
        });

        try {
            demonstrateCommonPoolUsage();
            log.info("");
            demonstrateCustomPoolUsage(customPool);
            log.info("");
            demonstratePoolComparison(customPool);

        } finally {
            shutdownPool(customPool);
        }
    }

    /**
     * Example 1: Default behavior - uses ForkJoinPool.commonPool()
     */
    private void demonstrateCommonPoolUsage() {
        log.info("--- Example 1: Using Common Pool (Default) ---");

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("[{}] Executing on COMMON POOL", threadName);
            sleep(500);
            return "Result from common pool";
        });

        try {
            String result = future.get();
            log.info("Result: {}", result);
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    /**
     * Example 2: Using custom thread pool
     */
    private void demonstrateCustomPoolUsage(ExecutorService customPool) {
        log.info("--- Example 2: Using Custom Pool ---");

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("[{}] Executing on CUSTOM POOL", threadName);
            sleep(500);
            return "Result from custom pool";
        }, customPool); // Pass custom executor as second parameter

        try {
            String result = future.get();
            log.info("Result: {}", result);
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    /**
     * Example 3: Side-by-side comparison
     */
    private void demonstratePoolComparison(ExecutorService customPool) {
        log.info("--- Example 3: Pool Comparison (5 tasks each) ---");

        // Submit 5 tasks to common pool
        log.info("Submitting to COMMON pool:");
        for (int i = 1; i <= 5; i++) {
            final int taskNum = i;
            CompletableFuture.supplyAsync(() -> {
                log.info("[{}] Common pool task {}", Thread.currentThread().getName(), taskNum);
                sleep(100);
                return "Common-" + taskNum;
            });
        }

        sleep(200); // Let common pool tasks start

        // Submit 5 tasks to custom pool
        log.info("\nSubmitting to CUSTOM pool:");
        for (int i = 1; i <= 5; i++) {
            final int taskNum = i;
            CompletableFuture.supplyAsync(() -> {
                log.info("[{}] Custom pool task {}", Thread.currentThread().getName(), taskNum);
                sleep(100);
                return "Custom-" + taskNum;
            }, customPool);
        }

        sleep(500); // Wait for completion
    }

    private void shutdownPool(ExecutorService pool) {
        log.info("\n--- Shutting Down Custom Pool ---");
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Pool did not terminate in time, forcing shutdown");
                pool.shutdownNow();
            } else {
                log.info("Custom pool shut down successfully");
            }
        } catch (InterruptedException e) {
            log.error("Interrupted during shutdown", e);
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
