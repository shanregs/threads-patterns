# Java Executor Framework - Comprehensive Guide

## Table of Contents
1. [Overview](#overview)
2. [Executor Framework Architecture](#executor-framework-architecture)
3. [ExecutorService Types](#executorservice-types)
4. [BlockingQueue Strategies](#blockingqueue-strategies)
5. [ThreadPoolExecutor Deep Dive](#threadpoolexecutor-deep-dive)
6. [Task Lifecycle](#task-lifecycle)
7. [Visual Diagrams](#visual-diagrams)
8. [Best Practices](#best-practices)
9. [Common Pitfalls](#common-pitfalls)

---

## Overview

The Java Executor Framework provides a higher-level replacement for working with threads directly. It separates task submission from task execution mechanics.

### Why Use Executor Framework?

- **Thread Reuse**: Avoids overhead of creating new threads
- **Resource Management**: Controls concurrent thread count
- **Task Queue Management**: Handles task backlog efficiently
- **Lifecycle Management**: Controlled shutdown mechanisms
- **Better Scalability**: Handles thousands of tasks with limited threads

---

## Executor Framework Architecture

### Class Hierarchy

```
                    Executor (I)
                        |
                 ExecutorService (I)
                        |
        +---------------+---------------+
        |                               |
  ThreadPoolExecutor         ScheduledThreadPoolExecutor
        |                               |
        |                     ScheduledExecutorService (I)
        |
  AbstractExecutorService (Abstract)
```

### Core Interfaces

```java
// 1. Executor - Basic task execution
public interface Executor {
    void execute(Runnable command);
}

// 2. ExecutorService - Lifecycle management + task submission
public interface ExecutorService extends Executor {
    void shutdown();
    List<Runnable> shutdownNow();
    <T> Future<T> submit(Callable<T> task);
    <T> Future<T> submit(Runnable task, T result);
    Future<?> submit(Runnable task);
    // ... more methods
}

// 3. ScheduledExecutorService - Delayed/periodic execution
public interface ScheduledExecutorService extends ExecutorService {
    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);
    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);
    ScheduledFuture<?> scheduleAtFixedRate(...);
    ScheduledFuture<?> scheduleWithFixedDelay(...);
}
```

---

## ExecutorService Types

### Visual Comparison

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    ExecutorService Types                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. FixedThreadPool                                                     │
│     [T1] [T2] [T3]  ←→  [Queue: unlimited]                             │
│     Fixed threads, unbounded queue                                      │
│                                                                         │
│  2. CachedThreadPool                                                    │
│     [T1] [T2] ... [Tn]  ←→  [No Queue - Direct Handoff]               │
│     Dynamic threads (0 to ∞), 60s idle timeout                         │
│                                                                         │
│  3. SingleThreadExecutor                                                │
│     [T1]  ←→  [Queue: unlimited, Sequential]                           │
│     Single thread, guaranteed order                                     │
│                                                                         │
│  4. ScheduledThreadPool                                                 │
│     [T1] [T2] ... [Tn]  ←→  [DelayQueue]                              │
│     Delayed/periodic tasks                                              │
│                                                                         │
│  5. WorkStealingPool                                                    │
│     [T1-Q] [T2-Q] [T3-Q] [T4-Q]  ←→  Work Stealing                    │
│     ForkJoinPool, each thread has deque                                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1. FixedThreadPool

**Architecture:**
```
Task Submission → [Queue] → [Worker-1]
                           → [Worker-2]
                           → [Worker-3]
                           → [Worker-N]
```

**Characteristics:**
- **Threads**: Fixed count (corePoolSize = maxPoolSize)
- **Queue**: Unbounded `LinkedBlockingQueue`
- **Behavior**: Tasks wait in queue if all threads busy

**When to Use:**
- CPU-intensive tasks
- Known concurrency limit
- Want to prevent resource exhaustion

**Code Example:**
```java
ExecutorService executor = Executors.newFixedThreadPool(4);
```

**Internal Configuration:**
```java
new ThreadPoolExecutor(
    nThreads,           // corePoolSize
    nThreads,           // maximumPoolSize (same)
    0L,                 // keepAliveTime
    TimeUnit.MILLISECONDS,
    new LinkedBlockingQueue<Runnable>()
)
```

---

### 2. CachedThreadPool

**Architecture:**
```
Task → SynchronousQueue → Available Thread?
                         ↓ Yes: Use it
                         ↓ No: Create new thread

Idle threads (60s) → Terminated
```

**Characteristics:**
- **Threads**: 0 to Integer.MAX_VALUE
- **Queue**: `SynchronousQueue` (no storage)
- **Behavior**: Creates threads on demand, reuses idle threads

**When to Use:**
- Many short-lived async tasks
- I/O-bound operations
- Tasks arrive unpredictably

**Warning:** Can create too many threads → OOM

**Code Example:**
```java
ExecutorService executor = Executors.newCachedThreadPool();
```

**Internal Configuration:**
```java
new ThreadPoolExecutor(
    0,                  // corePoolSize
    Integer.MAX_VALUE,  // maximumPoolSize (unbounded!)
    60L,                // keepAliveTime
    TimeUnit.SECONDS,
    new SynchronousQueue<Runnable>()
)
```

---

### 3. SingleThreadExecutor

**Architecture:**
```
Task-1 → Task-2 → Task-3 → [Queue] → [Single Worker Thread]
                                      (Sequential execution)
```

**Characteristics:**
- **Threads**: Exactly 1
- **Queue**: Unbounded `LinkedBlockingQueue`
- **Behavior**: Guarantees sequential execution

**When to Use:**
- Order-sensitive operations
- Avoid concurrency issues
- Simple background processing

**Code Example:**
```java
ExecutorService executor = Executors.newSingleThreadExecutor();
```

---

### 4. ScheduledThreadPool

**Architecture:**
```
Delayed Tasks → [DelayQueue] → Workers check → Ready? → Execute
                                             ↓ Not ready → Wait
```

**Characteristics:**
- **Threads**: Fixed count
- **Queue**: `DelayedWorkQueue` (priority queue + delay)
- **Behavior**: Executes tasks after delay or periodically

**When to Use:**
- Scheduled jobs
- Periodic maintenance tasks
- Delayed execution

**Code Example:**
```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

// One-time delayed
executor.schedule(task, 5, TimeUnit.SECONDS);

// Periodic (fixed rate)
executor.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);

// Periodic (fixed delay)
executor.scheduleWithFixedDelay(task, 0, 10, TimeUnit.SECONDS);
```

**Fixed Rate vs Fixed Delay:**
```
Fixed Rate (period = 2s):
Task: |---1s---|  |---1s---|  |---1s---|
Time: 0s   1s   2s   3s   4s   5s   6s
      ↑Start   ↑Start   ↑Start

Fixed Delay (delay = 2s):
Task: |---1s---|     |---1s---|     |---1s---|
Time: 0s   1s   2s 3s   4s   5s 6s   7s   8s
      ↑Start   ↑Wait ↑Start   ↑Wait ↑Start
```

---

### 5. WorkStealingPool

**Architecture:**
```
Worker-1: [Deque-1] ← Steal from others when idle
Worker-2: [Deque-2] ← Steal from others when idle
Worker-3: [Deque-3] ← Own work + stolen work
Worker-4: [Deque-4]

Each worker has its own deque (double-ended queue)
Idle workers steal from the tail of busy workers' deques
```

**Characteristics:**
- **Implementation**: `ForkJoinPool`
- **Threads**: Parallelism level (default = available processors)
- **Behavior**: Work-stealing algorithm for load balancing

**When to Use:**
- Parallel recursive algorithms
- Divide-and-conquer tasks
- CPU-intensive parallel processing

**Code Example:**
```java
ExecutorService executor = Executors.newWorkStealingPool();
// Or specify parallelism:
ExecutorService executor = Executors.newWorkStealingPool(4);
```

---

## BlockingQueue Strategies

### Queue Comparison Matrix

| Queue Type | Bounded | Ordering | Use Case |
|------------|---------|----------|----------|
| **ArrayBlockingQueue** | ✅ Yes | FIFO | Bounded queue, backpressure |
| **LinkedBlockingQueue** | ⚠️ Optional | FIFO | High throughput, flexible |
| **SynchronousQueue** | ✅ (0) | N/A | Direct handoff, immediate |
| **PriorityBlockingQueue** | ❌ No | Priority | Priority-based scheduling |
| **DelayQueue** | ❌ No | Delay | Scheduled/delayed tasks |

### Visual Representation

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    BlockingQueue Strategies                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. ArrayBlockingQueue (Capacity: 5)                                    │
│     [T1][T2][T3][T4][T5]  ← Array, bounded, FIFO                       │
│     Producer blocks if full, Consumer blocks if empty                   │
│                                                                         │
│  2. LinkedBlockingQueue (Capacity: optional)                            │
│     [T1]→[T2]→[T3]→[T4]...  ← Linked nodes, FIFO                      │
│     Better throughput than Array, can be unbounded                      │
│                                                                         │
│  3. SynchronousQueue (No storage!)                                      │
│     Producer → [Handoff] → Consumer                                     │
│     Direct transfer, no buffering                                       │
│                                                                         │
│  4. PriorityBlockingQueue (Unbounded)                                   │
│     [P1:High][P2:High][P3:Med][P4:Low]...  ← Priority order            │
│     Higher priority tasks execute first                                 │
│                                                                         │
│  5. DelayQueue (Unbounded)                                              │
│     [T1:500ms][T2:1s][T3:2s]...  ← Delay-based                         │
│     Elements available only after delay expires                         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1. ArrayBlockingQueue

**Internal Structure:**
```
Array: [0][1][2][3][4]
        ↑          ↑
      head       tail

Operations:
- put() blocks if array is full
- take() blocks if array is empty
- Fairness option for thread ordering
```

**Characteristics:**
- Fixed capacity (set at creation)
- FIFO ordering
- Optional fairness (fair lock ordering)
- Good for bounded resource pools

**Code Example:**
```java
BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2, 4, 10, TimeUnit.SECONDS,
    queue,
    new ThreadPoolExecutor.CallerRunsPolicy()
);
```

---

### 2. LinkedBlockingQueue

**Internal Structure:**
```
Node → Node → Node → Node → ...
 ↑                      ↑
head                  tail

Each node: {item, next}
Separate locks for head and tail → better concurrency
```

**Characteristics:**
- Optionally bounded (default: Integer.MAX_VALUE)
- FIFO ordering
- Higher throughput than ArrayBlockingQueue
- Used by FixedThreadPool

**Code Example:**
```java
// Bounded
BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(200);

// Unbounded (dangerous!)
BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
```

---

### 3. SynchronousQueue

**Concept:**
```
Producer Thread              Consumer Thread
      |                            |
      | put(item)                  |
      |--------- [waiting] ------→ | take()
      |                            | (gets item immediately)
      | (unblocks)                 |

No internal capacity - pure handoff!
```

**Characteristics:**
- Zero capacity (no storage)
- Each put() waits for take()
- Direct thread-to-thread transfer
- Used by CachedThreadPool

**Code Example:**
```java
BlockingQueue<Runnable> queue = new SynchronousQueue<>();
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
    queue
);
```

---

### 4. PriorityBlockingQueue

**Internal Structure:**
```
Binary Heap:
         [P1:10]
        /       \
    [P2:5]     [P3:7]
    /    \     /    \
[P4:2][P5:4][P6:3][P7:6]

Higher priority = earlier execution
```

**Characteristics:**
- Unbounded
- Natural ordering or custom Comparator
- Not strictly FIFO
- Priority-based dequeue

**Code Example:**
```java
// Custom priority task
class PriorityTask implements Runnable, Comparable<PriorityTask> {
    int priority;

    @Override
    public int compareTo(PriorityTask other) {
        return Integer.compare(other.priority, this.priority); // Higher first
    }

    @Override
    public void run() { /* task logic */ }
}

BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
```

---

### 5. DelayQueue

**Concept:**
```
Current Time: T = 1000ms

Queue:
[Task1: T+500ms]  ← Available at 1500ms
[Task2: T+200ms]  ← Available at 1200ms (executes first!)
[Task3: T+1000ms] ← Available at 2000ms

Only expired elements can be taken
```

**Characteristics:**
- Elements must implement `Delayed`
- Unbounded
- Only expired elements can be taken
- Used for scheduled tasks

**Code Example:**
```java
// Element must implement Delayed
class DelayedTask implements Runnable, Delayed {
    long startTime;

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(startTime - System.currentTimeMillis(),
                           TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.startTime,
                          ((DelayedTask)other).startTime);
    }

    @Override
    public void run() { /* task logic */ }
}
```

---

## ThreadPoolExecutor Deep Dive

### Constructor Parameters

```java
public ThreadPoolExecutor(
    int corePoolSize,           // 1. Minimum threads to keep alive
    int maximumPoolSize,        // 2. Maximum threads allowed
    long keepAliveTime,         // 3. Idle thread timeout
    TimeUnit unit,              // 4. Unit for keepAliveTime
    BlockingQueue<Runnable> workQueue,  // 5. Task queue
    ThreadFactory threadFactory,        // 6. Thread creation (optional)
    RejectedExecutionHandler handler    // 7. Rejection policy (optional)
)
```

### Thread Pool Sizing

```
┌─────────────────────────────────────────────────────────────┐
│              ThreadPoolExecutor Behavior                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Task arrives:                                              │
│                                                             │
│  1. Active threads < corePoolSize?                          │
│     YES → Create new thread                                 │
│     NO  → Go to step 2                                      │
│                                                             │
│  2. Queue has space?                                        │
│     YES → Add to queue                                      │
│     NO  → Go to step 3                                      │
│                                                             │
│  3. Active threads < maximumPoolSize?                       │
│     YES → Create new thread (up to max)                     │
│     NO  → Go to step 4                                      │
│                                                             │
│  4. REJECT task (use RejectedExecutionHandler)              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Rejection Policies

```java
// 1. AbortPolicy (default) - Throws RejectedExecutionException
new ThreadPoolExecutor.AbortPolicy()

// 2. CallerRunsPolicy - Runs task in caller's thread
new ThreadPoolExecutor.CallerRunsPolicy()

// 3. DiscardPolicy - Silently discards task
new ThreadPoolExecutor.DiscardPolicy()

// 4. DiscardOldestPolicy - Discards oldest unhandled task
new ThreadPoolExecutor.DiscardOldestPolicy()

// 5. Custom policy
new RejectedExecutionHandler() {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        // Custom logic: log, queue elsewhere, etc.
    }
}
```

### Optimal Thread Pool Size

```
CPU-Intensive Tasks:
  threads = CPU cores + 1
  (Extra thread compensates for page faults)

I/O-Intensive Tasks:
  threads = CPU cores × (1 + Wait Time / Compute Time)

Example: If task waits 75% of time (3:1 ratio)
  threads = 8 cores × (1 + 3) = 32 threads

Mixed Workload:
  threads = CPU cores × Target CPU utilization × (1 + W/C)
```

---

## Task Lifecycle

### State Transitions

```
                    ┌─────────────┐
                    │   CREATED   │
                    └──────┬──────┘
                           │ submit()
                           ↓
                    ┌─────────────┐
                    │   QUEUED    │
                    └──────┬──────┘
                           │ thread available
                           ↓
                    ┌─────────────┐
                    │   RUNNING   │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │                         │
              ↓                         ↓
       ┌─────────────┐           ┌─────────────┐
       │  COMPLETED  │           │   FAILED    │
       └─────────────┘           └─────────────┘
```

### Future States

```java
Future<Result> future = executor.submit(task);

// States:
future.isDone()      // true if completed (success/failure/cancelled)
future.isCancelled() // true if cancelled before completion
future.get()         // Blocks until done, returns result or throws
future.get(5, SECONDS) // Blocks with timeout
future.cancel(true)  // Attempt to cancel (mayInterruptIfRunning)
```

---

## Visual Diagrams

### Complete Executor Framework

```
┌───────────────────────────────────────────────────────────────────────────┐
│                      EXECUTOR FRAMEWORK OVERVIEW                          │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│  Client Code                                                              │
│  ───────────                                                              │
│     │                                                                     │
│     │ submit(task)                                                        │
│     ↓                                                                     │
│  ┌──────────────────┐                                                    │
│  │ ExecutorService  │                                                    │
│  └────────┬─────────┘                                                    │
│           │                                                               │
│           │ manages                                                       │
│           ↓                                                               │
│  ┌─────────────────────────────────────────┐                            │
│  │     ThreadPoolExecutor                  │                            │
│  │  ┌──────────────────────────────────┐  │                            │
│  │  │  Core Components:                │  │                            │
│  │  │  • corePoolSize                  │  │                            │
│  │  │  • maximumPoolSize               │  │                            │
│  │  │  • keepAliveTime                 │  │                            │
│  │  │  • workQueue (BlockingQueue)     │  │                            │
│  │  │  • rejectedExecutionHandler      │  │                            │
│  │  └──────────────────────────────────┘  │                            │
│  └─────────┬───────────────────────────────┘                            │
│            │                                                              │
│            │ manages                                                      │
│            ↓                                                              │
│  ┌─────────────────────────────────────────┐                            │
│  │  BlockingQueue<Runnable>                │                            │
│  │  ┌──────────┬──────────┬──────────┐    │                            │
│  │  │  Task 1  │  Task 2  │  Task 3  │... │                            │
│  │  └──────────┴──────────┴──────────┘    │                            │
│  └─────────┬───────────────────────────────┘                            │
│            │ feeds                                                        │
│            ↓                                                              │
│  ┌─────────────────────────────────────────┐                            │
│  │      Worker Thread Pool                 │                            │
│  │  ┌────────┐ ┌────────┐ ┌────────┐      │                            │
│  │  │Thread-1│ │Thread-2│ │Thread-3│ ...  │                            │
│  │  └───┬────┘ └───┬────┘ └───┬────┘      │                            │
│  │      │          │          │            │                            │
│  │      ↓          ↓          ↓            │                            │
│  │   [Task]     [Task]     [Task]         │                            │
│  └─────────────────────────────────────────┘                            │
│            │                                                              │
│            │ produces                                                     │
│            ↓                                                              │
│  ┌─────────────────────────────────────────┐                            │
│  │         Future<Result>                  │                            │
│  │  ┌──────────────────────────────────┐  │                            │
│  │  │ • get() - blocks until done      │  │                            │
│  │  │ • isDone() - check completion    │  │                            │
│  │  │ • cancel() - attempt cancellation│  │                            │
│  │  └──────────────────────────────────┘  │                            │
│  └─────────────────────────────────────────┘                            │
│                                                                           │
└───────────────────────────────────────────────────────────────────────────┘
```

### Task Execution Flow

```
┌───────────────────────────────────────────────────────────────┐
│              Task Submission and Execution Flow               │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  executor.submit(task)                                        │
│         │                                                     │
│         ↓                                                     │
│  ┌──────────────┐                                            │
│  │ Check Active │                                            │
│  │   Threads    │                                            │
│  └──────┬───────┘                                            │
│         │                                                     │
│    < corePoolSize?                                           │
│         │                                                     │
│    ┌────┴────┐                                               │
│   YES       NO                                               │
│    │         │                                               │
│    │         ↓                                               │
│    │   ┌──────────────┐                                     │
│    │   │ Try add to  │                                     │
│    │   │    Queue     │                                     │
│    │   └──────┬───────┘                                     │
│    │          │                                              │
│    │     Queue full?                                         │
│    │          │                                              │
│    │     ┌────┴────┐                                         │
│    │    YES       NO                                         │
│    │     │         │                                         │
│    │     │         └───→ [Queued] ───→ Wait for thread      │
│    │     ↓                                                   │
│    │  ┌──────────────┐                                      │
│    │  │ Check Thread │                                      │
│    │  │    Count     │                                      │
│    │  └──────┬───────┘                                      │
│    │         │                                               │
│    │    < maxPoolSize?                                      │
│    │         │                                               │
│    │    ┌────┴────┐                                          │
│    │   YES       NO                                          │
│    │    │         │                                          │
│    ↓    ↓         ↓                                          │
│  ┌──────────────────┐     ┌──────────────────┐             │
│  │  Create Thread   │     │ Reject Handler   │             │
│  │  Execute Task    │     │  • Abort          │             │
│  └────────┬─────────┘     │  • CallerRuns     │             │
│           │               │  • Discard        │             │
│           │               │  • DiscardOldest  │             │
│           │               └───────────────────┘             │
│           ↓                                                  │
│  ┌──────────────────┐                                       │
│  │  Task Running    │                                       │
│  └────────┬─────────┘                                       │
│           │                                                  │
│      ┌────┴─────┐                                           │
│   Success    Exception                                      │
│      │            │                                          │
│      ↓            ↓                                          │
│  ┌─────────┐  ┌─────────┐                                  │
│  │ Result  │  │ Future  │                                  │
│  │ in      │  │ throws  │                                  │
│  │ Future  │  │ ExecutionException                         │
│  └─────────┘  └─────────┘                                  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## Best Practices

### 1. Sizing Thread Pools

```java
// ❌ Bad: Hardcoded arbitrary number
ExecutorService executor = Executors.newFixedThreadPool(10);

// ✅ Good: Based on workload characteristics
int cores = Runtime.getRuntime().availableProcessors();

// CPU-intensive
int cpuThreads = cores + 1;

// I/O-intensive (tune based on profiling)
int ioThreads = cores * 2; // or more based on wait/compute ratio

// Bounded executor for controlled resource usage
ExecutorService executor = new ThreadPoolExecutor(
    cpuThreads,
    cpuThreads * 2,
    60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(100),
    new ThreadPoolExecutor.CallerRunsPolicy()
);
```

### 2. Always Shutdown Executors

```java
// ✅ Good: try-with-resources (Java 19+)
try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
    executor.submit(task);
} // Auto-closes

// ✅ Good: Manual shutdown with timeout
ExecutorService executor = Executors.newFixedThreadPool(4);
try {
    executor.submit(task);
} finally {
    executor.shutdown();
    try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate");
            }
        }
    } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

### 3. Handle Rejections

```java
// ✅ Good: Appropriate rejection policy
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2, 4, 60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100),
    new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure
);

// ✅ Good: Custom rejection handling
new RejectedExecutionHandler() {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        logger.warn("Task rejected: {}", r);
        // Try alternative queue, log, metrics, etc.
    }
}
```

### 4. Monitor Executor Health

```java
ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

// Monitor metrics
System.out.println("Active: " + executor.getActiveCount());
System.out.println("Pool Size: " + executor.getPoolSize());
System.out.println("Queue Size: " + executor.getQueue().size());
System.out.println("Completed: " + executor.getCompletedTaskCount());
System.out.println("Total: " + executor.getTaskCount());
```

### 5. Use Appropriate Queue

```java
// CPU-bound: LinkedBlockingQueue (unbounded or large)
new LinkedBlockingQueue<>(1000)

// I/O-bound with backpressure: ArrayBlockingQueue
new ArrayBlockingQueue<>(100)

// Quick tasks: SynchronousQueue
new SynchronousQueue<>()

// Priority tasks: PriorityBlockingQueue
new PriorityBlockingQueue<>()
```

---

## Common Pitfalls

### 1. ❌ Using Unbounded Queues Carelessly

```java
// ❌ Bad: Can lead to OOM
ExecutorService executor = Executors.newFixedThreadPool(2);
// Uses unbounded LinkedBlockingQueue

for (int i = 0; i < 1_000_000; i++) {
    executor.submit(slowTask); // Queue grows unbounded!
}

// ✅ Good: Use bounded queue with rejection policy
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2, 4, 60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(1000),
    new ThreadPoolExecutor.CallerRunsPolicy()
);
```

### 2. ❌ Not Handling InterruptedException

```java
// ❌ Bad: Swallows interrupt
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    // Empty catch - loses interrupt status!
}

// ✅ Good: Restore interrupt status
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Restore!
    throw new RuntimeException(e);
}
```

### 3. ❌ Forgetting to Shutdown

```java
// ❌ Bad: Executor never shuts down
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.submit(task);
// JVM won't exit - threads still alive!

// ✅ Good: Always shutdown
executor.shutdown();
```

### 4. ❌ Blocking in Tasks

```java
// ❌ Bad: Blocking entire thread pool
executor.submit(() -> {
    future.get(); // Blocks thread - deadlock risk!
});

// ✅ Good: Use callbacks or separate executor
future.thenApply(result -> process(result));
```

### 5. ❌ Wrong Pool Type

```java
// ❌ Bad: CachedThreadPool for CPU tasks
ExecutorService executor = Executors.newCachedThreadPool();
for (int i = 0; i < 1000; i++) {
    executor.submit(cpuIntensiveTask); // Creates 1000 threads!
}

// ✅ Good: FixedThreadPool for CPU tasks
int cores = Runtime.getRuntime().availableProcessors();
ExecutorService executor = Executors.newFixedThreadPool(cores);
```

---

## Decision Tree

```
Need executor? ────YES──→ What type of tasks?
                          │
                          ├─→ CPU-intensive ──→ FixedThreadPool(cores+1)
                          │
                          ├─→ I/O-intensive ──→ Many short tasks? ──YES──→ CachedThreadPool
                          │                    │
                          │                    └──NO──→ FixedThreadPool(cores*2)
                          │
                          ├─→ Sequential ─────→ SingleThreadExecutor
                          │
                          ├─→ Scheduled/Delayed ──→ ScheduledThreadPool
                          │
                          └─→ Recursive/Parallel ──→ WorkStealingPool
```

---

## Summary Table

| Feature | Fixed | Cached | Single | Scheduled | WorkStealing |
|---------|-------|--------|--------|-----------|--------------|
| **Threads** | Fixed | 0→∞ | 1 | Fixed | Parallelism |
| **Queue** | Unbounded | Sync | Unbounded | Delay | Deque per thread |
| **Use Case** | CPU-bound | I/O many | Sequential | Delayed | Recursive |
| **Risk** | Queue OOM | Thread OOM | Slow if backed up | - | Complex |
| **Throughput** | Medium | High | Low | Medium | Very High |

---

## Running the Demos

```bash
# Run all ExecutorService demos
mvn spring-boot:run -Dspring-boot.run.arguments=--demo=executorservice

# Run tests
mvn test -Dtest=ExecutorServiceTypesDemoTest
mvn test -Dtest=BlockingQueueStrategiesDemoTest
```

---

**Project**: Java 21 Concurrency Patterns
**Module**: Executor Framework
**Spring Boot**: 4.0.1
