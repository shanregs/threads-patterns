# Virtual Threads Pattern - Lightweight High-Throughput Concurrency (Java 21+)

This package demonstrates Virtual Threads for handling massive concurrency with minimal resource overhead.

## 🎯 What Are Virtual Threads?

**Virtual Threads** (Java 21+) are lightweight threads managed by the JVM rather than the operating system. Unlike platform threads (traditional OS threads), millions of virtual threads can run concurrently without performance degradation.

**Key Characteristics**:
- ✅ **Lightweight**: Minimal memory overhead (~few KB vs MB for platform threads)
- ✅ **Massive scale**: Create millions without performance penalty
- ✅ **Automatic yielding**: Block during I/O without blocking platform threads
- ✅ **Same API**: Use Thread API, different implementation
- ✅ **JVM-managed**: Multiplexed onto small number of platform threads (carrier threads)

**Use Cases**:
- **Web servers**: Handle thousands of concurrent HTTP requests
- **Microservices**: I/O-bound service calls
- **Database applications**: Many concurrent DB queries
- **Network applications**: High-concurrency socket connections
- **Event processing**: Process millions of events concurrently

---

## 📊 Thread Flow Pattern: Virtual Threads Multiplexed onto Carrier Threads

### Pattern: Millions of Virtual Threads → Few Carrier Threads

```
Traditional Platform Threads (Limited):
──────────────────────────────────────────────
100 concurrent requests
100 platform threads created (OS limit: ~thousands)

Thread-1 (OS thread): Handle Request-1 ──┐
Thread-2 (OS thread): Handle Request-2 ──┼─ Each thread = ~1MB memory
...                                       ├─ Context switching overhead
Thread-100 (OS thread): Handle Request-100┘

Problem: Can't scale to 100,000 requests!


Virtual Threads (Unlimited):
──────────────────────────────────────────────
100,000 concurrent requests
100,000 virtual threads created
Only 8 carrier threads (platform threads)!

VirtualThread-1    ─┐
VirtualThread-2    ─┤
VirtualThread-3    ─┤
...                ├──→ Carrier-Thread-1 (OS thread)
VirtualThread-1000 ─┤
...                ─┘
                    ↓ (multiplexing)
VirtualThread-1001 ─┐
VirtualThread-1002 ─┤
...                ├──→ Carrier-Thread-2 (OS thread)
VirtualThread-2000 ─┤
...                ─┘

Each virtual thread = ~few KB memory
Automatic blocking/unblocking during I/O
Carrier threads: number of CPU cores


Request Processing Flow:
──────────────────────────────────────────────
VirtualThread-1:
  - Running on Carrier-Thread-1
  - Call database.query() → BLOCKS (I/O)
  - ✅ Automatically unmounted from Carrier-Thread-1
  - Carrier-Thread-1 now free for other virtual threads!

VirtualThread-2:
  - Mounted on Carrier-Thread-1 (was freed by VT-1)
  - Process request...

VirtualThread-1:
  - Database query complete
  - ✅ Remounted on next available carrier thread
  - Continue processing...

Carrier threads never blocked by I/O!
Efficient utilization, massive concurrency!
```

---

## 🔍 Demo: High-Throughput Web Server Simulation

**Scenario**: Compare platform threads vs virtual threads handling 1000 requests

### Thread Flow

```
Platform Threads (Limited Concurrency):
═══════════════════════════════════════════════════════════

FixedThreadPool (100 platform threads)
1000 requests submitted

Requests 1-100:    Running on threads 1-100   ✅ ACTIVE
Requests 101-1000: Waiting in queue          ⏳ WAITING

Total time: ~longer (sequential batches)


Virtual Threads (Unlimited Concurrency):
═══════════════════════════════════════════════════════════

VirtualThreadPerTaskExecutor
1000 requests submitted

All 1000 requests: Each gets own virtual thread ✅ CONCURRENT
Multiplexed onto 8 carrier threads
Automatic yield during I/O operations

Total time: ~faster (true parallelism for I/O)


Web Request Processing:
──────────────────────────────────────────────

VirtualThread-1:
  1. Parse HTTP request (CPU) ─── Running on Carrier-1
  2. Query database (I/O)     ─── ⏸️  UNMOUNTED (yields)
     ↓ (Carrier-1 now free for other virtual threads)
  3. Wait for DB response...  ─── Not consuming carrier thread
  4. DB response arrives      ─── ✅ REMOUNTED on Carrier-2
  5. Format JSON (CPU)        ─── Running on Carrier-2
  6. Send HTTP response (I/O) ─── ⏸️  UNMOUNTED (yields)
  7. Response sent            ─── ✅ REMOUNTED on Carrier-3
  8. Complete!                ─── Thread terminates

Carrier threads stay busy, never blocked by I/O!
```

### Code Pattern

**1. Create Virtual Threads with Executor**:
```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
for (int i = 0; i < 100_000; i++) {
    executor.submit(() -> handleRequest(i));
}
executor.shutdown();
```

**2. Create Individual Virtual Thread**:
```java
Thread vThread = Thread.ofVirtual()
    .name("VirtualThread-1")
    .start(() -> {
        log.info("Running in virtual thread");
        handleRequest();
    });
vThread.join();
```

**3. Create Unstarted Virtual Thread**:
```java
Thread vThread = Thread.ofVirtual()
    .name("VirtualThread-2")
    .unstarted(() -> processData());
vThread.start();
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.virtualthreads.Main"
```

### Expected Output
```
=== VirtualThreads Demo: High-Throughput Web Server ===

--- Comparison: Platform Threads vs Virtual Threads ---
Handling 1000 concurrent requests

--- Using Platform Threads (Traditional) ---
[pool-1-thread-1] Request-1 processing...
[pool-1-thread-2] Request-2 processing...
...
[pool-1-thread-100] Request-100 processing...
[main] Waiting for requests 101-1000... (queued)

Platform threads completed 1000 requests in 12543 ms


--- Using Virtual Threads (Java 21+) ---
[VirtualThread-1] Request-1 processing...
[VirtualThread-2] Request-2 processing...
...
[VirtualThread-1000] Request-1000 processing...
(All executing concurrently!)

Virtual threads completed 1000 requests in 1287 ms
⚡ 9.7x faster!


--- Creating Individual Virtual Threads ---
[VirtualThread-Custom-1] Custom virtual thread executing
[VirtualThread-Custom-2] Another virtual thread executing
[VirtualThread-Custom-1] Custom virtual thread completed
Individual virtual threads completed

=== VirtualThreads Demo Completed ===
```

---

## 🔑 Key APIs

### Create Virtual Threads
```java
// Executor for many virtual threads
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Individual virtual thread (started)
Thread vThread = Thread.ofVirtual().start(runnable);

// Individual virtual thread (unstarted)
Thread vThread = Thread.ofVirtual().unstarted(runnable);
vThread.start();

// Builder with name
Thread vThread = Thread.ofVirtual()
    .name("Worker-", 1)  // Worker-1, Worker-2, ...
    .start(runnable);
```

### Check if Virtual
```java
boolean isVirtual = Thread.currentThread().isVirtual();
```

---

## 🎯 Real-World Use Cases

### 1. Web Server (High Concurrency)
```java
public class WebServer {
    public void start() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            ServerSocket server = new ServerSocket(8080);
            while (running) {
                Socket client = server.accept();
                executor.submit(() -> handleClient(client)); // 1 virtual thread per connection
            }
        }
    }

    private void handleClient(Socket client) {
        // Parse request (CPU)
        Request req = parse Request(client.getInputStream());

        // Query database (I/O - virtual thread yields here!)
        Data data = database.query(req);

        // Format response (CPU)
        Response resp = format(data);

        // Send response (I/O - yields again!)
        client.getOutputStream().write(resp);
    }
}
// Can handle 100,000+ concurrent connections!
```

### 2. Microservices (Parallel API Calls)
```java
public class OrderService {
    public Order processOrder(OrderRequest req) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var userFuture = executor.submit(() -> userService.getUser(req.getUserId()));
            var inventoryFuture = executor.submit(() -> inventoryService.checkStock(req.getItems()));
            var pricingFuture = executor.submit(() -> pricingService.calculatePrice(req.getItems()));

            User user = userFuture.get();
            Inventory inventory = inventoryFuture.get();
            Price price = pricingFuture.get();

            return createOrder(user, inventory, price);
        }
    }
}
```

### 3. Event Processing
```java
public class EventProcessor {
    public void processEvents(Stream<Event> events) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            events.forEach(event ->
                executor.submit(() -> handleEvent(event))
            );
        }
    }
    // Can process millions of events concurrently!
}
```

---

## ⚠️ Common Pitfalls

### 1. Using for CPU-Bound Tasks
```java
// ❌ BAD: Virtual threads for CPU-intensive work
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 1000; i++) {
        executor.submit(() -> calculatePrimes(1_000_000)); // CPU-bound
    }
}
// No benefit! Virtual threads excel at I/O, not CPU
```

**Solution**: Use ForkJoinPool for CPU-bound tasks
```java
// ✅ GOOD: ForkJoinPool for CPU-intensive work
ForkJoinPool.commonPool().invoke(new PrimeCalculator(...));
```

### 2. Thread Pools with Virtual Threads
```java
// ❌ BAD: Pooling virtual threads (unnecessary!)
ExecutorService pool = Executors.newFixedThreadPool(100,
    Thread.ofVirtual().factory());
// Virtual threads are cheap! Don't pool them.
```

**Solution**: Create virtual threads per task
```java
// ✅ GOOD: One virtual thread per task
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> task());
}
```

### 3. ThreadLocal with Virtual Threads
```java
// ⚠️ CAUTION: ThreadLocal with millions of virtual threads
ThreadLocal<ExpensiveObject> threadLocal = new ThreadLocal<>();
// Each of 1,000,000 virtual threads gets copy = memory issue!
```

**Solution**: Use Scoped Values (Java 21+) or minimize ThreadLocal usage
```java
// ✅ BETTER: Scoped Values (Java 21+)
ScopedValue<ExpensiveObject> scopedValue = ScopedValue.newInstance();
```

---

## 🎓 Best Practices

✅ **DO:**
- Use for I/O-bound tasks (network, database, file I/O)
- Create one virtual thread per task (don't pool)
- Use for high-concurrency web servers and microservices
- Combine with async I/O libraries

❌ **DON'T:**
- Use for CPU-bound tasks (use ForkJoinPool instead)
- Pool virtual threads (they're cheap to create)
- Use excessive ThreadLocal with millions of virtual threads
- Use synchronized with long critical sections (blocks carrier thread)

---

## 📊 Virtual Threads vs Platform Threads vs Async/Reactive

| Feature | Virtual Threads | Platform Threads | Async/Reactive |
|---------|-----------------|------------------|----------------|
| **Scalability** | ✅ Millions | 🟡 Thousands | ✅ Very high |
| **Memory** | ✅ ~KB each | ❌ ~MB each | ✅ Minimal |
| **Code Style** | ✅ Sequential | ✅ Sequential | ❌ Complex (callbacks) |
| **Learning Curve** | ✅ Easy | ✅ Easy | 🟡 Steep |
| **I/O Efficiency** | ✅ Excellent | 🟡 Poor | ✅ Excellent |
| **CPU Tasks** | 🟡 No benefit | ✅ Good | 🟡 Complex |

**When to Choose**:
- **Virtual Threads**: I/O-bound, high concurrency, simple code (Java 21+)
- **Platform Threads**: CPU-bound, moderate concurrency
- **Async/Reactive**: Existing reactive codebase, non-blocking everything
- **ForkJoinPool**: CPU-intensive divide-and-conquer algorithms

---

**Package**: `com.shan.concurrency.threadspatterns.virtualthreads`

**Pattern Type**: Concurrency - Lightweight Threads
**Thread Flow**: Millions of virtual threads multiplexed onto carrier threads
**Best For**: High-concurrency I/O-bound applications (web servers, microservices, database apps)
**Requires**: Java 21+
