# CompletableFuture Pattern - Async API Call Chain

This package demonstrates asynchronous operations using CompletableFuture with the default ForkJoinPool.commonPool().

## 📚 Documentation

**Main Theory Document**: [CompletableFuture Thread Flow Analysis](../../../../docs/COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md)

**Related Documents**:
- [Why Thread Reuse](../../../../docs/WHY_THREAD_REUSE.md) - Understanding ForkJoinPool behavior
- [Custom Pool Patterns](../../../../docs/CUSTOM_POOL_PATTERNS_THEORY.md) - Advanced usage with custom pools

## 📦 Package Contents

### Demo Class
- **CompletableFutureDemo.java** - Main demonstration class with 3 examples

### Supporting Classes
- **ApiService.java** - Simulates async API calls
- **UserProfile.java** - Data model for user profile
- **OrderHistory.java** - Data model for order history
- **Recommendations.java** - Data model for recommendations

### Runner
- **Main.java** - Run the demo independently

## 🎯 What This Demo Teaches

### Example 1: Chaining Async Operations (Sequential)
**Pattern**: Profile → Orders → Recommendations (each depends on previous)

**Code**:
```java
apiService.fetchUserProfile(userId)
    .thenApply(profile -> profile.getUserId())
    .thenCompose(id -> apiService.fetchOrderHistory(id))
    .thenCompose(orderHistory -> apiService.fetchRecommendations(orderHistory));
```

**Thread Flow**:
```
worker-2: fetchUserProfile (800ms)
          → thenApply (same thread, immediate)
worker-1: fetchOrderHistory (1000ms)
worker-2: fetchRecommendations (600ms)

Total: ~2400ms (sequential)
```

**Key Learning**: Sequential chaining with `thenCompose()`

---

### Example 2: Combining Independent Operations (Parallel)
**Pattern**: Profile + Orders run in parallel, then combine

**Code**:
```java
CompletableFuture<UserProfile> profileFuture = apiService.fetchUserProfile(userId);
CompletableFuture<OrderHistory> ordersFuture = apiService.fetchOrderHistory(userId);

CompletableFuture<String> combined = profileFuture.thenCombine(ordersFuture,
    (profile, orders) -> combineResults(profile, orders)
);
```

**Thread Flow**:
```
worker-2: fetchUserProfile (800ms)   ┐
worker-1: fetchOrderHistory (1000ms) ┘ Both run in PARALLEL

worker-1: thenCombine (executes on last completer)

Total: ~1000ms (parallel, 1.8x faster!)
```

**Key Learning**: Parallel execution with `thenCombine()`

---

### Example 3: Error Handling
**Pattern**: Graceful error recovery with `exceptionally()` and `handle()`

**Code**:
```java
// Option 1: exceptionally (only handles errors)
future.exceptionally(ex -> {
    log.warn("Error occurred: {}", ex.getMessage());
    return "FALLBACK_VALUE";
});

// Option 2: handle (handles both success and error)
future.handle((result, ex) -> {
    if (ex != null) return "DEFAULT_VALUE";
    return result;
});
```

**Key Learning**: Error handling in async chains

---

## 🚀 Running the Demo

### Compile
```bash
mvn compile
```

### Run
```bash
# Using Main class
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.completablefuture.Main"

# Or run directly
java -cp target/classes com.shan.concurrency.threadspatterns.completablefuture.Main
```

### Expected Output
```
=== CompletableFuture Demo: Async API Call Chain ===

--- Example 1: Chaining Async Operations ---
[ForkJoinPool.commonPool-worker-2] Fetching user profile for userId: USER-123
[ForkJoinPool.commonPool-worker-2] User profile fetched
[ForkJoinPool.commonPool-worker-2] Profile received, proceeding to fetch orders
[ForkJoinPool.commonPool-worker-1] Fetching order history for userId: USER-123
[ForkJoinPool.commonPool-worker-1] Order history fetched: 3 orders
[ForkJoinPool.commonPool-worker-2] Generating recommendations based on 3 orders
[main] Final result: Recommendations(userId=USER-123, ...)

--- Example 2: Combining Independent Operations ---
[ForkJoinPool.commonPool-worker-2] Fetching user profile
[ForkJoinPool.commonPool-worker-1] Fetching order history
[ForkJoinPool.commonPool-worker-1] Combining results
[main] Combined result: User: User-USER-456, Orders: 3

--- Example 3: Error Handling ---
[ForkJoinPool.commonPool-worker-1] Success case result: Success-USER-789
[ForkJoinPool.commonPool-worker-2] API call failed!
[ForkJoinPool.commonPool-worker-2] Error occurred, providing default
[main] Failure case result: DEFAULT_VALUE
```

## 🔍 Key Concepts

### 1. Default Thread Pool
All operations use **ForkJoinPool.commonPool()** by default:
- Size: `Runtime.getRuntime().availableProcessors() - 1`
- Shared by all async operations in the JVM
- Threads named: `ForkJoinPool.commonPool-worker-N`

### 2. Sequential vs Parallel

**Sequential** (use `thenCompose()`):
- Each step waits for previous to complete
- Time = Sum of all steps
- Use when steps depend on each other

**Parallel** (use `thenCombine()` or `allOf()`):
- All steps start simultaneously
- Time = Max of all steps
- Use when steps are independent

### 3. Thread Reuse
ForkJoinPool reuses threads efficiently:
- Sequential operations: 1-2 threads (reused)
- Parallel operations: Multiple threads (up to pool size)

See [Why Thread Reuse](../../../../docs/WHY_THREAD_REUSE.md) for detailed explanation.

## 📊 Performance

### Sequential Chaining (Example 1)
```
fetchUserProfile:      800ms
fetchOrderHistory:    1000ms
fetchRecommendations:  600ms
─────────────────────────────
Total:                2400ms
```

### Parallel Combining (Example 2)
```
fetchUserProfile:     800ms  ┐
fetchOrderHistory:   1000ms  ┘ In parallel
─────────────────────────────
Total:               1000ms (max of 800 and 1000)

Speedup: 2.4x faster than sequential!
```

## 🎓 Common Operations

### supplyAsync()
Starts an async computation that returns a value:
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // Runs on ForkJoinPool.commonPool-worker-N
    return "result";
});
```

### thenApply()
Transforms the result (synchronous):
```java
future.thenApply(result -> result.toUpperCase())
```

### thenCompose()
Chains another CompletableFuture (for sequential operations):
```java
future.thenCompose(result -> anotherAsyncOperation(result))
```

### thenCombine()
Combines two independent futures:
```java
future1.thenCombine(future2, (r1, r2) -> r1 + r2)
```

### allOf()
Waits for multiple futures:
```java
CompletableFuture.allOf(f1, f2, f3).join()
```

### exceptionally()
Handles errors with fallback:
```java
future.exceptionally(ex -> "fallback")
```

### handle()
Handles both success and error:
```java
future.handle((result, ex) -> {
    if (ex != null) return "error";
    return result;
})
```

### get() / join()
Blocks and waits for result:
```java
String result = future.get();  // Throws checked exceptions
String result = future.join(); // Throws unchecked exceptions
```

## ⚠️ Important Notes

### 1. Blocking the Main Thread
```java
CompletableFuture<String> future = supplyAsync(() -> longRunningTask());
String result = future.get(); // ❌ Main thread BLOCKS here
```
Main thread waits until the future completes.

### 2. Error Propagation
Exceptions propagate through the chain until handled:
```java
supplyAsync(() -> mightThrow())
    .thenApply(r -> process(r))      // Skipped if exception
    .thenApply(r -> transform(r))    // Skipped if exception
    .exceptionally(ex -> handleError(ex)); // Handles exception
```

### 3. Thread Context
Callbacks (`thenApply`, `thenCompose`) may execute on:
- The completing thread (if future already complete)
- The thread that triggers completion
- A ForkJoinPool worker thread

## 🔗 Related Patterns

### Want Custom Thread Pool?
See **custompoolpatterns** package:
```java
ExecutorService customPool = Executors.newFixedThreadPool(10);
CompletableFuture.supplyAsync(() -> task(), customPool);
```

### Want More Control?
See **executorservice** package for:
- Different pool types
- Queue strategies
- Rejection policies

### Want Synchronization?
See other packages:
- **countdownlatch** - Wait for multiple tasks
- **cyclicbarrier** - Multi-phase synchronization
- **phaser** - Dynamic phase control

## 📈 Best Practices

✅ **DO:**
- Use `thenCompose()` for sequential dependencies
- Use `thenCombine()` or `allOf()` for parallel operations
- Handle errors with `exceptionally()` or `handle()`
- Use `CompletableFuture.completedFuture()` for already-known values

❌ **DON'T:**
- Chain independent operations sequentially (use parallel instead)
- Forget error handling in production code
- Block on futures in async callbacks (deadlock risk)
- Use common pool for long-blocking operations (use custom pool)

## 🎯 Next Steps

1. ✅ Run this demo and observe thread names
2. ✅ Read [Thread Flow Analysis](../../../../docs/COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md)
3. ✅ Understand [Thread Reuse](../../../../docs/WHY_THREAD_REUSE.md)
4. ✅ Move to **custompoolpatterns** for production patterns

---

**Package**: `com.shan.concurrency.threadspatterns.completablefuture`
