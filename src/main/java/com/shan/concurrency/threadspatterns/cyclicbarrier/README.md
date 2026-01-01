# CyclicBarrier Pattern - Multi-Phase Synchronization

This package demonstrates thread synchronization using CyclicBarrier, where multiple threads wait for each other at a common barrier point before proceeding.

## 📚 Documentation

**Detailed Example**: [Multi-Hop Tour Example](../../../../docs/MULTI_HOP_TOUR_EXAMPLE.md) - Complete walkthrough of the multi-hop tour demo

## 📦 Package Contents

### Demo Classes
1. **CyclicBarrierDemo.java** - Matrix row processing (basic example)
2. **MultiHopTourDemo.java** - City tour with multiple stops (advanced example)

### Supporting Classes
- **MatrixRowProcessor.java** - Worker for matrix processing demo
- **Tourist.java** - Worker for tour demo

### Runner
- **Main.java** - Run demos independently

## 🎯 What Is CyclicBarrier?

**CyclicBarrier** is a synchronization primitive that allows a set of threads to wait for each other at a common barrier point.

**Key Characteristics**:
- ✅ **Cyclic**: Can be reused multiple times
- ✅ **Barrier Action**: Optional action executed when all threads arrive
- ✅ **Fixed Parties**: Number of threads is set at creation
- ✅ **All-or-Nothing**: All threads must arrive before any can proceed

**Use Cases**:
- Multi-phase algorithms where all threads must complete each phase
- Batch processing with synchronization points
- Simulations with discrete time steps
- Game development (synchronize player actions between rounds)
- Parallel testing (coordinate test threads)

## 📊 Demo 1: Matrix Row Processing (Basic)

**Scenario**: Process 4 matrix rows in parallel, synchronize before column processing

### Code Pattern
```java
// Create barrier with number of parties and barrier action
Runnable barrierAction = () -> {
    log.info("All rows processed! Ready for column processing.");
};

CyclicBarrier barrier = new CyclicBarrier(4, barrierAction);

// Submit row processors
for (int i = 0; i < 4; i++) {
    executor.submit(new MatrixRowProcessor(i, rowData[i], barrier));
}
```

### Thread Flow
```
Thread-1: Process Row 0 → [Wait at Barrier] ────┐
Thread-2: Process Row 1 → [Wait at Barrier] ────┤
Thread-3: Process Row 2 → [Wait at Barrier] ────┼→ [All Arrived!]
Thread-4: Process Row 3 → [Wait at Barrier] ────┘   [Barrier Action]
                                                      [All Released]
```

### Running
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.cyclicbarrier.Main" \
  -Dexec.args="matrix"
```

---

## 🗺️ Demo 2: Multi-Hop City Tour (Advanced)

**Scenario**: 5 tourists visit 4 city attractions, synchronizing at each spot for group activities

**Why This Example?**
- Demonstrates **cyclic** nature (barrier reused 4 times)
- Shows **barrier actions** (group meals, photo sessions)
- Realistic use case (tour guide coordination)

### Tour Route
1. **Historical Museum** → Group lunch at café
2. **Art Gallery** → Group photo session
3. **Botanical Garden** → Refreshment break
4. **Observation Deck** → Farewell dinner

### Code Pattern
```java
// Create barriers for each tour stop
List<CyclicBarrier> spotBarriers = new ArrayList<>();

for (int i = 0; i < tourSpots.size(); i++) {
    Runnable barrierAction = () -> {
        log.info("All tourists at {}! Having {}", spot, activity);
    };

    CyclicBarrier barrier = new CyclicBarrier(5, barrierAction);
    spotBarriers.add(barrier);
}

// Each tourist visits all spots
for (String touristName : tourists) {
    executor.submit(new Tourist(touristName, tourSpots, spotBarriers));
}
```

### Thread Flow
```
Tourist Alice:
  Travel to Museum → Explore → [Wait] ────┐
                                           │
Tourist Bob:                               │
  Travel to Museum → Explore → [Wait] ────┤
                                           │
Tourist Charlie:                           ├→ [All 5 Arrived!]
  Travel to Museum → Explore → [Wait] ────┤   [Group Lunch]
                                           │   [Released]
Tourist Diana:                             │
  Travel to Museum → Explore → [Wait] ────┤
                                           │
Tourist Eve:                               │
  Travel to Museum → Explore → [Wait] ────┘

Then same pattern for Art Gallery, Botanical Garden, Observation Deck...
```

### Sample Output
```
[pool-1-thread-1] Alice traveling to 'Historical Museum'...
[pool-1-thread-2] Bob traveling to 'Historical Museum'...
[pool-1-thread-3] Charlie traveling to 'Historical Museum'...
[pool-1-thread-4] Diana traveling to 'Historical Museum'...
[pool-1-thread-5] Eve traveling to 'Historical Museum'...

[pool-1-thread-1] Alice exploring 'Historical Museum'...
[pool-1-thread-2] Bob exploring 'Historical Museum'...
...

[pool-1-thread-1] Alice arrived at 'Historical Museum' and waiting... (1/5)
[pool-1-thread-2] Bob arrived at 'Historical Museum' and waiting... (2/5)
[pool-1-thread-3] Charlie arrived at 'Historical Museum' and waiting... (3/5)
[pool-1-thread-4] Diana arrived at 'Historical Museum' and waiting... (4/5)
[pool-1-thread-5] Eve arrived at 'Historical Museum' and waiting... (5/5)

╔════════════════════════════════════════════════════════════════╗
║ 🎯 SPOT 1/4: Historical Museum
║ 🍽️  GROUP ACTIVITY: Having group lunch at museum café
║ ✅ All 5 tourists are present!
╚════════════════════════════════════════════════════════════════╝

[pool-1-thread-1] Alice - All assembled! Proceeding with group activity.
[pool-1-thread-2] Bob - All assembled! Proceeding with group activity.
...

[pool-1-thread-1] Alice traveling to 'Art Gallery'...
...
```

### Running
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.cyclicbarrier.Main" \
  -Dexec.args="tour"
```

---

## 🔍 Key Concepts

### 1. Creating a Barrier

**Without Barrier Action**:
```java
CyclicBarrier barrier = new CyclicBarrier(5); // 5 parties
```

**With Barrier Action**:
```java
Runnable action = () -> System.out.println("All threads arrived!");
CyclicBarrier barrier = new CyclicBarrier(5, action);
```

### 2. Waiting at Barrier

```java
barrier.await(); // Thread blocks until all parties arrive
```

**With Timeout**:
```java
barrier.await(10, TimeUnit.SECONDS); // Throws TimeoutException if timeout
```

### 3. Barrier Action Execution

**Who executes it?**: The **last thread** to arrive at the barrier
**When?**: After all parties call `await()`, before any are released
**Purpose**: Perform a group action (merge results, log status, etc.)

### 4. Cyclic Nature

The barrier can be reused:
```java
CyclicBarrier barrier = new CyclicBarrier(3);

// First use
thread1.await(); thread2.await(); thread3.await(); // Released!

// Second use (same barrier)
thread1.await(); thread2.await(); thread3.await(); // Released again!
```

### 5. Broken Barriers

A barrier becomes "broken" if:
- A thread is interrupted while waiting
- A thread times out
- The barrier action throws an exception

When broken, all waiting threads throw `BrokenBarrierException`.

**Reset a broken barrier**:
```java
barrier.reset(); // Resets to initial state
```

## 📊 CyclicBarrier vs CountDownLatch

| Feature | CyclicBarrier | CountDownLatch |
|---------|---------------|----------------|
| **Reusable** | ✅ Yes (cyclic) | ❌ No (one-time) |
| **Participants** | Threads wait for each other | Threads wait for countdown |
| **Barrier Action** | ✅ Supported | ❌ Not supported |
| **Use Case** | Multi-phase synchronization | Wait for initialization |
| **Reset** | ✅ Can reset | ❌ Cannot reset |

**Example**:
```java
// CyclicBarrier: All threads wait for each other (mutual)
CyclicBarrier barrier = new CyclicBarrier(5);
// Each of 5 threads calls: barrier.await()

// CountDownLatch: Threads wait for countdown (one-way)
CountDownLatch latch = new CountDownLatch(5);
// 5 tasks call: latch.countDown()
// Main thread calls: latch.await()
```

## 🎯 Real-World Use Cases

### 1. Batch Processing
```java
// Process 1000 records in batches of 100
CyclicBarrier batchBarrier = new CyclicBarrier(10, () -> {
    log.info("Batch complete! Merging results...");
    mergeResults();
});

for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        processRecords(100);
        batchBarrier.await(); // Wait for batch to complete
    });
}
```

### 2. Simulation Systems
```java
// Discrete time simulation: all agents must complete step before next
CyclicBarrier stepBarrier = new CyclicBarrier(numAgents, () -> {
    log.info("Time step {} complete", currentStep++);
});

for (Agent agent : agents) {
    executor.submit(() -> {
        while (running) {
            agent.performActions();
            stepBarrier.await(); // Sync before next time step
        }
    });
}
```

### 3. Parallel Testing
```java
// Launch multiple threads simultaneously to test race conditions
CyclicBarrier startGate = new CyclicBarrier(numThreads + 1);

for (int i = 0; i < numThreads; i++) {
    executor.submit(() -> {
        startGate.await(); // Wait for all threads ready
        performConcurrentOperation(); // All start at same time!
    });
}

startGate.await(); // Release all threads simultaneously
```

## ⚠️ Common Pitfalls

### 1. Wrong Party Count
```java
CyclicBarrier barrier = new CyclicBarrier(5);

// Only 4 threads wait
thread1.await();
thread2.await();
thread3.await();
thread4.await();
// ❌ Deadlock! All 4 wait forever for 5th thread
```

### 2. Forgetting to Handle Exceptions
```java
try {
    barrier.await();
} catch (InterruptedException | BrokenBarrierException e) {
    // ✅ Must handle both exceptions
    log.error("Barrier failed", e);
}
```

### 3. Barrier Action Exceptions
```java
Runnable action = () -> {
    throw new RuntimeException("Oops!"); // ❌ Breaks barrier!
};

CyclicBarrier barrier = new CyclicBarrier(3, action);
// When action throws, barrier becomes broken
```

## 🚀 Running the Demos

### Run Both Demos
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.cyclicbarrier.Main"
```

### Run Matrix Demo Only
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.cyclicbarrier.Main" \
  -Dexec.args="matrix"
```

### Run Tour Demo Only
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.cyclicbarrier.Main" \
  -Dexec.args="tour"
```

## 🎓 Best Practices

✅ **DO:**
- Handle `InterruptedException` and `BrokenBarrierException`
- Use barrier actions for group operations (merging, logging)
- Reset barrier if it becomes broken (and safe to do so)
- Use timeout with `await()` to prevent indefinite blocking

❌ **DON'T:**
- Forget to await() in all participating threads (deadlock)
- Throw exceptions in barrier actions (breaks barrier)
- Reuse barrier for different party counts
- Use when one-time synchronization is sufficient (use CountDownLatch)

## 🔗 Related Patterns

- **CountDownLatch** - One-time countdown synchronization
- **Phaser** - Like CyclicBarrier but with dynamic parties
- **Semaphore** - Limit concurrent access to resources
- **CompletableFuture.allOf()** - Wait for multiple async operations

## 📈 Performance Considerations

**Overhead**: CyclicBarrier has low overhead for coordination
**Scalability**: Works well with moderate party counts (<100)
**Reuse**: Barrier reuse is efficient (no object creation)

**When Not to Use**:
- Very high party counts (consider Phaser)
- Dynamic participant joining/leaving (use Phaser)
- Simple wait-for-completion (use CountDownLatch)

---

**Package**: `com.shan.concurrency.threadspatterns.cyclicbarrier`

**See Also**: [Multi-Hop Tour Example](../../../../docs/MULTI_HOP_TOUR_EXAMPLE.md) for detailed walkthrough
