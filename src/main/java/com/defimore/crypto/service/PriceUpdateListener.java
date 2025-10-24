package com.defimore.crypto.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Listener interface for price updates.
 */
public interface PriceUpdateListener {
    
    /**
     * Called when prices have been updated.
     * @param prices Map of symbol to updated price
     * @param isOnline Whether the update came from live data or cached data
     */
    void onPricesUpdated(Map<String, BigDecimal> prices, boolean isOnline);
    
    /**
     * Called when a price update fails.
     * @param error The error that occurred
     */
    void onPriceUpdateFailed(Exception error);
}