# Semaphore Pattern - Resource Access Control

This package demonstrates Semaphore for limiting concurrent access to shared resources.

## 🎯 What Is Semaphore?

**Semaphore** maintains a set of permits that control access to a limited resource. Threads acquire permits before accessing the resource and release them when done. If no permits are available, threads block until one becomes available.

**Key Characteristics**:
- ✅ **Permit-based**: Controls access via permits (like tokens)
- ✅ **Bounded concurrency**: Limits number of concurrent accesses
- ✅ **Fairness**: Optional FIFO ordering to prevent starvation
- ✅ **Flexible**: Can acquire/release multiple permits at once
- ✅ **Non-ownership**: Any thread can release a permit (unlike locks)

**Use Cases**:
- **Connection pooling**: Limit concurrent database connections
- **Rate limiting**: Throttle API requests
- **Resource management**: Control access to limited resources (printers, ATMs)
- **Thread pool size control**: Limit concurrent operations

---

## 📊 Thread Flow Pattern: Limited Resource Access

### Pattern: N Resources, M Threads (N < M)

```
Semaphore with 3 permits (3 ATM terminals)
10 customers waiting to use ATMs

Time 0ms - First Wave (3 customers acquire permits):
  Customer-1: acquire() → ✅ Permit acquired (2 permits left)
  Customer-2: acquire() → ✅ Permit acquired (1 permit left)
  Customer-3: acquire() → ✅ Permit acquired (0 permits left)

  Customer-1, 2, 3: Using ATM terminals... (parallel execution)

Time 50ms - More customers arrive (no permits available):
  Customer-4: acquire() → ⏳ BLOCKING (waiting for permit)
  Customer-5: acquire() → ⏳ BLOCKING (waiting for permit)
  Customer-6: acquire() → ⏳ BLOCKING (waiting for permit)
  Customer-7: acquire() → ⏳ BLOCKING (waiting for permit)
  ...

Time 1200ms - Customer-1 finishes:
  Customer-1: release() → ✅ Permit released (1 permit available)

  Customer-4: acquire() → ✅ Permit acquired! (was waiting)
  Customer-4: Using ATM terminal...

Time 1400ms - Customer-2 finishes:
  Customer-2: release() → ✅ Permit released (1 permit available)

  Customer-5: acquire() → ✅ Permit acquired! (was waiting)
  Customer-5: Using ATM terminal...

Time 1600ms - Customer-3 finishes:
  Customer-3: release() → ✅ Permit released (1 permit available)

  Customer-6: acquire() → ✅ Permit acquired! (was waiting)
  Customer-6: Using ATM terminal...

... Process continues until all customers served ...

Final: All 10 customers processed with only 3 ATMs (parallel batches)
```

---

## 🔍 Demo: ATM Access Control

**Scenario**: 3 ATM terminals serving 10 customers

### Thread Flow

```
3 ATM Terminals (3 Permits) - 10 Customers Waiting

┌────────────────── FIRST BATCH (3 concurrent) ──────────────────────┐
│                                                                     │
│  Semaphore initialized with 3 permits                              │
│                                                                     │
│  Customer-1: acquire() ✅ → Use ATM-1 ──┐                         │
│  Customer-2: acquire() ✅ → Use ATM-2 ──┼─ PARALLEL execution      │
│  Customer-3: acquire() ✅ → Use ATM-3 ──┘                         │
│                                                                     │
│  Customer-4: acquire() ⏳ WAITING... (no permits)                  │
│  Customer-5: acquire() ⏳ WAITING... (no permits)                  │
│  Customer-6: acquire() ⏳ WAITING... (no permits)                  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
        ↓ (Customer-1 finishes and releases permit)
        ↓
┌────────────────── SECOND BATCH (permit released) ──────────────────┐
│                                                                     │
│  Customer-1: release() ✅ → Permit available                       │
│  Customer-4: acquire() ✅ → Use ATM-1 ──┐                         │
│                                          │                          │
│  Customer-2: Still using ATM-2 ─────────┼─ PARALLEL execution      │
│  Customer-3: Still using ATM-3 ─────────┘                         │
│                                                                     │
│  Customer-5: acquire() ⏳ WAITING...                               │
│  Customer-6: acquire() ⏳ WAITING...                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
        ↓ (Customer-2 finishes and releases permit)
        ↓
┌────────────────── THIRD BATCH (permit released) ───────────────────┐
│                                                                     │
│  Customer-2: release() ✅ → Permit available                       │
│  Customer-5: acquire() ✅ → Use ATM-2 ──┐                         │
│                                          │                          │
│  Customer-3: Still using ATM-3 ─────────┼─ PARALLEL execution      │
│  Customer-4: Still using ATM-1 ─────────┘                         │
│                                                                     │
│  Customer-6: acquire() ⏳ WAITING...                               │
│  Customer-7: acquire() ⏳ WAITING...                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘

... Process continues until all 10 customers are served ...

Total Throughput: 10 customers served using only 3 ATMs
Pattern: Batched parallel execution (up to 3 concurrent at any time)
```

### Code Pattern

**1. Create Semaphore**:
```java
// 3 permits, fair ordering
Semaphore semaphore = new Semaphore(3, true);
```

**2. Acquire Permit (Blocking)**:
```java
public void useATM(String customerName) {
    try {
        semaphore.acquire(); // Block until permit available
        try {
            log.info("{} using ATM terminal", customerName);
            performTransaction(); // Critical section
        } finally {
            semaphore.release(); // ALWAYS release in finally
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

**3. Try Acquire (Non-Blocking)**:
```java
if (semaphore.tryAcquire(2, TimeUnit.SECONDS)) {
    try {
        useResource();
    } finally {
        semaphore.release();
    }
} else {
    log.warn("Could not acquire permit within timeout");
}
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.semaphore.Main"
```

### Expected Output
```
=== Semaphore Demo: ATM Access Control ===
Scenario: 3 ATM terminals serving 10 customers

[pool-1-thread-1] Customer-1 waiting for ATM...
[pool-1-thread-2] Customer-2 waiting for ATM...
[pool-1-thread-3] Customer-3 waiting for ATM...

[pool-1-thread-1] Customer-1 acquired ATM terminal (Available: 2)
[pool-1-thread-1] Customer-1 performing transaction...
[pool-1-thread-2] Customer-2 acquired ATM terminal (Available: 1)
[pool-1-thread-2] Customer-2 performing transaction...
[pool-1-thread-3] Customer-3 acquired ATM terminal (Available: 0)
[pool-1-thread-3] Customer-3 performing transaction...

[pool-1-thread-4] Customer-4 waiting for ATM...
[pool-1-thread-5] Customer-5 waiting for ATM...

[pool-1-thread-1] Customer-1 completed transaction
[pool-1-thread-1] Customer-1 released ATM terminal (Available: 1)

[pool-1-thread-4] Customer-4 acquired ATM terminal (Available: 0)
[pool-1-thread-4] Customer-4 performing transaction...

[pool-1-thread-2] Customer-2 completed transaction
[pool-1-thread-2] Customer-2 released ATM terminal (Available: 1)

[pool-1-thread-5] Customer-5 acquired ATM terminal (Available: 0)
[pool-1-thread-5] Customer-5 performing transaction...

... continues until all customers served ...

=== Semaphore Demo Completed ===
```

---

## 🔑 Key Methods

### Semaphore Creation
```java
Semaphore sem = new Semaphore(3);        // 3 permits, unfair
Semaphore sem = new Semaphore(3, true);  // 3 permits, fair (FIFO)
```

### Acquire Permits
```java
sem.acquire();              // Acquire 1 permit (blocking)
sem.acquire(2);             // Acquire 2 permits (blocking)
sem.acquireUninterruptibly(); // Acquire without InterruptedException
```

### Try Acquire (Non-Blocking)
```java
boolean acquired = sem.tryAcquire();              // Immediate attempt
boolean acquired = sem.tryAcquire(2);             // Try acquire 2 permits
boolean acquired = sem.tryAcquire(5, TimeUnit.SECONDS); // Timeout
```

### Release Permits
```java
sem.release();    // Release 1 permit
sem.release(2);   // Release 2 permits
```

### Information
```java
int available = sem.availablePermits();   // Permits currently available
boolean hasWaiters = sem.hasQueuedThreads(); // Threads waiting
int waiting = sem.getQueueLength();       // Number of waiting threads
```

---

## 🎯 Real-World Use Cases

### 1. Database Connection Pool
```java
public class ConnectionPool {
    private final Semaphore semaphore;
    private final List<Connection> pool;

    public ConnectionPool(int poolSize) {
        this.semaphore = new Semaphore(poolSize, true);
        this.pool = initializeConnections(poolSize);
    }

    public Connection getConnection() throws InterruptedException {
        semaphore.acquire(); // Wait for available connection
        return pool.remove(0); // Get connection from pool
    }

    public void releaseConnection(Connection conn) {
        pool.add(conn); // Return connection to pool
        semaphore.release(); // Make permit available
    }
}
```

### 2. Rate Limiter
```java
public class RateLimiter {
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;

    public RateLimiter(int requestsPerSecond) {
        this.semaphore = new Semaphore(requestsPerSecond);

        // Replenish permits every second
        scheduler.scheduleAtFixedRate(() -> {
            int permitsToAdd = requestsPerSecond - semaphore.availablePermits();
            if (permitsToAdd > 0) {
                semaphore.release(permitsToAdd);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void executeRequest(Runnable task) {
        if (semaphore.tryAcquire()) {
            try {
                task.run();
            } finally {
                // Don't release - permits replenished by scheduler
            }
        } else {
            throw new RateLimitExceededException();
        }
    }
}
```

### 3. Bounded Resource Manager
```java
public class PrinterManager {
    private final Semaphore semaphore;
    private final Queue<Printer> availablePrinters;

    public PrinterManager(List<Printer> printers) {
        this.semaphore = new Semaphore(printers.size(), true);
        this.availablePrinters = new LinkedList<>(printers);
    }

    public void print(Document doc) throws InterruptedException {
        semaphore.acquire(); // Wait for available printer
        Printer printer = null;
        synchronized (availablePrinters) {
            printer = availablePrinters.poll();
        }

        try {
            printer.print(doc);
        } finally {
            synchronized (availablePrinters) {
                availablePrinters.offer(printer);
            }
            semaphore.release(); // Return permit
        }
    }
}
```

---

## ⚠️ Common Pitfalls

### 1. Forgetting to Release
```java
// ❌ BAD: Permit not released if exception occurs
semaphore.acquire();
performOperation(); // May throw exception
semaphore.release(); // Never reached if exception!
```

**Solution**: Always use try-finally
```java
// ✅ GOOD: Permit always released
semaphore.acquire();
try {
    performOperation();
} finally {
    semaphore.release(); // Always executes
}
```

### 2. Unbalanced Acquire/Release
```java
// ❌ BAD: Acquire 2, release 1
semaphore.acquire(2);
try {
    useResource();
} finally {
    semaphore.release(); // ❌ Only releases 1 permit!
}
```

**Solution**: Match acquire and release counts
```java
// ✅ GOOD: Balanced acquire/release
semaphore.acquire(2);
try {
    useResource();
} finally {
    semaphore.release(2); // Release 2 permits
}
```

### 3. Deadlock with Multiple Semaphores
```java
// ❌ BAD: Potential deadlock
Thread-1: sem1.acquire(); sem2.acquire(); // ... release both
Thread-2: sem2.acquire(); sem1.acquire(); // ... release both
// Classic deadlock scenario!
```

**Solution**: Always acquire in consistent order
```java
// ✅ GOOD: Consistent ordering
Thread-1: sem1.acquire(); sem2.acquire(); // ... release
Thread-2: sem1.acquire(); sem2.acquire(); // ... release
```

### 4. Not Using Fairness
```java
// ❌ BAD: Unfair semaphore can starve threads
Semaphore sem = new Semaphore(3, false); // Unfair (default)
// Long-running thread may starve short operations
```

**Solution**: Use fairness for long-running operations
```java
// ✅ GOOD: Fair semaphore prevents starvation
Semaphore sem = new Semaphore(3, true); // Fair (FIFO)
```

---

## 🎓 Best Practices

✅ **DO:**
- Always release permits in finally block
- Use fair semaphore (true) to prevent thread starvation
- Use tryAcquire() with timeout for non-critical operations
- Match acquire() count with release() count
- Use semaphores for limiting resource access

❌ **DON'T:**
- Forget to release permits (causes permit leak)
- Release permits you didn't acquire (breaks invariant)
- Use semaphore for mutual exclusion (use ReentrantLock instead)
- Acquire multiple semaphores in inconsistent order (deadlock risk)
- Use semaphore when you need ownership semantics (use locks)

---

## 📊 Semaphore vs ReentrantLock vs ReadWriteLock

| Feature | Semaphore | ReentrantLock | ReadWriteLock |
|---------|-----------|---------------|---------------|
| **Purpose** | Limit concurrency | Mutual exclusion | Read/write optimization |
| **Permits** | N permits | 1 (exclusive) | 1 write, N reads |
| **Ownership** | ❌ No | ✅ Yes | ✅ Yes |
| **Reentrant** | ❌ No | ✅ Yes | ✅ Yes |
| **Fairness** | ✅ Optional | ✅ Optional | ✅ Optional |
| **Use Case** | Resource pool | Critical section | Read-heavy workload |

**When to Choose**:
- **Semaphore**: Limit concurrent access to N resources (N > 1)
- **ReentrantLock**: Mutual exclusion with advanced features
- **ReadWriteLock**: Frequent reads, occasional writes
- **Synchronized**: Simple mutual exclusion

---

## 🔗 Related Patterns

- **ReentrantLock** - Mutual exclusion with ownership
- **ReadWriteLock** - Optimized for read-heavy workloads
- **BlockingQueue** - Producer-consumer with bounded capacity
- **CountDownLatch** - One-time synchronization barrier

---

**Package**: `com.shan.concurrency.threadspatterns.semaphore`

**Pattern Type**: Synchronization - Resource Access Control
**Thread Flow**: Permit-based bounded concurrency
**Best For**: Connection pools, rate limiting, resource management with limited capacity
