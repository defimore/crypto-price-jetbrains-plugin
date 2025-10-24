package com.defimore.crypto;

import com.defimore.crypto.model.CryptoPluginConfig;
import com.defimore.crypto.service.*;
import com.defimore.crypto.service.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Status bar widget for displaying crypto prices.
 */
public class CryptoPriceStatusBarWidget implements StatusBarWidget, PriceUpdateListener, ConfigChangeListener {

    private final Project project;
    private final PriceService priceService;
    private final ConfigurationService configService;
    private StatusBar statusBar;
    private String currentText = "₿ Loading...";
    private boolean isOnline = false;
    private boolean hasError = false;
    private volatile boolean isDisposed = false;

    public CryptoPriceStatusBarWidget(Project project) {
        this.project = project;
        this.priceService = PriceServiceFactory.getInstance();
        this.configService = ConfigurationServiceFactory.getInstance();

        // Set initial text
        this.currentText = "₿ Loading...";

        // Register listeners
        priceService.addPriceUpdateListener(this);
        configService.addConfigChangeListener(this);

        // Initialize widget with minimal delay, but do heavy work later
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!isDisposed) {
                // Show cached data immediately if available
                Map<String, BigDecimal> cachedPrices = priceService.getCachedPrices();
                if (!cachedPrices.isEmpty()) {
                    updateDisplayText(cachedPrices);
                    updateStatusBar();
                }
                
                // Schedule heavy initialization for later
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    try {
                        Thread.sleep(3000); // 3 second delay
                        if (!isDisposed) {
                            initializeWidget();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        });
    }

    /**
     * Initialize the widget after services are ready.
     */
    private void initializeWidget() {
        try {
            // Show cached data first if available
            Map<String, BigDecimal> cachedPrices = priceService.getCachedPrices();
            if (!cachedPrices.isEmpty()) {
                onPricesUpdated(cachedPrices, false);
            }

            // Delay network operations to avoid blocking EDT
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    if (!isDisposed) {
                        // Start price updates in background
                        priceService.startPeriodicUpdates();

                        // Initial fetch with delay to avoid startup impact
                        Thread.sleep(2000); // 2 second delay
                        
                        if (!isDisposed) {
                            CryptoPluginConfig config = configService.getConfig();
                            if (config != null && !config.getSymbols().isEmpty()) {
                                priceService.fetchPrices(config.getSymbols()).whenComplete((prices, throwable) -> {
                                    if (!isDisposed) {
                                        if (throwable == null && prices != null && !prices.isEmpty()) {
                                            onPricesUpdated(prices, true);
                                        } else {
                                            ApplicationManager.getApplication().invokeLater(() -> {
                                                if (!isDisposed) {
                                                    currentText = "₿ Connection Error";
                                                    updateStatusBar();
                                                }
                                            });
                                        }
                                    }
                                });
                            } else {
                                ApplicationManager.getApplication().invokeLater(() -> {
                                    if (!isDisposed) {
                                        currentText = "₿ No symbols configured";
                                        updateStatusBar();
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!isDisposed) {
                            System.err.println("Error initializing crypto price widget: " + e.getMessage());
                            currentText = "₿ Initialization Error";
                            updateStatusBar();
                        }
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("Error setting up crypto price widget: " + e.getMessage());
            currentText = "₿ Setup Error";
            updateStatusBar();
        }
    }

    @Override
    public @NonNls @NotNull String ID() {
        return CryptoPriceWidgetFactory.WIDGET_ID;
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return new TextPresentation() {
            @Override
            public @NotNull String getText() {
                return currentText;
            }

            @Override
            public @Nullable String getTooltipText() {
                String status = hasError ? "Error" : (isOnline ? "Live" : "Cached");
                return "Crypto prices (" + status + ")";
            }

            @Override
            public @Nullable Consumer<MouseEvent> getClickConsumer() {
                return mouseEvent -> handleClick(mouseEvent);
            }

            @Override
            public float getAlignment() {
                return Component.RIGHT_ALIGNMENT;
            }
        };
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void dispose() {
        isDisposed = true;

        // Clean up listeners
        priceService.removePriceUpdateListener(this);
        configService.removeConfigChangeListener(this);

        // Stop price updates if this is the last widget
        priceService.stopPeriodicUpdates();
    }

    @Override
    public void onPricesUpdated(Map<String, BigDecimal> prices, boolean isOnline) {
        if (isDisposed) return;

        this.isOnline = isOnline;
        this.hasError = false;

        ApplicationManager.getApplication().invokeLater(() -> {
            if (!isDisposed) {
                updateDisplayText(prices);
                updateStatusBar();
            }
        });
    }

    @Override
    public void onPriceUpdateFailed(Exception error) {
        if (isDisposed) return;

        this.isOnline = false;
        this.hasError = true;

        ApplicationManager.getApplication().invokeLater(() -> {
            if (!isDisposed) {
                // Try to show cached prices
                Map<String, BigDecimal> cachedPrices = priceService.getCachedPrices();
                if (!cachedPrices.isEmpty()) {
                    updateDisplayText(cachedPrices);
                } else {
                    currentText = "₿ Connection Error";
                }
                updateStatusBar();
            }
        });
    }

    @Override
    public void onConfigChanged(CryptoPluginConfig oldConfig, CryptoPluginConfig newConfig) {
        if (isDisposed) return;

        // Configuration changed, fetch new prices immediately
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!isDisposed) {
                // First update display with cached data if available
                Map<String, BigDecimal> cachedPrices = priceService.getCachedPrices();
                if (!cachedPrices.isEmpty()) {
                    updateDisplayText(cachedPrices);
                    updateStatusBar();
                }

                // Then fetch fresh data for the new symbols
                if (!newConfig.getSymbols().isEmpty()) {
                    priceService.fetchPrices(newConfig.getSymbols()).whenComplete((prices, throwable) -> {
                        if (!isDisposed) {
                            if (throwable == null && prices != null && !prices.isEmpty()) {
                                onPricesUpdated(prices, true);
                            } else {
                                // Show error or cached data
                                ApplicationManager.getApplication().invokeLater(() -> {
                                    if (!isDisposed) {
                                        currentText = "₿ Error fetching new symbols";
                                        updateStatusBar();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Update the display text based on current prices and configuration.
     */
    private void updateDisplayText(Map<String, BigDecimal> prices) {
        CryptoPluginConfig config = configService.getConfig();

        if (prices.isEmpty()) {
            currentText = "No Data";
            return;
        }

        StringBuilder sb = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(config.getFractionDigits());
        formatter.setMinimumFractionDigits(0);
        formatter.setGroupingUsed(false);

        boolean first = true;
        for (String symbol : config.getSymbols()) {
            BigDecimal price = prices.get(symbol);
            if (price != null) {
                if (!first) {
                    sb.append(" | ");
                }
                sb.append(symbol).append(": ").append(formatter.format(price));
                first = false;
            }
        }

        if (sb.length() == 0) {
            currentText = "₿ No Data";
        } else {
            currentText = "₿ " + sb.toString();
            if (!isOnline && !hasError) {
                currentText += " (Cached)";
            }
        }
    }

    /**
     * Update the status bar widget display.
     */
    private void updateStatusBar() {
        if (statusBar != null && !isDisposed) {
            statusBar.updateWidget(ID());
        }
    }

    /**
     * Check if the widget is disposed.
     */
    public boolean isDisposed() {
        return isDisposed;
    }

    /**
     * Handle click events on the status bar widget.
     */
    private void handleClick(MouseEvent mouseEvent) {
        try {
            if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                // Left click - show popup with detailed information
                showDetailPopup(mouseEvent);
            } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                // Right click - show context menu
                showContextMenu(mouseEvent);
            }
        } catch (Exception e) {
            // If click handling fails, just log the error
            System.err.println("Error handling click: " + e.getMessage());
        }
    }

    /**
     * Show a popup with detailed price information.
     */
    private void showDetailPopup(MouseEvent mouseEvent) {
        try {
            Map<String, BigDecimal> prices = priceService.getCachedPrices();
            CryptoPluginConfig config = configService.getConfig();

            if (prices.isEmpty()) {
                return;
            }

            List<String> items = new ArrayList<>();
            DecimalFormat formatter = new DecimalFormat();
            formatter.setMaximumFractionDigits(config.getFractionDigits());
            formatter.setMinimumFractionDigits(0);
            formatter.setGroupingUsed(true);

            for (String symbol : config.getSymbols()) {
                BigDecimal price = prices.get(symbol);
                if (price != null) {
                    items.add(symbol + ": " + formatter.format(price) + " " + config.getStableSymbol());
                }
            }

            if (!items.isEmpty()) {
                String status = hasError ? "Error" : (isOnline ? "Live" : "Cached");
                items.add(""); // Separator
                items.add("Status: " + status);

                BaseListPopupStep<String> step = new BaseListPopupStep<String>("Crypto Prices", items) {
                    @Override
                    public PopupStep onChosen(String selectedValue, boolean finalChoice) {
                        return FINAL_CHOICE;
                    }

                    @Override
                    public boolean isSelectable(String value) {
                        return false; // Make items non-selectable
                    }
                };

                ListPopup popup = JBPopupFactory.getInstance().createListPopup(step);
                popup.showUnderneathOf(mouseEvent.getComponent());
            }
        } catch (Exception e) {
            // If popup fails, just ignore silently
            System.err.println("Error showing detail popup: " + e.getMessage());
        }
    }

    /**
     * Show context menu with actions.
     */
    private void showContextMenu(MouseEvent mouseEvent) {
        try {
            List<String> actions = new ArrayList<>();
            actions.add("Refresh Now");
            actions.add("Settings...");
            actions.add("About");

            BaseListPopupStep<String> step = new BaseListPopupStep<String>("Crypto Price Plugin", actions) {
                @Override
                public PopupStep onChosen(String selectedValue, boolean finalChoice) {
                    try {
                        switch (selectedValue) {
                            case "Refresh Now":
                                refreshPrices();
                                break;
                            case "Settings...":
                                openSettings();
                                break;
                            case "About":
                                showAbout();
                                break;
                        }
                    } catch (Exception e) {
                        System.err.println("Error handling menu action: " + e.getMessage());
                    }
                    return FINAL_CHOICE;
                }
            };

            ListPopup popup = JBPopupFactory.getInstance().createListPopup(step);
            popup.showUnderneathOf(mouseEvent.getComponent());
        } catch (Exception e) {
            // If context menu fails, just ignore silently
            System.err.println("Error showing context menu: " + e.getMessage());
        }
    }

    /**
     * Refresh prices immediately.
     */
    private void refreshPrices() {
        CryptoPluginConfig config = configService.getConfig();
        priceService.fetchPrices(config.getSymbols()).whenComplete((prices, throwable) -> {
            // Update will be handled by the listener
        });
    }

    /**
     * Open the plugin settings.
     */
    private void openSettings() {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, CryptoPluginConfigurable.class);
    }

    /**
     * Show about information.
     */
    private void showAbout() {
        List<String> info = new ArrayList<>();
        info.add("Crypto Price Display Plugin");
        info.add("Version 1.0.0");
        info.add("");
        info.add("Displays real-time cryptocurrency prices");
        info.add("in the IDE status bar.");

        BaseListPopupStep<String> step = new BaseListPopupStep<String>("About", info) {
            @Override
            public PopupStep onChosen(String selectedValue, boolean finalChoice) {
                return FINAL_CHOICE;
            }

            @Override
            public boolean isSelectable(String value) {
                return false;
            }
        };

        ListPopup popup = JBPopupFactory.getInstance().createListPopup(step);
        popup.showUnderneathOf(statusBar.getComponent());
    }
}