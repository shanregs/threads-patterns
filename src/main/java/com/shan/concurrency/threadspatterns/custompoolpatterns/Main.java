package com.shan.concurrency.threadspatterns.custompoolpatterns;

/**
 * Main class to run Custom Pool Pattern demos
 *
 * Usage:
 * - Run without arguments to see all demos
 * - Run with specific demo name to see only that demo:
 *   - "basic"      : Basic custom pool usage
 *   - "sequential" : Sequential operations with custom pool
 *   - "parallel"   : Parallel operations with custom pool
 *   - "mixed"      : Using common pool + custom pool together
 *   - "combined"   : Complete real-world example (e-commerce order processing)
 */
public class Main {

    public static void main(String[] args) {
        String demo = args.length > 0 ? args[0].toLowerCase() : "all";

        switch (demo) {
            case "basic":
                runBasicDemo();
                break;

            case "sequential":
                runSequentialDemo();
                break;

            case "parallel":
                runParallelDemo();
                break;

            case "mixed":
                runMixedDemo();
                break;

            case "combined":
                runCombinedDemo();
                break;

            case "all":
            default:
                runAllDemos();
                break;
        }
    }

    private static void runBasicDemo() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RUNNING: Custom Pool Basic Demo");
        System.out.println("=".repeat(80) + "\n");

        CustomPoolBasicDemo demo = new CustomPoolBasicDemo();
        demo.demonstrate();
    }

    private static void runSequentialDemo() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RUNNING: Custom Pool Sequential Operations Demo");
        System.out.println("=".repeat(80) + "\n");

        CustomPoolSequentialDemo demo = new CustomPoolSequentialDemo();
        demo.demonstrate();
    }

    private static void runParallelDemo() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RUNNING: Custom Pool Parallel Operations Demo");
        System.out.println("=".repeat(80) + "\n");

        CustomPoolParallelDemo demo = new CustomPoolParallelDemo();
        demo.demonstrate();
    }

    private static void runMixedDemo() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RUNNING: Mixed Pool Demo (Common + Custom)");
        System.out.println("=".repeat(80) + "\n");

        MixedPoolDemo demo = new MixedPoolDemo();
        demo.demonstrate();
    }

    private static void runCombinedDemo() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RUNNING: Combined Demo (Real-world E-commerce Order Processing)");
        System.out.println("=".repeat(80) + "\n");

        CustomPoolCombinedDemo demo = new CustomPoolCombinedDemo();
        demo.demonstrate();
    }

    private static void runAllDemos() {
        System.out.println("\n" + "█".repeat(80));
        System.out.println("█ CUSTOM POOL PATTERNS - COMPLETE DEMO SUITE");
        System.out.println("█".repeat(80) + "\n");

        runBasicDemo();
        waitBetweenDemos();

        runSequentialDemo();
        waitBetweenDemos();

        runParallelDemo();
        waitBetweenDemos();

        runMixedDemo();
        waitBetweenDemos();

        runCombinedDemo();

        System.out.println("\n" + "█".repeat(80));
        System.out.println("█ ALL DEMOS COMPLETED");
        System.out.println("█".repeat(80) + "\n");
    }

    private static void waitBetweenDemos() {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("Waiting 2 seconds before next demo...");
        System.out.println("-".repeat(80) + "\n");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
