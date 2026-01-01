package com.shan.concurrency.threadspatterns.custompoolpatterns;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CustomPoolSequentialDemo - Sequential operations with custom thread pool
 *
 * Demonstrates:
 * 1. Sequential chaining with thenCompose() using custom pool
 * 2. Thread reuse within custom pool
 * 3. How to ensure all stages use custom pool
 * 4. Comparison with common pool sequential operations
 */
@Slf4j
public class CustomPoolSequentialDemo {

    public void demonstrate() {
        log.info("=== Custom Pool Sequential Operations Demo ===\n");

        ExecutorService customPool = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r);
            t.setName("CustomSeq-" + t.getId());
            return t;
        });

        try {
            demonstrateSequentialWithCommonPool();
            log.info("\n" + "=".repeat(70) + "\n");
            demonstrateSequentialWithCustomPool(customPool);
            log.info("\n" + "=".repeat(70) + "\n");
            demonstrateProperCustomPoolChaining(customPool);

        } finally {
            shutdownPool(customPool);
        }
    }

    /**
     * Example 1: Sequential with common pool (for comparison)
     */
    private void demonstrateSequentialWithCommonPool() {
        log.info("--- Example 1: Sequential Chaining (Common Pool) ---");
        long start = System.currentTimeMillis();

        CompletableFuture<String> result = CompletableFuture
                .supplyAsync(() -> {
                    log.info("[{}] Step 1: Fetch user profile (500ms)",
                            Thread.currentThread().getName());
                    sleep(500);
                    return "UserProfile-123";
                })
                .thenCompose(profile -> CompletableFuture.supplyAsync(() -> {
                    log.info("[{}] Step 2: Fetch orders for {} (700ms)",
                            Thread.currentThread().getName(), profile);
                    sleep(700);
                    return "Orders-" + profile;
                }))
                .thenCompose(orders -> CompletableFuture.supplyAsync(() -> {
                    log.info("[{}] Step 3: Generate recommendations for {} (400ms)",
                            Thread.currentThread().getName(), orders);
                    sleep(400);
                    return "Recommendations-" + orders;
                }));

        try {
            String finalResult = result.get();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[{}] Final result: {}", Thread.currentThread().getName(), finalResult);
            log.info("Total time: {}ms", elapsed);
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    /**
     * Example 2: Sequential with custom pool (WRONG WAY - some stages use common pool)
     */
    private void demonstrateSequentialWithCustomPool(ExecutorService customPool) {
        log.info("--- Example 2: Sequential with Custom Pool (MIXED - Some stages use common pool!) ---");
        long start = System.currentTimeMillis();

        CompletableFuture<String> result = CompletableFuture
                .supplyAsync(() -> {
                    log.info("[{}] Step 1: Fetch user profile (500ms)",
                            Thread.currentThread().getName());
                    sleep(500);
                    return "UserProfile-456";
                }, customPool) // ✅ Uses custom pool
                .thenCompose(profile -> CompletableFuture.supplyAsync(() -> {
                    // ❌ WARNING: This uses COMMON POOL (no executor specified)!
                    log.info("[{}] Step 2: Fetch orders (700ms) - OOPS! Common pool!",
                            Thread.currentThread().getName());
                    sleep(700);
                    return "Orders-" + profile;
                }))
                .thenCompose(orders -> CompletableFuture.supplyAsync(() -> {
                    // ❌ WARNING: This also uses COMMON POOL!
                    log.info("[{}] Step 3: Generate recommendations (400ms) - OOPS! Common pool!",
                            Thread.currentThread().getName());
                    sleep(400);
                    return "Recommendations-" + orders;
                }));

        try {
            String finalResult = result.get();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[{}] Final result: {}", Thread.currentThread().getName(), finalResult);
            log.info("Total time: {}ms", elapsed);
            log.info("⚠️  Notice: Only Step 1 used custom pool! Steps 2 & 3 used common pool.");
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    /**
     * Example 3: Sequential with ALL stages using custom pool (CORRECT WAY)
     */
    private void demonstrateProperCustomPoolChaining(ExecutorService customPool) {
        log.info("--- Example 3: Sequential with ALL stages on Custom Pool (CORRECT) ---");
        long start = System.currentTimeMillis();

        CompletableFuture<String> result = CompletableFuture
                .supplyAsync(() -> {
                    log.info("[{}] Step 1: Fetch user profile (500ms)",
                            Thread.currentThread().getName());
                    sleep(500);
                    return "UserProfile-789";
                }, customPool) // ✅ Uses custom pool
                .thenCompose(profile -> CompletableFuture.supplyAsync(() -> {
                    log.info("[{}] Step 2: Fetch orders (700ms)",
                            Thread.currentThread().getName());
                    sleep(700);
                    return "Orders-" + profile;
                }, customPool)) // ✅ Explicitly use custom pool
                .thenCompose(orders -> CompletableFuture.supplyAsync(() -> {
                    log.info("[{}] Step 3: Generate recommendations (400ms)",
                            Thread.currentThread().getName());
                    sleep(400);
                    return "Recommendations-" + orders;
                }, customPool)); // ✅ Explicitly use custom pool

        try {
            String finalResult = result.get();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[{}] Final result: {}", Thread.currentThread().getName(), finalResult);
            log.info("Total time: {}ms", elapsed);
            log.info("✅ All steps executed on custom pool threads!");
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    private void shutdownPool(ExecutorService pool) {
        log.info("\n--- Shutting Down Custom Pool ---");
        pool.shutdown();
        try {
            pool.awaitTermination(5, TimeUnit.SECONDS);
            log.info("Custom pool shut down successfully");
        } catch (InterruptedException e) {
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
