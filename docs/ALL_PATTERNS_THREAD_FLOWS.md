# All Concurrency Patterns - Thread Flow Reference

Complete reference of all concurrency patterns with thread flow diagrams and sequential/parallel analysis.

## 📊 Pattern Categories

### 1. Async Composition
- CompletableFuture (common pool)
- CompletableFuture (custom pool)

### 2. Synchronization Primitives
- CountDownLatch
- CyclicBarrier
- Phaser
- Semaphore
- Exchanger

### 3. Locking Mechanisms
- ReentrantLock
- ReadWriteLock (coming soon)
- StampedLock (coming soon)

### 4. Thread-Safe Collections
- BlockingQueue
- ConcurrentHashMap (coming soon)

### 5. Thread Pools
- ExecutorService
- ForkJoinPool
- Virtual Threads

### 6. Thread-Local Storage
- ThreadLocal

---

## Pattern 1: CountDownLatch

**Type**: Synchronization - Wait for Completion
**Thread Flow**: Parallel → Single Wait Point

### Pattern
```
Main Thread:        [Submit Tasks] → [await()] ──────────┐
                                                          │
Worker Threads:                                           │
Thread-1: Task 1 → countDown() ──────────────────────────┤
Thread-2: Task 2 → countDown() ──────────────────────────┼→ Count = 0
Thread-3: Task 3 → countDown() ──────────────────────────┤  Main Unblocks
Thread-4: Task 4 → countDown() ──────────────────────────┘
```

### Use Case
- **Batch job coordination**: Wait for all workers to complete
- **Service startup**: Ensure all services initialized
- **Test synchronization**: Start all test threads together

### Key Characteristic
- **One-time use**: Cannot be reset
- **Unidirectional**: Workers count down, main waits

### Code
```java
CountDownLatch latch = new CountDownLatch(4);
executor.submit(() -> { doWork(); latch.countDown(); });
latch.await(); // Main waits for all
```

---

## Pattern 2: CyclicBarrier

**Type**: Synchronization - Multi-Phase Coordination
**Thread Flow**: Mutual Wait Points (Reusable)

### Pattern
```
Phase 1:
Thread-1: Work → [await()] ────┐
Thread-2: Work → [await()] ────┤
Thread-3: Work → [await()] ────┼→ All arrived → Barrier Action
Thread-4: Work → [await()] ────┘              → All released

Phase 2 (Same Barrier Reused):
Thread-1: Work → [await()] ────┐
Thread-2: Work → [await()] ────┤
Thread-3: Work → [await()] ────┼→ All arrived → Barrier Action
Thread-4: Work → [await()] ────┘              → All released
```

### Use Case
- **Multi-phase algorithms**: Matrix processing (rows → barrier → columns)
- **Simulations**: Discrete time steps
- **Guided tours**: Tourists sync at each attraction

### Key Characteristic
- **Cyclic/Reusable**: Same barrier for multiple phases
- **Mutual wait**: All threads wait for each other
- **Barrier action**: Execute action when all arrive

### Code
```java
Runnable action = () -> log.info("All arrived!");
CyclicBarrier barrier = new CyclicBarrier(4, action);

// Each thread
barrier.await(); // Waits for all, then all released
```

---

## Pattern 3: Phaser

**Type**: Synchronization - Flexible Multi-Phase
**Thread Flow**: Dynamic Phases with Arrival/Advance

### Pattern
```
Phase 0:
Thread-1: Work → arriveAndAwaitAdvance() ────┐
Thread-2: Work → arriveAndAwaitAdvance() ────┼→ Advance to Phase 1
Thread-3: Work → arriveAndAwaitAdvance() ────┘

Phase 1:
Thread-1: Work → arriveAndAwaitAdvance() ────┐
Thread-2: Work → arriveAndAwaitAdvance() ────┼→ Advance to Phase 2
Thread-3: Work → arriveAndDeregister()  ─────┘ (Leaves)

Phase 2:
Thread-1: Work → arriveAndAwaitAdvance() ────┐
Thread-2: Work → arriveAndAwaitAdvance() ────┘ (Only 2 now)
```

### Use Case
- **Game development**: Levels with players joining/leaving
- **Multi-stage processing**: Stages with varying participants
- **Dynamic workflows**: Participants can register/deregister

### Key Characteristic
- **Dynamic parties**: Threads can register/deregister
- **Phase tracking**: Know current phase number
- **Flexible**: Like CyclicBarrier but more powerful

### Code
```java
Phaser phaser = new Phaser(3); // 3 parties

// Each thread
phaser.arriveAndAwaitAdvance(); // Phase 0 → Phase 1
phaser.arriveAndAwaitAdvance(); // Phase 1 → Phase 2

// Thread can leave
phaser.arriveAndDeregister();
```

---

## Pattern 4: Semaphore

**Type**: Resource Control - Limit Concurrent Access
**Thread Flow**: Permit Acquisition/Release

### Pattern
```
Semaphore (3 permits available)

Time 0:
Thread-1: acquire() → ✅ Got permit (2 remaining)
Thread-2: acquire() → ✅ Got permit (1 remaining)
Thread-3: acquire() → ✅ Got permit (0 remaining)
Thread-4: acquire() → ⏳ WAITING (no permits)
Thread-5: acquire() → ⏳ WAITING (no permits)

Time 500ms:
Thread-1: release() → Permit returned (Thread-4 acquires)

Time 700ms:
Thread-2: release() → Permit returned (Thread-5 acquires)
```

### Use Case
- **Connection pooling**: Limit database connections
- **Rate limiting**: Max N concurrent API calls
- **Resource management**: Limited ATM terminals

### Key Characteristic
- **Counting semaphore**: Track number of permits
- **Fairness**: Optional FIFO for waiting threads
- **Not reentrant**: Same thread can acquire multiple permits

### Code
```java
Semaphore semaphore = new Semaphore(3); // 3 permits

semaphore.acquire(); // Get permit (blocks if none)
try {
    useResource();
} finally {
    semaphore.release(); // Return permit
}
```

---

## Pattern 5: BlockingQueue

**Type**: Producer-Consumer - Thread-Safe Queue
**Thread Flow**: Producers → Queue → Consumers

### Pattern
```
Producers (Parallel):
Thread-1: produce() → put() ─────┐
Thread-2: produce() → put() ─────┼→ [Queue: bounded/unbounded]
Thread-3: produce() → put() ─────┘

Consumers (Parallel):
Thread-4: ← take() → consume() ──┐
Thread-5: ← take() → consume() ──┼← From Queue
Thread-6: ← take() → consume() ──┘
```

### Use Case
- **Task queues**: Thread pool work queue
- **Pipeline processing**: Stage 1 → Queue → Stage 2
- **Event handling**: Event producers → Queue → Handlers

### Key Characteristic
- **Blocking**: `put()` blocks if full, `take()` blocks if empty
- **Thread-safe**: No external synchronization needed
- **Bounded/Unbounded**: Choose based on memory constraints

### Code
```java
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);

// Producer
queue.put(task); // Blocks if queue full

// Consumer
Task task = queue.take(); // Blocks if queue empty
```

---

## Pattern 6: ReentrantLock

**Type**: Locking - Advanced Lock Control
**Thread Flow**: Explicit Lock/Unlock

### Pattern
```
Thread-1:
  lock.lock()
    ↓
  Critical Section (exclusive access)
    ↓
  lock.unlock()

Thread-2: (Waits while Thread-1 holds lock)
  lock.lock() → ⏳ WAITING
    ↓ (Thread-1 unlocks)
  lock.lock() → ✅ Acquired
    ↓
  Critical Section
    ↓
  lock.unlock()
```

### Use Case
- **Bank transfers**: Ensure atomic operations
- **Resource management**: Exclusive access with tryLock
- **Condition variables**: Complex waiting conditions

### Key Characteristic
- **Reentrant**: Same thread can acquire multiple times
- **Fairness**: Optional FIFO for waiting threads
- **tryLock**: Non-blocking acquire attempt
- **Conditions**: Multiple condition variables

### Code
```java
ReentrantLock lock = new ReentrantLock(true); // Fair

lock.lock();
try {
    // Critical section
} finally {
    lock.unlock(); // Always unlock
}

// Or with tryLock
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try {
        // Got lock
    } finally {
        lock.unlock();
    }
}
```

---

## Pattern 7: Exchanger

**Type**: Data Exchange - Thread Pair Synchronization
**Thread Flow**: Pair-wise Exchange

### Pattern
```
Thread-1 (Buyer):
  Order buyOrder = createOrder();
  Order soldOrder = exchanger.exchange(buyOrder); → ⏳ Waiting
                                                     ↓
Thread-2 (Seller):                                  ↓
  Order sellOrder = createOrder();                  ↓
  Order boughtOrder = exchanger.exchange(sellOrder);→ Exchange happens!

Both threads continue with exchanged data
```

### Use Case
- **Pipeline stages**: Data handoff between stages
- **Trading systems**: Match buy/sell orders
- **Buffer swapping**: Producer fills buffer, consumer empties

### Key Characteristic
- **Pair-wise**: Exactly 2 threads exchange
- **Bidirectional**: Both threads give and receive
- **Synchronization point**: Both wait until partner arrives

### Code
```java
Exchanger<Order> exchanger = new Exchanger<>();

// Thread 1
Order myOrder = new Order("BUY");
Order otherOrder = exchanger.exchange(myOrder);

// Thread 2
Order myOrder = new Order("SELL");
Order otherOrder = exchanger.exchange(myOrder);
```

---

## Pattern 8: ThreadLocal

**Type**: Thread-Specific Storage
**Thread Flow**: Isolated Per-Thread State

### Pattern
```
ThreadLocal<Context> contextHolder = new ThreadLocal<>();

Thread-1:
  contextHolder.set(context1);
  ↓
  method1() → contextHolder.get() → context1 ✅
  ↓
  method2() → contextHolder.get() → context1 ✅

Thread-2:
  contextHolder.set(context2);
  ↓
  method1() → contextHolder.get() → context2 ✅
  ↓
  method2() → contextHolder.get() → context2 ✅

Each thread has its own isolated copy!
```

### Use Case
- **Web requests**: Request-scoped data (user, session, transaction)
- **Database connections**: Thread-local connection per thread
- **Date formatters**: SimpleDateFormat (not thread-safe)

### Key Characteristic
- **Thread isolation**: Each thread has own value
- **Implicit propagation**: Available throughout call stack
- **Memory leak risk**: Must call `remove()` when done

### Code
```java
ThreadLocal<User> currentUser = new ThreadLocal<>();

// Set
currentUser.set(user);

// Get (anywhere in call stack)
User user = currentUser.get();

// Clean up (important!)
currentUser.remove();
```

---

## Pattern 9: ForkJoinPool

**Type**: Divide-and-Conquer - Work Stealing
**Thread Flow**: Recursive Task Splitting

### Pattern
```
Main Task:
  Task[0-1000]
    ↓ fork()
  ┌─────────────┬─────────────┐
  │             │             │
Task[0-500]  Task[500-1000]  │
  ↓ fork()      ↓ fork()     │
┌───┬───┐    ┌───┬───┐       │
│   │   │    │   │   │       │
[0  [250 [500 [750 [Join all │
-250]-500]-750]-1000]         │
  ↓   ↓    ↓   ↓             │
  └───┴────┴───┴─────────────┘
           ↓
      Combined Result
```

### Use Case
- **Image processing**: Divide image into regions
- **Array operations**: Parallel sort, sum, filter
- **Tree traversal**: Process subtrees in parallel

### Key Characteristic
- **Work stealing**: Idle threads steal work from busy threads
- **Recursive**: Tasks spawn subtasks
- **Optimized**: For divide-and-conquer algorithms

### Code
```java
class SumTask extends RecursiveTask<Long> {
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            return sumSequentially();
        } else {
            int mid = (start + end) / 2;
            SumTask left = new SumTask(start, mid);
            SumTask right = new SumTask(mid, end);

            left.fork();  // Async
            long rightResult = right.compute(); // Sync
            long leftResult = left.join(); // Wait

            return leftResult + rightResult;
        }
    }
}

ForkJoinPool pool = new ForkJoinPool();
long result = pool.invoke(new SumTask(0, array.length));
```

---

## Pattern 10: Virtual Threads (Java 21+)

**Type**: Lightweight Threads - Massive Concurrency
**Thread Flow**: Millions of Concurrent Tasks

### Pattern
```
Traditional Threads (Limited):
OS Thread-1 ─────────────────────┐
OS Thread-2 ─────────────────────┼→ ~Thousands max
OS Thread-N ─────────────────────┘

Virtual Threads (Massive):
Virtual-1 ────┐
Virtual-2 ────┤
Virtual-3 ────┼→ Carrier Thread-1 ─┐
...           │                     ├→ Millions possible
Virtual-1000 ─┘                     │
Virtual-1001 ─┐                     │
...           ├→ Carrier Thread-2 ─┘
Virtual-2000 ─┘
```

### Use Case
- **Web servers**: Handle millions of concurrent requests
- **I/O-heavy**: Each virtual thread can block on I/O
- **Microservices**: High-concurrency architectures

### Key Characteristic
- **Lightweight**: Minimal memory footprint
- **Blocking is OK**: Virtual threads can block without wasting OS threads
- **Carrier threads**: Virtual threads run on platform thread pool

### Code
```java
// Create virtual thread
Thread.startVirtualThread(() -> {
    handleRequest();
});

// ExecutorService with virtual threads
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> processTask());

// Structured concurrency
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Future<String> f1 = scope.fork(() -> fetchUser());
    Future<String> f2 = scope.fork(() -> fetchOrders());

    scope.join();
    scope.throwIfFailed();

    return f1.resultNow() + f2.resultNow();
}
```

---

## Summary: Sequential vs Parallel Patterns

### Sequential Patterns (One-at-a-time)
- **ReentrantLock**: Only one thread in critical section
- **Semaphore(1)**: Only one thread has permit
- **Exchanger**: Pairs sync sequentially

### Parallel Patterns (Concurrent Execution)
- **CountDownLatch**: Multiple workers run in parallel, main waits
- **CyclicBarrier**: All threads run in parallel, sync at barrier
- **Phaser**: Dynamic parallel execution with phases
- **Semaphore(N)**: N threads can execute concurrently
- **BlockingQueue**: Multiple producers and consumers
- **ForkJoinPool**: Recursive parallel decomposition
- **Virtual Threads**: Millions of concurrent tasks

### Hybrid Patterns (Both)
- **CompletableFuture**: Chain sequential, combine parallel
- **ThreadLocal**: Parallel threads with isolated state

---

## Quick Selection Guide

| Need | Use |
|------|-----|
| Wait for tasks to complete | CountDownLatch |
| Multi-phase synchronization | CyclicBarrier |
| Dynamic phases | Phaser |
| Limit concurrent access | Semaphore |
| Producer-consumer | BlockingQueue |
| Exclusive access with try | ReentrantLock |
| Thread pair exchange | Exchanger |
| Thread-specific data | ThreadLocal |
| Divide-and-conquer | ForkJoinPool |
| Async composition | CompletableFuture |
| Massive concurrency | Virtual Threads |

---

**See individual package READMEs for detailed examples and code!**
