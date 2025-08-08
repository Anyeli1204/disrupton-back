package com.disrupton.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para integración con Gemini API
 */
@Configuration("sharedGeminiConfig")
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiConfig {
    
    /**
     * La clave API para Gemini
     */
    private String apiKey;
    
    /**
     * URL base para Gemini API
     */
    private String apiUrl = "https://generativelanguage.googleapis.com/v1beta";
    
    /**
     * Versión del modelo a utilizar
     */
    private String model = "gemini-pro";
    
    /**
     * Temperatura para generación de texto (0.0-1.0)
     */
    private double temperature = 0.7;
    
    /**
     * Número máximo de tokens en la respuesta
     */
    private int maxOutputTokens = 1024;
    
    /**
     * Timeout para las solicitudes a Gemini en milisegundos
     */
    private int timeoutMs = 30000;
}
