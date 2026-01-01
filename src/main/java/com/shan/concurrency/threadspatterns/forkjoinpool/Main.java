package com.shan.concurrency.threadspatterns.forkjoinpool;

/**
 * Main class to run ForkJoinPool demo independently
 */
public class Main {
    public static void main(String[] args) {
        ForkJoinPoolDemo demo = new ForkJoinPoolDemo();
        demo.demonstrate();
    }
}
