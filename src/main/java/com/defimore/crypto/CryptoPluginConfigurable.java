package com.defimore.crypto;

import com.defimore.crypto.model.CryptoPluginConfig;
import com.defimore.crypto.service.ConfigurationService;
import com.defimore.crypto.service.ConfigurationServiceFactory;
import com.defimore.crypto.ui.SimpleCryptoConfigPanel;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Configurable for the crypto price plugin settings.
 */
public class CryptoPluginConfigurable implements Configurable {
    
    private SimpleCryptoConfigPanel configPanel;
    private ConfigurationService configService;
    
    public CryptoPluginConfigurable() {
        // Delay service initialization to avoid EDT blocking
    }
    
    private ConfigurationService getConfigService() {
        if (configService == null) {
            configService = ConfigurationServiceFactory.getInstance();
        }
        return configService;
    }
    
    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Crypto Price Display";
    }
    
    @Override
    public @Nullable String getHelpTopic() {
        return "crypto.price.plugin.settings";
    }
    
    @Override
    public @Nullable JComponent createComponent() {
        if (configPanel == null) {
            // Create panel immediately with defaults to avoid blocking
            configPanel = new SimpleCryptoConfigPanel();
            configPanel.loadConfig(new CryptoPluginConfig()); // Load defaults first
            
            // Load actual configuration asynchronously
            com.intellij.openapi.application.ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    CryptoPluginConfig currentConfig = getConfigService().getConfig();
                    SwingUtilities.invokeLater(() -> {
                        if (configPanel != null) {
                            configPanel.loadConfig(currentConfig);
                        }
                    });
                } catch (Exception e) {
                    // If loading fails, keep defaults
                    System.err.println("Failed to load config: " + e.getMessage());
                }
            });
        }
        return configPanel;
    }
    
    @Override
    public boolean isModified() {
        return configPanel != null && configPanel.isModified();
    }
    
    @Override
    public void apply() throws ConfigurationException {
        if (configPanel == null) {
            return;
        }
        
        // Fast save without excessive validation
        CryptoPluginConfig newConfig = configPanel.saveConfig();
        getConfigService().saveConfig(newConfig);
    }
    
    @Override
    public void reset() {
        if (configPanel != null) {
            CryptoPluginConfig currentConfig = getConfigService().getConfig();
            configPanel.loadConfig(currentConfig);
        }
    }
    
    @Override
    public void disposeUIResources() {
        configPanel = null;
    }
}