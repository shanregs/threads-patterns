# Exchanger Pattern - Bidirectional Data Exchange

This package demonstrates Exchanger for synchronizing and exchanging data between exactly two threads.

## 🎯 What Is Exchanger?

**Exchanger** is a synchronization point where two threads can exchange objects. Each thread calls `exchange()` and blocks until another thread arrives. When both threads have called `exchange()`, the objects are swapped and both threads continue.

**Key Characteristics**:
- ✅ **Pair-wise exchange**: Works with exactly 2 threads
- ✅ **Bidirectional**: Both threads send and receive data
- ✅ **Synchronization point**: Threads wait for each other
- ✅ **Type-safe**: Uses generics for compile-time type safety
- ✅ **Simple API**: Just one method - `exchange(V data)`

**Use Cases**:
- **Trading systems**: Exchange orders between two traders
- **Pipeline stages**: Exchange buffers between producer and consumer
- **Genetic algorithms**: Crossover operations between two genomes
- **Buffer swapping**: Double buffering in graphics or I/O

---

## 📊 Thread Flow Pattern: Bidirectional Exchange

### Pattern: Two Threads Meet and Swap Data

```
Time 0ms - Setup:
  Exchanger<TradeOrder> exchanger = new Exchanger<>();

  Trader-A has: TradeOrder(AAPL, 100 shares, $150.50)
  Trader-B has: TradeOrder(GOOGL, 50 shares, $2800.75)

  Both traders submitted to thread pool...


Time 100ms - Trader-A arrives first:
  Trader-A: exchanger.exchange(myOrder) → ⏳ BLOCKING (waiting for Trader-B)

  [Trader-A is waiting at exchange point...]


Time 800ms - Trader-B arrives:
  Trader-B: exchanger.exchange(myOrder) → ⏳ Both threads at exchange point!

  ┌─────────────────── EXCHANGE HAPPENS ───────────────────┐
  │                                                         │
  │  Trader-A gives: TradeOrder(AAPL, 100, $150.50)        │
  │           ↓↓↓↓↓↓↓↓   SWAP   ↑↑↑↑↑↑↑↑                  │
  │  Trader-B gives: TradeOrder(GOOGL, 50, $2800.75)       │
  │                                                         │
  └─────────────────────────────────────────────────────────┘


Time 801ms - Exchange complete:
  Trader-A: exchange() returns → ✅ Received TradeOrder(GOOGL, 50, $2800.75)
  Trader-B: exchange() returns → ✅ Received TradeOrder(AAPL, 100, $150.50)

  Both traders continue executing (with swapped data)


Time 802ms - Process received orders:
  Trader-A: Processing received order (GOOGL)...
  Trader-B: Processing received order (AAPL)...

  Both threads complete independently
```

---

## 🔍 Demo: Trade Exchange Between Two Traders

**Scenario**: Trader-A and Trader-B exchange trade orders

### Thread Flow

```
Two Traders Exchange Orders at Synchronization Point

┌────────────── TRADER-A (Thread-1) ──────────────┐
│                                                  │
│  1. Prepare order: AAPL, 100, $150.50          │
│  2. Sleep 500-1500ms (preparation time)         │
│  3. Call exchanger.exchange(myOrder)            │
│     → BLOCKING... waiting for Trader-B          │
│                                                  │
└──────────────────────────────────────────────────┘
                      ↓
                      ↓ (waiting...)
                      ↓
┌──────────────── EXCHANGE POINT ─────────────────┐
│                                                  │
│         Both threads arrive here                │
│                                                  │
│    Trader-A ←──── DATA SWAP ────→ Trader-B     │
│                                                  │
│  Sends: AAPL order                              │
│  Receives: GOOGL order                          │
│                                                  │
└──────────────────────────────────────────────────┘
                      ↑
                      ↑ (arrives later)
                      ↑
┌────────────── TRADER-B (Thread-2) ──────────────┐
│                                                  │
│  1. Prepare order: GOOGL, 50, $2800.75         │
│  2. Sleep 500-1500ms (preparation time)         │
│  3. Call exchanger.exchange(myOrder)            │
│     → MEETS Trader-A at exchange point          │
│                                                  │
└──────────────────────────────────────────────────┘
                      ↓
            Exchange completes!
                      ↓
┌──────────────── POST-EXCHANGE ──────────────────┐
│                                                  │
│  Trader-A: Process GOOGL order (received)       │
│  Trader-B: Process AAPL order (received)        │
│                                                  │
│  Both threads continue independently            │
│                                                  │
└──────────────────────────────────────────────────┘
```

### Code Pattern

**1. Create Exchanger**:
```java
Exchanger<TradeOrder> exchanger = new Exchanger<>();
```

**2. Thread 1: Exchange Data**:
```java
public void run() {
    try {
        TradeOrder myOrder = new TradeOrder("Trader-A", "AAPL", 100, 150.50);

        // Wait for other thread and exchange
        TradeOrder receivedOrder = exchanger.exchange(myOrder);

        // Process received data
        processOrder(receivedOrder);

    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

**3. Thread 2: Exchange Data**:
```java
public void run() {
    try {
        TradeOrder myOrder = new TradeOrder("Trader-B", "GOOGL", 50, 2800.75);

        // Wait for other thread and exchange
        TradeOrder receivedOrder = exchanger.exchange(myOrder);

        // Process received data
        processOrder(receivedOrder);

    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.exchanger.Main"
```

### Expected Output
```
=== Exchanger Demo: Trade Exchange ===
Scenario: Two traders exchanging trade orders

[main] Both traders submitted

[pool-1-thread-1] Trader 'Trader-A' preparing to exchange order: TradeOrder{trader='Trader-A', asset='STOCK-AAPL', qty=100, price=$150.50}
[pool-1-thread-2] Trader 'Trader-B' preparing to exchange order: TradeOrder{trader='Trader-B', asset='STOCK-GOOGL', qty=50, price=$2800.75}

[pool-1-thread-1] Trader 'Trader-A' ready to exchange at exchange point...
[pool-1-thread-1] ⏳ Waiting for other trader...

[pool-1-thread-2] Trader 'Trader-B' ready to exchange at exchange point...
[pool-1-thread-2] ⏳ Both traders arrived!

[pool-1-thread-1] Trader 'Trader-A' EXCHANGED!
  Sent: TradeOrder{trader='Trader-A', asset='STOCK-AAPL', qty=100, price=$150.50}
  Received: TradeOrder{trader='Trader-B', asset='STOCK-GOOGL', qty=50, price=$2800.75}

[pool-1-thread-2] Trader 'Trader-B' EXCHANGED!
  Sent: TradeOrder{trader='Trader-B', asset='STOCK-GOOGL', qty=50, price=$2800.75}
  Received: TradeOrder{trader='Trader-A', asset='STOCK-AAPL', qty=100, price=$150.50}

[pool-1-thread-1] Trader 'Trader-A' processing received order: TradeOrder{trader='Trader-B', asset='STOCK-GOOGL', qty=50, price=$2800.75}
[pool-1-thread-2] Trader 'Trader-B' processing received order: TradeOrder{trader='Trader-A', asset='STOCK-AAPL', qty=100, price=$150.50}

[pool-1-thread-1] Trader 'Trader-A' completed processing received order
[pool-1-thread-2] Trader 'Trader-B' completed processing received order

=== Exchanger Demo Completed ===
```

---

## 🔑 Key Methods

### Exchanger Creation
```java
Exchanger<V> exchanger = new Exchanger<>(); // V is the type to exchange
```

### Exchange Data (Blocking)
```java
V receivedData = exchanger.exchange(myData); // Blocks until pair arrives
```

### Exchange with Timeout
```java
try {
    V receivedData = exchanger.exchange(myData, 5, TimeUnit.SECONDS);
    // Successfully exchanged
} catch (TimeoutException e) {
    // No partner arrived within 5 seconds
    log.warn("Exchange timed out");
}
```

---

## 🎯 Real-World Use Cases

### 1. Producer-Consumer Buffer Swap
```java
public class BufferSwapDemo {
    private final Exchanger<List<Data>> exchanger = new Exchanger<>();

    // Producer thread
    public void produce() {
        List<Data> fillBuffer = new ArrayList<>(CAPACITY);
        while (running) {
            // Fill buffer with data
            for (int i = 0; i < CAPACITY; i++) {
                fillBuffer.add(produceData());
            }

            try {
                // Exchange full buffer for empty buffer
                fillBuffer = exchanger.exchange(fillBuffer);
                // Now have empty buffer to fill again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Consumer thread
    public void consume() {
        List<Data> emptyBuffer = new ArrayList<>(CAPACITY);
        while (running) {
            try {
                // Exchange empty buffer for full buffer
                List<Data> fullBuffer = exchanger.exchange(emptyBuffer);

                // Process full buffer
                fullBuffer.forEach(this::processData);

                // Clear buffer for next exchange
                fullBuffer.clear();
                emptyBuffer = fullBuffer;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

### 2. Genetic Algorithm Crossover
```java
public class GeneticAlgorithm {
    private final Exchanger<Genome> exchanger = new Exchanger<>();

    public void evolve(List<Genome> population) {
        // Pair up genomes for crossover
        for (int i = 0; i < population.size(); i += 2) {
            Genome genome1 = population.get(i);
            Genome genome2 = population.get(i + 1);

            executor.submit(() -> performCrossover(genome1));
            executor.submit(() -> performCrossover(genome2));
        }
    }

    private void performCrossover(Genome myGenome) {
        try {
            // Exchange genetic material with partner
            Genome partnerGenome = exchanger.exchange(myGenome);

            // Perform crossover operation
            Genome offspring = myGenome.crossover(partnerGenome);

            // Continue with offspring
            mutate(offspring);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 3. Pipeline Stage Handoff
```java
public class PipelineStage {
    private final Exchanger<DataBatch> exchanger;

    // Stage 1: Data extraction
    public void extract() {
        DataBatch batch = new DataBatch();
        while (hasMoreData()) {
            batch.add(extractNextData());

            if (batch.isFull()) {
                try {
                    // Hand off full batch, get empty batch back
                    batch = exchanger.exchange(batch);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // Stage 2: Data transformation
    public void transform() {
        DataBatch emptyBatch = new DataBatch();
        while (running) {
            try {
                // Get full batch from extraction stage
                DataBatch fullBatch = exchanger.exchange(emptyBatch);

                // Transform data
                fullBatch.forEach(this::transformData);

                // Prepare empty batch for next exchange
                fullBatch.clear();
                emptyBatch = fullBatch;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

---

## ⚠️ Common Pitfalls

### 1. Using with More Than Two Threads
```java
// ❌ BAD: Exchanger works with exactly 2 threads
Exchanger<Data> exchanger = new Exchanger<>();
executor.submit(() -> exchanger.exchange(data1)); // Thread-1
executor.submit(() -> exchanger.exchange(data2)); // Thread-2
executor.submit(() -> exchanger.exchange(data3)); // Thread-3 ❌ Will pair with Thread-1 or Thread-2!
```

**Solution**: Use Exchanger only for thread pairs
```java
// ✅ GOOD: One exchanger per pair
Exchanger<Data> exchanger1 = new Exchanger<>(); // For Thread-1 and Thread-2
Exchanger<Data> exchanger2 = new Exchanger<>(); // For Thread-3 and Thread-4
```

### 2. Deadlock with Single Thread
```java
// ❌ BAD: Single thread calling exchange() deadlocks
Exchanger<Data> exchanger = new Exchanger<>();
Data received = exchanger.exchange(myData); // ❌ Blocks forever! No partner thread
```

**Solution**: Always have exactly two threads
```java
// ✅ GOOD: Two threads exchanging
executor.submit(() -> exchanger.exchange(data1)); // Thread-1
executor.submit(() -> exchanger.exchange(data2)); // Thread-2
```

### 3. Not Handling Timeout
```java
// ❌ BAD: No timeout, may block forever if partner fails
Data received = exchanger.exchange(myData); // Blocks indefinitely
```

**Solution**: Use timeout for reliability
```java
// ✅ GOOD: Timeout prevents indefinite blocking
try {
    Data received = exchanger.exchange(myData, 10, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    log.warn("Partner thread did not arrive within timeout");
    handleTimeout();
}
```

### 4. Reusing Exchanged Objects Unsafely
```java
// ❌ BAD: Both threads modify same object
List<Data> buffer = new ArrayList<>();
buffer.add(myData);
List<Data> receivedBuffer = exchanger.exchange(buffer);
buffer.clear(); // ❌ Modifies object that partner thread may still be using!
```

**Solution**: Create new objects or ensure thread-safety
```java
// ✅ GOOD: Create new buffer after exchange
List<Data> buffer = new ArrayList<>();
buffer.add(myData);
List<Data> receivedBuffer = exchanger.exchange(buffer);

// Create new buffer for next iteration
buffer = new ArrayList<>();
```

---

## 🎓 Best Practices

✅ **DO:**
- Use for exactly two threads
- Use timeout with `exchange(data, timeout, unit)`
- Ensure exchanged objects are thread-safe or immutable
- Handle InterruptedException properly
- Consider using for buffer swapping in producer-consumer

❌ **DON'T:**
- Use with more than two threads (use other patterns instead)
- Call exchange() from single thread (deadlock)
- Modify exchanged objects without synchronization
- Ignore timeout exceptions
- Use for complex multi-party synchronization (use CyclicBarrier or Phaser)

---

## 📊 Exchanger vs BlockingQueue vs Phaser

| Feature | Exchanger | BlockingQueue | Phaser |
|---------|-----------|---------------|--------|
| **Participants** | Exactly 2 threads | Multiple producers/consumers | Dynamic (1..N) |
| **Data Flow** | Bidirectional | Unidirectional | No data exchange |
| **Synchronization** | Pair-wise | Queue-based | Phase-based |
| **Buffer** | No buffer | Buffered (capacity) | No buffer |
| **Use Case** | Data swap | Producer-consumer | Multi-phase sync |

**When to Choose**:
- **Exchanger**: Exactly two threads need to swap data
- **BlockingQueue**: Multiple producers/consumers with buffering
- **Phaser**: Multiple threads need phase synchronization
- **CyclicBarrier**: Fixed number of threads need to synchronize repeatedly

---

## 🔗 Related Patterns

- **BlockingQueue** - Producer-consumer with buffering
- **CyclicBarrier** - Multi-thread synchronization barrier
- **Phaser** - Dynamic multi-phase synchronization
- **CompletableFuture** - Async composition without explicit threads

---

**Package**: `com.shan.concurrency.threadspatterns.exchanger`

**Pattern Type**: Synchronization - Bidirectional Data Exchange
**Thread Flow**: Pair-wise thread synchronization with data swap
**Best For**: Buffer swapping, pipeline stages, genetic algorithms, trading systems
