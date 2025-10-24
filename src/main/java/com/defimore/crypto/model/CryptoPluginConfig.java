package com.defimore.crypto.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Configuration model for the crypto price plugin.
 */
public class CryptoPluginConfig {
    
    // Default configuration values
    public static final List<String> DEFAULT_SYMBOLS = Arrays.asList("BTC", "ETH");
    public static final String DEFAULT_STABLE_SYMBOL = "USDT";
    public static final int DEFAULT_REFRESH_INTERVAL = 60000; // 60 seconds in milliseconds
    public static final int DEFAULT_FRACTION_DIGITS = 3;
    public static final boolean DEFAULT_SHOW_ICON = false;
    public static final boolean DEFAULT_SHOW_IN_STATUS_BAR = true;
    
    private List<String> symbols;
    private String stableSymbol;
    private int refreshInterval;
    private int fractionDigits;
    private boolean showIcon;
    private boolean showInStatusBar;
    
    /**
     * Default constructor with default values.
     */
    public CryptoPluginConfig() {
        this.symbols = DEFAULT_SYMBOLS;
        this.stableSymbol = DEFAULT_STABLE_SYMBOL;
        this.refreshInterval = DEFAULT_REFRESH_INTERVAL;
        this.fractionDigits = DEFAULT_FRACTION_DIGITS;
        this.showIcon = DEFAULT_SHOW_ICON;
        this.showInStatusBar = DEFAULT_SHOW_IN_STATUS_BAR;
    }
    
    /**
     * Copy constructor.
     */
    public CryptoPluginConfig(CryptoPluginConfig other) {
        this.symbols = other.symbols;
        this.stableSymbol = other.stableSymbol;
        this.refreshInterval = other.refreshInterval;
        this.fractionDigits = other.fractionDigits;
        this.showIcon = other.showIcon;
        this.showInStatusBar = other.showInStatusBar;
    }
    
    // Getters and setters
    public List<String> getSymbols() {
        return symbols;
    }
    
    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }
    
    public String getStableSymbol() {
        return stableSymbol;
    }
    
    public void setStableSymbol(String stableSymbol) {
        this.stableSymbol = stableSymbol;
    }
    
    public int getRefreshInterval() {
        return refreshInterval;
    }
    
    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
    
    public int getFractionDigits() {
        return fractionDigits;
    }
    
    public void setFractionDigits(int fractionDigits) {
        this.fractionDigits = fractionDigits;
    }
    
    public boolean isShowIcon() {
        return showIcon;
    }
    
    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }
    
    public boolean isShowInStatusBar() {
        return showInStatusBar;
    }
    
    public void setShowInStatusBar(boolean showInStatusBar) {
        this.showInStatusBar = showInStatusBar;
    }
    
    /**
     * Validate the configuration values.
     * @return true if configuration is valid
     */
    public boolean isValid() {
        return symbols != null && !symbols.isEmpty() &&
               stableSymbol != null && !stableSymbol.trim().isEmpty() &&
               refreshInterval >= 1000 && refreshInterval <= 3600000 && // 1 second to 1 hour
               fractionDigits >= 0 && fractionDigits <= 8;
    }
    
    /**
     * Validate and sanitize the configuration values.
     * @return ValidationResult containing validation status and messages
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Validate symbols
        if (symbols == null || symbols.isEmpty()) {
            result.addError("Symbols list cannot be empty");
        } else {
            for (String symbol : symbols) {
                if (symbol == null || symbol.trim().isEmpty()) {
                    result.addError("Symbol cannot be empty");
                } else if (!isValidSymbol(symbol.trim())) {
                    result.addError("Invalid symbol format: " + symbol);
                }
            }
        }
        
        // Validate stable symbol
        if (stableSymbol == null || stableSymbol.trim().isEmpty()) {
            result.addError("Stable symbol cannot be empty");
        } else if (!isValidSymbol(stableSymbol.trim())) {
            result.addError("Invalid stable symbol format: " + stableSymbol);
        }
        
        // Validate refresh interval
        if (refreshInterval < 1000) {
            result.addError("Refresh interval must be at least 1000ms (1 second)");
        } else if (refreshInterval > 3600000) {
            result.addError("Refresh interval must be at most 3600000ms (1 hour)");
        }
        
        // Validate fraction digits
        if (fractionDigits < 0) {
            result.addError("Fraction digits cannot be negative");
        } else if (fractionDigits > 8) {
            result.addError("Fraction digits cannot exceed 8");
        }
        
        return result;
    }
    
    /**
     * Check if a symbol has valid format (alphanumeric, 2-10 characters).
     * @param symbol Symbol to validate
     * @return true if valid
     */
    private boolean isValidSymbol(String symbol) {
        return symbol.matches("^[A-Z0-9]{2,10}$");
    }
    
    /**
     * Sanitize and fix configuration values where possible.
     * @return Sanitized configuration
     */
    public CryptoPluginConfig sanitize() {
        CryptoPluginConfig sanitized = new CryptoPluginConfig(this);
        
        // Sanitize symbols
        if (sanitized.symbols != null) {
            sanitized.symbols = sanitized.symbols.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(s -> s.trim().toUpperCase())
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        // Sanitize stable symbol
        if (sanitized.stableSymbol != null) {
            sanitized.stableSymbol = sanitized.stableSymbol.trim().toUpperCase();
        }
        
        // Clamp refresh interval
        if (sanitized.refreshInterval < 1000) {
            sanitized.refreshInterval = 1000;
        } else if (sanitized.refreshInterval > 3600000) {
            sanitized.refreshInterval = 3600000;
        }
        
        // Clamp fraction digits
        if (sanitized.fractionDigits < 0) {
            sanitized.fractionDigits = 0;
        } else if (sanitized.fractionDigits > 8) {
            sanitized.fractionDigits = 8;
        }
        
        return sanitized;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CryptoPluginConfig that = (CryptoPluginConfig) o;
        return refreshInterval == that.refreshInterval &&
               fractionDigits == that.fractionDigits &&
               showIcon == that.showIcon &&
               showInStatusBar == that.showInStatusBar &&
               Objects.equals(symbols, that.symbols) &&
               Objects.equals(stableSymbol, that.stableSymbol);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(symbols, stableSymbol, refreshInterval, fractionDigits, showIcon, showInStatusBar);
    }
    
    @Override
    public String toString() {
        return "CryptoPluginConfig{" +
               "symbols=" + symbols +
               ", stableSymbol='" + stableSymbol + '\'' +
               ", refreshInterval=" + refreshInterval +
               ", fractionDigits=" + fractionDigits +
               ", showIcon=" + showIcon +
               ", showInStatusBar=" + showInStatusBar +
               '}';
    }
}