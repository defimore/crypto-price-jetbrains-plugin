package com.defimore.crypto.service;

import com.defimore.crypto.model.CryptoPluginConfig;

/**
 * Listener interface for configuration changes.
 */
public interface ConfigChangeListener {
    
    /**
     * Called when configuration has been changed.
     * @param oldConfig Previous configuration
     * @param newConfig New configuration
     */
    void onConfigChanged(CryptoPluginConfig oldConfig, CryptoPluginConfig newConfig);
}