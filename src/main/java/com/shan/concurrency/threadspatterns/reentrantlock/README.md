# ReentrantLock Pattern - Advanced Explicit Locking

This package demonstrates ReentrantLock for advanced thread synchronization with explicit lock control.

## 🎯 What Is ReentrantLock?

**ReentrantLock** is an explicit lock implementation that provides more flexibility than synchronized blocks. It supports advanced features like fairness, tryLock, interruptible locking, and condition variables.

**Key Characteristics**:
- ✅ **Explicit control**: Manual lock() and unlock()
- ✅ **Reentrant**: Same thread can acquire lock multiple times
- ✅ **Fairness**: Optional FIFO ordering (prevents starvation)
- ✅ **Interruptible**: lockInterruptibly() respects interrupts
- ✅ **Try-lock**: Non-blocking acquisition with tryLock()
- ✅ **Condition variables**: Multiple wait/signal conditions

**Use Cases**:
- **Bank transactions**: Atomic operations with fairness
- **Resource management**: Complex locking scenarios
- **Timed locking**: Timeout-based lock acquisition
- **Interruptible operations**: Cancellable lock waits
- **Multiple conditions**: Producer-consumer with separate conditions

---

## 📊 Thread Flow Pattern: Exclusive Access with Fairness

### Pattern: Mutual Exclusion with Optional FIFO Ordering

```
Bank Account with ReentrantLock (Fair Mode)

Initial: balance = $1000, lock is free

Time 0ms - Multiple Transactions Submitted:
  TX-1 (Deposit $150):  Submitted
  TX-2 (Withdraw $200): Submitted
  TX-3 (Deposit $250):  Submitted
  TX-4 (Withdraw $300): Submitted


Time 10ms - TX-1 arrives first:
  TX-1: lock.lock() → ✅ ACQUIRED (balance: $1000)
        Processing deposit $150...
        Sleep 200ms...

  TX-2: lock.lock() → ⏳ WAITING (added to wait queue)
  TX-3: lock.lock() → ⏳ WAITING (added to wait queue after TX-2)
  TX-4: lock.lock() → ⏳ WAITING (added to wait queue after TX-3)

  Wait Queue (FIFO): [TX-2, TX-3, TX-4]


Time 210ms - TX-1 completes:
  TX-1: balance += $150 → balance = $1150
        lock.unlock() → ✅ Released

  Fair mode: TX-2 (first in queue) gets lock next!
  TX-2: lock.lock() → ✅ ACQUIRED (balance: $1150)
        Processing withdraw $200...
        Sleep 200ms...

  Wait Queue (FIFO): [TX-3, TX-4]


Time 410ms - TX-2 completes:
  TX-2: balance -= $200 → balance = $950
        lock.unlock() → ✅ Released

  TX-3: lock.lock() → ✅ ACQUIRED (balance: $950)
        Processing deposit $250...
        Sleep 200ms...

  Wait Queue (FIFO): [TX-4]


Time 610ms - TX-3 completes:
  TX-3: balance += $250 → balance = $1200
        lock.unlock() → ✅ Released

  TX-4: lock.lock() → ✅ ACQUIRED (balance: $1200)
        Processing withdraw $300...
        Sleep 200ms...

  Wait Queue: []


Time 810ms - TX-4 completes:
  TX-4: balance -= $300 → balance = $900
        lock.unlock() → ✅ Released

Final balance: $900
All transactions processed in fair order!
```

---

## 🔍 Demo: Thread-Safe Bank Account

**Scenario**: 8 concurrent transactions (deposits and withdrawals) on shared account

### Thread Flow

```
Bank Account with ReentrantLock

┌──────────────── FAIR MODE = FALSE (Default) ────────────────┐
│                                                              │
│  Thread-1: TX-1 (Withdraw) → lock() ✅ ACQUIRED             │
│                                                              │
│  Thread-2: TX-2 (Deposit)  → lock() ⏳ WAITING              │
│  Thread-3: TX-3 (Withdraw) → lock() ⏳ WAITING              │
│  Thread-4: TX-4 (Deposit)  → lock() ⏳ WAITING              │
│                                                              │
│  ❌ Unfair: Any waiting thread might acquire lock next      │
│     (better performance, possible starvation)                │
│                                                              │
└──────────────────────────────────────────────────────────────┘

┌──────────────── FAIR MODE = TRUE ───────────────────────────┐
│                                                              │
│  Thread-1: TX-1 (Withdraw) → lock() ✅ ACQUIRED             │
│            Modify balance...                                 │
│            unlock() → Released                               │
│                                                              │
│  Wait Queue (FIFO): [TX-2, TX-3, TX-4]                      │
│                                                              │
│  ✅ Fair: TX-2 (first in queue) acquires lock next          │
│     (guaranteed FIFO order, prevents starvation)             │
│                                                              │
│  Thread-2: TX-2 (Deposit) → lock() ✅ ACQUIRED (was first)  │
│            Modify balance...                                 │
│            unlock() → Released                               │
│                                                              │
│  Thread-3: TX-3 (Withdraw) → lock() ✅ ACQUIRED (was second)│
│            ...                                               │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### Code Pattern

**1. Create ReentrantLock**:
```java
private final Lock lock = new ReentrantLock();        // Unfair (default, faster)
private final Lock lock = new ReentrantLock(true);    // Fair (FIFO, prevents starvation)
```

**2. Basic Lock/Unlock Pattern**:
```java
public void deposit(double amount) {
    lock.lock(); // Acquire lock (blocking)
    try {
        // Critical section - only one thread executes this
        balance += amount;
        log.info("Deposited: {} New balance: {}", amount, balance);

    } finally {
        lock.unlock(); // ALWAYS unlock in finally
    }
}
```

**3. Try-Lock Pattern (Non-Blocking)**:
```java
public boolean tryDeposit(double amount) {
    if (lock.tryLock()) { // Try to acquire without blocking
        try {
            balance += amount;
            return true;
        } finally {
            lock.unlock();
        }
    } else {
        log.warn("Could not acquire lock, operation skipped");
        return false;
    }
}
```

**4. Try-Lock with Timeout**:
```java
public boolean depositWithTimeout(double amount) throws InterruptedException {
    if (lock.tryLock(5, TimeUnit.SECONDS)) { // Wait up to 5 seconds
        try {
            balance += amount;
            return true;
        } finally {
            lock.unlock();
        }
    } else {
        log.warn("Lock acquisition timed out");
        return false;
    }
}
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.reentrantlock.Main"
```

### Expected Output
```
=== ReentrantLock Demo: Thread-Safe Bank Account ===

--- Testing with Fairness: false ---
BankAccount 'ACC-001' created with balance $1000.0 (Fair mode: false)

[pool-1-thread-1] Transaction TX-1 started: WITHDRAW $150.0
[pool-1-thread-1] TX-1 LOCKED | Withdrawing $150.0 (Current: $1000.0) | Waiting threads: 0
[pool-1-thread-2] Transaction TX-2 started: DEPOSIT $200.0
[pool-1-thread-2] TX-2 WAITING for lock... (Waiting threads: 1)

[pool-1-thread-1] TX-1 COMPLETED | New balance: $850.0
[pool-1-thread-1] TX-1 UNLOCKED

[pool-1-thread-2] TX-2 LOCKED | Depositing $200.0 (Current: $850.0) | Waiting threads: 0
[pool-1-thread-3] Transaction TX-3 started: WITHDRAW $250.0
[pool-1-thread-3] TX-3 WAITING for lock...

[pool-1-thread-2] TX-2 COMPLETED | New balance: $1050.0 | Hold count: 1
[pool-1-thread-2] TX-2 UNLOCKED

...

Final balance: $1250.0
=== Fairness false Test Completed ===


--- Testing with Fairness: true ---
BankAccount 'ACC-001' created with balance $1000.0 (Fair mode: true)

[pool-1-thread-1] Transaction TX-1 started: WITHDRAW $150.0
[pool-1-thread-1] TX-1 LOCKED | Withdrawing $150.0 (Current: $1000.0) | Waiting threads: 0
[pool-1-thread-2] Transaction TX-2 started: DEPOSIT $200.0
[pool-1-thread-2] TX-2 WAITING for lock... (FIFO order guaranteed)

[pool-1-thread-1] TX-1 COMPLETED | New balance: $850.0
[pool-1-thread-1] TX-1 UNLOCKED

[pool-1-thread-2] TX-2 LOCKED (fair mode - was first in queue)
[pool-1-thread-2] TX-2 COMPLETED | New balance: $1050.0
[pool-1-thread-2] TX-2 UNLOCKED

...

Final balance: $1250.0
=== Fairness true Test Completed ===
```

---

## 🔑 Key Methods

### Lock Acquisition
```java
lock.lock();                              // Blocking acquisition
lock.lockInterruptibly();                 // Blocking, respects interrupts
boolean acquired = lock.tryLock();        // Non-blocking attempt
boolean acquired = lock.tryLock(5, TimeUnit.SECONDS); // Timed attempt
```

### Lock Release
```java
lock.unlock(); // ALWAYS call in finally block
```

### Lock Information
```java
boolean held = lock.isHeldByCurrentThread();  // Does current thread hold lock?
int holdCount = lock.getHoldCount();          // Reentrant count
boolean hasWaiters = lock.hasQueuedThreads(); // Threads waiting?
int queueLength = lock.getQueueLength();      // Number of waiting threads
boolean isFair = lock.isFair();               // Fair or unfair?
```

### Condition Variables
```java
Condition condition = lock.newCondition();
condition.await();                            // Wait for signal
condition.signal();                           // Wake one waiting thread
condition.signalAll();                        // Wake all waiting threads
```

---

## 🎯 Real-World Use Cases

### 1. Bank Transfer with Deadlock Prevention
```java
public class BankTransfer {
    public void transfer(BankAccount from, BankAccount to, double amount) {
        // Always acquire locks in consistent order to prevent deadlock
        BankAccount first = from.getId() < to.getId() ? from : to;
        BankAccount second = from.getId() < to.getId() ? to : from;

        first.getLock().lock();
        try {
            second.getLock().lock();
            try {
                from.withdraw(amount);
                to.deposit(amount);
                log.info("Transferred ${} from {} to {}", amount, from.getId(), to.getId());
            } finally {
                second.getLock().unlock();
            }
        } finally {
            first.getLock().unlock();
        }
    }
}
```

### 2. Bounded Buffer with Condition Variables
```java
public class BoundedBuffer<T> {
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;

    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await(); // Wait until buffer not full
            }
            queue.offer(item);
            notEmpty.signal(); // Signal that buffer is not empty
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // Wait until buffer not empty
            }
            T item = queue.poll();
            notFull.signal(); // Signal that buffer is not full
            return item;
        } finally {
            lock.unlock();
        }
    }
}
```

### 3. Read-Modify-Write with TryLock
```java
public class Counter {
    private int count = 0;
    private final Lock lock = new ReentrantLock();

    public boolean incrementIfPossible() {
        if (lock.tryLock()) { // Don't block, just try
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false; // Couldn't increment
    }

    public int incrementOrWait() throws InterruptedException {
        if (!lock.tryLock(5, TimeUnit.SECONDS)) {
            throw new TimeoutException("Lock acquisition timed out");
        }
        try {
            return ++count;
        } finally {
            lock.unlock();
        }
    }
}
```

---

## ⚠️ Common Pitfalls

### 1. Forgetting to Unlock
```java
// ❌ BAD: Exception causes lock to never be released
lock.lock();
riskyOperation(); // May throw exception
lock.unlock();    // Never reached if exception thrown!
```

**Solution**: Always use try-finally
```java
// ✅ GOOD: Lock always released
lock.lock();
try {
    riskyOperation();
} finally {
    lock.unlock(); // Always executed
}
```

### 2. Unlocking Without Holding Lock
```java
// ❌ BAD: Unlock without acquire
lock.unlock(); // IllegalMonitorStateException!
```

**Solution**: Only unlock if you acquired the lock
```java
// ✅ GOOD: Check before unlock (for tryLock scenarios)
if (lock.tryLock()) {
    try {
        doWork();
    } finally {
        lock.unlock(); // Only unlock if acquired
    }
}
```

### 3. Deadlock with Multiple Locks
```java
// ❌ BAD: Thread-1 and Thread-2 deadlock
Thread-1: lock1.lock(); lock2.lock(); ... unlock both
Thread-2: lock2.lock(); lock1.lock(); ... unlock both
// Thread-1 holds lock1, waits for lock2
// Thread-2 holds lock2, waits for lock1
// DEADLOCK!
```

**Solution**: Always acquire locks in consistent order
```java
// ✅ GOOD: Consistent ordering prevents deadlock
Thread-1: lock1.lock(); lock2.lock(); ... unlock both
Thread-2: lock1.lock(); lock2.lock(); ... unlock both
```

### 4. Using Unfair Lock in Latency-Sensitive Code
```java
// ❌ BAD: Unfair lock can cause starvation
Lock lock = new ReentrantLock(false); // Unfair
// Some thread may wait indefinitely if system is busy
```

**Solution**: Use fair lock for latency-sensitive operations
```java
// ✅ GOOD: Fair lock guarantees bounded wait time
Lock lock = new ReentrantLock(true); // Fair (FIFO)
```

---

## 🎓 Best Practices

✅ **DO:**
- **ALWAYS** use try-finally for lock/unlock
- Use fair lock (true) for long-running critical sections
- Use tryLock() with timeout for deadlock-prone scenarios
- Acquire multiple locks in consistent order
- Use Condition variables for complex waiting scenarios

❌ **DON'T:**
- Forget to unlock in finally block
- Hold locks longer than necessary
- Use unfair locks for latency-sensitive operations
- Acquire locks in inconsistent order (deadlock risk)
- Use ReentrantLock when synchronized is sufficient

---

## 📊 ReentrantLock vs synchronized vs ReadWriteLock

| Feature | ReentrantLock | synchronized | ReadWriteLock |
|---------|---------------|--------------|---------------|
| **Explicit Control** | ✅ lock/unlock | ❌ Implicit | ✅ lock/unlock |
| **Fairness** | ✅ Optional | ❌ No | ✅ Optional |
| **Try-Lock** | ✅ tryLock() | ❌ No | ✅ tryLock() |
| **Interruptible** | ✅ Yes | ❌ No | ✅ Yes |
| **Conditions** | ✅ Multiple | 🟡 One (wait/notify) | ✅ Multiple |
| **Read Optimization** | ❌ No | ❌ No | ✅ Yes |
| **Simplicity** | 🟡 Medium | ✅ Simple | 🟡 Medium |
| **Performance** | 🟡 Similar | ✅ Slightly better | 🟡 Better for reads |

**When to Choose**:
- **synchronized**: Simple mutual exclusion, brief critical sections
- **ReentrantLock**: Need fairness, tryLock, or multiple conditions
- **ReadWriteLock**: Read-heavy workloads (90%+ reads)
- **StampedLock**: Optimistic reads (Java 8+)

---

## 🔗 Related Patterns

- **ReadWriteLock** - Optimized for read-heavy workloads
- **synchronized** - Built-in mutual exclusion
- **Semaphore** - Limit N concurrent accesses (N > 1)
- **StampedLock** - Optimistic locking (Java 8+)

---

**Package**: `com.shan.concurrency.threadspatterns.reentrantlock`

**Pattern Type**: Locking - Explicit Mutual Exclusion
**Thread Flow**: Exclusive access with advanced features
**Best For**: Complex locking scenarios, fairness requirements, timeouts, multiple conditions
