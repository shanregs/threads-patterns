# BlockingQueue Pattern - Producer-Consumer with Thread-Safe Queue

This package demonstrates BlockingQueue for implementing the producer-consumer pattern with automatic blocking and thread safety.

## 🎯 What Is BlockingQueue?

**BlockingQueue** is a thread-safe queue that blocks threads when they try to take from an empty queue or put into a full queue. It eliminates the need for manual synchronization in producer-consumer scenarios.

**Key Characteristics**:
- ✅ **Thread-safe**: Automatically synchronized
- ✅ **Blocking operations**: put() blocks when full, take() blocks when empty
- ✅ **Bounded/Unbounded**: Supports capacity limits
- ✅ **Multiple implementations**: ArrayBlockingQueue, LinkedBlockingQueue, etc.
- ✅ **FIFO ordering**: First-in, first-out (most implementations)

**Use Cases**:
- **Producer-consumer**: Multiple producers, multiple consumers
- **Task queues**: Background job processing
- **Event handling**: Async event processing
- **Log aggregation**: Collect logs from multiple sources
- **Thread pool executors**: Internal queue for tasks

---

## 📊 Thread Flow Pattern: Producer-Consumer with Bounded Queue

### Pattern: Producers → BlockingQueue → Consumers

```
BlockingQueue<LogEntry> (Capacity: 5)
3 Producers + 1 Consumer

Time 0ms - Setup:
  Queue: [] (empty, capacity=5)
  Producer-1, Producer-2, Producer-3 start producing
  Consumer starts consuming


Time 100ms - Producers Start:
  Producer-1: queue.put(Log-1) → ✅ Success (Queue: [Log-1])
  Producer-2: queue.put(Log-2) → ✅ Success (Queue: [Log-1, Log-2])
  Producer-3: queue.put(Log-3) → ✅ Success (Queue: [Log-1, Log-2, Log-3])


Time 150ms - Consumer Starts:
  Consumer: queue.take() → ✅ Got Log-1 (Queue: [Log-2, Log-3])
  Consumer: Writing to file (150ms)...


Time 200ms - More Production:
  Producer-1: queue.put(Log-4) → ✅ Success (Queue: [Log-2, Log-3, Log-4])
  Producer-2: queue.put(Log-5) → ✅ Success (Queue: [Log-2, Log-3, Log-4, Log-5])
  Producer-3: queue.put(Log-6) → ✅ Success (Queue: [Log-2, Log-3, Log-4, Log-5, Log-6])
                                         ↑ FULL (capacity=5)

Time 250ms - Queue Full:
  Producer-1: queue.put(Log-7) → ⏳ BLOCKING (queue full!)

  Consumer: Writing Log-1 complete
  Consumer: queue.take() → ✅ Got Log-2 (Queue: [Log-3, Log-4, Log-5, Log-6])
                                                ↑ Space available!

  Producer-1: queue.put(Log-7) → ✅ Unblocked! (Queue: [Log-3, Log-4, Log-5, Log-6, Log-7])


Flow continues:
  Producers: put() → blocks when full
  Consumer:  take() → blocks when empty
  Auto-synchronized, no explicit locks needed!


Poison Pill Shutdown:
  All producers done
  Main: queue.put(POISON_PILL)
  Consumer: take() → Got POISON_PILL → Shutdown gracefully
```

---

## 🔍 Demo: Producer-Consumer Log Writer

**Scenario**: 3 producers generating logs, 1 consumer writing to file (Queue capacity: 5)

### Thread Flow

```
Producer-Consumer with BlockingQueue

PRODUCERS (3 threads) ──────────────────┐
                                        │
  Producer-1 ──┐                       │
  Producer-2 ──┼─→ put(LogEntry) ──────┼──→ BlockingQueue (Capacity: 5)
  Producer-3 ──┘                       │
                                        │
  - put() adds to queue                │
  - BLOCKS if queue is full (5 items)  │
                                        │
                                        └──→ ┌─────────────────┐
                                             │  Bounded Queue  │
                                             │  [□ □ □ □ □]    │
                                             └─────────────────┘
                                                      ↓
                                                take() (blocking if empty)
                                                      ↓
CONSUMER (1 thread) ────────────────────────────────────────────┐
                                                                 │
  FileWriter ──→ take() → Process LogEntry → write to file     │
                                                                 │
  - take() removes from queue                                   │
  - BLOCKS if queue is empty                                    │
  - Poison pill signals shutdown                                │
                                                                 │
└─────────────────────────────────────────────────────────────────┘

Backpressure Handling:
- Queue full (5 items) → Producers block (can't add more)
- Queue empty → Consumer blocks (waits for data)
- Automatic flow control without explicit synchronization!
```

### Code Pattern

**1. Create BlockingQueue**:
```java
BlockingQueue<LogEntry> queue = new ArrayBlockingQueue<>(5); // Bounded capacity
```

**2. Producer: Put Items (Blocking)**:
```java
public void produce() {
    try {
        LogEntry log = createLog();
        queue.put(log); // Blocks if queue is full
        log.info("Produced: {}", log);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

**3. Consumer: Take Items (Blocking)**:
```java
public void consume() {
    try {
        while (true) {
            LogEntry log = queue.take(); // Blocks if queue is empty

            if (log.isPoison()) {
                break; // Shutdown signal
            }

            processLog(log);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

**4. Graceful Shutdown (Poison Pill)**:
```java
// After all producers finish
queue.put(LogEntry.poison()); // Signal consumer to stop
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.blockingqueue.Main"
```

### Expected Output
```
=== BlockingQueue Demo: Producer-Consumer Log Writer ===
Scenario: 3 producers generating logs, 1 consumer writing to file (Queue capacity: 5)

[main] All producers and consumer started

[pool-1-thread-1] Consumer 'FileWriter' started
[pool-1-thread-1] Consumer 'FileWriter' waiting for log entry (Queue size: 0)

[pool-1-thread-2] Producer 'Producer-1' started (Queue capacity: 5, Current size: 0)
[pool-1-thread-2] Producer 'Producer-1' putting log 1/4 into queue (Queue size: 0)
[pool-1-thread-2] Producer 'Producer-1' successfully put log 1/4 (Queue size: 1)

[pool-1-thread-1] Consumer 'FileWriter' took log entry: [2026-01-01...] INFO - Producer-1: Log message 1 from Producer-1 (Queue size: 0)
[pool-1-thread-1] Consumer 'FileWriter' wrote to file: ...

[pool-1-thread-3] Producer 'Producer-2' putting log 1/4 into queue (Queue size: 1)
[pool-1-thread-4] Producer 'Producer-3' putting log 1/4 into queue (Queue size: 2)

... (Queue fills up to capacity 5) ...

[pool-1-thread-2] Producer 'Producer-1' putting log 4/4 into queue (Queue size: 5)
⏳ BLOCKED! Queue is full, waiting for consumer...

[pool-1-thread-1] Consumer 'FileWriter' took log entry: ...
✅ Producer-1 unblocked! Queue has space now.

[main] Sending poison pill to consumer
[pool-1-thread-1] Consumer 'FileWriter' received poison pill. Shutting down.
[pool-1-thread-1] Consumer 'FileWriter' finished. Total logs written: 12

=== BlockingQueue Demo Completed ===
```

---

## 🔑 Key Methods

### Blocking Operations
```java
queue.put(item);     // Add item, BLOCK if full
E item = queue.take(); // Remove item, BLOCK if empty
```

### Non-Blocking Operations
```java
boolean added = queue.offer(item);  // Add, return false if full
E item = queue.poll();              // Remove, return null if empty
```

### Timed Operations
```java
boolean added = queue.offer(item, 5, TimeUnit.SECONDS); // Block up to 5s
E item = queue.poll(5, TimeUnit.SECONDS);               // Block up to 5s
```

### Information
```java
int size = queue.size();              // Current size
int remaining = queue.remainingCapacity(); // Space left
boolean empty = queue.isEmpty();
```

---

## 🎯 Real-World Use Cases

### 1. Task Processing System
```java
public class TaskProcessor {
    private final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(100);

    // Producer thread
    public void submitTask(Task task) {
        taskQueue.offer(task, 10, TimeUnit.SECONDS);
    }

    // Consumer thread
    public void processTasksWorker() {
        while (running) {
            Task task = taskQueue.take();
            processTask(task);
        }
    }
}
```

### 2. Event Bus
```java
public class EventBus {
    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();

    public void publish(Event event) {
        eventQueue.put(event);
    }

    public void subscribe() {
        executor.submit(() -> {
            while (true) {
                Event event = eventQueue.take();
                handleEvent(event);
            }
        });
    }
}
```

---

## 🎓 Best Practices

✅ **DO:**
- Use bounded queues to prevent memory issues
- Use poison pill pattern for graceful shutdown
- Handle InterruptedException properly

❌ **DON'T:**
- Use unbounded queues in production without limits
- Forget to handle interruption
- Block indefinitely without timeout

---

## 📊 BlockingQueue Implementations

| Implementation | Capacity | Ordering | Use Case |
|----------------|----------|----------|----------|
| **ArrayBlockingQueue** | Bounded | FIFO | Fixed capacity, backpressure |
| **LinkedBlockingQueue** | Optionally bounded | FIFO | High throughput |
| **PriorityBlockingQueue** | Unbounded | Priority | Priority-based processing |
| **SynchronousQueue** | 0 (direct handoff) | N/A | Immediate handoff |
| **DelayQueue** | Unbounded | Delay | Scheduled tasks |

---

**Package**: `com.shan.concurrency.threadspatterns.blockingqueue`

**Pattern Type**: Queue - Producer-Consumer
**Thread Flow**: Producers → Queue → Consumers with automatic blocking
**Best For**: Task queues, event handling, log aggregation, async processing
