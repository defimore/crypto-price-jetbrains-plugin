package com.defimore.crypto;

import com.defimore.crypto.service.ConfigurationService;
import com.defimore.crypto.service.ConfigurationServiceFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating crypto price status bar widgets.
 */
public class CryptoPriceWidgetFactory implements StatusBarWidgetFactory {
    
    public static final String WIDGET_ID = "CryptoPriceWidget";
    
    @Override
    public @NonNls @NotNull String getId() {
        return WIDGET_ID;
    }
    
    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Crypto Price Display";
    }
    
    @Override
    public boolean isAvailable(@NotNull Project project) {
        // Always available, but visibility is controlled by configuration
        return true;
    }
    
    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new CryptoPriceStatusBarWidget(project);
    }
    
    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        // Widget disposal is handled by the widget itself
        if (widget instanceof CryptoPriceStatusBarWidget) {
            widget.dispose();
        }
    }
    
    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        // Check if the widget should be shown based on configuration
        try {
            ConfigurationService configService = ConfigurationServiceFactory.getInstance();
            return configService.getConfig().isShowInStatusBar();
        } catch (Exception e) {
            // If there's an error getting config, default to true
            return true;
        }
    }
    
    /**
     * Check if the widget is currently enabled in configuration.
     */
    public static boolean isWidgetEnabled() {
        try {
            ConfigurationService configService = ConfigurationServiceFactory.getInstance();
            return configService.getConfig().isShowInStatusBar();
        } catch (Exception e) {
            return true;
        }
    }
}