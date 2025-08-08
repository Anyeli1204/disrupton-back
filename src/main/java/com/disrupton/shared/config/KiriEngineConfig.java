package com.disrupton.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para integración con KIRI Engine API
 */
@Configuration
public class KiriEngineConfig {
    
    @Value("${kiri.engine.api.key}")
    private String apiKey;
    
    @Value("${kiri.engine.api.base-url:https://api.kiriengine.app/api/v1}")
    private String baseUrl;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}
