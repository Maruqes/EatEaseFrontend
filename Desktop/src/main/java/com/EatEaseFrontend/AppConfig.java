package com.EatEaseFrontend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Global configuration class for the EatEase application.
 * Loads configuration from properties file or uses defaults.
 */
public class AppConfig {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "/config.properties";

    // Default values in case the config file is not found
    private static final String DEFAULT_API_BASE_URL = "https://p2.maruqes.com:10513";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = AppConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Could not load config file. Using default values.");
        }
    }

    /**
     * Gets the API base URL from the config or uses the default value.
     * 
     * @return The API base URL
     */
    public static String getApiBaseUrl() {
        return properties.getProperty("api.base.url", DEFAULT_API_BASE_URL);
    }

    /**
     * Gets a specific API endpoint by appending the path to the base URL.
     *
     * @param path The API endpoint path (e.g., "/auth/login")
     * @return The full API endpoint URL
     */
    public static String getApiEndpoint(String path) {
        return getApiBaseUrl() + path;
    }

    /**
     * Updates and saves a configuration property.
     * 
     * @param key   The property key
     * @param value The property value
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        // In a real app, you'd save this back to the config file
    }
}