package com.shan.concurrency.threadspatterns.countdownlatch;

/**
 * Main class to run CountDownLatch demo independently
 */
public class Main {
    public static void main(String[] args) {
        CountDownLatchDemo demo = new CountDownLatchDemo();
        demo.demonstrate();
    }
}
