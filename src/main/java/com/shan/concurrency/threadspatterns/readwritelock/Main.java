package com.shan.concurrency.threadspatterns.readwritelock;

/**
 * Main class to run ReadWriteLock demo independently
 */
public class Main {
    public static void main(String[] args) {
        ThreadSafeCacheDemo demo = new ThreadSafeCacheDemo();
        demo.demonstrate();
    }
}
