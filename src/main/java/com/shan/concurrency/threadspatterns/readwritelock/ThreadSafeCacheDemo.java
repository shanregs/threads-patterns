package com.shan.concurrency.threadspatterns.readwritelock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReadWriteLock Demo - Thread-Safe Cache with Read/Write Optimization
 *
 * Use Case: High-read, low-write cache where multiple readers can access simultaneously
 * Real-world Example: Configuration cache with frequent reads, occasional updates
 *
 * How it works:
 * 1. Multiple readers can hold read lock simultaneously
 * 2. Only one writer can hold write lock at a time
 * 3. Writers block readers and vice versa
 * 4. Optimized for read-heavy workloads
 */
@Slf4j
@Component
public class ThreadSafeCacheDemo {

    private final Map<String, CacheEntry<String>> cache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true); // Fair lock
    private static final int TTL_SECONDS = 10;

    public void demonstrate() {
        log.info("=== ReadWriteLock Demo: Thread-Safe Cache ===");
        log.info("Scenario: 10 readers + 2 writers accessing shared cache\n");

        ExecutorService executor = Executors.newFixedThreadPool(12);

        try {
            // Pre-populate cache
            put("user:1", "John Doe");
            put("user:2", "Jane Smith");
            put("config:timeout", "30");

            // Submit 10 reader threads
            for (int i = 1; i <= 10; i++) {
                final int readerId = i;
                executor.submit(() -> performReads(readerId));
            }

            // Submit 2 writer threads
            for (int i = 1; i <= 2; i++) {
                final int writerId = i;
                executor.submit(() -> performWrites(writerId));
            }

            // Let them run
            Thread.sleep(3000);

            log.info("\n=== Final Cache State ===");
            displayCacheStats();

        } catch (InterruptedException e) {
            log.error("Demo interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            shutdownExecutor(executor);
        }
    }

    /**
     * Reader thread: Performs multiple reads
     */
    private void performReads(int readerId) {
        for (int i = 0; i < 5; i++) {
            String key = "user:" + ((readerId % 2) + 1);
            String value = get(key);
            log.info("[Reader-{}] Read: {} = {}", readerId, key, value);

            sleep(100 + (readerId * 10)); // Stagger reads
        }
    }

    /**
     * Writer thread: Performs occasional writes
     */
    private void performWrites(int writerId) {
        for (int i = 0; i < 3; i++) {
            String key = "user:" + writerId;
            String value = "Updated-User-" + writerId + "-" + i;
            put(key, value);
            log.info("[Writer-{}] WRITE: {} = {}", writerId, key, value);

            sleep(500); // Writers are slower
        }
    }

    /**
     * Get value from cache (Read Lock)
     * Multiple readers can execute this simultaneously
     */
    public String get(String key) {
        lock.readLock().lock(); // Multiple threads can acquire read lock
        try {
            log.debug("[{}] Acquired READ lock for key: {}",
                    Thread.currentThread().getName(), key);

            CacheEntry<String> entry = cache.get(key);

            if (entry == null) {
                return null;
            }

            if (entry.isExpired(TTL_SECONDS)) {
                log.warn("Cache entry expired for key: {}", key);
                return null;
            }

            // Simulate read operation
            sleep(50);

            return entry.getValue();

        } finally {
            lock.readLock().unlock();
            log.debug("[{}] Released READ lock for key: {}",
                    Thread.currentThread().getName(), key);
        }
    }

    /**
     * Put value in cache (Write Lock)
     * Only ONE writer can execute this at a time, blocks all readers
     */
    public void put(String key, String value) {
        lock.writeLock().lock(); // Exclusive lock
        try {
            log.debug("[{}] Acquired WRITE lock for key: {}",
                    Thread.currentThread().getName(), key);

            // Simulate write operation
            sleep(100);

            cache.put(key, new CacheEntry<>(value, LocalDateTime.now()));
            log.info("✍️  Cache updated: {} = {}", key, value);

        } finally {
            lock.writeLock().unlock();
            log.debug("[{}] Released WRITE lock for key: {}",
                    Thread.currentThread().getName(), key);
        }
    }

    /**
     * Get cache size (Read Lock)
     */
    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clear cache (Write Lock)
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            log.info("Cache cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void displayCacheStats() {
        lock.readLock().lock();
        try {
            log.info("Cache size: {}", cache.size());
            cache.forEach((key, entry) -> {
                log.info("  {} = {} (updated: {})",
                        key, entry.getValue(), entry.getLastUpdated());
            });
        } finally {
            lock.readLock().unlock();
        }
    }

    private void shutdownExecutor(ExecutorService executor) {
        log.info("\n--- Shutting Down Executor ---");
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            log.info("Executor shut down successfully");
        } catch (InterruptedException e) {
            executor.shutdownNow();
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
