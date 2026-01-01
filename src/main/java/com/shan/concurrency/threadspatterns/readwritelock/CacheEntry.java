package com.shan.concurrency.threadspatterns.readwritelock;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Cache entry with value and timestamp
 */
@Data
@AllArgsConstructor
public class CacheEntry<T> {
    private T value;
    private LocalDateTime lastUpdated;

    public boolean isExpired(int ttlSeconds) {
        return LocalDateTime.now().isAfter(lastUpdated.plusSeconds(ttlSeconds));
    }
}
