package com.shan.concurrency.threadspatterns.reentrantlock;

/**
 * Main class to run ReentrantLock demo independently
 */
public class Main {
    public static void main(String[] args) {
        ReentrantLockDemo demo = new ReentrantLockDemo();
        demo.demonstrate();
    }
}
