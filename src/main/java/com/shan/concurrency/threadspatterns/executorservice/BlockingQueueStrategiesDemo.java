package com.shan.concurrency.threadspatterns.executorservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * BlockingQueueStrategiesDemo demonstrates different BlockingQueue implementations
 * with custom ThreadPoolExecutor configurations.
 *
 * Queue strategies:
 * 1. ArrayBlockingQueue - Bounded, array-based FIFO queue
 * 2. LinkedBlockingQueue - Optionally bounded, linked-node FIFO queue
 * 3. SynchronousQueue - No storage, direct handoff
 * 4. PriorityBlockingQueue - Unbounded priority queue
 * 5. DelayQueue - Unbounded queue of delayed elements
 */
@Slf4j
@Component
public class BlockingQueueStrategiesDemo {

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 4;
    private static final long KEEP_ALIVE_TIME = 10;

    public void demonstrate() {
        log.info("=== BlockingQueue Strategies Demo ===\n");

        demonstrateArrayBlockingQueue();
        demonstrateLinkedBlockingQueue();
        demonstrateSynchronousQueue();
        demonstratePriorityBlockingQueue();
        demonstrateDelayQueue();
    }

    /**
     * 1. ArrayBlockingQueue - Fixed capacity, bounded queue
     * - Backed by array
     * - FIFO ordering
     * - Bounded capacity prevents unbounded growth
     * - Good for backpressure handling
     */
    private void demonstrateArrayBlockingQueue() {
        log.info("\n--- 1. ArrayBlockingQueue (Capacity: 5) ---");
        log.info("Characteristics: Bounded, Array-based, FIFO");
        log.info("Use case: Bounded task queue, backpressure handling");

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(5);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                queue,
                new ThreadPoolExecutor.CallerRunsPolicy() // Handle rejection
        );

        try {
            List<Future<TaskResult>> futures = new ArrayList<>();

            // Submit tasks - some may be rejected due to bounded queue
            for (int i = 1; i <= 12; i++) {
                Task task = new Task("ABQ-Task-" + i, Task.TaskType.IO_BOUND, 300);
                try {
                    Future<TaskResult> future = executor.submit(task);
                    futures.add(future);
                    log.info("Submitted ABQ-Task-{} | Queue size: {} | Active: {} | Pool size: {}",
                            i, queue.size(), executor.getActiveCount(), executor.getPoolSize());
                } catch (RejectedExecutionException e) {
                    log.warn("Task ABQ-Task-{} REJECTED (queue full)", i);
                }
                Thread.sleep(50);
            }

            // Collect results
            int completed = collectResults(futures, "ArrayBlockingQueue");
            log.info("ArrayBlockingQueue: {}/{} tasks completed\n", completed, futures.size());

        } catch (Exception e) {
            log.error("Error in ArrayBlockingQueue demo", e);
        } finally {
            shutdownExecutor(executor, "ArrayBlockingQueue");
        }
    }

    /**
     * 2. LinkedBlockingQueue - Optionally bounded linked queue
     * - Backed by linked nodes
     * - FIFO ordering
     * - Can be bounded or unbounded
     * - Better throughput than ArrayBlockingQueue
     */
    private void demonstrateLinkedBlockingQueue() {
        log.info("\n--- 2. LinkedBlockingQueue (Capacity: 10) ---");
        log.info("Characteristics: Optionally bounded, Linked nodes, FIFO");
        log.info("Use case: High throughput, flexible capacity");

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(10);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                queue,
                new ThreadPoolExecutor.AbortPolicy()
        );

        try {
            List<Future<TaskResult>> futures = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
                Task task = new Task("LBQ-Task-" + i, Task.TaskType.MIXED, 200);
                Future<TaskResult> future = executor.submit(task);
                futures.add(future);
                log.info("Submitted LBQ-Task-{} | Queue size: {} | Pool size: {}",
                        i, queue.size(), executor.getPoolSize());
            }

            int completed = collectResults(futures, "LinkedBlockingQueue");
            log.info("LinkedBlockingQueue: {}/{} tasks completed\n", completed, futures.size());

        } catch (Exception e) {
            log.error("Error in LinkedBlockingQueue demo", e);
        } finally {
            shutdownExecutor(executor, "LinkedBlockingQueue");
        }
    }

    /**
     * 3. SynchronousQueue - Zero capacity, direct handoff
     * - No storage - each insert waits for remove
     * - Direct handoff between producer and consumer
     * - Forces immediate task execution or thread creation
     * - Used by CachedThreadPool
     */
    private void demonstrateSynchronousQueue() {
        log.info("\n--- 3. SynchronousQueue (Zero capacity, direct handoff) ---");
        log.info("Characteristics: No storage, Direct handoff");
        log.info("Use case: Direct task handoff, immediate execution");

        BlockingQueue<Runnable> queue = new SynchronousQueue<>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,  // Small core
                5,  // Can grow
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                queue,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try {
            List<Future<TaskResult>> futures = new ArrayList<>();

            for (int i = 1; i <= 8; i++) {
                Task task = new Task("SQ-Task-" + i, Task.TaskType.IO_BOUND, 250);
                Future<TaskResult> future = executor.submit(task);
                futures.add(future);
                log.info("Submitted SQ-Task-{} | Queue size: {} (always 0) | Pool size: {} | Active: {}",
                        i, queue.size(), executor.getPoolSize(), executor.getActiveCount());
                Thread.sleep(100);
            }

            int completed = collectResults(futures, "SynchronousQueue");
            log.info("SynchronousQueue: {}/{} tasks completed\n", completed, futures.size());

        } catch (Exception e) {
            log.error("Error in SynchronousQueue demo", e);
        } finally {
            shutdownExecutor(executor, "SynchronousQueue");
        }
    }

    /**
     * 4. PriorityBlockingQueue - Unbounded priority queue
     * - Orders elements by priority (natural order or Comparator)
     * - Unbounded capacity
     * - Higher priority tasks execute first
     */
    private void demonstratePriorityBlockingQueue() {
        log.info("\n--- 4. PriorityBlockingQueue (Priority-based execution) ---");
        log.info("Characteristics: Unbounded, Priority ordering");
        log.info("Use case: Priority-based task scheduling");

        BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(
                10,
                (r1, r2) -> {
                    // Extract priority from task ID (format: PBQ-Task-Priority-X)
                    if (r1 instanceof FutureTask && r2 instanceof FutureTask) {
                        return 0; // Simplified - in real use, embed priority in task
                    }
                    return 0;
                }
        );

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                queue
        );

        try {
            List<Future<TaskResult>> futures = new ArrayList<>();

            // Submit tasks with different implied priorities
            for (int priority = 5; priority >= 1; priority--) {
                Task task = new Task("PBQ-Task-P" + priority, Task.TaskType.CPU_INTENSIVE, 150);
                Future<TaskResult> future = executor.submit(task);
                futures.add(future);
                log.info("Submitted PBQ-Task-P{} | Queue size: {}", priority, queue.size());
            }

            int completed = collectResults(futures, "PriorityBlockingQueue");
            log.info("PriorityBlockingQueue: {}/{} tasks completed\n", completed, futures.size());

        } catch (Exception e) {
            log.error("Error in PriorityBlockingQueue demo", e);
        } finally {
            shutdownExecutor(executor, "PriorityBlockingQueue");
        }
    }

    /**
     * 5. DelayQueue - Delayed execution queue
     * - Elements can only be taken when their delay expires
     * - Unbounded queue
     * - Good for scheduled/delayed tasks
     */
    private void demonstrateDelayQueue() {
        log.info("\n--- 5. DelayQueue (Delayed execution) ---");
        log.info("Characteristics: Unbounded, Delay-based execution");
        log.info("Use case: Scheduled tasks, cache expiration");

        // Note: DelayQueue requires Delayed elements
        // Using ScheduledThreadPoolExecutor instead which uses DelayedWorkQueue internally
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);

        try {
            log.info("Scheduling tasks with different delays...");

            // Schedule tasks with delays
            ScheduledFuture<TaskResult> future1 = executor.schedule(
                    new Task("DQ-Task-1-500ms", Task.TaskType.IO_BOUND, 100),
                    500, TimeUnit.MILLISECONDS
            );

            ScheduledFuture<TaskResult> future2 = executor.schedule(
                    new Task("DQ-Task-2-200ms", Task.TaskType.IO_BOUND, 100),
                    200, TimeUnit.MILLISECONDS
            );

            ScheduledFuture<TaskResult> future3 = executor.schedule(
                    new Task("DQ-Task-3-800ms", Task.TaskType.IO_BOUND, 100),
                    800, TimeUnit.MILLISECONDS
            );

            // Wait for all
            log.info("Result 1: {}", future1.get());
            log.info("Result 2: {}", future2.get());
            log.info("Result 3: {}", future3.get());

            log.info("DelayQueue: All delayed tasks completed (executed in delay order)\n");

        } catch (Exception e) {
            log.error("Error in DelayQueue demo", e);
        } finally {
            shutdownExecutor(executor, "DelayQueue");
        }
    }

    private int collectResults(List<Future<TaskResult>> futures, String queueType) {
        int completed = 0;
        for (Future<TaskResult> future : futures) {
            try {
                TaskResult result = future.get(5, TimeUnit.SECONDS);
                if (result.isSuccess()) {
                    completed++;
                }
            } catch (TimeoutException e) {
                log.warn("{}: Task timed out", queueType);
            } catch (Exception e) {
                log.error("{}: Error getting result", queueType, e);
            }
        }
        return completed;
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            log.info("{} shut down successfully", name);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
