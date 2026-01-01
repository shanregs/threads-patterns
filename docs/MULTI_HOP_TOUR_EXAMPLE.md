# Multi-Hop City Tour Example - CyclicBarrier

This example demonstrates how CyclicBarrier coordinates multiple tourists visiting multiple tour spots where all tourists must assemble before proceeding with group activities.

## Overview

**Scenario**: 5 tourists visit 4 city attractions, synchronizing at each spot for group activities

**Concurrency Pattern**: CyclicBarrier

**Real-world Use Case**: Guided tours, multi-phase workflows, coordinated batch processing

## Key Components

### 1. Tourist (Thread)
Each tourist is a separate thread that:
- Travels to tour spots at different speeds (random travel times)
- Explores each spot independently
- Waits at the barrier for other tourists
- Participates in group activities when all arrive

### 2. Tour Spots (CyclicBarriers)
Four barriers, one for each tour spot:
1. **Historical Museum** → Group lunch at museum café
2. **Art Gallery** → Group photo session at gallery entrance
3. **Botanical Garden** → Refreshment break at garden pavilion
4. **Observation Deck** → Farewell dinner with city view

### 3. Barrier Actions
When all tourists arrive at a spot, the barrier action executes:
- Announces the spot and group activity
- Displays tourist count
- Simulates group activity (e.g., having lunch together)

## How It Works

```
Tourist 1: [Travel] → [Explore] → [Wait at Barrier] ────┐
Tourist 2: [Travel] → [Explore] → [Wait at Barrier] ────┤
Tourist 3: [Travel] → [Explore] → [Wait at Barrier] ────┼→ [All Arrived!]
Tourist 4: [Travel] → [Explore] → [Wait at Barrier] ────┤   [Group Activity]
Tourist 5: [Travel] → [Explore] → [Wait at Barrier] ────┘   [Proceed to Next]
```

## CyclicBarrier Benefits Demonstrated

1. **Synchronization Point**: All tourists wait until everyone arrives
2. **Reusability**: Same barrier pattern used for all 4 tour spots
3. **Barrier Action**: Centralized group activity execution
4. **Thread Coordination**: Handles varying speeds gracefully

## Running the Example

### Run the demo:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--demo=multihoptour
```

### Run all CyclicBarrier demos:
```bash
# Matrix processing example
mvn spring-boot:run -Dspring-boot.run.arguments=--demo=cyclicbarrier

# Multi-hop tour example
mvn spring-boot:run -Dspring-boot.run.arguments=--demo=multihoptour
```

## Sample Output

```
=== CyclicBarrier Demo: Multi-Hop City Tour ===
Scenario: 5 tourists visiting 4 attractions
Tour route: Historical Museum → Art Gallery → Botanical Garden → Observation Deck

[pool-2-thread-1] Alice started the tour!
[pool-2-thread-2] Bob started the tour!
...
[pool-2-thread-1] Alice traveling to 'Historical Museum'...
[pool-2-thread-1] Alice exploring 'Historical Museum'...
[pool-2-thread-1] Alice arrived at 'Historical Museum' and waiting for others... (Waiting: 1/5)
...
[pool-2-thread-5] Eve arrived at 'Historical Museum' and waiting for others... (Waiting: 5/5)

╔════════════════════════════════════════════════════════════════╗
║ 🎯 SPOT 1/4: Historical Museum
║ 🍽️  GROUP ACTIVITY: Having group lunch at museum café
║ ✅ All 5 tourists are present!
╚════════════════════════════════════════════════════════════════╝

[pool-2-thread-1] Alice - All tourists assembled! Proceeding with group activity.
...
```

## Key Learning Points

### When to Use CyclicBarrier

✅ **Use CyclicBarrier when:**
- Multiple threads need to reach a common synchronization point
- The barrier needs to be reused multiple times (cyclic)
- You want to execute an action when all threads arrive
- Threads should proceed together after synchronization

❌ **Don't use CyclicBarrier when:**
- You only need one-time synchronization (use CountDownLatch)
- Different threads can have different phase counts (use Phaser)
- Synchronization point count is not fixed

### Code Highlights

**Creating barriers with actions:**
```java
Runnable barrierAction = () -> {
    log.info("All tourists assembled! Having group lunch...");
};
CyclicBarrier barrier = new CyclicBarrier(numberOfTourists, barrierAction);
```

**Waiting at barriers:**
```java
barrier.await(); // Blocks until all tourists arrive
```

**Reusability:**
```java
for (String spot : tourSpots) {
    visitSpot(spot);
    barrier.await(); // Same barrier, multiple uses
}
```

## Related Examples

- **CyclicBarrierDemo**: Matrix row processing (simpler example)
- **PhaserDemo**: Multi-phase game with dynamic participants
- **CountDownLatchDemo**: One-time batch coordination

## Real-World Applications

1. **Batch Processing**: Process data chunks in parallel, sync before merge phase
2. **Simulation Systems**: Synchronize simulation steps across multiple agents
3. **Distributed Computing**: Coordinate map-reduce operations
4. **Game Development**: Synchronize player actions between game rounds
5. **Testing**: Coordinate test threads to stress-test race conditions

---

**Pattern**: CyclicBarrier | **Category**: Synchronization | **Complexity**: Medium
