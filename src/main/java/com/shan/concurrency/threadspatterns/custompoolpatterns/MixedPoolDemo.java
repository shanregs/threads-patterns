package com.shan.concurrency.threadspatterns.custompoolpatterns;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MixedPoolDemo - Using both Common Pool and Custom Pool together
 *
 * Demonstrates:
 * 1. When to use common pool vs custom pool
 * 2. Running tasks on different pools in the same application
 * 3. Combining results from both pools
 * 4. Best practices for mixed pool usage
 * 5. Thread isolation and resource management
 */
@Slf4j
public class MixedPoolDemo {

    public void demonstrate() {
        log.info("=== Mixed Pool Demo: Common Pool + Custom Pool ===\n");

        // Create custom pool for I/O-bound tasks (larger pool)
        ExecutorService ioPool = Executors.newFixedThreadPool(10, r -> {
            Thread t = new Thread(r);
            t.setName("IO-Pool-" + t.getId());
            return t;
        });

        // Create custom pool for CPU-bound tasks (smaller pool)
        ExecutorService cpuPool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setName("CPU-Pool-" + t.getId());
            return t;
        });

        try {
            demonstrateIsolation(ioPool, cpuPool);
            log.info("\n" + "=".repeat(70) + "\n");
            demonstrateMixedOperations(ioPool, cpuPool);
            log.info("\n" + "=".repeat(70) + "\n");
            demonstrateCombiningResults(ioPool, cpuPool);

        } finally {
            shutdownPool("IO Pool", ioPool);
            shutdownPool("CPU Pool", cpuPool);
        }
    }

    /**
     * Example 1: Pool isolation - different tasks on different pools
     */
    private void demonstrateIsolation(ExecutorService ioPool, ExecutorService cpuPool) {
        log.info("--- Example 1: Pool Isolation ---");
        log.info("Strategy: I/O tasks on I/O pool, CPU tasks on CPU pool, quick tasks on common pool\n");

        // I/O-bound task (database query) - use I/O pool
        CompletableFuture<String> dbQuery = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Executing DATABASE QUERY (I/O-bound, 800ms)",
                    Thread.currentThread().getName());
            sleep(800);
            return "DB_Result";
        }, ioPool);

        // CPU-bound task (heavy computation) - use CPU pool
        CompletableFuture<Integer> computation = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Executing HEAVY COMPUTATION (CPU-bound, 600ms)",
                    Thread.currentThread().getName());
            sleep(600);
            int result = 0;
            for (int i = 0; i < 100000; i++) result += i;
            return result;
        }, cpuPool);

        // Quick transformation - use common pool (no custom pool needed)
        CompletableFuture<String> quickTask = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Executing QUICK TASK (simple, 100ms)",
                    Thread.currentThread().getName());
            sleep(100);
            return "Quick_Result";
        }); // No executor = common pool

        try {
            CompletableFuture.allOf(dbQuery, computation, quickTask).get();
            log.info("\n✅ All tasks completed on their appropriate pools:");
            log.info("  - DB Query: {} (from I/O pool)", dbQuery.get());
            log.info("  - Computation: {} (from CPU pool)", computation.get());
            log.info("  - Quick Task: {} (from common pool)", quickTask.get());
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    /**
     * Example 2: Mixed operations - chaining across pools
     */
    private void demonstrateMixedOperations(ExecutorService ioPool, ExecutorService cpuPool) {
        log.info("--- Example 2: Mixed Operations - Chain across pools ---");
        log.info("Flow: I/O Pool → CPU Pool → Common Pool\n");

        long start = System.currentTimeMillis();

        CompletableFuture<String> result = CompletableFuture
                // Step 1: Fetch data (I/O-bound) - use I/O pool
                .supplyAsync(() -> {
                    log.info("[{}] Step 1: FETCH data from API (I/O-bound, 500ms)",
                            Thread.currentThread().getName());
                    sleep(500);
                    return "RawData";
                }, ioPool)

                // Step 2: Process data (CPU-bound) - switch to CPU pool
                .thenComposeAsync(data -> CompletableFuture.supplyAsync(() -> {
                    log.info("[{}] Step 2: PROCESS data (CPU-bound, 700ms)",
                            Thread.currentThread().getName());
                    sleep(700);
                    return "ProcessedData-" + data;
                }, cpuPool))

                // Step 3: Format result (quick) - use common pool
                .thenApplyAsync(processedData -> {
                    log.info("[{}] Step 3: FORMAT result (quick, 200ms)",
                            Thread.currentThread().getName());
                    sleep(200);
                    return "Formatted: " + processedData;
                });

        try {
            String finalResult = result.get();
            long elapsed = System.currentTimeMillis() - start;
            log.info("\nFinal result: {}", finalResult);
            log.info("Total time: {}ms", elapsed);
            log.info("✅ Task flowed through: I/O Pool → CPU Pool → Common Pool");
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    /**
     * Example 3: Combining results from multiple pools
     */
    private void demonstrateCombiningResults(ExecutorService ioPool, ExecutorService cpuPool) {
        log.info("--- Example 3: Combining Results from Different Pools ---");
        log.info("Scenario: Load user dashboard with data from multiple sources\n");

        long start = System.currentTimeMillis();

        // Parallel operations on different pools
        CompletableFuture<String> userProfile = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Fetching USER PROFILE from database (I/O, 600ms)",
                    Thread.currentThread().getName());
            sleep(600);
            return "Profile[John Doe]";
        }, ioPool);

        CompletableFuture<String> orderHistory = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Fetching ORDER HISTORY from database (I/O, 800ms)",
                    Thread.currentThread().getName());
            sleep(800);
            return "Orders[15]";
        }, ioPool);

        CompletableFuture<String> recommendations = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Calculating RECOMMENDATIONS (CPU, 700ms)",
                    Thread.currentThread().getName());
            sleep(700);
            return "Recommendations[5 items]";
        }, cpuPool);

        CompletableFuture<String> analytics = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Computing ANALYTICS (CPU, 500ms)",
                    Thread.currentThread().getName());
            sleep(500);
            return "Analytics[Score: 95]";
        }, cpuPool);

        CompletableFuture<String> notifications = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Fetching NOTIFICATIONS (quick, 200ms)",
                    Thread.currentThread().getName());
            sleep(200);
            return "Notifications[3 new]";
        }); // Common pool for quick task

        // Wait for all and combine
        CompletableFuture<String> dashboard = CompletableFuture.allOf(
                userProfile, orderHistory, recommendations, analytics, notifications
        ).thenApply(v -> {
            log.info("[{}] Assembling DASHBOARD from all results",
                    Thread.currentThread().getName());
            try {
                return String.format(
                        "Dashboard{ %s, %s, %s, %s, %s }",
                        userProfile.get(), orderHistory.get(), recommendations.get(),
                        analytics.get(), notifications.get()
                );
            } catch (Exception e) {
                return "Error assembling dashboard";
            }
        });

        try {
            String result = dashboard.get();
            long elapsed = System.currentTimeMillis() - start;
            log.info("\n🎯 Dashboard loaded:");
            log.info("{}", result);
            log.info("Total time: {}ms (parallel across all pools)", elapsed);
            log.info("Sequential would take: ~{}ms", 600 + 800 + 700 + 500 + 200);
            log.info("✅ Utilized: I/O Pool (2 tasks), CPU Pool (2 tasks), Common Pool (1 task)");
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    private void shutdownPool(String name, ExecutorService pool) {
        log.info("\n--- Shutting Down {} ---", name);
        pool.shutdown();
        try {
            pool.awaitTermination(5, TimeUnit.SECONDS);
            log.info("{} shut down successfully", name);
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
