# ExecutorService Pattern - Thread Pool Management and Task Execution

This package demonstrates ExecutorService types and BlockingQueue strategies for flexible thread pool management.

## 🎯 What Is ExecutorService?

**ExecutorService** is a high-level API for managing thread pools and executing tasks asynchronously. It provides various thread pool implementations optimized for different scenarios and queue strategies for task management.

**Key Characteristics**:
- ✅ **Thread pool management**: Reuse threads instead of creating new ones
- ✅ **Multiple implementations**: Fixed, Cached, Single, Scheduled, WorkStealing pools
- ✅ **Queue strategies**: Different BlockingQueue implementations for task buffering
- ✅ **Future-based results**: Track async task completion
- ✅ **Lifecycle management**: Graceful shutdown with await termination

**Use Cases**:
- **Background tasks**: Async job processing
- **Scheduled tasks**: Periodic or delayed execution
- **Thread pool sizing**: Control concurrent thread count
- **Task queuing**: Buffering tasks with different strategies

---

## 📊 Thread Flow Pattern: Task Submission → Queue → Thread Pool → Execution

### Pattern: Producers Submit Tasks → Queue Buffers → Pool Executes

```
ExecutorService Architecture:

Task Submissions ──────────────────┐
                                   ↓
    submit(task1) ─┐           ┌─────────────────┐
    submit(task2) ─┼──────→    │ BlockingQueue   │
    submit(task3) ─┤           │ [T1][T2][T3][T4]│
    submit(task4) ─┘           └─────────────────┘
                                       ↓
                                  (Dequeue)
                                       ↓
                               ┌───────────────┐
                               │ Thread Pool   │
                               │ [T1][T2][T3]  │ ← Core pool size
                               └───────────────┘
                                       ↓
                                   Execute!
```

---

## 🔍 Demo 1: ExecutorService Types

**Scenario**: Compare 5 different ExecutorService types

### Thread Flow by Type

```
1. FixedThreadPool (3 threads, unbounded queue):
   ════════════════════════════════════════════════
   Threads: [T1][T2][T3] (Fixed count)
   Queue: LinkedBlockingQueue (Unbounded)

   Task-1, Task-2, Task-3 → Immediate execution
   Task-4, Task-5, ... → Queued (no limit)

   Use: CPU-intensive tasks, controlled concurrency


2. CachedThreadPool (dynamic sizing, direct handoff):
   ══════════════════════════════════════════════════
   Threads: Create as needed, reuse idle (60s timeout)
   Queue: SynchronousQueue (Direct handoff, no buffering)

   Task-1 → Create Thread-1
   Task-2 → Create Thread-2
   Task-3 (Thread-1 idle) → Reuse Thread-1

   Use: Short-lived async tasks, bursty workloads


3. SingleThreadExecutor (1 thread, sequential):
   ═══════════════════════════════════════════════
   Threads: [T1] (Single thread only)
   Queue: LinkedBlockingQueue (Unbounded)

   Task-1 → Execute
   Task-2 → Wait (sequential)
   Task-3 → Wait

   Use: Order-sensitive operations, no concurrency


4. ScheduledThreadPool (delayed & periodic):
   ═══════════════════════════════════════════
   Threads: [T1][T2] (Fixed)
   Queue: DelayedWorkQueue

   schedule(task, 500ms delay) → Execute after 500ms
   scheduleAtFixedRate(task, 200ms, 300ms) → Execute periodically

   Use: Cron-like jobs, periodic tasks


5. WorkStealingPool (ForkJoinPool, work-stealing):
   ═════════════════════════════════════════════════
   Threads: Runtime.availableProcessors() (Default)
   Queue: Each thread has own deque (work-stealing)

   Thread-1: [T1][T2][T3]
   Thread-2: [T4] (idle)
   Thread-2 STEALS T3 from Thread-1's queue

   Use: Divide-and-conquer, parallel recursive tasks
```

---

## 🔍 Demo 2: BlockingQueue Strategies

**Scenario**: Compare 5 different queue strategies with custom ThreadPoolExecutor

### Queue Strategies

```
1. ArrayBlockingQueue (Bounded, FIFO):
   ═══════════════════════════════════════
   Capacity: 5 (Fixed)
   Backing: Array

   Core: 2, Max: 4
   Queue: [T1][T2][T3][T4][T5] ← FULL

   Task-6 arrives:
   - Queue full → Try create new thread (up to max=4)
   - Max reached → Reject (CallerRunsPolicy)

   Use: Backpressure, bounded task queue


2. LinkedBlockingQueue (Optionally bounded, FIFO):
   ═══════════════════════════════════════════════════
   Capacity: 10 (Can be unbounded)
   Backing: Linked nodes

   Better throughput than ArrayBlockingQueue
   More memory overhead

   Use: High throughput, flexible capacity


3. SynchronousQueue (Zero capacity, direct handoff):
   ════════════════════════════════════════════════════
   Capacity: 0 (No buffering!)

   Producer: offer(task) → Must wait for consumer
   Consumer: take() → Direct handoff from producer

   Core: 1, Max: 5
   Every task triggers thread creation (up to max)

   Use: Immediate execution, CachedThreadPool uses this


4. PriorityBlockingQueue (Unbounded, priority-ordered):
   ════════════════════════════════════════════════════════
   Capacity: Unbounded
   Ordering: Natural order or Comparator

   High-priority tasks execute first

   Use: Priority-based scheduling


5. DelayQueue (Unbounded, delay-based):
   ═══════════════════════════════════════
   Capacity: Unbounded
   Ordering: Delay expiration

   Task-1 (delay: 500ms)
   Task-2 (delay: 200ms) ← Executes first!
   Task-3 (delay: 800ms)

   Use: Scheduled tasks, ScheduledThreadPool uses DelayedWorkQueue
```

---

## 🚀 Running the Demos

### Run All ExecutorService Types:
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.executorservice.Main" -Dexec.args="types"
```

### Run BlockingQueue Strategies:
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.executorservice.Main" -Dexec.args="queues"
```

### Expected Output (Types Demo)
```
=== ExecutorService Types Demo ===

--- 1. FixedThreadPool (3 threads) ---
Use case: CPU-intensive tasks, controlled concurrency
Queue: Unbounded LinkedBlockingQueue

[pool-1-thread-1] Executing FTP-Task-1
[pool-1-thread-2] Executing FTP-Task-2
[pool-1-thread-3] Executing FTP-Task-3
[pool-1-thread-1] Executing FTP-Task-4 (thread reused)
...
FixedThreadPool: 10/10 tasks completed successfully


--- 2. CachedThreadPool (Dynamic sizing) ---
Use case: Many short-lived async tasks
Queue: SynchronousQueue (direct handoff)

[pool-2-thread-1] Executing CTP-Task-1
[pool-2-thread-2] Executing CTP-Task-2 (new thread created)
[pool-2-thread-1] Executing CTP-Task-3 (thread-1 reused)
...
CachedThreadPool: 10/10 tasks completed successfully


--- 3. SingleThreadExecutor (1 thread, sequential) ---
Use case: Order-sensitive operations, no concurrency
Queue: Unbounded LinkedBlockingQueue

[pool-3-thread-1] Executing STE-Task-1
[pool-3-thread-1] Executing STE-Task-2 (sequential)
[pool-3-thread-1] Executing STE-Task-3 (sequential)
...
SingleThreadExecutor: 5/5 tasks completed successfully (sequential)


--- 4. ScheduledThreadPool (Delayed & Periodic tasks) ---
Use case: Scheduled/periodic tasks, cron-like jobs

Scheduling one-time task with 500ms delay...
Scheduling periodic task (every 300ms, 3 times)...
[pool-4-thread-1] Periodic task executing
[pool-4-thread-1] Periodic task executing
[pool-4-thread-1] Periodic task executing
ScheduledThreadPool: Delayed and periodic tasks completed


--- 5. WorkStealingPool (ForkJoinPool, work-stealing) ---
Use case: Parallel recursive tasks, divide-and-conquer
Parallelism: 15

[ForkJoinPool-1-worker-1] Executing WSP-Task-1
[ForkJoinPool-1-worker-2] Executing WSP-Task-2
[ForkJoinPool-1-worker-3] Executing WSP-Task-3 (work-stealing!)
...
WorkStealingPool: 10/10 tasks completed successfully
```

---

## 🔑 Key Methods

### Create ExecutorService
```java
// Fixed thread pool
ExecutorService executor = Executors.newFixedThreadPool(4);

// Cached (dynamic sizing)
ExecutorService executor = Executors.newCachedThreadPool();

// Single thread (sequential)
ExecutorService executor = Executors.newSingleThreadExecutor();

// Scheduled pool
ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

// Work-stealing
ExecutorService executor = Executors.newWorkStealingPool();

// Custom ThreadPoolExecutor
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    corePoolSize, maxPoolSize, keepAliveTime,
    TimeUnit.SECONDS, blockingQueue, rejectionPolicy
);
```

### Submit Tasks
```java
executor.execute(runnable);                    // Fire and forget
Future<?> future = executor.submit(runnable);  // Track completion
Future<T> future = executor.submit(callable);  // Get result
```

### Shutdown
```java
executor.shutdown();                           // Graceful shutdown
executor.awaitTermination(60, TimeUnit.SECONDS); // Wait for completion
executor.shutdownNow();                        // Force shutdown
```

---

## 🎯 Real-World Use Cases

### 1. Background Job Processing
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
for (Job job : jobs) {
    executor.submit(() -> processJob(job));
}
executor.shutdown();
executor.awaitTermination(1, TimeUnit.HOURS);
```

### 2. Scheduled Tasks
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

// Run every hour
scheduler.scheduleAtFixedRate(() -> {
    cleanupOldData();
}, 0, 1, TimeUnit.HOURS);

// Run with delay after completion
scheduler.scheduleWithFixedDelay(() -> {
    pollQueue();
}, 0, 10, TimeUnit.SECONDS);
```

### 3. Custom Queue Strategy
```java
BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    4, 10, 60, TimeUnit.SECONDS, queue,
    new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
);
```

---

## 🎓 Best Practices

✅ **DO:**
- Shutdown executors when done
- Use FixedThreadPool for CPU tasks
- Use CachedThreadPool for I/O tasks
- Handle RejectedExecutionException
- Choose appropriate queue size

❌ **DON'T:**
- Create unbounded queues in production
- Forget to shutdown executors
- Use too many threads for CPU tasks
- Ignore rejected tasks

---

## 📊 ExecutorService Type Selection Guide

| Workload | Recommended Type | Reasoning |
|----------|------------------|-----------|
| **CPU-intensive** | FixedThreadPool | Limit threads to CPU count |
| **I/O-intensive** | CachedThreadPool or Virtual Threads | Create threads on demand |
| **Sequential** | SingleThreadExecutor | Guarantee order |
| **Scheduled** | ScheduledThreadPool | Built-in delay/period support |
| **Divide-and-conquer** | WorkStealingPool | Automatic load balancing |

---

**Package**: `com.shan.concurrency.threadspatterns.executorservice`

**Pattern Type**: Thread Pool - Task Execution
**Thread Flow**: Task submission → Queue → Thread pool → Execution
**Best For**: Background processing, scheduled tasks, thread pool management
