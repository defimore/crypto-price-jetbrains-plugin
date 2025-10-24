package com.defimore.crypto.service.impl;

import com.defimore.crypto.model.BinancePriceItem;
import com.defimore.crypto.model.CryptoPluginConfig;
import com.defimore.crypto.service.ConfigChangeListener;
import com.defimore.crypto.service.ConfigurationService;
import com.defimore.crypto.service.ConfigurationServiceFactory;
import com.defimore.crypto.service.ErrorRecoveryManager;
import com.defimore.crypto.service.PriceService;
import com.defimore.crypto.service.PriceUpdateListener;
import com.defimore.crypto.util.HttpClientConfig;

import com.defimore.crypto.util.ThreadManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Implementation of PriceService that fetches prices from Binance API.
 */
public class BinancePriceService implements PriceService, ConfigChangeListener {
    
    private static final String API_URL = "https://data-api.binance.vision/api/v3/ticker/price";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, BigDecimal> priceCache;
    private final List<PriceUpdateListener> listeners;
    private final ConfigurationService configService;
    private final ErrorRecoveryManager errorRecoveryManager;
    
    private static final int MAX_CACHE_SIZE = 100; // Limit cache size
    private ThreadManager threadManager;
    private volatile boolean isOnline;
    private volatile boolean isPeriodicUpdatesEnabled;
    private volatile LocalDateTime lastSuccessfulUpdate;
    
    public BinancePriceService() {
        this.httpClient = HttpClientConfig.createClient();
        this.objectMapper = new ObjectMapper();
        this.priceCache = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.configService = ConfigurationServiceFactory.getInstance();
        this.errorRecoveryManager = new ErrorRecoveryManager();
        this.threadManager = new ThreadManager();
        this.isOnline = false;
        this.isPeriodicUpdatesEnabled = false;
        this.lastSuccessfulUpdate = null;
        
        // Listen for configuration changes
        configService.addConfigChangeListener(this);
    }
    
    @Override
    public CompletableFuture<Map<String, BigDecimal>> fetchPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build the symbols parameter for the API using configured stable symbol
                CryptoPluginConfig config = configService.getConfig();
                String stableSymbol = config.getStableSymbol();
                List<String> tradingPairs = symbols.stream()
                        .map(symbol -> symbol + stableSymbol)
                        .collect(Collectors.toList());
                
                String symbolsParam = buildSymbolsParameter(tradingPairs);
                String url = API_URL + "?symbols=" + URLEncoder.encode(symbolsParam, StandardCharsets.UTF_8);
                
                // Create HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(HttpClientConfig.getRequestTimeout())
                        .GET()
                        .build();
                
                // Send request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    // Parse response
                    List<BinancePriceItem> priceItems = objectMapper.readValue(
                            response.body(), 
                            new TypeReference<List<BinancePriceItem>>() {}
                    );
                    
                    // Convert to map and update cache
                    Map<String, BigDecimal> prices = new HashMap<>();
                    for (BinancePriceItem item : priceItems) {
                        String symbol = item.getSymbol().replace(stableSymbol, "");
                        BigDecimal price = new BigDecimal(item.getPrice());
                        prices.put(symbol, price);
                    }
                    
                    // Update cache and status with size limit
                    synchronized (priceCache) {
                        priceCache.putAll(prices);
                        
                        // Limit cache size to prevent memory issues
                        if (priceCache.size() > MAX_CACHE_SIZE) {
                            // Remove oldest entries (simple approach)
                            int toRemove = priceCache.size() - MAX_CACHE_SIZE;
                            priceCache.entrySet().removeIf(entry -> toRemove > 0);
                        }
                    }
                    
                    // Update status and error recovery
                    isOnline = true;
                    lastSuccessfulUpdate = LocalDateTime.now();
                    errorRecoveryManager.onSuccess();
                    
                    // Notify listeners
                    notifyPriceUpdate(prices, true);
                    
                    return prices;
                } else {
                    throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                }
                
            } catch (Exception e) {
                // Handle error with recovery manager
                errorRecoveryManager.handleError(e);
                isOnline = !errorRecoveryManager.isInFallbackMode();
                
                // Notify listeners with cached data if available
                Map<String, BigDecimal> cachedPrices = getCachedPrices();
                if (!cachedPrices.isEmpty()) {
                    notifyPriceUpdate(cachedPrices, false);
                }
                
                notifyPriceUpdateFailed(e);
                throw new RuntimeException("Failed to fetch prices: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public Map<String, BigDecimal> getCachedPrices() {
        synchronized (priceCache) {
            return new HashMap<>(priceCache);
        }
    }
    
    @Override
    public void startPeriodicUpdates() {
        if (isPeriodicUpdatesEnabled) {
            return; // Already started
        }
        
        isPeriodicUpdatesEnabled = true;
        schedulePeriodicUpdate();
    }
    
    @Override
    public void stopPeriodicUpdates() {
        isPeriodicUpdatesEnabled = false;
        if (threadManager != null) {
            threadManager.shutdown();
            threadManager = new ThreadManager(); // Create new instance for future use
        }
    }
    
    @Override
    public void addPriceUpdateListener(PriceUpdateListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removePriceUpdateListener(PriceUpdateListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public boolean isOnline() {
        return isOnline && !errorRecoveryManager.isInFallbackMode();
    }
    
    /**
     * Build the symbols parameter for the Binance API.
     * Format: ["BTCUSDT","ETHUSDT","ASTERUSDT"]
     */
    private String buildSymbolsParameter(List<String> symbols) {
        return "[" + symbols.stream()
                .map(symbol -> "\"" + symbol + "\"")
                .collect(Collectors.joining(",")) + "]";
    }
    
    /**
     * Notify all listeners of price updates.
     */
    private void notifyPriceUpdate(Map<String, BigDecimal> prices, boolean isOnline) {
        for (PriceUpdateListener listener : listeners) {
            try {
                listener.onPricesUpdated(prices, isOnline);
            } catch (Exception e) {
                System.err.println("Error notifying price update listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify all listeners of price update failures.
     */
    private void notifyPriceUpdateFailed(Exception error) {
        for (PriceUpdateListener listener : listeners) {
            try {
                listener.onPriceUpdateFailed(error);
            } catch (Exception e) {
                System.err.println("Error notifying price update failure listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get the timestamp of the last successful update.
     */
    public LocalDateTime getLastSuccessfulUpdate() {
        return lastSuccessfulUpdate;
    }
    
    /**
     * Schedule periodic price updates based on current configuration.
     */
    private void schedulePeriodicUpdate() {
        if (!isPeriodicUpdatesEnabled || threadManager.isShutdown()) {
            return;
        }
        
        CryptoPluginConfig config = configService.getConfig();
        List<String> symbols = config.getSymbols();
        
        if (symbols.isEmpty()) {
            return;
        }
        
        Runnable updateTask = () -> {
            if (isPeriodicUpdatesEnabled) {
                // Use async approach to avoid blocking
                fetchPrices(symbols).whenComplete((prices, throwable) -> {
                    // Schedule next update with retry delay if needed
                    if (isPeriodicUpdatesEnabled) {
                        CryptoPluginConfig currentConfig = configService.getConfig();
                        long delay = Math.max(currentConfig.getRefreshInterval(), errorRecoveryManager.getRetryDelay());
                        threadManager.scheduleOnce(this::schedulePeriodicUpdate, delay);
                    }
                });
            }
        };
        
        threadManager.scheduleOnce(updateTask, 0); // Start immediately
    }
    
    /**
     * Handle configuration changes by restarting periodic updates if needed.
     */
    @Override
    public void onConfigChanged(CryptoPluginConfig oldConfig, CryptoPluginConfig newConfig) {
        if (isPeriodicUpdatesEnabled) {
            // Restart periodic updates with new configuration
            boolean wasEnabled = isPeriodicUpdatesEnabled;
            stopPeriodicUpdates();
            if (wasEnabled) {
                startPeriodicUpdates();
            }
        }
    }
    
    /**
     * Clean up resources when the service is disposed.
     */
    public void dispose() {
        stopPeriodicUpdates();
        configService.removeConfigChangeListener(this);
        listeners.clear();
        synchronized (priceCache) {
            priceCache.clear();
        }
    }
    
    /**
     * Check if periodic updates are currently enabled.
     */
    public boolean isPeriodicUpdatesEnabled() {
        return isPeriodicUpdatesEnabled;
    }
    
    /**
     * Get the error recovery manager for status information.
     */
    public ErrorRecoveryManager getErrorRecoveryManager() {
        return errorRecoveryManager;
    }
    
    /**
     * Get a user-friendly status message.
     */
    public String getStatusMessage() {
        return errorRecoveryManager.getStatusMessage();
    }
    
    /**
     * Check if cached data is stale.
     * @param staleThresholdMinutes Minutes after which data is considered stale
     * @return true if data is stale
     */
    public boolean isDataStale(int staleThresholdMinutes) {
        return errorRecoveryManager.isDataStale(staleThresholdMinutes);
    }
}