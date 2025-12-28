package com.shan.concurrency.threadspatterns.cyclicbarrier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test class for MultiHopTourDemo
 */
@SpringBootTest
class MultiHopTourDemoTest {

    @Autowired
    private MultiHopTourDemo demo;

    @Test
    void testMultiHopTourDemo() {
        assertDoesNotThrow(() -> demo.demonstrate(),
                "MultiHopTourDemo should execute without throwing exceptions");
    }

    @Test
    void testMultipleExecutions() {
        // CyclicBarrier should handle multiple executions
        assertDoesNotThrow(() -> {
            demo.demonstrate();
            Thread.sleep(1000); // Wait between executions
            demo.demonstrate();
        }, "MultiHopTourDemo should handle multiple consecutive executions");
    }
}
