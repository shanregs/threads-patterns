# Custom Thread Pool Patterns - Complete Theory Guide

This document explains how to use custom ExecutorService thread pools with CompletableFuture, how they work alongside the common pool, and patterns for sequential and parallel operations.

## Table of Contents
1. [Why Use Custom Thread Pools?](#why-use-custom-thread-pools)
2. [Common Pool vs Custom Pool](#common-pool-vs-custom-pool)
3. [Creating Custom Pools](#creating-custom-pools)
4. [Sequential Operations with Custom Pool](#sequential-operations)
5. [Parallel Operations with Custom Pool](#parallel-operations)
6. [Using Both Pools Together](#mixed-pool-usage)
7. [Thread Execution Patterns](#thread-execution-patterns)
8. [Best Practices](#best-practices)
9. [Real-World Examples](#real-world-examples)

---

## Why Use Custom Thread Pools?

### Problems with ForkJoinPool.commonPool()

**1. Shared Resource**
- Common pool is shared by ALL async operations in the JVM
- Other libraries and frameworks also use it
- Risk of thread starvation if pool is saturated

**2. Fixed Size**
- Size = `processors - 1`
- Cannot be adjusted for workload needs
- Not optimal for I/O-bound tasks

**3. No Isolation**
- Cannot separate critical vs non-critical tasks
- Cannot prioritize certain operations
- Cannot limit resource usage per module

**4. No Lifecycle Control**
- Cannot shutdown the common pool
- Cannot wait for completion
- Harder to track and manage

### Benefits of Custom Pools

✅ **Resource Control**: Dedicated threads for specific workloads
✅ **Isolation**: Separate pools for different concerns (I/O, CPU, critical tasks)
✅ **Sizing**: Optimize pool size for task characteristics
✅ **Lifecycle Management**: Explicit shutdown and cleanup
✅ **Monitoring**: Track pool-specific metrics
✅ **Priority**: Different pools can have different priorities

---

## Common Pool vs Custom Pool

### Comparison Table

| Aspect | Common Pool | Custom Pool |
|--------|-------------|-------------|
| **Creation** | Automatic (lazy) | Manual via `Executors` |
| **Size** | processors - 1 (fixed) | User-defined |
| **Lifecycle** | JVM-managed | Application-managed |
| **Shutdown** | Cannot shutdown | Must shutdown explicitly |
| **Isolation** | Shared globally | Isolated per pool |
| **Use Case** | Quick tasks, general purpose | Specific workloads, I/O, CPU |
| **Thread Names** | `ForkJoinPool.commonPool-worker-N` | Custom (user-defined) |
| **Default for** | `supplyAsync()` with no executor | N/A (must pass explicitly) |

### When to Use Which?

#### Use Common Pool:
- ✅ Quick, lightweight transformations
- ✅ Non-blocking operations
- ✅ General-purpose async tasks
- ✅ Prototyping and simple applications

#### Use Custom Pool:
- ✅ I/O-bound operations (database, network, file)
- ✅ CPU-intensive computations
- ✅ Long-running tasks
- ✅ Need for isolation or resource limits
- ✅ Production applications with specific performance requirements

---

## Creating Custom Pools

### 1. Fixed Thread Pool (Most Common)

```java
ExecutorService customPool = Executors.newFixedThreadPool(10, r -> {
    Thread t = new Thread(r);
    t.setName("CustomPool-" + t.getId());
    t.setDaemon(false); // Keeps JVM alive
    return t;
});
```

**Characteristics:**
- Fixed number of threads (10 in this example)
- Threads reused for multiple tasks
- Queue is unbounded (can grow indefinitely)
- **Best for:** Known, stable workload

### 2. Cached Thread Pool

```java
ExecutorService cachedPool = Executors.newCachedThreadPool(r -> {
    Thread t = new Thread(r);
    t.setName("CachedPool-" + t.getId());
    t.setDaemon(true);
    return t;
});
```

**Characteristics:**
- Creates threads on demand
- Reuses idle threads (60-second timeout)
- No limit on pool size (can grow unbounded)
- **Best for:** Many short-lived tasks

### 3. Single Thread Executor

```java
ExecutorService singlePool = Executors.newSingleThreadExecutor();
```

**Characteristics:**
- Only 1 thread
- Tasks execute sequentially
- **Best for:** Ordered execution, serial processing

### 4. Custom ThreadPoolExecutor (Advanced)

```java
ExecutorService customPool = new ThreadPoolExecutor(
    5,                      // corePoolSize
    10,                     // maximumPoolSize
    60L,                    // keepAliveTime
    TimeUnit.SECONDS,       // timeUnit
    new LinkedBlockingQueue<>(100), // workQueue (bounded)
    new ThreadFactory() {   // threadFactory
        private final AtomicInteger count = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("Custom-" + count.getAndIncrement());
            return t;
        }
    },
    new ThreadPoolExecutor.CallerRunsPolicy() // rejectionPolicy
);
```

**Characteristics:**
- Full control over all parameters
- Bounded queue (prevents memory issues)
- Rejection policy for overload
- **Best for:** Fine-tuned production systems

---

## Sequential Operations

### Problem: Forgetting to Specify Custom Pool in Chain

**❌ WRONG - Only first stage uses custom pool:**
```java
CompletableFuture.supplyAsync(() -> {
    // ✅ Uses customPool
    return fetchData();
}, customPool)
.thenCompose(data -> CompletableFuture.supplyAsync(() -> {
    // ❌ Uses COMMON POOL! (no executor specified)
    return processData(data);
}))
.thenApply(result -> {
    // ❌ Also uses completing thread (might be common pool)
    return formatResult(result);
});
```

### ✅ CORRECT - All stages use custom pool:

```java
CompletableFuture.supplyAsync(() -> {
    return fetchData();
}, customPool)
.thenCompose(data -> CompletableFuture.supplyAsync(() -> {
    return processData(data);
}, customPool)) // ✅ Explicitly specify custom pool
.thenApplyAsync(result -> {
    return formatResult(result);
}, customPool); // ✅ Use *Async version with custom pool
```

### Thread Flow: Sequential with Custom Pool

```
Time 0ms:    CustomPool-1 → fetchData() (500ms)
             CustomPool-2, CustomPool-3, ... IDLE

Time 500ms:  CustomPool-1 finishes
             CustomPool-2 → processData() (700ms) ← Picks up next task
             CustomPool-1 now IDLE

Time 1200ms: CustomPool-2 finishes
             CustomPool-1 → formatResult() (200ms) ← Reused!
             CustomPool-2 now IDLE

Time 1400ms: Complete
```

**Key Points:**
- Tasks submitted one at a time (sequential)
- Threads from custom pool picked up
- Thread reuse within custom pool
- **Total time**: Sum of all tasks (~1400ms)

---

## Parallel Operations

### Parallel with Custom Pool

```java
// All submitted simultaneously
CompletableFuture<A> f1 = CompletableFuture.supplyAsync(() -> taskA(), customPool);
CompletableFuture<B> f2 = CompletableFuture.supplyAsync(() -> taskB(), customPool);
CompletableFuture<C> f3 = CompletableFuture.supplyAsync(() -> taskC(), customPool);

// Wait for all
CompletableFuture.allOf(f1, f2, f3).join();
```

### Thread Flow: Parallel with Custom Pool

```
Time 0ms:    All 3 tasks submitted to customPool
             CustomPool-1 → taskA() (800ms)
             CustomPool-2 → taskB() (1000ms)
             CustomPool-3 → taskC() (600ms)
             CustomPool-4, CustomPool-5, ... IDLE

Time 600ms:  CustomPool-3 finishes taskC()
             CustomPool-3 now IDLE (no more work)

Time 800ms:  CustomPool-1 finishes taskA()
             CustomPool-1 now IDLE

Time 1000ms: CustomPool-2 finishes taskB() ← LAST completer
             All tasks complete!
```

**Key Points:**
- All tasks start simultaneously
- Use multiple threads from custom pool
- **Total time**: Max of all tasks (~1000ms)
- Speedup vs sequential: 2.4x faster

### Using thenCombine with Custom Pool

```java
CompletableFuture<String> result = future1.thenCombineAsync(
    future2,
    (r1, r2) -> combineResults(r1, r2),
    customPool // ✅ Combine function also uses custom pool
);
```

---

## Mixed Pool Usage

### Strategy: Different Pools for Different Workloads

```java
// I/O Pool: Larger size for blocking operations
ExecutorService ioPool = Executors.newFixedThreadPool(20, r -> {
    Thread t = new Thread(r);
    t.setName("IO-" + t.getId());
    return t;
});

// CPU Pool: Size = processors for computational tasks
ExecutorService cpuPool = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors(),
    r -> {
        Thread t = new Thread(r);
        t.setName("CPU-" + t.getId());
        return t;
    }
);

// Common Pool: For quick, lightweight tasks
// (automatically available, no creation needed)
```

### When to Use Each Pool

| Task Type | Pool to Use | Example |
|-----------|-------------|---------|
| **Database Query** | I/O Pool | `CompletableFuture.supplyAsync(() -> db.query(), ioPool)` |
| **HTTP Request** | I/O Pool | `CompletableFuture.supplyAsync(() -> http.get(), ioPool)` |
| **File I/O** | I/O Pool | `CompletableFuture.supplyAsync(() -> readFile(), ioPool)` |
| **Heavy Computation** | CPU Pool | `CompletableFuture.supplyAsync(() -> calculate(), cpuPool)` |
| **Data Processing** | CPU Pool | `CompletableFuture.supplyAsync(() -> process(), cpuPool)` |
| **Image Processing** | CPU Pool | `CompletableFuture.supplyAsync(() -> resize(), cpuPool)` |
| **Quick Transform** | Common Pool | `CompletableFuture.supplyAsync(() -> transform())` |
| **Format String** | Common Pool | `.thenApply(data -> format(data))` |

### Example: Chaining Across Pools

```java
CompletableFuture<String> result = CompletableFuture
    // Step 1: Fetch from database (I/O pool)
    .supplyAsync(() -> dbService.fetchData(), ioPool)

    // Step 2: Process data (CPU pool)
    .thenComposeAsync(data -> CompletableFuture.supplyAsync(
        () -> processor.process(data),
        cpuPool
    ))

    // Step 3: Save to database (I/O pool)
    .thenComposeAsync(processed -> CompletableFuture.supplyAsync(
        () -> dbService.save(processed),
        ioPool
    ))

    // Step 4: Format result (common pool - quick)
    .thenApply(saved -> formatter.format(saved));
```

### Thread Flow: Mixed Pools

```
Pool        Thread          Task
════════════════════════════════════════════════════════════
IO Pool     IO-1            fetchData() (500ms)
                            ↓
CPU Pool    CPU-1           processData() (700ms)
                            ↓
IO Pool     IO-2            saveData() (400ms)
                            ↓
Common      ForkJoin-1      formatResult() (100ms)
                            ↓
Main        main()          get() returns result
```

**Benefits:**
- ✅ I/O threads don't block CPU tasks
- ✅ CPU threads not wasted on I/O wait
- ✅ Common pool free for other uses
- ✅ Optimal resource utilization

---

## Thread Execution Patterns

### Pattern 1: Fan-Out / Fan-In (Parallel)

**Scenario**: Load multiple data sources in parallel, then combine

```
        ┌──── Future1 (DB) ────┐
        │                      │
Main ───┼──── Future2 (API) ───┼──→ Combine ──→ Result
        │                      │
        └──── Future3 (Cache)──┘

Thread Timeline:
IO-1:    ████████████  (DB query)
IO-2:    ██████████    (API call)
IO-3:    ██████        (Cache lookup)
         ↓ All complete ↓
CPU-1:   ████          (Combine results)
```

**Code:**
```java
CompletableFuture<Data1> f1 = CompletableFuture.supplyAsync(() -> db.query(), ioPool);
CompletableFuture<Data2> f2 = CompletableFuture.supplyAsync(() -> api.fetch(), ioPool);
CompletableFuture<Data3> f3 = CompletableFuture.supplyAsync(() -> cache.get(), ioPool);

CompletableFuture<Result> combined = CompletableFuture.allOf(f1, f2, f3)
    .thenApplyAsync(v -> combiner.combine(f1.join(), f2.join(), f3.join()), cpuPool);
```

### Pattern 2: Pipeline (Sequential)

**Scenario**: Each step depends on previous step's output

```
Main ──→ Fetch ──→ Validate ──→ Process ──→ Save ──→ Result

Thread Timeline:
IO-1:    ████████  (Fetch)
                   ████████  (Validate)
                             ████████  (Process on CPU? No, reuse IO-1)
                                       ████████  (Save)
```

**Code:**
```java
CompletableFuture<Result> result = CompletableFuture
    .supplyAsync(() -> fetch(), ioPool)
    .thenComposeAsync(data -> validate(data), ioPool)
    .thenComposeAsync(valid -> process(valid), cpuPool)
    .thenComposeAsync(processed -> save(processed), ioPool);
```

### Pattern 3: Hybrid (Parallel + Sequential)

**Scenario**: Parallel data loading, then sequential processing

```
        ┌──── Fetch User ────┐
        │                    │
Main ───┼──── Fetch Orders ──┼──→ Combine ──→ Process ──→ Save
        │                    │
        └──── Fetch Prefs ───┘

Thread Timeline:
IO-1:    ████████  (User)     ┐
IO-2:    ██████████  (Orders) ┼─→ Parallel
IO-3:    ██████  (Prefs)      ┘
                               ↓
CPU-1:                         ████████  (Process)
                                         ↓
IO-4:                                    ████  (Save)
```

**Code:**
```java
CompletableFuture<User> userFuture = supplyAsync(() -> fetchUser(), ioPool);
CompletableFuture<Orders> ordersFuture = supplyAsync(() -> fetchOrders(), ioPool);
CompletableFuture<Prefs> prefsFuture = supplyAsync(() -> fetchPrefs(), ioPool);

CompletableFuture<Result> result = allOf(userFuture, ordersFuture, prefsFuture)
    .thenComposeAsync(v -> processData(
        userFuture.join(), ordersFuture.join(), prefsFuture.join()
    ), cpuPool)
    .thenComposeAsync(processed -> saveData(processed), ioPool);
```

---

## Best Practices

### 1. Always Shutdown Custom Pools

```java
ExecutorService pool = Executors.newFixedThreadPool(10);
try {
    // Use pool
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> task(), pool);
    String result = future.get();

} finally {
    // Always shutdown
    pool.shutdown();
    try {
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            pool.shutdownNow(); // Force shutdown if not terminated
        }
    } catch (InterruptedException e) {
        pool.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

### 2. Name Your Threads

Makes debugging and monitoring much easier:

```java
ExecutorService pool = Executors.newFixedThreadPool(10, r -> {
    Thread t = new Thread(r);
    t.setName("MyApp-IO-" + t.getId()); // ✅ Descriptive name
    return t;
});

// Now logs show: [MyApp-IO-42] Processing request
// Instead of:    [pool-1-thread-3] Processing request
```

### 3. Size Pools Appropriately

**I/O-Bound Tasks** (network, database, file):
```java
// Rule of thumb: larger pool (threads wait for I/O)
int ioPoolSize = 2 * Runtime.getRuntime().availableProcessors();
ExecutorService ioPool = Executors.newFixedThreadPool(ioPoolSize);
```

**CPU-Bound Tasks** (computation, processing):
```java
// Rule of thumb: pool size = processors (threads always busy)
int cpuPoolSize = Runtime.getRuntime().availableProcessors();
ExecutorService cpuPool = Executors.newFixedThreadPool(cpuPoolSize);
```

**Mixed Workload**:
```java
// Start with processors, tune based on monitoring
int poolSize = Runtime.getRuntime().availableProcessors() + 1;
```

### 4. Use Bounded Queues for Production

Prevent memory issues from unbounded queues:

```java
ExecutorService pool = new ThreadPoolExecutor(
    5,                      // core threads
    10,                     // max threads
    60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(1000), // ✅ Bounded queue (max 1000 tasks)
    new ThreadPoolExecutor.CallerRunsPolicy() // Handle overflow
);
```

### 5. Monitor Pool Health

```java
ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

// Log periodically
log.info("Pool stats: active={}, queued={}, completed={}",
    pool.getActiveCount(),
    pool.getQueue().size(),
    pool.getCompletedTaskCount()
);
```

### 6. Use *Async Methods Explicitly

When chaining, use `*Async` versions to control which pool executes:

```java
future
    .thenApplyAsync(x -> transform(x), customPool)  // ✅ Explicit pool
    .thenComposeAsync(y -> process(y), customPool)  // ✅ Explicit pool
```

Not:
```java
future
    .thenApply(x -> transform(x))  // ❌ May use completing thread
    .thenCompose(y -> process(y))  // ❌ May use completing thread
```

---

## Real-World Examples

### Example 1: Web Dashboard Loading

```java
public CompletableFuture<Dashboard> loadDashboard(String userId) {
    // Parallel: Fetch all widgets concurrently
    CompletableFuture<UserProfile> profileFuture =
        supplyAsync(() -> userService.getProfile(userId), ioPool);

    CompletableFuture<List<Order>> ordersFuture =
        supplyAsync(() -> orderService.getOrders(userId), ioPool);

    CompletableFuture<Stats> statsFuture =
        supplyAsync(() -> analyticsService.getStats(userId), cpuPool);

    CompletableFuture<Notifications> notifsFuture =
        supplyAsync(() -> notifService.getNotifications(userId), ioPool);

    // Combine all results
    return allOf(profileFuture, ordersFuture, statsFuture, notifsFuture)
        .thenApply(v -> new Dashboard(
            profileFuture.join(),
            ordersFuture.join(),
            statsFuture.join(),
            notifsFuture.join()
        ));
}

// Thread usage:
// IO-1: getProfile()
// IO-2: getOrders()
// CPU-1: getStats()  (computation-heavy)
// IO-3: getNotifications()
// Common: Dashboard construction (quick)
```

### Example 2: Batch Processing

```java
public void processBatch(List<Task> tasks) {
    ExecutorService batchPool = Executors.newFixedThreadPool(20);

    try {
        // Process all tasks in parallel
        List<CompletableFuture<Result>> futures = tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(
                () -> processTask(task),
                batchPool
            ))
            .collect(Collectors.toList());

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();

        // Collect results
        List<Result> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        log.info("Processed {} tasks", results.size());

    } finally {
        shutdownPool(batchPool);
    }
}
```

### Example 3: Microservice Orchestration

```java
public CompletableFuture<AggregatedResponse> orchestrate(Request request) {
    // Call multiple microservices in parallel
    CompletableFuture<ServiceAResponse> serviceA =
        supplyAsync(() -> callServiceA(request), ioPool);

    CompletableFuture<ServiceBResponse> serviceB =
        supplyAsync(() -> callServiceB(request), ioPool);

    CompletableFuture<ServiceCResponse> serviceC =
        supplyAsync(() -> callServiceC(request), ioPool);

    // When all services respond, aggregate results (CPU-intensive)
    return allOf(serviceA, serviceB, serviceC)
        .thenApplyAsync(v -> aggregator.aggregate(
            serviceA.join(),
            serviceB.join(),
            serviceC.join()
        ), cpuPool)
        // Transform to final format (quick)
        .thenApply(aggregated -> transformer.transform(aggregated));
}

// Thread flow:
// IO-1, IO-2, IO-3: Parallel service calls
// CPU-1: Heavy aggregation
// Common: Quick transformation
```

---

## Summary: Key Takeaways

### When to Use Custom Pools

1. **I/O Operations**: Database, network, file I/O → Large custom pool
2. **CPU Operations**: Computation, processing → Pool size = processors
3. **Isolation**: Critical vs non-critical tasks → Separate pools
4. **Resource Limits**: Prevent saturation → Bounded custom pool
5. **Production**: Monitoring, control → Custom pools

### Common Pool vs Custom Pool

| Use Common Pool For | Use Custom Pool For |
|---------------------|---------------------|
| Quick transformations | I/O-bound operations |
| Lightweight tasks | CPU-intensive tasks |
| General-purpose async | Specific workloads |
| Prototyping | Production systems |
| No resource limits needed | Need resource control |

### Thread Patterns

1. **Sequential**: Tasks depend on each other → `thenCompose()`
2. **Parallel**: Independent tasks → Multiple `supplyAsync()` + `allOf()`
3. **Hybrid**: Mix of both → Parallel fetching + Sequential processing

### Critical Rules

✅ **DO:**
- Name your threads
- Shutdown custom pools
- Size pools for workload type
- Use `*Async` methods with custom pool
- Monitor pool health

❌ **DON'T:**
- Forget to specify executor in chains
- Use unbounded queues in production
- Size all pools the same
- Mix workload types in same pool
- Forget error handling

---

## Running the Examples

```bash
# Compile
mvn compile

# Run all demos
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.custompoolpatterns.Main"

# Run specific demo
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.custompoolpatterns.Main" \
  -Dexec.args="combined"

# Available demos:
# - basic      : Basic custom pool usage
# - sequential : Sequential operations
# - parallel   : Parallel operations
# - mixed      : Common + custom pools
# - combined   : Real-world example
```

---

**Package**: `com.shan.concurrency.threadspatterns.custompoolpatterns`

**Demos**:
- `CustomPoolBasicDemo` - Introduction to custom pools
- `CustomPoolSequentialDemo` - Sequential chaining patterns
- `CustomPoolParallelDemo` - Parallel execution patterns
- `MixedPoolDemo` - Using multiple pools together
- `CustomPoolCombinedDemo` - Complete real-world example
