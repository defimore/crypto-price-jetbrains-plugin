package com.defimore.crypto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model representing a single price item from Binance API response.
 * Maps to JSON format: {"symbol": "BTCUSDT", "price": "108699.99000000"}
 */
public class BinancePriceItem {
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("price")
    private String price;
    
    public BinancePriceItem() {
        // Default constructor for Jackson
    }
    
    public BinancePriceItem(String symbol, String price) {
        this.symbol = symbol;
        this.price = price;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getPrice() {
        return price;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    @Override
    public String toString() {
        return "BinancePriceItem{" +
               "symbol='" + symbol + '\'' +
               ", price='" + price + '\'' +
               '}';
    }
}