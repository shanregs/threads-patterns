package com.shan.concurrency.threadspatterns;

import com.shan.concurrency.threadspatterns.blockingqueue.BlockingQueueDemo;
import com.shan.concurrency.threadspatterns.completablefuture.CompletableFutureDemo;
import com.shan.concurrency.threadspatterns.countdownlatch.CountDownLatchDemo;
import com.shan.concurrency.threadspatterns.cyclicbarrier.CyclicBarrierDemo;
import com.shan.concurrency.threadspatterns.cyclicbarrier.MultiHopTourDemo;
import com.shan.concurrency.threadspatterns.exchanger.ExchangerDemo;
import com.shan.concurrency.threadspatterns.executorservice.BlockingQueueStrategiesDemo;
import com.shan.concurrency.threadspatterns.executorservice.ExecutorServiceTypesDemo;
import com.shan.concurrency.threadspatterns.forkjoinpool.ForkJoinPoolDemo;
import com.shan.concurrency.threadspatterns.phaser.PhaserDemo;
import com.shan.concurrency.threadspatterns.reentrantlock.ReentrantLockDemo;
import com.shan.concurrency.threadspatterns.semaphore.SemaphoreDemo;
import com.shan.concurrency.threadspatterns.threadlocal.ThreadLocalDemo;
import com.shan.concurrency.threadspatterns.virtualthreads.VirtualThreadsDemo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * DemoRunner executes all concurrency pattern demonstrations.
 * Run with: mvn spring-boot:run
 * Or run specific demo with: mvn spring-boot:run -Dspring-boot.run.arguments=--demo=countdownlatch
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoRunner implements CommandLineRunner {

    private final CountDownLatchDemo countDownLatchDemo;
    private final CyclicBarrierDemo cyclicBarrierDemo;
    private final MultiHopTourDemo multiHopTourDemo;
    private final PhaserDemo phaserDemo;
    private final SemaphoreDemo semaphoreDemo;
    private final ExchangerDemo exchangerDemo;
    private final ThreadLocalDemo threadLocalDemo;
    private final ReentrantLockDemo reentrantLockDemo;
    private final BlockingQueueDemo blockingQueueDemo;
    private final ForkJoinPoolDemo forkJoinPoolDemo;
    private final CompletableFutureDemo completableFutureDemo;
    private final VirtualThreadsDemo virtualThreadsDemo;
    private final ExecutorServiceTypesDemo executorServiceTypesDemo;
    private final BlockingQueueStrategiesDemo blockingQueueStrategiesDemo;
    private final ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        log.info("\n");
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║   Java Concurrency Patterns - Demo Suite                    ║");
        log.info("║   Spring Boot 4.0.1 | Java 21 LTS                           ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
        log.info("\n");

        if (args.length > 0 && args[0].startsWith("--demo=")) {
            String demoName = args[0].substring(7).toLowerCase();
            runSpecificDemo(demoName);
        } else {
            runAllDemos();
        }

        log.info("\n");
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║   All Demonstrations Completed Successfully!                ║");
        log.info("║   Check README-THREAD-PATTERNS.md for detailed docs         ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
        log.info("\n");

        // Gracefully shutdown the application
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    private void runAllDemos() throws InterruptedException {
        log.info("Running all concurrency pattern demonstrations...\n");

        runDemo("1. CountDownLatch", () -> countDownLatchDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("2. CyclicBarrier - Matrix Processing", () -> cyclicBarrierDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("3. CyclicBarrier - Multi-Hop Tour", () -> multiHopTourDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("4. Phaser", () -> phaserDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("5. Semaphore", () -> semaphoreDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("6. Exchanger", () -> exchangerDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("7. ThreadLocal", () -> threadLocalDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("8. ReentrantLock", () -> reentrantLockDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("9. BlockingQueue", () -> blockingQueueDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("10. ForkJoinPool", () -> forkJoinPoolDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("11. CompletableFuture", () -> completableFutureDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("12. Virtual Threads", () -> virtualThreadsDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("13. ExecutorService Types", () -> executorServiceTypesDemo.demonstrate());
        Thread.sleep(1000);

        runDemo("14. BlockingQueue Strategies", () -> blockingQueueStrategiesDemo.demonstrate());
    }

    private void runSpecificDemo(String demoName) {
        log.info("Running specific demo: {}\n", demoName);

        switch (demoName) {
            case "countdownlatch" -> runDemo("CountDownLatch", () -> countDownLatchDemo.demonstrate());
            case "cyclicbarrier" -> runDemo("CyclicBarrier - Matrix Processing", () -> cyclicBarrierDemo.demonstrate());
            case "multihoptour" -> runDemo("CyclicBarrier - Multi-Hop Tour", () -> multiHopTourDemo.demonstrate());
            case "phaser" -> runDemo("Phaser", () -> phaserDemo.demonstrate());
            case "semaphore" -> runDemo("Semaphore", () -> semaphoreDemo.demonstrate());
            case "exchanger" -> runDemo("Exchanger", () -> exchangerDemo.demonstrate());
            case "threadlocal" -> runDemo("ThreadLocal", () -> threadLocalDemo.demonstrate());
            case "reentrantlock" -> runDemo("ReentrantLock", () -> reentrantLockDemo.demonstrate());
            case "blockingqueue" -> runDemo("BlockingQueue", () -> blockingQueueDemo.demonstrate());
            case "forkjoinpool" -> runDemo("ForkJoinPool", () -> forkJoinPoolDemo.demonstrate());
            case "completablefuture" -> runDemo("CompletableFuture", () -> completableFutureDemo.demonstrate());
            case "virtualthreads" -> runDemo("Virtual Threads", () -> virtualThreadsDemo.demonstrate());
            case "executorservice" -> runDemo("ExecutorService Types", () -> executorServiceTypesDemo.demonstrate());
            case "blockingqueuestrategies" -> runDemo("BlockingQueue Strategies", () -> blockingQueueStrategiesDemo.demonstrate());
            default -> {
                log.error("Unknown demo: {}. Available demos:", demoName);
                log.error("  - countdownlatch, cyclicbarrier, multihoptour, phaser, semaphore, exchanger");
                log.error("  - threadlocal, reentrantlock, blockingqueue, forkjoinpool");
                log.error("  - completablefuture, virtualthreads, executorservice, blockingqueuestrategies");
            }
        }
    }

    private void runDemo(String name, Runnable demo) {
        try {
            log.info("\n▶ Starting: {}", name);
            log.info("─".repeat(70));
            demo.run();
            log.info("─".repeat(70));
            log.info("✓ Completed: {}\n", name);
        } catch (Exception e) {
            log.error("✗ Error in {}: {}", name, e.getMessage(), e);
        }
    }
}
