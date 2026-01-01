# Custom Thread Pool Patterns - Complete Package

This package demonstrates how to use custom ExecutorService thread pools with CompletableFuture, showing sequential operations, parallel operations, and mixed pool usage.

## 📚 Documentation

### Main Theory Document
**[CUSTOM_POOL_PATTERNS_THEORY.md](CUSTOM_POOL_PATTERNS_THEORY.md)** - Complete guide covering:
- Why use custom pools vs common pool
- Creating and configuring custom pools
- Sequential vs parallel operation patterns
- Using multiple pools together
- Thread execution flows and timelines
- Best practices and real-world examples

### Related Documents
- **[COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md](COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md)** - Deep dive into CompletableFuture thread flows with common pool
- **[WHY_THREAD_REUSE.md](WHY_THREAD_REUSE.md)** - Explains ForkJoinPool thread reuse behavior

## 🎯 Package Overview

**Location**: `com.shan.concurrency.threadspatterns.custompoolpatterns`

### Demo Classes

1. **CustomPoolBasicDemo** - Introduction to custom pools
   - Common pool vs custom pool comparison
   - How to create custom thread pool
   - Thread naming and identification
   - Basic usage patterns

2. **CustomPoolSequentialDemo** - Sequential operations
   - Chaining with thenCompose()
   - Common mistake: forgetting to specify executor in chain
   - Correct way: all stages use custom pool
   - Thread reuse within custom pool

3. **CustomPoolParallelDemo** - Parallel operations
   - Multiple independent tasks with thenCombine()
   - Using allOf() with custom pool
   - Dashboard loading example (5 widgets in parallel)
   - Performance benefits demonstration

4. **MixedPoolDemo** - Using multiple pools together
   - I/O pool for database/network operations
   - CPU pool for computational tasks
   - Common pool for quick transformations
   - Pool isolation and resource management
   - Combining results from different pools

5. **CustomPoolCombinedDemo** - Real-world example
   - E-commerce order processing system
   - Sequential validation pipeline
   - Parallel data fetching
   - Mixed pool usage (I/O, CPU, common)
   - Complete workflow with error handling

## 🚀 Running the Demos

### Compile
```bash
mvn compile
```

### Run All Demos
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.custompoolpatterns.Main"
```

### Run Specific Demo
```bash
# Basic demo
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.custompoolpatterns.Main" \
  -Dexec.args="basic"

# Sequential operations
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.custompoolpatterns.Main" \
  -Dexec.args="sequential"

# Parallel operations
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.custompoolpatterns.Main" \
  -Dexec.args="parallel"

# Mixed pools (common + custom)
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.custompoolpatterns.Main" \
  -Dexec.args="mixed"

# Complete real-world example
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.custompoolpatterns.Main" \
  -Dexec.args="combined"
```

## 📊 Key Concepts Demonstrated

### 1. Custom Pool Creation

```java
ExecutorService customPool = Executors.newFixedThreadPool(10, r -> {
    Thread t = new Thread(r);
    t.setName("CustomPool-" + t.getId());
    return t;
});
```

### 2. Sequential Operations (All stages use custom pool)

```java
CompletableFuture.supplyAsync(() -> fetchData(), customPool)
    .thenComposeAsync(data -> CompletableFuture.supplyAsync(
        () -> processData(data),
        customPool  // ✅ Explicitly specify custom pool
    ), customPool)
    .thenApplyAsync(result -> formatResult(result), customPool);
```

### 3. Parallel Operations (Multiple tasks on custom pool)

```java
CompletableFuture<A> f1 = CompletableFuture.supplyAsync(() -> taskA(), customPool);
CompletableFuture<B> f2 = CompletableFuture.supplyAsync(() -> taskB(), customPool);
CompletableFuture<C> f3 = CompletableFuture.supplyAsync(() -> taskC(), customPool);

CompletableFuture.allOf(f1, f2, f3).join();
```

### 4. Mixed Pool Usage (Different pools for different workloads)

```java
// I/O pool for blocking operations
ExecutorService ioPool = Executors.newFixedThreadPool(20);

// CPU pool for computational tasks
ExecutorService cpuPool = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);

CompletableFuture
    .supplyAsync(() -> fetchFromDB(), ioPool)      // I/O pool
    .thenComposeAsync(data -> processData(data), cpuPool)  // CPU pool
    .thenApply(result -> formatResult(result));     // Common pool
```

## 🔍 Thread Flow Examples

### Example 1: Sequential with Custom Pool

```
CustomPool-1: fetchData (500ms)
              ↓
CustomPool-2: processData (700ms)  ← New thread picks up task
              ↓
CustomPool-1: formatResult (200ms) ← Thread reused
              ↓
Main thread:  get() returns

Total time: ~1400ms (sequential: 500 + 700 + 200)
```

### Example 2: Parallel with Custom Pool

```
Time 0ms:
  CustomPool-1: taskA (800ms)  ┐
  CustomPool-2: taskB (1000ms) ┼─ All start simultaneously
  CustomPool-3: taskC (600ms)  ┘

Time 600ms:  CustomPool-3 done
Time 800ms:  CustomPool-1 done
Time 1000ms: CustomPool-2 done ← All complete

Total time: ~1000ms (parallel: max of 800, 1000, 600)
Speedup: 2.4x vs sequential
```

### Example 3: Mixed Pools

```
IO-1:     fetchFromDB() (500ms)
          ↓
CPU-1:    processData() (700ms)
          ↓
IO-2:     saveToCache() (300ms)
          ↓
Common:   formatResult() (100ms)

Total time: ~1600ms
Each pool handles its specialized workload
```

## 📈 Real-World Example Output

From **CustomPoolCombinedDemo** (E-commerce order processing):

```
🛒 Starting order processing for: ORDER-12345

[IO-60]  📋 Step 1: Validating order (I/O, 300ms)
[IO-60]     ✓ Order validated

[IO-60]  📦 Step 2: Fetching order data in PARALLEL...
[IO-61]     → Fetching CUSTOMER info (I/O, 400ms)
[IO-62]     → Fetching PRODUCT details (I/O, 500ms)
[IO-63]     → Fetching PRICING info (I/O, 300ms)
[IO-62]     ✓ All order data fetched (parallel time: ~500ms)

[CPU-64] 💳 Step 3: Processing payment (CPU, 600ms)
[CPU-64]    ✓ Payment processed

[CPU-64] 🧮 Step 4: Calculating tax and shipping in PARALLEL...
[CPU-65]    → Calculating TAX (CPU, 400ms)
[CPU-66]    → Calculating SHIPPING (CPU, 300ms)
[CPU-65]    ✓ Tax: $120.0, Shipping: $25.0

[IO-67]  📊 Step 5: Reserving inventory (I/O, 400ms)
[IO-67]     ✓ Inventory reserved

[IO-68]  📮 Step 6: Creating shipment (I/O, 500ms)
[IO-68]     ✓ Shipment created: SHIP-ORDER-12345

[IO-68]  📧 Step 7: Sending notifications in PARALLEL...
[IO-69]     → Sending EMAIL notification (I/O, 300ms)
[IO-70]     → Sending SMS notification (I/O, 200ms)
[ForkJoinPool.commonPool-worker-2] → Sending PUSH notification (quick, 100ms)
[IO-69]     ✓ All notifications sent

✅ ORDER PROCESSING RESULT: SHIP-ORDER-12345
⏱️  Total processing time: 3067ms
```

**Thread Usage Summary**:
- **I/O Pool**: 10 different threads (IO-60 through IO-70)
- **CPU Pool**: 3 threads (CPU-64, CPU-65, CPU-66)
- **Common Pool**: 1 thread (for quick push notification)

## 🎓 Learning Path

### Step 1: Understand the Basics
1. Read [CUSTOM_POOL_PATTERNS_THEORY.md](CUSTOM_POOL_PATTERNS_THEORY.md) sections 1-3
2. Run `CustomPoolBasicDemo` to see common vs custom pool
3. Understand why custom pools are needed

### Step 2: Sequential Operations
1. Read theory section 4 (Sequential Operations)
2. Run `CustomPoolSequentialDemo`
3. Observe the **common mistake** (example 2) vs **correct way** (example 3)
4. Key lesson: Always specify executor in `thenComposeAsync()`

### Step 3: Parallel Operations
1. Read theory section 5 (Parallel Operations)
2. Run `CustomPoolParallelDemo`
3. See performance benefits (parallel vs sequential)
4. Understand `thenCombine()` and `allOf()` patterns

### Step 4: Mixed Pool Usage
1. Read theory section 6 (Using Both Pools Together)
2. Run `MixedPoolDemo`
3. Learn when to use which pool (I/O vs CPU vs common)
4. Understand pool isolation benefits

### Step 5: Real-World Application
1. Read theory sections 7-9 (Patterns, Best Practices, Examples)
2. Run `CustomPoolCombinedDemo`
3. Study complete order processing workflow
4. Apply patterns to your own use cases

## 🔑 Key Takeaways

### Common Pool vs Custom Pool

| Use Common Pool For | Use Custom Pool For |
|---------------------|---------------------|
| ✅ Quick transformations | ✅ I/O-bound operations |
| ✅ Lightweight tasks | ✅ CPU-intensive tasks |
| ✅ General async | ✅ Resource control |
| ✅ Prototyping | ✅ Production systems |

### Critical Rules

✅ **DO:**
- Name your threads for easier debugging
- Shutdown custom pools in finally block
- Size pools appropriately (I/O: large, CPU: processors)
- Use `*Async()` methods with executor parameter
- Separate I/O and CPU workloads into different pools

❌ **DON'T:**
- Forget to specify executor in `thenCompose()` chains
- Use unbounded queues in production
- Mix I/O and CPU tasks in same pool
- Forget to shutdown custom pools
- Size all pools the same way

### Thread Pattern Summary

1. **Sequential**: `thenCompose()` - tasks depend on each other
   - Total time = Sum of all tasks
   - Uses 1-2 threads (reused)

2. **Parallel**: Multiple `supplyAsync()` + `allOf()` - independent tasks
   - Total time = Max of all tasks
   - Uses many threads (up to pool size)
   - Speedup = Sequential time / Parallel time

3. **Hybrid**: Mix of both - parallel fetching + sequential processing
   - Optimize critical path
   - Best resource utilization

## 🏆 Best Practices from Demos

### 1. Thread Naming (from CustomPoolBasicDemo)
```java
ExecutorService pool = Executors.newFixedThreadPool(10, r -> {
    Thread t = new Thread(r);
    t.setName("MyApp-IO-" + t.getId()); // ✅ Descriptive
    return t;
});
```

### 2. Proper Shutdown (from all demos)
```java
try {
    // Use pool
} finally {
    pool.shutdown();
    pool.awaitTermination(5, TimeUnit.SECONDS);
}
```

### 3. Explicit Executor in Chains (from CustomPoolSequentialDemo)
```java
future.thenComposeAsync(x ->
    CompletableFuture.supplyAsync(() -> process(x), customPool),
    customPool  // ✅ Don't forget this!
);
```

### 4. Pool Sizing (from MixedPoolDemo)
```java
// I/O pool: large (threads block on I/O)
ExecutorService ioPool = Executors.newFixedThreadPool(
    2 * Runtime.getRuntime().availableProcessors()
);

// CPU pool: size = processors (threads always busy)
ExecutorService cpuPool = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);
```

## 📞 Support

- **Documentation**: See theory document for detailed explanations
- **Examples**: All demo classes have extensive comments
- **Issues**: Check logs for thread names to identify which pool is used

## 🎯 Next Steps

After mastering custom pools:
1. Explore other concurrency patterns in parent package
2. Study ExecutorService advanced features (rejection policies, monitoring)
3. Learn about virtual threads (Java 21+) for even better I/O performance
4. Implement custom pools in your applications

---

**Happy Concurrent Programming! 🚀**
