package com.defimore.crypto;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Action to open the crypto price plugin settings.
 * This provides quick access to plugin configuration.
 */
public class OpenSettingsAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(
            e.getProject(), 
            CryptoPluginConfigurable.class
        );
    }
}