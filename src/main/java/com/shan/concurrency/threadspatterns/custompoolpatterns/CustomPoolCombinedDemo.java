package com.shan.concurrency.threadspatterns.custompoolpatterns;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CustomPoolCombinedDemo - Comprehensive demo showing all patterns together
 *
 * Real-world scenario: E-commerce order processing system
 *
 * Demonstrates:
 * 1. Sequential operations (order validation pipeline)
 * 2. Parallel operations (fetch related data)
 * 3. Mixed pool usage (I/O vs CPU tasks)
 * 4. Combining results from multiple sources
 * 5. Error handling across pools
 */
@Slf4j
public class CustomPoolCombinedDemo {

    private final ExecutorService ioPool;
    private final ExecutorService cpuPool;

    public CustomPoolCombinedDemo() {
        // I/O pool for database and network operations
        this.ioPool = Executors.newFixedThreadPool(8, r -> {
            Thread t = new Thread(r);
            t.setName("IO-" + t.getId());
            t.setDaemon(false);
            return t;
        });

        // CPU pool for computational tasks
        this.cpuPool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setName("CPU-" + t.getId());
            t.setDaemon(false);
            return t;
        });
    }

    public void demonstrate() {
        log.info("=== Combined Demo: E-commerce Order Processing ===");
        log.info("Scenario: Process customer order with parallel data fetching and sequential validation\n");

        try {
            String orderId = "ORDER-12345";
            processOrder(orderId);

        } finally {
            shutdown();
        }
    }

    /**
     * Main order processing workflow
     */
    private void processOrder(String orderId) {
        long startTime = System.currentTimeMillis();
        log.info("🛒 Starting order processing for: {}\n", orderId);

        CompletableFuture<String> orderResult = CompletableFuture

                // STEP 1: Validate order (Sequential on I/O pool)
                .supplyAsync(() -> validateOrder(orderId), ioPool)

                // STEP 2: Fetch all required data in PARALLEL
                .thenCompose(validOrder -> fetchOrderData(validOrder))

                // STEP 3: Process payment (Sequential on CPU pool)
                .thenComposeAsync(orderData -> processPayment(orderData), cpuPool)

                // STEP 4: Calculate tax and shipping in PARALLEL (CPU pool)
                .thenCompose(this::calculateTaxAndShipping)

                // STEP 5: Reserve inventory (Sequential on I/O pool)
                .thenComposeAsync(this::reserveInventory, ioPool)

                // STEP 6: Create shipment (Sequential on I/O pool)
                .thenComposeAsync(this::createShipment, ioPool)

                // STEP 7: Send notifications in PARALLEL (I/O pool + Common pool)
                .thenCompose(this::sendNotifications)

                // Final result
                .thenApply(shipment -> {
                    log.info("[{}] Order processing COMPLETE!", Thread.currentThread().getName());
                    return shipment;
                })

                // Error handling
                .exceptionally(ex -> {
                    log.error("[{}] ❌ Order processing FAILED: {}",
                            Thread.currentThread().getName(), ex.getMessage());
                    return "FAILED: " + ex.getMessage();
                });

        try {
            String result = orderResult.get();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("\n" + "=".repeat(70));
            log.info("✅ ORDER PROCESSING RESULT: {}", result);
            log.info("⏱️  Total processing time: {}ms", elapsed);
            log.info("=".repeat(70));
        } catch (Exception e) {
            log.error("Error waiting for result", e);
        }
    }

    // ==================== Order Processing Steps ====================

    private String validateOrder(String orderId) {
        log.info("[{}] 📋 Step 1: Validating order (I/O, 300ms)",
                Thread.currentThread().getName());
        sleep(300);
        log.info("[{}]    ✓ Order validated", Thread.currentThread().getName());
        return orderId;
    }

    private CompletableFuture<OrderData> fetchOrderData(String orderId) {
        log.info("[{}] 📦 Step 2: Fetching order data in PARALLEL...",
                Thread.currentThread().getName());

        // Fetch customer info (I/O pool)
        CompletableFuture<String> customerFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}]    → Fetching CUSTOMER info (I/O, 400ms)",
                    Thread.currentThread().getName());
            sleep(400);
            return "Customer[Jane Smith]";
        }, ioPool);

        // Fetch product details (I/O pool)
        CompletableFuture<String> productFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}]    → Fetching PRODUCT details (I/O, 500ms)",
                    Thread.currentThread().getName());
            sleep(500);
            return "Product[Laptop, $1200]";
        }, ioPool);

        // Fetch pricing info (I/O pool)
        CompletableFuture<String> pricingFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}]    → Fetching PRICING info (I/O, 300ms)",
                    Thread.currentThread().getName());
            sleep(300);
            return "Price[$1200]";
        }, ioPool);

        // Combine all data
        return CompletableFuture.allOf(customerFuture, productFuture, pricingFuture)
                .thenApply(v -> {
                    try {
                        log.info("[{}]    ✓ All order data fetched (parallel time: ~500ms)",
                                Thread.currentThread().getName());
                        return new OrderData(orderId, customerFuture.get(),
                                productFuture.get(), pricingFuture.get());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to fetch order data", e);
                    }
                });
    }

    private CompletableFuture<OrderData> processPayment(OrderData orderData) {
        log.info("[{}] 💳 Step 3: Processing payment (CPU, 600ms)",
                Thread.currentThread().getName());
        sleep(600);
        log.info("[{}]    ✓ Payment processed", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(orderData);
    }

    private CompletableFuture<OrderData> calculateTaxAndShipping(OrderData orderData) {
        log.info("[{}] 🧮 Step 4: Calculating tax and shipping in PARALLEL...",
                Thread.currentThread().getName());

        // Calculate tax (CPU pool)
        CompletableFuture<Double> taxFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}]    → Calculating TAX (CPU, 400ms)",
                    Thread.currentThread().getName());
            sleep(400);
            return 120.0; // 10% tax
        }, cpuPool);

        // Calculate shipping (CPU pool)
        CompletableFuture<Double> shippingFuture = CompletableFuture.supplyAsync(() -> {
            log.info("[{}]    → Calculating SHIPPING (CPU, 300ms)",
                    Thread.currentThread().getName());
            sleep(300);
            return 25.0;
        }, cpuPool);

        return CompletableFuture.allOf(taxFuture, shippingFuture)
                .thenApply(v -> {
                    try {
                        log.info("[{}]    ✓ Tax: ${}, Shipping: ${}",
                                Thread.currentThread().getName(), taxFuture.get(), shippingFuture.get());
                        return orderData;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to calculate costs", e);
                    }
                });
    }

    private CompletableFuture<OrderData> reserveInventory(OrderData orderData) {
        log.info("[{}] 📊 Step 5: Reserving inventory (I/O, 400ms)",
                Thread.currentThread().getName());
        sleep(400);
        log.info("[{}]    ✓ Inventory reserved", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(orderData);
    }

    private CompletableFuture<String> createShipment(OrderData orderData) {
        log.info("[{}] 📮 Step 6: Creating shipment (I/O, 500ms)",
                Thread.currentThread().getName());
        sleep(500);
        String shipmentId = "SHIP-" + orderData.orderId;
        log.info("[{}]    ✓ Shipment created: {}", Thread.currentThread().getName(), shipmentId);
        return CompletableFuture.completedFuture(shipmentId);
    }

    private CompletableFuture<String> sendNotifications(String shipmentId) {
        log.info("[{}] 📧 Step 7: Sending notifications in PARALLEL...",
                Thread.currentThread().getName());

        // Send email (I/O pool)
        CompletableFuture<Void> emailFuture = CompletableFuture.runAsync(() -> {
            log.info("[{}]    → Sending EMAIL notification (I/O, 300ms)",
                    Thread.currentThread().getName());
            sleep(300);
        }, ioPool);

        // Send SMS (I/O pool)
        CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(() -> {
            log.info("[{}]    → Sending SMS notification (I/O, 200ms)",
                    Thread.currentThread().getName());
            sleep(200);
        }, ioPool);

        // Send push notification (common pool - quick task)
        CompletableFuture<Void> pushFuture = CompletableFuture.runAsync(() -> {
            log.info("[{}]    → Sending PUSH notification (quick, 100ms)",
                    Thread.currentThread().getName());
            sleep(100);
        });

        return CompletableFuture.allOf(emailFuture, smsFuture, pushFuture)
                .thenApply(v -> {
                    log.info("[{}]    ✓ All notifications sent", Thread.currentThread().getName());
                    return shipmentId;
                });
    }

    // ==================== Helper Classes ====================

    private static class OrderData {
        final String orderId;
        final String customer;
        final String product;
        final String pricing;

        OrderData(String orderId, String customer, String product, String pricing) {
            this.orderId = orderId;
            this.customer = customer;
            this.product = product;
            this.pricing = pricing;
        }
    }

    // ==================== Utility Methods ====================

    private void shutdown() {
        log.info("\n--- Shutting Down Thread Pools ---");
        shutdownPool("I/O Pool", ioPool);
        shutdownPool("CPU Pool", cpuPool);
    }

    private void shutdownPool(String name, ExecutorService pool) {
        pool.shutdown();
        try {
            if (pool.awaitTermination(5, TimeUnit.SECONDS)) {
                log.info("{} shut down successfully", name);
            } else {
                log.warn("{} did not terminate in time", name);
                pool.shutdownNow();
            }
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
