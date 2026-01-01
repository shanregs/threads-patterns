# Phaser Pattern - Dynamic Multi-Phase Synchronization

This package demonstrates Phaser for coordinating threads through multiple phases with dynamic participant registration.

## 🎯 What Is Phaser?

**Phaser** is a flexible synchronization barrier that supports multiple phases and dynamic participant registration/deregistration. Unlike CyclicBarrier with a fixed number of parties, Phaser allows threads to join or leave at any time.

**Key Characteristics**:
- ✅ **Multi-phase**: Supports unlimited sequential phases
- ✅ **Dynamic registration**: Threads can register/deregister dynamically
- ✅ **Flexible**: More powerful than CountDownLatch or CyclicBarrier
- ✅ **Phase callbacks**: onAdvance() method for phase transition actions
- ✅ **Termination**: Can terminate when desired phase is reached

**Use Cases**:
- **Game levels**: Multiplayer games where players complete multiple rounds
- **Multi-stage processing**: Data pipelines with multiple processing stages
- **Simulation**: Iterative simulations with varying participants
- **Dynamic workflows**: Workflows where participants join/leave mid-execution

---

## 📊 Thread Flow Pattern: Dynamic Multi-Phase Synchronization

### Pattern: All Threads Wait at Each Phase

```
Phase 0: GAME INITIALIZATION
─────────────────────────────────────────────────────────────
Phaser created with 1 party (Main thread)

Player-1: register() ──────┐ Registered: 2 parties
Player-2: register() ──────┤ Registered: 3 parties
Player-3: register() ──────┤ Registered: 4 parties
Player-4: register() ──────┘ Registered: 5 parties

Main thread: arriveAndAwaitAdvance() ⏳ Waiting...
Player-1:    arriveAndAwaitAdvance() ⏳ Waiting...
Player-2:    arriveAndAwaitAdvance() ⏳ Waiting...
Player-3:    arriveAndAwaitAdvance() ⏳ Waiting...
Player-4:    arriveAndAwaitAdvance() ✅ All arrived!

───────── onAdvance() callback ─────────
*** PHASE 0 COMPLETED *** (5 parties)
────────────────────────────────────────

Phase advances to Phase 1
All threads resume simultaneously


Phase 1: SECOND ROUND
─────────────────────────────────────────────────────────────
Main thread: arriveAndAwaitAdvance() ⏳ Waiting...
Player-1:    arriveAndAwaitAdvance() ⏳ Waiting...
Player-2:    arriveAndAwaitAdvance() ⏳ Waiting...
Player-3:    arriveAndDeregister()   ✅ Leaving game! (4 parties now)
Player-4:    arriveAndAwaitAdvance() ⏳ Waiting...

All remaining parties arrived!

───────── onAdvance() callback ─────────
*** PHASE 1 COMPLETED *** (4 parties)
────────────────────────────────────────

Phase advances to Phase 2
Remaining threads continue


Phase 2: FINAL ROUND
─────────────────────────────────────────────────────────────
Main thread: arriveAndAwaitAdvance() ⏳
Player-1:    arriveAndAwaitAdvance() ⏳
Player-2:    arriveAndAwaitAdvance() ⏳
Player-4:    arriveAndAwaitAdvance() ✅ All arrived!

───────── onAdvance() callback ─────────
*** PHASE 2 COMPLETED *** (4 parties)
onAdvance() returns true → Phaser terminates
────────────────────────────────────────
```

---

## 🔍 Demo: Multi-Phase Game Rounds

**Scenario**: 4 players playing a 3-phase game

### Thread Flow

```
Game Coordinator (Main Thread) + 4 Game Players

┌────────────────── PHASE 0 (Registration & Start) ──────────────────┐
│                                                                     │
│  Main:    register(1) → arriveAndAwaitAdvance() ⏳                 │
│  Player-1: register() → arriveAndAwaitAdvance() ⏳                 │
│  Player-2: register() → arriveAndAwaitAdvance() ⏳                 │
│  Player-3: register() → arriveAndAwaitAdvance() ⏳                 │
│  Player-4: register() → arriveAndAwaitAdvance() ✅ All 5 arrived!  │
│                                                                     │
│  → onAdvance(phase=0, parties=5) called                           │
│  → All threads resume                                              │
└─────────────────────────────────────────────────────────────────────┘
        ↓
┌────────────────── PHASE 1 (Second Round) ──────────────────────────┐
│                                                                     │
│  Main:    arriveAndAwaitAdvance() ⏳                               │
│  Player-1: arriveAndAwaitAdvance() ⏳                              │
│  Player-2: arriveAndAwaitAdvance() ⏳                              │
│  Player-3: arriveAndAwaitAdvance() ⏳                              │
│  Player-4: arriveAndAwaitAdvance() ✅ All 5 arrived!               │
│                                                                     │
│  → onAdvance(phase=1, parties=5) called                           │
│  → All threads resume                                              │
└─────────────────────────────────────────────────────────────────────┘
        ↓
┌────────────────── PHASE 2 (Final Round) ───────────────────────────┐
│                                                                     │
│  Main:    arriveAndAwaitAdvance() ⏳                               │
│  Player-1: arriveAndAwaitAdvance() ⏳                              │
│  Player-2: arriveAndAwaitAdvance() ⏳                              │
│  Player-3: arriveAndAwaitAdvance() ⏳                              │
│  Player-4: arriveAndAwaitAdvance() ✅ All 5 arrived!               │
│                                                                     │
│  → onAdvance(phase=2, parties=5) returns true (terminate)         │
│  → Phaser terminates                                               │
└─────────────────────────────────────────────────────────────────────┘
```

### Code Pattern

**1. Create Phaser with onAdvance Callback**:
```java
Phaser phaser = new Phaser(1) { // Initial party = main thread
    @Override
    protected boolean onAdvance(int phase, int registeredParties) {
        log.info("*** PHASE {} COMPLETED *** (Parties: {})", phase, registeredParties);
        return phase >= NUMBER_OF_PHASES - 1 || registeredParties == 0; // Terminate?
    }
};
```

**2. Dynamic Registration**:
```java
// Thread joins the phaser
phaser.register(); // Increments party count
```

**3. Wait at Phase Barrier**:
```java
// Thread waits for all parties to arrive
phaser.arriveAndAwaitAdvance(); // Blocks until all parties arrive
```

**4. Leave Phaser**:
```java
// Thread leaves the phaser
phaser.arriveAndDeregister(); // Decrements party count and arrives
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.phaser.Main"
```

### Expected Output
```
=== Phaser Demo: Multi-Phase Game Rounds ===
Scenario: 4 players playing a 3-phase game

[pool-1-thread-1] Player-1 registered for game (Phase: 0)
[pool-1-thread-2] Player-2 registered for game (Phase: 0)
[pool-1-thread-3] Player-3 registered for game (Phase: 0)
[pool-1-thread-4] Player-4 registered for game (Phase: 0)

[main] All players created. Game starting...

[pool-1-thread-1] Player-1 playing phase 0...
[pool-1-thread-2] Player-2 playing phase 0...
[pool-1-thread-3] Player-3 playing phase 0...
[pool-1-thread-4] Player-4 playing phase 0...

[pool-1-thread-4] *** PHASE 0 COMPLETED *** (Parties: 5)
[main] Main thread: Phase 0 synchronized

[pool-1-thread-1] Player-1 playing phase 1...
[pool-1-thread-2] Player-2 playing phase 1...
[pool-1-thread-3] Player-3 playing phase 1...
[pool-1-thread-4] Player-4 playing phase 1...

[pool-1-thread-4] *** PHASE 1 COMPLETED *** (Parties: 5)
[main] Main thread: Phase 1 synchronized

[pool-1-thread-1] Player-1 playing phase 2...
[pool-1-thread-2] Player-2 playing phase 2...
[pool-1-thread-3] Player-3 playing phase 2...
[pool-1-thread-4] Player-4 playing phase 2...

[pool-1-thread-4] *** PHASE 2 COMPLETED *** (Parties: 5)
[main] Main thread: Phase 2 synchronized

[main] Game coordinator finished
=== Phaser Demo Completed ===
```

---

## 🔑 Key Methods

### Phaser Creation
```java
Phaser phaser = new Phaser();           // Empty phaser
Phaser phaser = new Phaser(3);          // 3 initial parties
Phaser phaser = new Phaser(parentPhaser); // Hierarchical phaser
```

### Registration
```java
phaser.register();           // Add 1 party
phaser.bulkRegister(5);      // Add 5 parties
int parties = phaser.getRegisteredParties(); // Get party count
```

### Arrival and Waiting
```java
phaser.arriveAndAwaitAdvance();  // Arrive and wait for others
phaser.arriveAndDeregister();    // Arrive and leave phaser
phaser.arrive();                 // Arrive without waiting
```

### Phase Information
```java
int phase = phaser.getPhase();            // Current phase number
int arrived = phaser.getArrivedParties(); // Parties that have arrived
int unarrived = phaser.getUnarrivedParties(); // Parties not yet arrived
```

### Phase Control
```java
@Override
protected boolean onAdvance(int phase, int registeredParties) {
    // Called when all parties arrive
    // Return true to terminate phaser, false to continue
    return phase >= maxPhases || registeredParties == 0;
}
```

---

## 🎯 Real-World Use Cases

### 1. Iterative Map-Reduce
```java
public class MapReducePhaser {
    private final Phaser phaser = new Phaser(1); // Main coordinator

    public void processDataset(List<Data> dataset, int iterations) {
        // Register workers
        for (Worker worker : workers) {
            phaser.register();
            executor.submit(() -> {
                for (int i = 0; i < iterations; i++) {
                    worker.map(dataset);      // Map phase
                    phaser.arriveAndAwaitAdvance(); // Wait for all mappers

                    worker.reduce();           // Reduce phase
                    phaser.arriveAndAwaitAdvance(); // Wait for all reducers
                }
                phaser.arriveAndDeregister();
            });
        }

        // Coordinator synchronizes each phase
        for (int i = 0; i < iterations * 2; i++) {
            phaser.arriveAndAwaitAdvance();
        }
    }
}
```

### 2. Simulation with Dynamic Actors
```java
public class SimulationPhaser {
    private final Phaser phaser = new Phaser(1);

    public void runSimulation(int timeSteps) {
        for (int t = 0; t < timeSteps; t++) {
            // Some actors may join or leave each time step
            if (shouldSpawnActor()) {
                phaser.register();
                executor.submit(new Actor(phaser));
            }

            phaser.arriveAndAwaitAdvance(); // Wait for all actors

            // Process time step results
            processTimeStep(t);
        }
        phaser.arriveAndDeregister();
    }
}

class Actor implements Runnable {
    private final Phaser phaser;

    public void run() {
        while (!done()) {
            act();
            if (shouldLeave()) {
                phaser.arriveAndDeregister(); // Leave simulation
                break;
            } else {
                phaser.arriveAndAwaitAdvance(); // Continue to next time step
            }
        }
    }
}
```

### 3. Multi-Stage Data Pipeline
```java
public class DataPipeline {
    private final Phaser phaser = new Phaser();

    public void processBatch(List<Data> batch) {
        phaser.bulkRegister(batch.size());

        for (Data data : batch) {
            executor.submit(() -> {
                // Stage 1: Extract
                Data extracted = extract(data);
                phaser.arriveAndAwaitAdvance();

                // Stage 2: Transform
                Data transformed = transform(extracted);
                phaser.arriveAndAwaitAdvance();

                // Stage 3: Load
                load(transformed);
                phaser.arriveAndDeregister();
            });
        }
    }
}
```

---

## ⚠️ Common Pitfalls

### 1. Forgetting to Register
```java
// ❌ BAD: Thread not registered
Phaser phaser = new Phaser(1);
executor.submit(() -> {
    phaser.arriveAndAwaitAdvance(); // IllegalStateException! Not registered
});
```

**Solution**: Always register before arriving
```java
// ✅ GOOD: Register first
Phaser phaser = new Phaser(1);
executor.submit(() -> {
    phaser.register(); // Register this thread
    phaser.arriveAndAwaitAdvance();
});
```

### 2. Unbalanced Registration/Deregistration
```java
// ❌ BAD: Deregister without arriving
phaser.register();
// ... do work ...
phaser.arriveAndDeregister(); // First arrival
phaser.arriveAndDeregister(); // ❌ Second deregister without registration!
```

**Solution**: Match registrations with deregistrations
```java
// ✅ GOOD: Balanced register/deregister
phaser.register();
try {
    for (int i = 0; i < phases; i++) {
        doWork();
        phaser.arriveAndAwaitAdvance();
    }
} finally {
    phaser.arriveAndDeregister(); // Only once
}
```

### 3. Incorrect Termination Logic
```java
// ❌ BAD: Phaser never terminates
Phaser phaser = new Phaser(1) {
    @Override
    protected boolean onAdvance(int phase, int parties) {
        return false; // Never terminates!
    }
};
```

**Solution**: Return true when done
```java
// ✅ GOOD: Terminate after N phases
Phaser phaser = new Phaser(1) {
    @Override
    protected boolean onAdvance(int phase, int parties) {
        return phase >= MAX_PHASES || parties == 0;
    }
};
```

---

## 🎓 Best Practices

✅ **DO:**
- Use for multi-phase algorithms with varying participants
- Register threads before they start participating
- Use onAdvance() for phase transition logic
- Check phase termination conditions
- Use arriveAndDeregister() when thread is done

❌ **DON'T:**
- Use for simple one-time synchronization (use CountDownLatch)
- Use for fixed parties with simple reusable barrier (use CyclicBarrier)
- Forget to call arriveAndDeregister() when thread completes
- Mix arrive(), arriveAndAwaitAdvance(), and arriveAndDeregister() carelessly

---

## 📊 Phaser vs CountDownLatch vs CyclicBarrier

| Feature | Phaser | CountDownLatch | CyclicBarrier |
|---------|--------|----------------|---------------|
| **Reusable** | ✅ Yes (multi-phase) | ❌ No (one-time) | ✅ Yes (cyclic) |
| **Dynamic Parties** | ✅ Yes | ❌ No | ❌ No |
| **Phase Callback** | ✅ onAdvance() | ❌ No | ✅ barrierAction |
| **Termination** | ✅ Controllable | ✅ Auto (count=0) | ❌ Never |
| **Complexity** | 🟡 High | 🟢 Low | 🟢 Medium |
| **Use Case** | Multi-phase, dynamic | One-time wait | Fixed parties, reusable |

**When to Choose**:
- **Phaser**: Multiple phases + dynamic participants + complex control
- **CountDownLatch**: One-time wait for multiple tasks to complete
- **CyclicBarrier**: Fixed parties + reusable barrier + simple callback

---

## 🔗 Related Patterns

- **CountDownLatch** - One-time synchronization point
- **CyclicBarrier** - Fixed-party reusable barrier
- **Semaphore** - Limit concurrent access to resources
- **CompletableFuture.allOf()** - Async variant for future completion

---

**Package**: `com.shan.concurrency.threadspatterns.phaser`

**Pattern Type**: Synchronization - Multi-Phase Coordination
**Thread Flow**: Multiple phases with dynamic registration
**Best For**: Iterative algorithms, simulations, multi-stage processing with varying participants
