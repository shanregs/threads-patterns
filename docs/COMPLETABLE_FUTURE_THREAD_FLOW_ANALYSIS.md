# CompletableFuture Thread Flow Analysis

This document explains in detail how CompletableFuture uses threads from the ForkJoinPool.commonPool(), when operations run in parallel vs sequentially, and how waiting/combining works.

## Thread Pool Used

**Default Thread Pool**: `ForkJoinPool.commonPool()`
- All `supplyAsync()` calls without explicit executor use this pool
- Pool size: `Runtime.getRuntime().availableProcessors() - 1`
- Threads are named: `ForkJoinPool.commonPool-worker-N`

**Main Thread**: `com.shan.concurrency.threadspatterns.completablefuture.Main.main()`
- Executes the demo code
- Blocks on `.get()` calls waiting for results

---

## Example 1: Basic Chaining (SEQUENTIAL Operations)

### Code Flow
```java
apiService.fetchUserProfile(userId)              // Step 1: supplyAsync
    .thenApply(profile -> profile.getUserId())   // Step 2: transformation
    .thenCompose(id -> fetchOrderHistory(id))    // Step 3: supplyAsync
    .thenCompose(orderHistory -> fetchRecommendations(orderHistory)) // Step 4: supplyAsync
    .get();                                      // Step 5: block and wait
```

### Thread Execution Sequence

```
┌─────────────┐                  ┌──────────────────────┐              ┌──────────────────────┐
│ Main Thread │                  │ ForkJoinPool-worker-2│              │ ForkJoinPool-worker-1│
└──────┬──────┘                  └──────────┬───────────┘              └──────────┬───────────┘
       │                                    │                                     │
       │ 1. fetchUserProfile(USER-123)      │                                     │
       │────────────────────────────────────>│                                     │
       │                                    │                                     │
       │                                    │ 2. Fetch user profile               │
       │                                    │    (sleeps 800ms)                   │
       │                                    │                                     │
       │                                    │ 3. Profile fetched                  │
       │                                    │                                     │
       │                                    │ 4. thenApply executes               │
       │                                    │    (same thread)                    │
       │                                    │    Returns: USER-123                │
       │                                    │                                     │
       │                                    │ 5. thenCompose triggers             │
       │                                    │    fetchOrderHistory()              │
       │                                    │─────────────────────────────────────>│
       │                                    │                                     │
       │                                    │                                     │ 6. Fetch order history
       │                                    │                                     │    (sleeps 1000ms)
       │                                    │                                     │
       │                                    │                                     │ 7. Orders fetched (3 orders)
       │                                    │                                     │
       │                                    │ 8. thenCompose triggers             │
       │                                    │<─────────────────────────────────────│
       │                                    │    fetchRecommendations()           │
       │                                    │                                     │
       │                                    │ 9. Generate recommendations         │
       │                                    │    (sleeps 600ms)                   │
       │                                    │                                     │
       │                                    │ 10. Recommendations ready           │
       │<───────────────────────────────────│     [Product-A, B, C]               │
       │ 11. get() returns result           │                                     │
       │                                    │                                     │
       ▼                                    ▼                                     ▼
```

### Key Points

#### Thread Usage
- **ForkJoinPool.commonPool-worker-2**: Executes fetchUserProfile + thenApply + fetchRecommendations
- **ForkJoinPool.commonPool-worker-1**: Executes fetchOrderHistory
- **Main thread**: Blocks on get() until chain completes

#### Sequential Execution
1. **fetchUserProfile** → 800ms (worker-2)
2. **thenApply** → immediate (worker-2, reuses completing thread)
3. **thenCompose → fetchOrderHistory** → 1000ms (worker-1, new async task)
4. **thenCompose → fetchRecommendations** → 600ms (worker-2, new async task)

**Total Time**: ~2400ms (sequential: 800 + 1000 + 600)

#### Why Sequential?
- `thenCompose()` creates a **dependency chain**
- Each step waits for previous step to complete
- New async task launched only AFTER previous completes
- This is the "then" in thenCompose - "do this THEN do that"

#### Thread Transition Points
```
worker-2: fetchUserProfile
          ↓ (stays on worker-2)
worker-2: thenApply
          ↓ (new supplyAsync submitted)
worker-1: fetchOrderHistory
          ↓ (new supplyAsync submitted)
worker-2: fetchRecommendations
          ↓ (completes)
main:     get() unblocks
```

---

## Example 2: Combining (PARALLEL Operations)

### Code Flow
```java
CompletableFuture<UserProfile> profileFuture = fetchUserProfile(userId);   // Starts immediately
CompletableFuture<OrderHistory> ordersFuture = fetchOrderHistory(userId);  // Starts immediately

profileFuture.thenCombine(ordersFuture, (profile, orders) -> {  // Waits for BOTH
    return String.format("User: %s, Orders: %d", profile.getName(), orders.getOrders().size());
});
```

### Thread Execution Sequence

```
┌─────────────┐         ┌──────────────────────┐              ┌──────────────────────┐
│ Main Thread │         │ ForkJoinPool-worker-2│              │ ForkJoinPool-worker-1│
└──────┬──────┘         └──────────┬───────────┘              └──────────┬───────────┘
       │                           │                                     │
       │ 1. fetchUserProfile()     │                                     │
       │───────────────────────────>│                                     │
       │                           │                                     │
       │ 2. fetchOrderHistory()    │                                     │
       │───────────────────────────────────────────────────────────────>│
       │                           │                                     │
       │                           │ 3. Fetch profile                    │ 3. Fetch orders
       │                           │    (sleeps 800ms)                   │    (sleeps 1000ms)
       │                           │                                     │
       │  ╔══════════════════════════════════════════════════════════════════════╗
       │  ║              BOTH OPERATIONS RUN IN PARALLEL                         ║
       │  ╚══════════════════════════════════════════════════════════════════════╝
       │                           │                                     │
       │                           │ 4. Profile ready (800ms)            │
       │                           │    Waits for orders...              │
       │                           │                                     │
       │                           │                                     │ 5. Orders ready (1000ms)
       │                           │                                     │    LAST to complete
       │                           │                                     │
       │                           │                                     │ 6. thenCombine executes
       │                           │                                     │    on worker-1 (last completer)
       │                           │                                     │    Combines both results
       │                           │                                     │
       │<─────────────────────────────────────────────────────────────────────────│
       │ 7. get() returns combined result                               │
       │                           │                                     │
       ▼                           ▼                                     ▼
```

### Key Points

#### Thread Usage
- **ForkJoinPool.commonPool-worker-2**: Executes fetchUserProfile (800ms)
- **ForkJoinPool.commonPool-worker-1**: Executes fetchOrderHistory (1000ms) + thenCombine
- **Main thread**: Blocks on get() until both complete

#### Parallel Execution
- Both futures start **simultaneously** (not waiting for each other)
- Profile finishes at 800ms (worker-2 becomes idle)
- Orders finish at 1000ms (worker-1 stays busy)
- **Total Time**: ~1000ms (max of 800 and 1000, NOT sum)

#### Why Parallel?
- Two **independent** CompletableFutures created
- No dependency between them
- Both submit tasks to pool immediately
- Different workers pick them up

#### Waiting/Combining Mechanism
```
Time 0ms:    Both tasks submitted to ForkJoinPool
             worker-2 → fetchUserProfile
             worker-1 → fetchOrderHistory

Time 800ms:  worker-2 completes profile
             Profile future = COMPLETED
             But thenCombine NOT triggered yet (still waiting for orders)

Time 1000ms: worker-1 completes orders
             Orders future = COMPLETED
             BOTH futures now complete!
             worker-1 executes thenCombine (last completer wins)

Time 1000ms: main thread's get() unblocks
```

#### Performance Benefit
- **Sequential would take**: 800 + 1000 = 1800ms
- **Parallel takes**: max(800, 1000) = 1000ms
- **Speedup**: 1.8x faster

---

## Example 3: Error Handling

### Code Flow
```java
// Success case
fetchWithPossibleError("USER-789", false)
    .exceptionally(ex -> "FALLBACK_VALUE");

// Failure case
fetchWithPossibleError("USER-999", true)
    .handle((result, ex) -> {
        if (ex != null) return "DEFAULT_VALUE";
        return result;
    });
```

### Thread Execution Sequence

```
┌─────────────┐         ┌──────────────────────┐              ┌──────────────────────┐
│ Main Thread │         │ ForkJoinPool-worker-1│              │ ForkJoinPool-worker-2│
└──────┬──────┘         └──────────┬───────────┘              └──────────┬───────────┘
       │                           │                                     │
       │ 1. Success case           │                                     │
       │───────────────────────────>│                                     │
       │                           │                                     │
       │ 2. Failure case           │                                     │
       │───────────────────────────────────────────────────────────────>│
       │                           │                                     │
       │                           │ 3. shouldFail=false                 │ 3. shouldFail=true
       │                           │    (sleeps 300ms)                   │    (sleeps 300ms)
       │                           │                                     │
       │                           │ 4. SUCCESS                          │ 4. THROWS RuntimeException
       │                           │    Returns "Success-USER-789"       │    "API call failed..."
       │                           │                                     │
       │                           │ 5. exceptionally NOT invoked        │ 5. handle() invoked
       │                           │    (no error occurred)              │    on worker-2
       │                           │                                     │    (same thread that failed)
       │                           │                                     │
       │                           │                                     │ 6. ex != null
       │                           │                                     │    Returns "DEFAULT_VALUE"
       │                           │                                     │
       │<───────────────────────────│                                     │
       │ 7. get() returns           │                                     │
       │    "Success-USER-789"      │                                     │
       │                           │                                     │
       │<─────────────────────────────────────────────────────────────────│
       │ 8. get() returns           │                                     │
       │    "DEFAULT_VALUE"         │                                     │
       │                           │                                     │
       ▼                           ▼                                     ▼
```

### Key Points

#### Error Handler Execution Thread
- **exceptionally()**: Executes on the thread where error occurred (or completes normally)
- **handle()**: Executes on the completing thread (success or failure)

#### Success Case
```
worker-1: fetchWithPossibleError(false)
          ↓
worker-1: Returns "Success-USER-789" normally
          ↓
worker-1: exceptionally() SKIPPED (no exception)
          ↓
main:     get() returns "Success-USER-789"
```

#### Failure Case
```
worker-2: fetchWithPossibleError(true)
          ↓
worker-2: Throws RuntimeException
          ↓
worker-2: handle() catches exception
          ↓ (ex != null branch)
worker-2: Returns "DEFAULT_VALUE"
          ↓
main:     get() returns "DEFAULT_VALUE"
```

---

## Summary: When Operations Are Parallel vs Sequential

### ✅ PARALLEL Operations

**Pattern**: Create multiple CompletableFutures independently
```java
CompletableFuture<A> future1 = supplyAsync(() -> taskA());
CompletableFuture<B> future2 = supplyAsync(() -> taskB());
CompletableFuture<C> future3 = supplyAsync(() -> taskC());

// All three run in parallel on different threads
```

**Methods that combine parallel futures**:
- `thenCombine()` - waits for two futures
- `allOf()` - waits for all futures
- `anyOf()` - waits for first to complete

### ❌ SEQUENTIAL Operations

**Pattern**: Chain operations with then* methods
```java
supplyAsync(() -> taskA())
    .thenApply(a -> transformA(a))       // Sequential: waits for taskA
    .thenCompose(b -> supplyAsync(() -> taskB(b)))  // Sequential: waits for transform
    .thenApply(c -> transformC(c));      // Sequential: waits for taskB
```

**Methods that create sequential chains**:
- `thenApply()` - transform result (same thread usually)
- `thenCompose()` - chain another async operation
- `thenAccept()` - consume result
- `thenRun()` - run after completion

---

## Thread Pool Behavior

### How Threads Are Assigned

1. **supplyAsync()**: Submits task to ForkJoinPool, any available worker picks it up
2. **thenApply()**: Often runs on the completing thread (no context switch)
3. **thenCompose()**: Submits new async task, may get different worker
4. **thenCombine()**: Runs on whichever future completes LAST

### Thread Reuse Pattern
```
Example 1 (Chaining):
  worker-2 → fetchUserProfile → thenApply (stays on worker-2)
  worker-1 → fetchOrderHistory (new task)
  worker-2 → fetchRecommendations (reused for new task)

Example 2 (Parallel):
  worker-2 → fetchUserProfile (800ms)
  worker-1 → fetchOrderHistory (1000ms) → thenCombine (stays on worker-1)
```

### Why Last Completer Executes Combine?
When using `thenCombine(other, biFunction)`:
- If `this` future completes first → waits for `other`
- If `other` future completes first → waits for `this`
- Whichever completes LAST triggers the biFunction on its thread
- Avoids thread context switch since that thread is already active

---

## Waiting Mechanisms

### 1. thenCompose() - Sequential Wait
```java
future1.thenCompose(result1 -> future2)
```
- future2 is NOT created until future1 completes
- Thread executing future1 completion submits future2 to pool
- New thread picks up future2

### 2. thenCombine() - Parallel Wait
```java
future1.thenCombine(future2, combiner)
```
- Both futures created and start immediately
- Internal mechanism: each future registers callback
- First to complete: registers "waiting for other"
- Last to complete: triggers combiner with both results

### 3. get() - Blocking Wait
```java
result = future.get();  // Main thread blocks here
```
- Calling thread (main) blocks until future completes
- Uses park/unpark mechanism internally
- When future completes, main thread is unparked

### 4. allOf() - Multiple Wait
```java
CompletableFuture.allOf(future1, future2, future3).join();
```
- Creates dependency on all futures
- Tracks completion count
- Completes when count reaches total
- Can run on any completing thread

---

## Performance Optimization Tips

### ✅ DO: Use Parallel for Independent Operations
```java
// GOOD: Runs in parallel (1000ms total)
CompletableFuture<A> fa = supplyAsync(() -> fetchA());  // 800ms
CompletableFuture<B> fb = supplyAsync(() -> fetchB());  // 1000ms
fa.thenCombine(fb, combiner);
```

### ❌ DON'T: Chain Independent Operations
```java
// BAD: Runs sequentially (1800ms total)
supplyAsync(() -> fetchA())              // 800ms
    .thenCompose(a -> supplyAsync(() -> fetchB()));  // 1000ms
```

### ✅ DO: Use thenApply for Cheap Transformations
```java
// GOOD: Transformation on same thread, no context switch
supplyAsync(() -> fetchData())
    .thenApply(data -> data.toUpperCase())  // Cheap, same thread
```

### ✅ DO: Use thenCompose When Next Step Needs Result
```java
// GOOD: Orders depend on userId from profile
fetchUserProfile(userId)
    .thenCompose(profile -> fetchOrders(profile.getUserId()))
```

---

## Real-World Thread Flow Example

**Scenario**: E-commerce dashboard loading

```java
// Three independent API calls (PARALLEL)
CompletableFuture<Profile> profile = fetchProfile();        // 500ms
CompletableFuture<Orders> orders = fetchOrders();           // 800ms
CompletableFuture<Recommendations> recs = fetchRecs();      // 600ms

// Wait for all three (max 800ms, not sum of 1900ms)
CompletableFuture.allOf(profile, orders, recs).join();

// Then sequentially load related data
orders.thenCompose(o -> fetchOrderDetails(o.getLatestId())) // 400ms
      .thenApply(details -> enrichWithTracking(details));    // 100ms

// Total: 800ms (parallel) + 400ms + 100ms = 1300ms
// vs Sequential: 500 + 800 + 600 + 400 + 100 = 2400ms
// Speedup: 1.85x faster
```

### Thread Timeline
```
0ms:     worker-1 → fetchProfile
         worker-2 → fetchOrders
         worker-3 → fetchRecs

500ms:   worker-1 done (profile complete)
600ms:   worker-3 done (recs complete)
800ms:   worker-2 done (orders complete) ← LAST completer
         worker-2 → fetchOrderDetails

1200ms:  worker-1 → enrichWithTracking (reused)
1300ms:  ALL COMPLETE
```

---

## Diagram: CompletableFuture State Machine

```
                    supplyAsync()
                         │
                         ▼
              ┌──────────────────────┐
              │   RUNNING            │
              │  (on worker thread)  │
              └──────────┬───────────┘
                         │
                ┌────────┴────────┐
                │                 │
         Success│                 │Exception
                │                 │
                ▼                 ▼
    ┌──────────────────┐    ┌──────────────────┐
    │   COMPLETED      │    │  COMPLETED       │
    │  (has result)    │    │  EXCEPTIONALLY   │
    └────────┬─────────┘    │  (has exception) │
             │              └────────┬─────────┘
             │                       │
             │ thenApply             │ exceptionally
             │ thenCompose           │ handle
             │ thenCombine           │
             │                       │
             ▼                       ▼
    ┌──────────────────┐    ┌──────────────────┐
    │   NEXT STAGE     │    │  ERROR HANDLER   │
    │   RUNNING        │    │  RUNNING         │
    └──────────────────┘    └──────────────────┘
```

---

## Key Takeaways

1. **Thread Pool**: ForkJoinPool.commonPool() by default (size = processors - 1)

2. **Parallel**: Create multiple CompletableFutures independently
   - `thenCombine()` waits for both
   - Last completer executes combining function

3. **Sequential**: Chain with `then*` methods
   - Each step waits for previous to complete
   - May reuse threads or get new ones from pool

4. **Error Handling**: Executes on the thread where error occurred
   - `exceptionally()` for errors only
   - `handle()` for both success and error

5. **Blocking**: `.get()` or `.join()` blocks calling thread until future completes

6. **Performance**: Identify independent operations and run them in parallel!

---

Generated by analyzing: `com.shan.concurrency.threadspatterns.completablefuture`
