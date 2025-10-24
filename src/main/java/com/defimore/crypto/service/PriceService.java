package com.defimore.crypto.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for fetching and managing cryptocurrency prices.
 */
public interface PriceService {
    
    /**
     * Fetch current prices for the specified symbols.
     * @param symbols List of cryptocurrency symbols to fetch
     * @return Future containing map of symbol to price
     */
    CompletableFuture<Map<String, BigDecimal>> fetchPrices(List<String> symbols);
    
    /**
     * Get cached prices from the last successful fetch.
     * @return Map of symbol to cached price
     */
    Map<String, BigDecimal> getCachedPrices();
    
    /**
     * Start periodic price updates based on configuration.
     */
    void startPeriodicUpdates();
    
    /**
     * Stop periodic price updates.
     */
    void stopPeriodicUpdates();
    
    /**
     * Add a listener for price updates.
     * @param listener Listener to be notified of price updates
     */
    void addPriceUpdateListener(PriceUpdateListener listener);
    
    /**
     * Remove a price update listener.
     * @param listener Listener to remove
     */
    void removePriceUpdateListener(PriceUpdateListener listener);
    
    /**
     * Check if the service is currently online and fetching data.
     * @return true if online, false if offline
     */
    boolean isOnline();
}