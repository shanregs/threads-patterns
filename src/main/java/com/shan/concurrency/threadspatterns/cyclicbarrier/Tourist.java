package com.shan.concurrency.threadspatterns.cyclicbarrier;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Tourist represents an individual tourist on a guided tour.
 * Each tourist visits multiple spots and synchronizes with other tourists at each spot.
 */
@Slf4j
public class Tourist implements Runnable {

    private final String name;
    private final List<String> tourSpots;
    private final List<CyclicBarrier> spotBarriers;
    private final Random random = new Random();

    public Tourist(String name, List<String> tourSpots, List<CyclicBarrier> spotBarriers) {
        this.name = name;
        this.tourSpots = tourSpots;
        this.spotBarriers = spotBarriers;
    }

    @Override
    public void run() {
        try {
            log.info("[{}] {} started the tour!", Thread.currentThread().getName(), name);

            // Visit each tour spot
            for (int i = 0; i < tourSpots.size(); i++) {
                String spot = tourSpots.get(i);
                CyclicBarrier barrier = spotBarriers.get(i);

                // Travel to the spot
                travelToSpot(spot);

                // Explore the spot
                exploreSpot(spot);

                // Wait at barrier for all tourists to arrive
                log.info("[{}] {} arrived at '{}' and waiting for others... (Waiting: {}/{})",
                        Thread.currentThread().getName(),
                        name,
                        spot,
                        barrier.getNumberWaiting() + 1,
                        barrier.getParties());

                // Wait for all tourists at this spot
                barrier.await();

                // After barrier: All tourists are now together at this spot
                log.info("[{}] {} - All tourists assembled at '{}'! Proceeding with group activity.",
                        Thread.currentThread().getName(), name, spot);

            }

            log.info("[{}] {} completed the entire tour! 🎉",
                    Thread.currentThread().getName(), name);

        } catch (InterruptedException e) {
            log.error("[{}] {} was interrupted during the tour",
                    Thread.currentThread().getName(), name);
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
            log.error("[{}] {} encountered a broken barrier",
                    Thread.currentThread().getName(), name);
        }
    }

    private void travelToSpot(String spot) throws InterruptedException {
        int travelTime = 500 + random.nextInt(1000); // 500-1500ms
        log.info("[{}] {} traveling to '{}'...",
                Thread.currentThread().getName(), name, spot);
        Thread.sleep(travelTime);
    }

    private void exploreSpot(String spot) throws InterruptedException {
        int exploreTime = 800 + random.nextInt(1200); // 800-2000ms
        log.info("[{}] {} exploring '{}'...",
                Thread.currentThread().getName(), name, spot);
        Thread.sleep(exploreTime);
    }
}
