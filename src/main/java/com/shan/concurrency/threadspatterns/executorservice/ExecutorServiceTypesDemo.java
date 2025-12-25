package com.shan.concurrency.threadspatterns.executorservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * ExecutorServiceTypesDemo demonstrates different types of ExecutorService implementations.
 *
 * Types covered:
 * 1. FixedThreadPool - Fixed number of threads, unbounded queue
 * 2. CachedThreadPool - Dynamically sized pool, creates threads as needed
 * 3. SingleThreadExecutor - Single worker thread, sequential execution
 * 4. ScheduledThreadPool - For delayed and periodic tasks
 * 5. WorkStealingPool - Uses ForkJoinPool, work-stealing algorithm
 */
@Slf4j
@Component
public class ExecutorServiceTypesDemo {

    private static final int TASK_COUNT = 10;

    public void demonstrate() {
        log.info("=== ExecutorService Types Demo ===\n");

        demonstrateFixedThreadPool();
        demonstrateCachedThreadPool();
        demonstrateSingleThreadExecutor();
        demonstrateScheduledThreadPool();
        demonstrateWorkStealingPool();
    }

    /**
     * 1. FixedThreadPool - Best for CPU-intensive tasks with known concurrency limit
     * - Fixed number of threads
     * - Uses unbounded LinkedBlockingQueue
     * - Good when you want to limit resource usage
     */
    private void demonstrateFixedThreadPool() {
        log.info("\n--- 1. FixedThreadPool (3 threads) ---");
        log.info("Use case: CPU-intensive tasks, controlled concurrency");
        log.info("Queue: Unbounded LinkedBlockingQueue");

        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            List<Future<TaskResult>> futures = new ArrayList<>();

            // Submit tasks
            for (int i = 1; i <= TASK_COUNT; i++) {
                Task task = new Task("FTP-Task-" + i, Task.TaskType.CPU_INTENSIVE, 200);
                Future<TaskResult> future = executor.submit(task);
                futures.add(future);
            }

            // Wait for all tasks to complete
            int completed = 0;
            for (Future<TaskResult> future : futures) {
                TaskResult result = future.get();
                if (result.isSuccess()) completed++;
            }

            log.info("FixedThreadPool: {}/{} tasks completed successfully\n", completed, TASK_COUNT);

        } catch (Exception e) {
            log.error("Error in FixedThreadPool demo", e);
        } finally {
            shutdownExecutor(executor, "FixedThreadPool");
        }
    }

    /**
     * 2. CachedThreadPool - Best for many short-lived async tasks
     * - Creates new threads as needed
     * - Reuses idle threads (60s timeout)
     * - Uses SynchronousQueue (direct handoff)
     * - Can grow unbounded - use with caution!
     */
    private void demonstrateCachedThreadPool() {
        log.info("\n--- 2. CachedThreadPool (Dynamic sizing) ---");
        log.info("Use case: Many short-lived async tasks");
        log.info("Queue: SynchronousQueue (direct handoff)");

        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            List<Future<TaskResult>> futures = new ArrayList<>();

            // Submit tasks with varying arrival times
            for (int i = 1; i <= TASK_COUNT; i++) {
                Task task = new Task("CTP-Task-" + i, Task.TaskType.IO_BOUND, 150);
                Future<TaskResult> future = executor.submit(task);
                futures.add(future);
                Thread.sleep(50); // Stagger submissions
            }

            // Wait for completion
            int completed = 0;
            for (Future<TaskResult> future : futures) {
                TaskResult result = future.get();
                if (result.isSuccess()) completed++;
            }

            log.info("CachedThreadPool: {}/{} tasks completed successfully\n", completed, TASK_COUNT);

        } catch (Exception e) {
            log.error("Error in CachedThreadPool demo", e);
        } finally {
            shutdownExecutor(executor, "CachedThreadPool");
        }
    }

    /**
     * 3. SingleThreadExecutor - Guarantees sequential execution
     * - Single worker thread
     * - Tasks execute in submission order
     * - Uses unbounded LinkedBlockingQueue
     * - Good for ensuring order and avoiding concurrency issues
     */
    private void demonstrateSingleThreadExecutor() {
        log.info("\n--- 3. SingleThreadExecutor (1 thread, sequential) ---");
        log.info("Use case: Order-sensitive operations, no concurrency");
        log.info("Queue: Unbounded LinkedBlockingQueue");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            List<Future<TaskResult>> futures = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                Task task = new Task("STE-Task-" + i, Task.TaskType.MIXED, 100);
                Future<TaskResult> future = executor.submit(task);
                futures.add(future);
            }

            // Wait for completion
            int completed = 0;
            for (Future<TaskResult> future : futures) {
                TaskResult result = future.get();
                if (result.isSuccess()) completed++;
            }

            log.info("SingleThreadExecutor: {}/{} tasks completed successfully (sequential)\n",
                    completed, 5);

        } catch (Exception e) {
            log.error("Error in SingleThreadExecutor demo", e);
        } finally {
            shutdownExecutor(executor, "SingleThreadExecutor");
        }
    }

    /**
     * 4. ScheduledThreadPool - For delayed and periodic tasks
     * - Schedule tasks with delays
     * - Schedule periodic tasks (fixed-rate or fixed-delay)
     * - Uses DelayedWorkQueue
     */
    private void demonstrateScheduledThreadPool() {
        log.info("\n--- 4. ScheduledThreadPool (Delayed & Periodic tasks) ---");
        log.info("Use case: Scheduled/periodic tasks, cron-like jobs");
        log.info("Queue: DelayedWorkQueue");

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        try {
            // One-time delayed task
            log.info("Scheduling one-time task with 500ms delay...");
            ScheduledFuture<TaskResult> delayedFuture = executor.schedule(
                    new Task("SCHED-Delayed-1", Task.TaskType.IO_BOUND, 100),
                    500,
                    TimeUnit.MILLISECONDS
            );

            // Periodic task (fixed-rate)
            log.info("Scheduling periodic task (every 300ms, 3 times)...");
            ScheduledFuture<?> periodicFuture = executor.scheduleAtFixedRate(
                    () -> log.info("[{}] Periodic task executing",
                            Thread.currentThread().getName()),
                    200,  // initial delay
                    300,  // period
                    TimeUnit.MILLISECONDS
            );

            // Wait for delayed task
            TaskResult result = delayedFuture.get();
            log.info("Delayed task result: {}", result);

            // Let periodic task run a few times
            Thread.sleep(1200);
            periodicFuture.cancel(false);

            log.info("ScheduledThreadPool: Delayed and periodic tasks completed\n");

        } catch (Exception e) {
            log.error("Error in ScheduledThreadPool demo", e);
        } finally {
            shutdownExecutor(executor, "ScheduledThreadPool");
        }
    }

    /**
     * 5. WorkStealingPool - ForkJoinPool with work-stealing
     * - Uses available processors by default
     * - Work-stealing algorithm for load balancing
     * - Good for parallel recursive tasks
     */
    private void demonstrateWorkStealingPool() {
        log.info("\n--- 5. WorkStealingPool (ForkJoinPool, work-stealing) ---");
        log.info("Use case: Parallel recursive tasks, divide-and-conquer");
        log.info("Parallelism: {}", Runtime.getRuntime().availableProcessors());

        ExecutorService executor = Executors.newWorkStealingPool();

        try {
            List<Future<TaskResult>> futures = new ArrayList<>();

            for (int i = 1; i <= TASK_COUNT; i++) {
                Task task = new Task("WSP-Task-" + i, Task.TaskType.CPU_INTENSIVE, 150);
                Future<TaskResult> future = executor.submit(task);
                futures.add(future);
            }

            // Wait for completion
            int completed = 0;
            for (Future<TaskResult> future : futures) {
                TaskResult result = future.get();
                if (result.isSuccess()) completed++;
            }

            log.info("WorkStealingPool: {}/{} tasks completed successfully\n", completed, TASK_COUNT);

        } catch (Exception e) {
            log.error("Error in WorkStealingPool demo", e);
        } finally {
            shutdownExecutor(executor, "WorkStealingPool");
        }
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
