package com.shan.concurrency.threadspatterns.executorservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class BlockingQueueStrategiesDemoTest {

    @Autowired
    private BlockingQueueStrategiesDemo blockingQueueStrategiesDemo;

    @Test
    void testBlockingQueueStrategiesDemo() {
        assertDoesNotThrow(() -> blockingQueueStrategiesDemo.demonstrate(),
                "BlockingQueueStrategies demo should execute without throwing exceptions");
    }
}
