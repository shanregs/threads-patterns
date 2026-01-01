package com.shan.concurrency.threadspatterns.cyclicbarrier;

/**
 * Main class to run CyclicBarrier demos independently
 *
 * Usage:
 * - Run without arguments to see both demos
 * - Run with "matrix" to see only matrix processing demo
 * - Run with "tour" to see only multi-hop tour demo
 */
public class Main {
    public static void main(String[] args) {
        String demo = args.length > 0 ? args[0].toLowerCase() : "all";

        switch (demo) {
            case "matrix":
                runMatrixDemo();
                break;
            case "tour":
                runTourDemo();
                break;
            case "all":
            default:
                runMatrixDemo();
                System.out.println("\n" + "=".repeat(70) + "\n");
                runTourDemo();
                break;
        }
    }

    private static void runMatrixDemo() {
        CyclicBarrierDemo demo = new CyclicBarrierDemo();
        demo.demonstrate();
    }

    private static void runTourDemo() {
        MultiHopTourDemo demo = new MultiHopTourDemo();
        demo.demonstrate();
    }
}
