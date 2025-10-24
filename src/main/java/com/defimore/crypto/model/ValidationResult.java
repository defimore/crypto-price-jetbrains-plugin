package com.defimore.crypto.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of configuration validation containing errors and warnings.
 */
public class ValidationResult {
    
    private final List<String> errors;
    private final List<String> warnings;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    /**
     * Add an error message.
     * @param error Error message
     */
    public void addError(String error) {
        errors.add(error);
    }
    
    /**
     * Add a warning message.
     * @param warning Warning message
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }
    
    /**
     * Check if validation passed (no errors).
     * @return true if valid
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
    
    /**
     * Check if there are any warnings.
     * @return true if there are warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * Get all error messages.
     * @return List of error messages
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Get all warning messages.
     * @return List of warning messages
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    /**
     * Get a formatted error message containing all errors.
     * @return Formatted error message
     */
    public String getErrorMessage() {
        if (errors.isEmpty()) {
            return null;
        }
        return String.join("\n", errors);
    }
    
    /**
     * Get a formatted warning message containing all warnings.
     * @return Formatted warning message
     */
    public String getWarningMessage() {
        if (warnings.isEmpty()) {
            return null;
        }
        return String.join("\n", warnings);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValidationResult{");
        if (!errors.isEmpty()) {
            sb.append("errors=").append(errors);
        }
        if (!warnings.isEmpty()) {
            if (!errors.isEmpty()) sb.append(", ");
            sb.append("warnings=").append(warnings);
        }
        sb.append('}');
        return sb.toString();
    }
}