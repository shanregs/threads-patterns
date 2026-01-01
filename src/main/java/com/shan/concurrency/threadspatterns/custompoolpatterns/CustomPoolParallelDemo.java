package com.shan.concurrency.threadspatterns.custompoolpatterns;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CustomPoolParallelDemo - Parallel operations with custom thread pool
 *
 * Demonstrates:
 * 1. Running multiple independent tasks in parallel on custom pool
 * 2. Using thenCombine() with custom pool
 * 3. Using allOf() with custom pool
 * 4. Thread distribution across custom pool
 * 5. Performance benefits of parallelism
 */
@Slf4j
public class CustomPoolParallelDemo {

    public void demonstrate() {
        log.info("=== Custom Pool Parallel Operations Demo ===\n");

        ExecutorService customPool = Executors.newFixedThreadPool(5, r -> {
            Thread t = new Thread(r);
            t.setName("CustomPar-" + t.getId());
            return t;
        });

        try {
            demonstrateBasicParallel(customPool);
            log.info("\n" + "=".repeat(70) + "\n");
            demonstrateThenCombine(customPool);
            log.info("\n" + "=".repeat(70) + "\n");
            demonstrateAllOf(customPool);

        } finally {
            shutdownPool(customPool);
        }
    }

    /**
     * Example 1: Basic parallel execution - multiple independent tasks
     */
    private void demonstrateBasicParallel(ExecutorService customPool) {
        log.info("--- Example 1: Basic Parallel Execution (3 independent tasks) ---");
        long start = System.currentTimeMillis();

        // Three independent API calls that can run in parallel
        CompletableFuture<String> profileFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Fetching user profile (800ms)",
                    Thread.currentThread().getName());
            sleep(800);
            log.info("[{}] Profile fetch COMPLETE", Thread.currentThread().getName());
            return "UserProfile";
        }, customPool);

        CompletableFuture<String> ordersFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Fetching orders (1000ms)",
                    Thread.currentThread().getName());
            sleep(1000);
            log.info("[{}] Orders fetch COMPLETE", Thread.currentThread().getName());
            return "Orders";
        }, customPool);

        CompletableFuture<String> recommendationsFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Fetching recommendations (600ms)",
                    Thread.currentThread().getName());
            sleep(600);
            log.info("[{}] Recommendations fetch COMPLETE", Thread.currentThread().getName());
            return "Recommendations";
        }, customPool);

        // Wait for all to complete
        try {
            String profile = profileFuture.get();
            String orders = ordersFuture.get();
            String recommendations = recommendationsFuture.get();

            long elapsed = System.currentTimeMillis() - start;
            log.info("\nAll tasks completed:");
            log.info("  - Profile: {}", profile);
            log.info("  - Orders: {}", orders);
            log.info("  - Recommendations: {}", recommendations);
            log.info("Total time: {}ms (parallel, max of 800, 1000, 600)", elapsed);
            log.info("Sequential would take: {}ms (sum: 800 + 1000 + 600)", 800 + 1000 + 600);
            log.info("Speedup: {}", String.format("%.2fx", 2400.0 / elapsed));
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    /**
     * Example 2: Using thenCombine to combine two parallel results
     */
    private void demonstrateThenCombine(ExecutorService customPool) {
        log.info("--- Example 2: Parallel with thenCombine ---");
        long start = System.currentTimeMillis();

        CompletableFuture<String> userDataFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Fetching user data (700ms)",
                    Thread.currentThread().getName());
            sleep(700);
            log.info("[{}] User data ready", Thread.currentThread().getName());
            return "User: John Doe";
        }, customPool);

        CompletableFuture<Integer> purchaseCountFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Counting purchases (900ms)",
                    Thread.currentThread().getName());
            sleep(900);
            log.info("[{}] Purchase count ready", Thread.currentThread().getName());
            return 42;
        }, customPool);

        // Combine both results when both complete
        CompletableFuture<String> combined = userDataFuture.thenCombine(
                purchaseCountFuture,
                (userData, purchaseCount) -> {
                    log.info("[{}] Combining results (thenCombine callback)",
                            Thread.currentThread().getName());
                    return String.format("%s, Total Purchases: %d", userData, purchaseCount);
                }
        );

        try {
            String result = combined.get();
            long elapsed = System.currentTimeMillis() - start;
            log.info("\nCombined result: {}", result);
            log.info("Total time: {}ms (parallel, max of 700, 900)", elapsed);
            log.info("✅ Both tasks ran in parallel on custom pool!");
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    /**
     * Example 3: Using allOf() to wait for multiple futures
     */
    private void demonstrateAllOf(ExecutorService customPool) {
        log.info("--- Example 3: Parallel with allOf() - Dashboard Loading ---");
        long start = System.currentTimeMillis();

        // Simulate loading multiple dashboard widgets in parallel
        CompletableFuture<String> widget1 = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Loading Widget 1: User Stats (400ms)",
                    Thread.currentThread().getName());
            sleep(400);
            log.info("[{}] Widget 1 LOADED", Thread.currentThread().getName());
            return "UserStats";
        }, customPool);

        CompletableFuture<String> widget2 = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Loading Widget 2: Sales Chart (600ms)",
                    Thread.currentThread().getName());
            sleep(600);
            log.info("[{}] Widget 2 LOADED", Thread.currentThread().getName());
            return "SalesChart";
        }, customPool);

        CompletableFuture<String> widget3 = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Loading Widget 3: Recent Activity (300ms)",
                    Thread.currentThread().getName());
            sleep(300);
            log.info("[{}] Widget 3 LOADED", Thread.currentThread().getName());
            return "RecentActivity";
        }, customPool);

        CompletableFuture<String> widget4 = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Loading Widget 4: Notifications (500ms)",
                    Thread.currentThread().getName());
            sleep(500);
            log.info("[{}] Widget 4 LOADED", Thread.currentThread().getName());
            return "Notifications";
        }, customPool);

        CompletableFuture<String> widget5 = CompletableFuture.supplyAsync(() -> {
            log.info("[{}] Loading Widget 5: Analytics (700ms)",
                    Thread.currentThread().getName());
            sleep(700);
            log.info("[{}] Widget 5 LOADED", Thread.currentThread().getName());
            return "Analytics";
        }, customPool);

        // Wait for all widgets to load
        CompletableFuture<Void> allWidgets = CompletableFuture.allOf(
                widget1, widget2, widget3, widget4, widget5
        );

        try {
            allWidgets.get(); // Wait for all
            long elapsed = System.currentTimeMillis() - start;

            log.info("\n🎯 All 5 widgets loaded:");
            log.info("  - {}", widget1.get());
            log.info("  - {}", widget2.get());
            log.info("  - {}", widget3.get());
            log.info("  - {}", widget4.get());
            log.info("  - {}", widget5.get());
            log.info("Total time: {}ms (parallel, max of all)", elapsed);
            log.info("Sequential would take: {}ms", 400 + 600 + 300 + 500 + 700);
            log.info("Speedup: {}", String.format("%.2fx", 2500.0 / elapsed));
            log.info("✅ All 5 widgets loaded in parallel using custom pool!");
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
