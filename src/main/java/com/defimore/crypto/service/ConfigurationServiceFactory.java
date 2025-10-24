package com.defimore.crypto.service;

import com.defimore.crypto.service.impl.ConfigurationServiceImpl;

/**
 * Factory for getting configuration service instances.
 */
public class ConfigurationServiceFactory {
    
    private static volatile ConfigurationService instance;
    
    /**
     * Get the configuration service instance with lazy initialization.
     * @return ConfigurationService instance
     */
    public static ConfigurationService getInstance() {
        if (instance == null) {
            synchronized (ConfigurationServiceFactory.class) {
                if (instance == null) {
                    instance = ConfigurationServiceImpl.getInstance();
                }
            }
        }
        return instance;
    }
}