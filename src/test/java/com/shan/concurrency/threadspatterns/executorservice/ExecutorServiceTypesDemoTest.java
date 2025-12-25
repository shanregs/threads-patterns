package com.shan.concurrency.threadspatterns.executorservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ExecutorServiceTypesDemoTest {

    @Autowired
    private ExecutorServiceTypesDemo executorServiceTypesDemo;

    @Test
    void testExecutorServiceTypesDemo() {
        assertDoesNotThrow(() -> executorServiceTypesDemo.demonstrate(),
                "ExecutorServiceTypes demo should execute without throwing exceptions");
    }
}
