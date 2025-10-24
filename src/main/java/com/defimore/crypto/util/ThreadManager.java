package com.defimore.crypto.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for managing background threads.
 */
public class ThreadManager {
    
    private final ScheduledExecutorService scheduler;
    
    public ThreadManager() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CryptoPriceUpdater");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Schedule a task to run periodically.
     * @param task Task to run
     * @param intervalMs Interval in milliseconds
     */
    public void schedulePeriodicUpdate(Runnable task, long intervalMs) {
        scheduler.scheduleAtFixedRate(task, 0, intervalMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Schedule a task to run once after a delay.
     * @param task Task to run
     * @param delayMs Delay in milliseconds
     */
    public void scheduleOnce(Runnable task, long delayMs) {
        scheduler.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Shutdown the thread manager and clean up resources.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Check if the scheduler is shutdown.
     * @return true if shutdown
     */
    public boolean isShutdown() {
        return scheduler.isShutdown();
    }
}