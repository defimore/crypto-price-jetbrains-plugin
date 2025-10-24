package com.defimore.crypto.util;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Utility class for HTTP client configuration.
 */
public class HttpClientConfig {
    
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    
    /**
     * Create a configured HTTP client for API requests.
     * @return Configured HttpClient instance
     */
    public static HttpClient createClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
    
    /**
     * Get the request timeout duration.
     * @return Request timeout duration
     */
    public static Duration getRequestTimeout() {
        return Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);
    }
}