package com.defimore.crypto;

import com.defimore.crypto.service.ConfigurationService;
import com.defimore.crypto.service.ConfigurationServiceFactory;
import com.defimore.crypto.service.PriceService;
import com.defimore.crypto.service.PriceServiceFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * Action to manually refresh cryptocurrency prices.
 */
public class RefreshPricesAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            ConfigurationService configService = ConfigurationServiceFactory.getInstance();
            PriceService priceService = PriceServiceFactory.getInstance();
            
            // Fetch prices immediately
            priceService.fetchPrices(configService.getConfig().getSymbols())
                    .whenComplete((prices, throwable) -> {
                        if (throwable != null) {
                            Messages.showErrorDialog(
                                    e.getProject(),
                                    "Failed to refresh prices: " + throwable.getMessage(),
                                    "Crypto Price Refresh Error"
                            );
                        } else {
                            Messages.showInfoMessage(
                                    e.getProject(),
                                    "Prices refreshed successfully!",
                                    "Crypto Price Refresh"
                            );
                        }
                    });
                    
        } catch (Exception ex) {
            Messages.showErrorDialog(
                    e.getProject(),
                    "Error refreshing prices: " + ex.getMessage(),
                    "Crypto Price Error"
            );
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always enable the action
        e.getPresentation().setEnabled(true);
    }
}