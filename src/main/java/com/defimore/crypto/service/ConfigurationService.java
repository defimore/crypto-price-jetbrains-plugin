package com.defimore.crypto.service;

import com.defimore.crypto.model.CryptoPluginConfig;

/**
 * Service interface for managing plugin configuration.
 * Handles reading, saving, and validating user settings.
 */
public interface ConfigurationService {
    
    /**
     * Get the current plugin configuration.
     * @return Current configuration settings
     */
    CryptoPluginConfig getConfig();
    
    /**
     * Save the plugin configuration.
     * @param config Configuration to save
     */
    void saveConfig(CryptoPluginConfig config);
    
    /**
     * Reset configuration to default values.
     */
    void resetToDefaults();
    
    /**
     * Add a listener for configuration changes.
     * @param listener Listener to be notified of config changes
     */
    void addConfigChangeListener(ConfigChangeListener listener);
    
    /**
     * Remove a configuration change listener.
     * @param listener Listener to remove
     */
    void removeConfigChangeListener(ConfigChangeListener listener);
}