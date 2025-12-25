package com.shan.concurrency.threadspatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java 21 Concurrency Patterns - Comprehensive Learning Project
 *
 * This application demonstrates 13 essential Java 21 concurrency patterns:
 * 1. CountDownLatch - Batch job coordination
 * 2. CyclicBarrier - Matrix row processing synchronization
 * 3. Phaser - Multi-phase game rounds
 * 4. Semaphore - ATM access control
 * 5. Exchanger - Trade exchange between threads
 * 6. ThreadLocal - Per-thread request context
 * 7. ReentrantLock - Thread-safe bank account
 * 8. BlockingQueue - Producer-consumer log writer
 * 9. ForkJoinPool - Parallel image processing
 * 10. CompletableFuture - Async API call chains
 * 11. Virtual Threads - High-throughput web server
 * 12. ExecutorService Types - FixedThreadPool, CachedThreadPool, SingleThreadExecutor, etc.
 * 13. BlockingQueue Strategies - ArrayBlockingQueue, LinkedBlockingQueue, SynchronousQueue, etc.
 *
 * Usage:
 * - Run all demos: mvn spring-boot:run
 * - Run specific demo: mvn spring-boot:run -Dspring-boot.run.arguments=--demo=executorservice
 * - Run tests: mvn test
 *
 * For comprehensive documentation, see:
 * - README-THREAD-PATTERNS.md (Concurrency patterns)
 * - EXECUTOR-FRAMEWORK-GUIDE.md (ExecutorService & BlockingQueue)
 */
@SpringBootApplication
public class ThreadsPatternsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreadsPatternsApplication.class, args);
    }

}
