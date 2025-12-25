package com.shan.concurrency.threadspatterns.executorservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * Task represents a unit of work that can be executed by an executor.
 * Implements Callable to return a result.
 */
@Slf4j
@Data
@AllArgsConstructor
public class Task implements Callable<TaskResult> {

    private final String taskId;
    private final TaskType taskType;
    private final int processingTimeMs;

    @Override
    public TaskResult call() throws Exception {
        long startTime = System.currentTimeMillis();

        log.info("[{}] Task {} started | Type: {} | Queue size info in pool",
                Thread.currentThread().getName(), taskId, taskType);

        try {
            // Simulate work based on task type
            performWork();

            long duration = System.currentTimeMillis() - startTime;
            TaskResult result = new TaskResult(taskId, true, duration, null);

            log.info("[{}] Task {} COMPLETED | Duration: {} ms",
                    Thread.currentThread().getName(), taskId, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            TaskResult result = new TaskResult(taskId, false, duration, e.getMessage());

            log.error("[{}] Task {} FAILED | Duration: {} ms | Error: {}",
                    Thread.currentThread().getName(), taskId, duration, e.getMessage());

            return result;
        }
    }

    private void performWork() throws InterruptedException {
        switch (taskType) {
            case CPU_INTENSIVE -> {
                // Simulate CPU-intensive work
                log.info("[{}] Task {} performing CPU-intensive calculation",
                        Thread.currentThread().getName(), taskId);
                long result = 0;
                for (int i = 0; i < 1_000_000; i++) {
                    result += Math.sqrt(i);
                }
                Thread.sleep(processingTimeMs);
            }
            case IO_BOUND -> {
                // Simulate I/O operation (database, file, network)
                log.info("[{}] Task {} performing I/O operation",
                        Thread.currentThread().getName(), taskId);
                Thread.sleep(processingTimeMs);
            }
            case MIXED -> {
                // Simulate mixed workload
                log.info("[{}] Task {} performing mixed workload",
                        Thread.currentThread().getName(), taskId);
                Thread.sleep(processingTimeMs / 2);
                long result = 0;
                for (int i = 0; i < 500_000; i++) {
                    result += Math.sqrt(i);
                }
                Thread.sleep(processingTimeMs / 2);
            }
        }
    }

    public enum TaskType {
        CPU_INTENSIVE,
        IO_BOUND,
        MIXED
    }
}
