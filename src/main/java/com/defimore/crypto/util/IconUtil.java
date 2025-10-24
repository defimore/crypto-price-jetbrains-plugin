package com.defimore.crypto.util;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Utility class for loading plugin icons.
 */
public class IconUtil {
    
    public static final Icon CRYPTO_ICON = IconLoader.getIcon("/icons/crypto-icon-16.svg", IconUtil.class);
    
    /**
     * Get the crypto icon (main plugin icon).
     */
    public static Icon getCryptoIcon() {
        return CRYPTO_ICON;
    }
    
    /**
     * Get the main plugin icon (same as crypto icon).
     */
    public static Icon getPluginIcon() {
        return CRYPTO_ICON;
    }
}