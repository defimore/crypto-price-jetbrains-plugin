package com.defimore.crypto.service.impl;

import com.defimore.crypto.model.CryptoPluginConfig;
import com.defimore.crypto.service.ConfigChangeListener;
import com.defimore.crypto.service.ConfigurationService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of ConfigurationService that persists settings using IntelliJ's state management.
 */
@Service
@State(
    name = "CryptoPluginConfig",
    storages = @Storage("cryptoPricePlugin.xml")
)
public final class ConfigurationServiceImpl implements ConfigurationService, PersistentStateComponent<CryptoPluginConfig> {
    
    private CryptoPluginConfig config = new CryptoPluginConfig();
    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * Get the singleton instance of the configuration service.
     */
    public static ConfigurationServiceImpl getInstance() {
        return ApplicationManager.getApplication().getService(ConfigurationServiceImpl.class);
    }
    
    @Override
    public CryptoPluginConfig getConfig() {
        // Return direct reference to avoid unnecessary copying for read operations
        // Only copy when modification is needed
        return config;
    }
    
    @Override
    public void saveConfig(CryptoPluginConfig newConfig) {
        if (newConfig == null) {
            return; // Fail silently for speed
        }
        
        CryptoPluginConfig oldConfig = this.config;
        this.config = newConfig;
        
        // Notify listeners of the change
        notifyConfigChanged(oldConfig, newConfig);
    }
    
    @Override
    public void resetToDefaults() {
        CryptoPluginConfig oldConfig = new CryptoPluginConfig(this.config);
        this.config = new CryptoPluginConfig();
        
        // Notify listeners of the change
        notifyConfigChanged(oldConfig, this.config);
    }
    
    @Override
    public void addConfigChangeListener(ConfigChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removeConfigChangeListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners of configuration changes.
     */
    private void notifyConfigChanged(CryptoPluginConfig oldConfig, CryptoPluginConfig newConfig) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigChanged(oldConfig, newConfig);
            } catch (Exception e) {
                // Log error but don't let one listener failure affect others
                System.err.println("Error notifying config change listener: " + e.getMessage());
            }
        }
    }
    
    // PersistentStateComponent implementation
    
    @Override
    public @Nullable CryptoPluginConfig getState() {
        return config;
    }
    
    @Override
    public void loadState(@NotNull CryptoPluginConfig state) {
        // Validate and sanitize loaded state
        CryptoPluginConfig sanitized = state.sanitize();
        if (sanitized.isValid()) {
            this.config = sanitized;
        } else {
            // If loaded state is invalid, use defaults
            this.config = new CryptoPluginConfig();
        }
    }
}