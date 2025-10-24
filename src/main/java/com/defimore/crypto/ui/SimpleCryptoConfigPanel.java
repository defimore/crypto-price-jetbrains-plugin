package com.defimore.crypto.ui;

import com.defimore.crypto.model.CryptoPluginConfig;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Simplified configuration panel for testing.
 */
public class SimpleCryptoConfigPanel extends JPanel {
    
    private JBTextField symbolsField;
    private JBTextField stableSymbolField;
    private JSpinner refreshIntervalSpinner;
    private JSpinner fractionDigitsSpinner;
    
    private CryptoPluginConfig currentConfig;
    
    public SimpleCryptoConfigPanel() {
        // Fast initialization without exception handling overhead
        initializeComponents();
        layoutComponents();
    }
    
    private void initializeComponents() {
        // Initialize with default values to avoid any potential blocking
        symbolsField = new JBTextField();
        symbolsField.setText("BTC,ETH");
        
        stableSymbolField = new JBTextField();
        stableSymbolField.setText("USDT");
        
        refreshIntervalSpinner = new JSpinner(new SpinnerNumberModel(60, 1, 3600, 1));
        fractionDigitsSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 8, 1));
        // No display options needed - always show in status bar without icon
    }
    
    private void layoutComponents() {
        // Use simpler BoxLayout for faster rendering
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Crypto Price Display Settings");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titleLabel);
        add(Box.createVerticalStrut(15));
        
        // Symbols
        JPanel symbolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        symbolsPanel.add(new JLabel("Symbols:"));
        symbolsField.setPreferredSize(new Dimension(200, 25));
        symbolsPanel.add(symbolsField);
        symbolsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(symbolsPanel);
        
        // Stable Symbol
        JPanel stablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stablePanel.add(new JLabel("Stable Symbol:"));
        stableSymbolField.setPreferredSize(new Dimension(100, 25));
        stablePanel.add(stableSymbolField);
        stablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(stablePanel);
        
        // Refresh Interval
        JPanel intervalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        intervalPanel.add(new JLabel("Refresh Interval (seconds):"));
        refreshIntervalSpinner.setPreferredSize(new Dimension(80, 25));
        intervalPanel.add(refreshIntervalSpinner);
        intervalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(intervalPanel);
        
        // Fraction Digits
        JPanel digitsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        digitsPanel.add(new JLabel("Fraction Digits:"));
        fractionDigitsSpinner.setPreferredSize(new Dimension(60, 25));
        digitsPanel.add(fractionDigitsSpinner);
        digitsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(digitsPanel);
        
        // Filler
        add(Box.createVerticalGlue());
    }
    
    public void loadConfig(CryptoPluginConfig config) {
        // Fast loading without excessive validation
        this.currentConfig = config;
        
        // Direct assignment for speed
        symbolsField.setText(String.join(",", config.getSymbols()));
        stableSymbolField.setText(config.getStableSymbol());
        refreshIntervalSpinner.setValue(config.getRefreshInterval() / 1000);
        fractionDigitsSpinner.setValue(config.getFractionDigits());
    }
    
    public CryptoPluginConfig saveConfig() {
        CryptoPluginConfig config = new CryptoPluginConfig();
        
        String symbolsText = symbolsField.getText().trim();
        if (!symbolsText.isEmpty()) {
            config.setSymbols(Arrays.asList(symbolsText.split("\\s*,\\s*")));
        }
        
        config.setStableSymbol(stableSymbolField.getText().trim());
        config.setRefreshInterval((Integer) refreshIntervalSpinner.getValue() * 1000);
        config.setFractionDigits((Integer) fractionDigitsSpinner.getValue());
        config.setShowIcon(false); // Always false for simplicity
        config.setShowInStatusBar(true); // Always true for simplicity
        
        return config;
    }
    
    public boolean isModified() {
        if (currentConfig == null) {
            return true;
        }
        
        CryptoPluginConfig uiConfig = saveConfig();
        return !currentConfig.equals(uiConfig);
    }
}