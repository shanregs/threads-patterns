# Java Concurrency Patterns - Complete Learning Project

A comprehensive Spring Boot 4 application demonstrating 14 essential Java 21 concurrency patterns with real-world examples, detailed logging, and unit tests.

## 🎯 Project Overview

This project provides hands-on implementations of all major Java concurrency utilities with Java 21 and Spring Boot 4, designed for learning and reference. Each pattern includes:

- **Real-world examples** with clear use cases
- **Detailed logging** showing thread behavior
- **Step-by-step code explanations**
- **Unit tests** for verification
- **Separate classes** (no anonymous implementations)
- **Production-ready patterns**

## 📚 Covered Patterns

| # | Pattern | Use Case | Example |
|---|---------|----------|---------|
| 1 | **CountDownLatch** | Batch coordination | Wait for 5 data files to process |
| 2 | **CyclicBarrier** | Phase synchronization | Matrix row processing + Multi-hop city tour |
| 3 | **Phaser** | Multi-phase sync | Multiplayer game rounds |
| 4 | **Semaphore** | Resource limiting | ATM with 3 terminals |
| 5 | **Exchanger** | Two-party exchange | Trading system |
| 6 | **ThreadLocal** | Thread isolation | Web request context |
| 7 | **ReentrantLock** | Explicit locking | Bank account operations |
| 8 | **BlockingQueue** | Producer-consumer | Log file writer |
| 9 | **ForkJoinPool** | Divide-and-conquer | Image processing |
| 10 | **CompletableFuture** | Async pipelines | API call chains |
| 11 | **Virtual Threads** | High concurrency | Web server (1000s requests) |
| 12 | **ExecutorService Types** | Task execution | Thread pool strategies |
| 13 | **BlockingQueue Strategies** | Task queuing | Queue implementations |
| 14 | **CyclicBarrier (Tour)** | Multi-hop coordination | Guided city tour with stops |

## 🚀 Quick Start

### Prerequisites

- Java 21 (LTS with Virtual Threads support)
- Maven 3.9+
- Spring Boot 4.0.1

### Run All Demos

```bash
mvn clean install
mvn spring-boot:run
```

### Run Specific Demo

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--demo=countdownlatch
mvn spring-boot:run -Dspring-boot.run.arguments=--demo=virtualthreads
```

Available demos: `countdownlatch`, `cyclicbarrier`, `multihoptour`, `phaser`, `semaphore`, `exchanger`, `threadlocal`, `reentrantlock`, `blockingqueue`, `forkjoinpool`, `completablefuture`, `virtualthreads`, `executorservice`, `blockingqueuestrategies`

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=CountDownLatchDemoTest
mvn test -Dtest=VirtualThreadsDemoTest
```

## 📖 Documentation

### Concurrency Patterns Guide
For comprehensive documentation on all concurrency patterns:
- Visual representations
- Advantages/disadvantages
- Performance characteristics
- Comparison matrix
- Best practices
- Common pitfalls

See **[README-THREAD-PATTERNS.md](README-THREAD-PATTERNS.md)**

### Executor Framework Guide
For comprehensive ExecutorService and BlockingQueue documentation:
- Executor framework architecture
- Different ExecutorService types (FixedThreadPool, CachedThreadPool, etc.)
- BlockingQueue strategies (ArrayBlockingQueue, LinkedBlockingQueue, etc.)
- ThreadPoolExecutor deep dive
- Task lifecycle and handling
- Visual diagrams and flow charts
- Best practices and common pitfalls

See **[EXECUTOR-FRAMEWORK-GUIDE.md](EXECUTOR-FRAMEWORK-GUIDE.md)**

## 🏗️ Project Structure

```
src/main/java/com/shan/concurrency/threadspatterns/
├── countdownlatch/
│   ├── CountDownLatchDemo.java
│   └── BatchJobTask.java
├── cyclicbarrier/
│   ├── CyclicBarrierDemo.java
│   ├── MatrixRowProcessor.java
│   ├── MultiHopTourDemo.java
│   └── Tourist.java
├── phaser/
│   ├── PhaserDemo.java
│   └── GamePlayer.java
├── semaphore/
│   ├── SemaphoreDemo.java
│   └── AtmCustomer.java
├── exchanger/
│   ├── ExchangerDemo.java
│   ├── Trader.java
│   └── TradeOrder.java
├── threadlocal/
│   ├── ThreadLocalDemo.java
│   ├── RequestProcessor.java
│   ├── RequestContext.java
│   └── RequestContextHolder.java
├── reentrantlock/
│   ├── ReentrantLockDemo.java
│   ├── BankAccount.java
│   └── BankTransaction.java
├── blockingqueue/
│   ├── BlockingQueueDemo.java
│   ├── LogProducer.java
│   ├── LogConsumer.java
│   └── LogEntry.java
├── forkjoinpool/
│   ├── ForkJoinPoolDemo.java
│   └── ImageProcessor.java
├── completablefuture/
│   ├── CompletableFutureDemo.java
│   ├── ApiService.java
│   ├── UserProfile.java
│   ├── OrderHistory.java
│   └── Recommendations.java
├── virtualthreads/
│   ├── VirtualThreadsDemo.java
│   └── WebRequest.java
├── executorservice/
│   ├── ExecutorServiceTypesDemo.java
│   ├── BlockingQueueStrategiesDemo.java
│   ├── Task.java
│   └── TaskResult.java
├── DemoRunner.java
└── ThreadsPatternsApplication.java
```

## 💡 Key Features

### Clear Logging
Every operation logs thread names and important events:
```
[pool-1-thread-1] Task 'DataFile-1' started
[pool-1-thread-1] Task 'DataFile-1' completed successfully
[pool-1-thread-1] Task 'DataFile-1' counted down. Remaining tasks: 4
```

### Real-world Examples
Not just toy examples - production patterns:
- **ATM system** with limited terminals (Semaphore)
- **Bank account** with concurrent transactions (ReentrantLock)
- **Web server** handling thousands of requests (Virtual Threads)
- **Trading platform** exchanging orders (Exchanger)
- **API gateway** chaining calls (CompletableFuture)
- **Guided city tour** with multiple stops and group synchronization (CyclicBarrier)

### Comprehensive Tests
Every demo has a test:
```java
@SpringBootTest
class CountDownLatchDemoTest {
    @Autowired
    private CountDownLatchDemo demo;

    @Test
    void testDemo() {
        assertDoesNotThrow(() -> demo.demonstrate());
    }
}
```

## 🎓 Learning Path

1. **Start simple**: CountDownLatch, Semaphore
2. **Progress to barriers**: CyclicBarrier, Phaser
3. **Explore isolation**: ThreadLocal, Exchanger
4. **Master locking**: ReentrantLock
5. **Async patterns**: BlockingQueue, CompletableFuture
6. **Advanced**: ForkJoinPool, Virtual Threads

## 📊 When to Use What?

```
I/O-bound + High concurrency? → Virtual Threads
CPU-intensive parallel task? → ForkJoinPool
Async API calls? → CompletableFuture
Producer-consumer? → BlockingQueue
Resource limiting? → Semaphore
One-time sync? → CountDownLatch
Multi-phase sync? → Phaser or CyclicBarrier
Thread isolation? → ThreadLocal
Fine-grained locking? → ReentrantLock
```

## 🔧 Technologies

- **Java**: 21 LTS (with Virtual Threads, Pattern Matching, Records)
- **Spring Boot**: 4.0.1
- **Build Tool**: Maven
- **Testing**: JUnit 5, Spring Boot Test
- **Logging**: SLF4J with Logback
- **Code Quality**: Lombok for reducing boilerplate

## 📝 Code Highlights

### Virtual Threads (Java 21+)
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> handleRequest());
    }
}
// Handles 100K concurrent requests efficiently!
```

### CompletableFuture Chaining
```java
CompletableFuture.supplyAsync(() -> fetchUser(id))
    .thenCompose(user -> fetchOrders(user.getId()))
    .thenApply(orders -> processOrders(orders))
    .exceptionally(ex -> fallbackValue)
    .thenAccept(result -> log(result));
```

### ForkJoin Pattern
```java
class Task extends RecursiveTask<Result> {
    protected Result compute() {
        if (small) return computeDirectly();
        Task left = new Task(leftHalf);
        left.fork();
        Result rightResult = new Task(rightHalf).compute();
        return combine(left.join(), rightResult);
    }
}
```

## 🤝 Contributing

This is a learning project. Feel free to:
- Add more examples
- Improve documentation
- Fix bugs
- Suggest new patterns

## 📄 License

This project is for educational purposes.

## 🙏 Acknowledgments

Built following Java best practices and inspired by:
- "Java Concurrency in Practice" by Brian Goetz
- JEP 444: Virtual Threads
- Java 21+ concurrency improvements

---

**Happy Learning! 🚀**

For detailed pattern comparisons and usage guide, see [README-THREAD-PATTERNS.md](README-THREAD-PATTERNS.md)
