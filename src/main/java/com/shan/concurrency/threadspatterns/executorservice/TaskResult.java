package com.shan.concurrency.threadspatterns.executorservice;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TaskResult represents the outcome of executing a task.
 */
@Data
@AllArgsConstructor
public class TaskResult {
    private String taskId;
    private boolean success;
    private long durationMs;
    private String errorMessage;

    @Override
    public String toString() {
        if (success) {
            return String.format("TaskResult{id='%s', success=true, duration=%dms}",
                    taskId, durationMs);
        } else {
            return String.format("TaskResult{id='%s', success=false, duration=%dms, error='%s'}",
                    taskId, durationMs, errorMessage);
        }
    }
}
