package com.defimore.crypto.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Manages error recovery and fallback strategies for the price service.
 */
public class ErrorRecoveryManager {
    
    private static final int MAX_CONSECUTIVE_FAILURES = 3;
    private static final int FALLBACK_MODE_DURATION_MINUTES = 5;
    private static final long BASE_RETRY_DELAY_MS = 1000; // 1 second
    private static final long MAX_RETRY_DELAY_MS = 30000; // 30 seconds
    
    private int consecutiveFailures = 0;
    private LocalDateTime lastSuccessTime;
    private LocalDateTime fallbackModeStartTime;
    private boolean inFallbackMode = false;
    
    /**
     * Handle an error occurrence.
     * @param error The error that occurred
     */
    public void handleError(Exception error) {
        consecutiveFailures++;
        
        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES && !inFallbackMode) {
            switchToFallbackMode();
        }
    }
    
    /**
     * Handle a successful operation.
     */
    public void onSuccess() {
        consecutiveFailures = 0;
        lastSuccessTime = LocalDateTime.now();
        
        if (inFallbackMode) {
            exitFallbackMode();
        }
    }
    
    /**
     * Check if currently in fallback mode.
     * @return true if in fallback mode
     */
    public boolean isInFallbackMode() {
        // Check if fallback mode should be automatically exited
        if (inFallbackMode && fallbackModeStartTime != null) {
            long minutesInFallback = ChronoUnit.MINUTES.between(fallbackModeStartTime, LocalDateTime.now());
            if (minutesInFallback >= FALLBACK_MODE_DURATION_MINUTES) {
                exitFallbackMode();
            }
        }
        
        return inFallbackMode;
    }
    
    /**
     * Get the number of consecutive failures.
     * @return Number of consecutive failures
     */
    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }
    
    /**
     * Get the time of the last successful operation.
     * @return Last success time, or null if never succeeded
     */
    public LocalDateTime getLastSuccessTime() {
        return lastSuccessTime;
    }
    
    /**
     * Calculate the retry delay based on consecutive failures (exponential backoff).
     * @return Retry delay in milliseconds
     */
    public long getRetryDelay() {
        if (consecutiveFailures == 0) {
            return 0;
        }
        
        // Exponential backoff: base * 2^(failures-1)
        long delay = BASE_RETRY_DELAY_MS * (1L << Math.min(consecutiveFailures - 1, 5)); // Cap at 2^5
        return Math.min(delay, MAX_RETRY_DELAY_MS);
    }
    
    /**
     * Check if data is considered stale based on last success time.
     * @param staleThresholdMinutes Minutes after which data is considered stale
     * @return true if data is stale
     */
    public boolean isDataStale(int staleThresholdMinutes) {
        if (lastSuccessTime == null) {
            return true;
        }
        
        long minutesSinceLastSuccess = ChronoUnit.MINUTES.between(lastSuccessTime, LocalDateTime.now());
        return minutesSinceLastSuccess > staleThresholdMinutes;
    }
    
    /**
     * Get a user-friendly status message.
     * @return Status message
     */
    public String getStatusMessage() {
        if (inFallbackMode) {
            return "Offline - Using cached data";
        } else if (consecutiveFailures > 0) {
            return "Connection issues - Retrying...";
        } else if (lastSuccessTime != null) {
            long minutesAgo = ChronoUnit.MINUTES.between(lastSuccessTime, LocalDateTime.now());
            if (minutesAgo == 0) {
                return "Online - Just updated";
            } else {
                return "Online - Updated " + minutesAgo + " minute" + (minutesAgo == 1 ? "" : "s") + " ago";
            }
        } else {
            return "Starting up...";
        }
    }
    
    /**
     * Switch to fallback mode.
     */
    private void switchToFallbackMode() {
        inFallbackMode = true;
        fallbackModeStartTime = LocalDateTime.now();
    }
    
    /**
     * Exit fallback mode.
     */
    private void exitFallbackMode() {
        inFallbackMode = false;
        fallbackModeStartTime = null;
    }
    
    /**
     * Reset the error recovery state.
     */
    public void reset() {
        consecutiveFailures = 0;
        lastSuccessTime = null;
        exitFallbackMode();
    }
}