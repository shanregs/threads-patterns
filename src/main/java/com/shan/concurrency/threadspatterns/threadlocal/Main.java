package com.shan.concurrency.threadspatterns.threadlocal;

/**
 * Main class to run ThreadLocal demo independently
 */
public class Main {
    public static void main(String[] args) {
        ThreadLocalDemo demo = new ThreadLocalDemo();
        demo.demonstrate();
    }
}
