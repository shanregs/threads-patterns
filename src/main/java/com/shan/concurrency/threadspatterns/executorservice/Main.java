package com.shan.concurrency.threadspatterns.executorservice;

/**
 * Main class to run ExecutorService demos independently
 *
 * Usage:
 * - Run without arguments to see both demos
 * - Run with "types" to see only executor service types demo
 * - Run with "queues" to see only blocking queue strategies demo
 */
public class Main {
    public static void main(String[] args) {
        String demo = args.length > 0 ? args[0].toLowerCase() : "all";

        switch (demo) {
            case "types":
                runTypesDemo();
                break;
            case "queues":
                runQueuesDemo();
                break;
            case "all":
            default:
                runTypesDemo();
                System.out.println("\n" + "=".repeat(70) + "\n");
                runQueuesDemo();
                break;
        }
    }

    private static void runTypesDemo() {
        ExecutorServiceTypesDemo demo = new ExecutorServiceTypesDemo();
        demo.demonstrate();
    }

    private static void runQueuesDemo() {
        BlockingQueueStrategiesDemo demo = new BlockingQueueStrategiesDemo();
        demo.demonstrate();
    }
}
