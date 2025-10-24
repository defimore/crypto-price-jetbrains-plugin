package com.defimore.crypto.service;

import com.defimore.crypto.service.impl.BinancePriceService;

/**
 * Factory for creating price service instances.
 */
public class PriceServiceFactory {
    
    private static volatile PriceService instance;
    
    /**
     * Get the singleton price service instance.
     * @return PriceService instance
     */
    public static PriceService getInstance() {
        if (instance == null) {
            synchronized (PriceServiceFactory.class) {
                if (instance == null) {
                    instance = new BinancePriceService();
                }
            }
        }
        return instance;
    }
    
    /**
     * Set a custom price service instance (mainly for testing).
     * @param priceService Custom price service
     */
    public static void setInstance(PriceService priceService) {
        synchronized (PriceServiceFactory.class) {
            instance = priceService;
        }
    }
}