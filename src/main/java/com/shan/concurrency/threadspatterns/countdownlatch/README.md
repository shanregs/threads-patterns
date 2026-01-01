# CountDownLatch Pattern - Wait for Multiple Tasks

This package demonstrates using CountDownLatch to wait for multiple tasks to complete before proceeding.

## 🎯 What Is CountDownLatch?

**CountDownLatch** is a synchronization primitive that allows one or more threads to wait until a set of operations being performed in other threads completes.

**Key Characteristics**:
- ✅ **One-time use**: Cannot be reset (use CyclicBarrier for reusable)
- ✅ **Countdown**: Threads count down, one or more wait for zero
- ✅ **Decoupling**: Waiting threads don't need to know about counting threads
- ✅ **Unidirectional**: Waiting is separate from counting

**Use Cases**:
- Wait for multiple services to start before proceeding
- Wait for batch job tasks to complete
- Ensure all resources are initialized
- Coordinate test thread startup
- Wait for parallel computations to finish

## 📊 Demo: Batch Job Coordination

**Scenario**: Process 5 data files in parallel, then aggregate results

### Thread Flow Pattern: **Parallel Processing + Single Wait Point**

```
Main Thread:
  Submit all tasks → [Wait at Latch] ─────────────┐
                                                   │
Worker Threads (Parallel):                        │
  Thread-1: Process File-1 (1500ms) → CountDown ──┤
  Thread-2: Process File-2 (2000ms) → CountDown ──┤
  Thread-3: Process File-3 (2500ms) → CountDown ──┼→ Count reaches 0
  Thread-4: Process File-4 (3000ms) → CountDown ──┤   Main thread released
  Thread-5: Process File-5 (3500ms) → CountDown ──┘
                                                   ↓
Main Thread:
  Aggregate results → Complete
```

### Code Pattern

**1. Create Latch with Count**:
```java
CountDownLatch latch = new CountDownLatch(5); // Wait for 5 tasks
```

**2. Workers Count Down**:
```java
executor.submit(() -> {
    processFile("File-1");
    latch.countDown(); // Decrement count
});
```

**3. Main Thread Waits**:
```java
latch.await(); // Blocks until count reaches 0
log.info("All tasks completed! Aggregating results...");
```

### Sequential Flow

```
Time 0ms:
  Main: Create latch(5)
  Main: Submit 5 tasks to executor
  Main: await() → BLOCKS

  Worker-1: Start File-1 (1500ms)
  Worker-2: Start File-2 (2000ms)
  Worker-3: Start File-3 (2500ms)

Time 1500ms:
  Worker-1: Complete File-1
  Worker-1: countDown() → count = 4
  Worker-1: Start File-4 (3000ms)

Time 2000ms:
  Worker-2: Complete File-2
  Worker-2: countDown() → count = 3
  Worker-2: Start File-5 (3500ms)

Time 2500ms:
  Worker-3: Complete File-3
  Worker-3: countDown() → count = 2

Time 4500ms:
  Worker-1: Complete File-4
  Worker-1: countDown() → count = 1

Time 5500ms:
  Worker-2: Complete File-5
  Worker-2: countDown() → count = 0 ← LATCH RELEASED!
  Main: await() returns → Proceed with aggregation
```

### Running
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.countdownlatch.Main"
```

### Expected Output
```
[main] All tasks submitted. Waiting for completion...
[pool-3-thread-1] Task 'DataFile-1' started
[pool-3-thread-2] Task 'DataFile-2' started
[pool-3-thread-3] Task 'DataFile-3' started
[pool-3-thread-1] Task 'DataFile-1' completed successfully
[pool-3-thread-1] Task 'DataFile-1' counted down. Remaining tasks: 4
[pool-3-thread-1] Task 'DataFile-4' started
[pool-3-thread-2] Task 'DataFile-2' completed successfully
[pool-3-thread-2] Task 'DataFile-2' counted down. Remaining tasks: 3
[pool-3-thread-2] Task 'DataFile-5' started
[pool-3-thread-3] Task 'DataFile-3' completed successfully
[pool-3-thread-3] Task 'DataFile-3' counted down. Remaining tasks: 2
[pool-3-thread-1] Task 'DataFile-4' completed successfully
[pool-3-thread-1] Task 'DataFile-4' counted down. Remaining tasks: 1
[pool-3-thread-2] Task 'DataFile-5' completed successfully
[pool-3-thread-2] Task 'DataFile-5' counted down. Remaining tasks: 0
[main] All tasks completed! Proceeding with result aggregation.
```

## 🔍 Thread Flow Analysis

### Pattern: Fan-Out, Wait, Continue

**Phase 1: Fan-Out (Parallel)**
```
Main Thread → Spawns 5 worker threads
All workers execute in PARALLEL
```

**Phase 2: Wait Point**
```
Main Thread: BLOCKED at await()
Workers: Execute independently, count down when done
```

**Phase 3: Continue**
```
Last worker: countDown() → count = 0
Main Thread: UNBLOCKED → Continue execution
```

### Key Insight: Decoupled Waiting

**Workers don't wait for each other**:
- File-1 completes at 1500ms, immediately counts down and exits
- File-5 completes at 5500ms, counts down and exits
- Workers don't synchronize with each other

**Main thread waits for all**:
- Blocks at await() until ALL workers count down
- Doesn't care which order they complete

## 🎯 Real-World Use Cases

### 1. Service Startup Coordination
```java
CountDownLatch startupLatch = new CountDownLatch(3);

// Start database
executor.submit(() -> {
    database.initialize();
    startupLatch.countDown();
});

// Start cache
executor.submit(() -> {
    cache.initialize();
    startupLatch.countDown();
});

// Start message queue
executor.submit(() -> {
    messageQueue.initialize();
    startupLatch.countDown();
});

// Wait for all services
startupLatch.await();
log.info("All services started! Application ready.");
```

### 2. Parallel Test Execution
```java
CountDownLatch testLatch = new CountDownLatch(numTests);

for (Test test : tests) {
    executor.submit(() -> {
        try {
            test.run();
        } finally {
            testLatch.countDown(); // Always count down
        }
    });
}

testLatch.await();
log.info("All tests completed!");
```

### 3. MapReduce Pattern
```java
// Map phase
CountDownLatch mapLatch = new CountDownLatch(numMappers);
for (DataChunk chunk : chunks) {
    executor.submit(() -> {
        map(chunk);
        mapLatch.countDown();
    });
}

// Wait for all mappers
mapLatch.await();

// Reduce phase
List<Result> results = reduce();
```

## 📊 CountDownLatch vs CyclicBarrier

| Feature | CountDownLatch | CyclicBarrier |
|---------|----------------|---------------|
| **Reusable** | ❌ One-time only | ✅ Cyclic/reusable |
| **Direction** | Unidirectional (workers → waiter) | Mutual (all wait for each other) |
| **Barrier Action** | ❌ Not supported | ✅ Supported |
| **Use Case** | Wait for completion | Multi-phase sync |
| **Parties** | Fixed at creation | Fixed at creation |
| **Who Waits** | Usually main thread | All participating threads |

**Example**:
```java
// CountDownLatch: Main waits for workers
CountDownLatch latch = new CountDownLatch(5);
for (int i = 0; i < 5; i++) {
    executor.submit(() -> {
        doWork();
        latch.countDown(); // Workers count down
    });
}
latch.await(); // Main waits

// CyclicBarrier: All threads wait for each other
CyclicBarrier barrier = new CyclicBarrier(5);
for (int i = 0; i < 5; i++) {
    executor.submit(() -> {
        doWork();
        barrier.await(); // Each worker waits for others
    });
}
```

## 🔑 Key Methods

### Creating
```java
CountDownLatch latch = new CountDownLatch(5); // Initial count
```

### Counting Down
```java
latch.countDown(); // Decrements count by 1
```

### Waiting
```java
latch.await(); // Wait indefinitely

latch.await(10, TimeUnit.SECONDS); // Wait with timeout
```

### Checking
```java
long count = latch.getCount(); // Get current count
```

## ⚠️ Common Pitfalls

### 1. Forgetting to Count Down
```java
executor.submit(() -> {
    processFile("data.txt");
    // ❌ Forgot countDown()! Main thread waits forever
});
```

**Solution**: Use try-finally
```java
executor.submit(() -> {
    try {
        processFile("data.txt");
    } finally {
        latch.countDown(); // ✅ Always count down
    }
});
```

### 2. Wrong Initial Count
```java
CountDownLatch latch = new CountDownLatch(5);

// Only submit 3 tasks
for (int i = 0; i < 3; i++) {
    executor.submit(() -> {
        doWork();
        latch.countDown();
    });
}

latch.await(); // ❌ Waits forever! Count never reaches 0
```

### 3. Trying to Reuse
```java
CountDownLatch latch = new CountDownLatch(3);
latch.await(); // Count reaches 0

// ❌ Cannot reuse! Latch stays at 0
latch.countDown(); // No effect
```

**Solution**: Create new latch or use CyclicBarrier

## 🎓 Best Practices

✅ **DO:**
- Use try-finally to ensure countDown()
- Set initial count to match number of tasks
- Use timeout with await() to prevent indefinite blocking
- Create new latch for each use (not reusable)

❌ **DON'T:**
- Forget to count down (causes deadlock)
- Try to reuse the latch (create new one)
- Count down multiple times in same task (unless intended)
- Use for mutual synchronization (use CyclicBarrier)

## 📈 Performance Considerations

**Overhead**: Very low - simple counter with wait/notify
**Scalability**: Excellent - works well with large counts
**Memory**: Minimal - single counter

**When to Use**:
- ✅ One-time wait for multiple tasks
- ✅ Service initialization
- ✅ Batch job coordination
- ✅ Test synchronization

**When Not to Use**:
- ❌ Need reusable barrier (use CyclicBarrier)
- ❌ Mutual thread synchronization (use CyclicBarrier)
- ❌ Single task wait (use Future.get())

## 🔗 Related Patterns

- **CyclicBarrier** - Reusable, mutual synchronization
- **Phaser** - Like CountDownLatch but reusable with phases
- **CompletableFuture.allOf()** - Wait for multiple async operations
- **Semaphore** - Control access to resources (different purpose)

---

**Package**: `com.shan.concurrency.threadspatterns.countdownlatch`

**Pattern Type**: Synchronization - Wait for Completion
**Thread Flow**: Parallel Execution → Single Wait Point
