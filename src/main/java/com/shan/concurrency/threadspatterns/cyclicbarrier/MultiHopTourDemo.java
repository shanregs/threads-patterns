package com.shan.concurrency.threadspatterns.cyclicbarrier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MultiHopTourDemo - Guided City Tour with Multiple Stops
 *
 * Use Case: Coordinate multiple tourists visiting multiple tour spots
 * Real-world Example: 5 tourists visit 4 city attractions, synchronizing at each spot for group activities
 *
 * How it works:
 * 1. Create multiple CyclicBarriers (one for each tour spot)
 * 2. Each barrier has a barrier action (group meal, photo session, etc.)
 * 3. Tourists (threads) travel at different speeds
 * 4. At each spot, all tourists must assemble before the group activity begins
 * 5. After the barrier action, tourists proceed to the next spot
 *
 * Tour Spots:
 * 1. Historical Museum - Group lunch
 * 2. Art Gallery - Group photo session
 * 3. Botanical Garden - Refreshment break
 * 4. Observation Deck - Final dinner
 */
@Slf4j
@Component
public class MultiHopTourDemo {

    private static final int NUMBER_OF_TOURISTS = 5;
    private static final List<String> TOUR_SPOTS = List.of(
            "Historical Museum",
            "Art Gallery",
            "Botanical Garden",
            "Observation Deck"
    );
    private static final List<String> GROUP_ACTIVITIES = List.of(
            "Having group lunch at museum café",
            "Taking group photos at gallery entrance",
            "Enjoying refreshments at garden pavilion",
            "Having farewell dinner with city view"
    );

    public void demonstrate() {
        log.info("=== CyclicBarrier Demo: Multi-Hop City Tour ===");
        log.info("Scenario: {} tourists visiting {} attractions", NUMBER_OF_TOURISTS, TOUR_SPOTS.size());
        log.info("Tour route: {}", String.join(" → ", TOUR_SPOTS));
        log.info("");

        // Step 1: Create CyclicBarriers for each tour spot
        List<CyclicBarrier> spotBarriers = createSpotBarriers();

        // Step 2: Create thread pool for tourists
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_TOURISTS);

        try {
            // Step 3: Create and submit tourists
            String[] touristNames = {"Alice", "Bob", "Charlie", "Diana", "Eve"};

            for (String touristName : touristNames) {
                Tourist tourist = new Tourist(touristName, TOUR_SPOTS, spotBarriers);
                executor.submit(tourist);
            }

            log.info("[{}] All {} tourists have started their tour!",
                    Thread.currentThread().getName(), NUMBER_OF_TOURISTS);
            log.info("");

        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Executor did not terminate in time");
                    executor.shutdownNow();
                }
                log.info("");
                log.info("=== Multi-Hop City Tour Completed Successfully! ===");
            } catch (InterruptedException e) {
                log.error("Executor termination interrupted", e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private List<CyclicBarrier> createSpotBarriers() {
        List<CyclicBarrier> barriers = new ArrayList<>();

        for (int i = 0; i < TOUR_SPOTS.size(); i++) {
            final int spotIndex = i;
            final String spot = TOUR_SPOTS.get(i);
            final String activity = GROUP_ACTIVITIES.get(i);

            // Create barrier action for each spot
            Runnable barrierAction = () -> {
                log.info("");
                log.info("╔════════════════════════════════════════════════════════════════╗");
                log.info("║ 🎯 SPOT {}/{}: {} ",
                        spotIndex + 1, TOUR_SPOTS.size(), spot);
                log.info("║ 🍽️  GROUP ACTIVITY: {}", activity);
                log.info("║ ✅ All {} tourists are present!", NUMBER_OF_TOURISTS);
                log.info("╚════════════════════════════════════════════════════════════════╝");
                log.info("");

                // Simulate group activity time
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            // Create barrier with number of tourists and barrier action
            CyclicBarrier barrier = new CyclicBarrier(NUMBER_OF_TOURISTS, barrierAction);
            barriers.add(barrier);
        }

        return barriers;
    }
}
