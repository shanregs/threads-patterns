# ReadWriteLock Pattern - Optimized Read-Heavy Access

This package demonstrates ReadWriteLock for scenarios with frequent reads and infrequent writes.

## 🎯 What Is ReadWriteLock?

**ReadWriteLock** maintains a pair of locks: one for read-only operations and one for write operations. Multiple readers can hold the read lock simultaneously, but only one writer can hold the write lock.

**Key Characteristics**:
- ✅ **Multiple readers**: Many threads can read simultaneously
- ✅ **Exclusive writer**: Only one thread can write at a time
- ✅ **Writer blocks readers**: When writing, no reads allowed
- ✅ **Reader blocks writer**: When reading, no writes allowed
- ✅ **Optimized**: For read-heavy workloads

**Use Cases**:
- **Caches**: Frequent reads, occasional updates
- **Configuration stores**: Many reads, rare writes
- **Reference data**: Read-mostly data structures
- **Shared resources**: Multiple readers, single updater

---

## 📊 Thread Flow Pattern: Read-Heavy Workload

### Pattern: Multiple Concurrent Readers, Exclusive Writer

```
Time 0ms - Multiple Readers (Parallel):
  Reader-1: readLock() → ✅ Acquired ─┐
  Reader-2: readLock() → ✅ Acquired ─┼→ All read SIMULTANEOUSLY
  Reader-3: readLock() → ✅ Acquired ─┘

  All readers execute in parallel (no blocking)

Time 200ms - Writer Arrives:
  Writer-1: writeLock() → ⏳ WAITING (readers still active)

  Readers-1,2,3: Still reading... (writer waits)

Time 300ms - Readers Complete:
  Reader-1: readUnlock() ─┐
  Reader-2: readUnlock() ─┼→ All readers done
  Reader-3: readUnlock() ─┘

  Writer-1: writeLock() → ✅ Acquired (exclusive)
  Writer-1: Perform write...
  Writer-1: writeUnlock()

Time 400ms - New Readers:
  Reader-4: readLock() → ✅ Acquired ─┐
  Reader-5: readLock() → ✅ Acquired ─┼→ Readers resume
  Reader-6: readLock() → ✅ Acquired ─┘
```

---

## 🔍 Demo: Thread-Safe Cache

**Scenario**: 10 readers + 2 writers accessing shared cache

### Thread Flow

```
Cache Operations:

┌─────────────────── READ LOCK (Shared) ───────────────────┐
│                                                           │
│  Reader-1 ────┐                                          │
│  Reader-2 ────┤                                          │
│  Reader-3 ────┼─→ All execute in PARALLEL               │
│  Reader-4 ────┤    (No blocking between readers)         │
│  Reader-5 ────┘                                          │
│                                                           │
└───────────────────────────────────────────────────────────┘
        ↓ (All readers complete)
        ↓
┌─────────────────── WRITE LOCK (Exclusive) ───────────────┐
│                                                           │
│  Writer-1: EXCLUSIVE ACCESS                              │
│           (Blocks all readers and writers)               │
│                                                           │
└───────────────────────────────────────────────────────────┘
        ↓ (Writer completes)
        ↓
┌─────────────────── READ LOCK (Shared) ───────────────────┐
│                                                           │
│  Reader-6 ────┐                                          │
│  Reader-7 ────┼─→ Resume parallel reads                  │
│  Reader-8 ────┘                                          │
│                                                           │
└───────────────────────────────────────────────────────────┘
```

### Code Pattern

**1. Create ReadWriteLock**:
```java
private final ReadWriteLock lock = new ReentrantReadWriteLock(true); // Fair
```

**2. Read Operation (Multiple Concurrent)**:
```java
public String get(String key) {
    lock.readLock().lock(); // Multiple threads can acquire
    try {
        return cache.get(key);
    } finally {
        lock.readLock().unlock();
    }
}
```

**3. Write Operation (Exclusive)**:
```java
public void put(String key, String value) {
    lock.writeLock().lock(); // Exclusive access
    try {
        cache.put(key, value);
    } finally {
        lock.writeLock().unlock();
    }
}
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.readwritelock.Main"
```

### Expected Output
```
=== ReadWriteLock Demo: Thread-Safe Cache ===
Scenario: 10 readers + 2 writers accessing shared cache

✍️  Cache updated: user:1 = John Doe
✍️  Cache updated: user:2 = Jane Smith
✍️  Cache updated: config:timeout = 30

[Reader-1] Read: user:1 = John Doe
[Reader-2] Read: user:2 = Jane Smith
[Reader-3] Read: user:1 = John Doe
[Reader-4] Read: user:2 = Jane Smith
[Reader-5] Read: user:1 = John Doe

[Writer-1] WRITE: user:1 = Updated-User-1-0  ← Exclusive write
[Writer-2] WRITE: user:2 = Updated-User-2-0  ← Exclusive write

[Reader-6] Read: user:1 = Updated-User-1-0   ← Reads new value
[Reader-7] Read: user:2 = Updated-User-2-0
...

=== Final Cache State ===
Cache size: 4
  user:1 = Updated-User-1-2
  user:2 = Updated-User-2-2
  config:timeout = 30
```

---

## 📈 Performance Benefits

### ReentrantLock vs ReadWriteLock

**ReentrantLock (Single Lock)**:
```
Reader-1: lock() → read → unlock() ────┐
Reader-2:                   lock() → read → unlock() ────┐
Reader-3:                                    lock() → read → unlock()

Sequential: Only one reader at a time
Time: 300ms (100ms × 3 readers)
```

**ReadWriteLock (Read Lock)**:
```
Reader-1: readLock() → read → readUnlock() ─┐
Reader-2: readLock() → read → readUnlock() ─┼─ ALL PARALLEL
Reader-3: readLock() → read → readUnlock() ─┘

Parallel: All readers simultaneously
Time: 100ms (max of all readers)
Speedup: 3x faster!
```

---

## 🔑 Key Methods

### ReadWriteLock Interface
```java
ReadWriteLock lock = new ReentrantReadWriteLock();
Lock readLock = lock.readLock();   // For read operations
Lock writeLock = lock.writeLock(); // For write operations
```

### Read Lock (Shared)
```java
lock.readLock().lock();      // Acquire read lock (shared)
lock.readLock().unlock();    // Release read lock
lock.readLock().tryLock();   // Try acquire (non-blocking)
```

### Write Lock (Exclusive)
```java
lock.writeLock().lock();     // Acquire write lock (exclusive)
lock.writeLock().unlock();   // Release write lock
lock.writeLock().tryLock();  // Try acquire (non-blocking)
```

### Fairness
```java
// Fair lock (FIFO ordering)
ReadWriteLock fairLock = new ReentrantReadWriteLock(true);

// Non-fair lock (better throughput, possible starvation)
ReadWriteLock unfairLock = new ReentrantReadWriteLock(false);
```

---

## 🎯 Real-World Use Cases

### 1. Configuration Cache
```java
public class ConfigCache {
    private final Map<String, String> config = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public String getConfig(String key) {
        lock.readLock().lock();
        try {
            return config.get(key); // Many reads in parallel
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updateConfig(String key, String value) {
        lock.writeLock().lock();
        try {
            config.put(key, value); // Exclusive write
            notifyListeners(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

### 2. Document Version Control
```java
public class DocumentStore {
    private String currentVersion;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public String read() {
        lock.readLock().lock();
        try {
            return currentVersion; // Many readers
        } finally {
            lock.readLock().unlock();
        }
    }

    public void publish(String newVersion) {
        lock.writeLock().lock();
        try {
            currentVersion = newVersion; // Single publisher
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

### 3. Statistics Tracking
```java
public class StatsTracker {
    private long totalRequests = 0;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public long getStats() {
        lock.readLock().lock();
        try {
            return totalRequests; // Frequent reads
        } finally {
            lock.readLock().unlock();
        }
    }

    public void incrementRequest() {
        lock.writeLock().lock();
        try {
            totalRequests++; // Occasional writes
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

---

## ⚠️ Common Pitfalls

### 1. Deadlock Risk
```java
// ❌ BAD: Lock upgrade attempt (deadlock!)
lock.readLock().lock();
try {
    if (needsUpdate()) {
        lock.writeLock().lock(); // ❌ DEADLOCK! Can't upgrade
        try {
            update();
        } finally {
            lock.writeLock().unlock();
        }
    }
} finally {
    lock.readLock().unlock();
}
```

**Solution**: Release read lock before acquiring write lock
```java
// ✅ GOOD: Release read, then acquire write
lock.readLock().lock();
boolean needsUpdate;
try {
    needsUpdate = checkIfUpdateNeeded();
} finally {
    lock.readLock().unlock();
}

if (needsUpdate) {
    lock.writeLock().lock();
    try {
        update();
    } finally {
        lock.writeLock().unlock();
    }
}
```

### 2. Writer Starvation
```java
// With unfair lock and continuous readers, writer may starve
ReadWriteLock unfairLock = new ReentrantReadWriteLock(false);

// ❌ Continuous readers may prevent writer from acquiring lock
```

**Solution**: Use fair lock
```java
// ✅ Fair lock prevents starvation
ReadWriteLock fairLock = new ReentrantReadWriteLock(true);
```

### 3. Forgetting to Unlock
```java
lock.readLock().lock();
return cache.get(key); // ❌ Forgot unlock! Lock held forever
```

**Solution**: Always use try-finally
```java
lock.readLock().lock();
try {
    return cache.get(key);
} finally {
    lock.readLock().unlock(); // ✅ Always unlocks
}
```

---

## 🎓 Best Practices

✅ **DO:**
- Use for read-heavy workloads (reads >> writes)
- Always use try-finally for unlock
- Consider fair lock for long-running operations
- Use tryLock() to avoid indefinite blocking

❌ **DON'T:**
- Try to upgrade read lock to write lock (deadlock)
- Use for write-heavy workloads (worse than ReentrantLock)
- Hold locks longer than necessary
- Forget to unlock in finally block

---

## 📊 When to Use ReadWriteLock

**✅ Use ReadWriteLock When**:
- Read operations >> Write operations (e.g., 90% reads, 10% writes)
- Read operations are slow enough to benefit from parallelization
- Need to optimize read throughput

**❌ Use ReentrantLock Instead When**:
- Write operations are frequent (> 50%)
- Read operations are very fast
- Simplicity is more important than read optimization

---

## 🔗 Related Patterns

- **ReentrantLock** - Single lock for all operations
- **StampedLock** - Optimistic read lock (Java 8+)
- **Semaphore** - Control number of concurrent accesses
- **ConcurrentHashMap** - Thread-safe map with fine-grained locking

---

**Package**: `com.shan.concurrency.threadspatterns.readwritelock`

**Pattern Type**: Locking - Read/Write Optimization
**Thread Flow**: Parallel Reads, Exclusive Writes
**Best For**: Read-heavy workloads with occasional updates
