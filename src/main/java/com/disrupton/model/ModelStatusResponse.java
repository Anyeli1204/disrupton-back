package com.disrupton.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ModelStatusResponse {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("msg")
    private String message;
    
    @JsonProperty("data")
    private StatusData data;
    
    @JsonProperty("ok")
    private Boolean ok;
    
    @Data
    public static class StatusData {
        @JsonProperty("serialize")
        private String serialize;
        
        @JsonProperty("status")
        private Integer status;
    }
    
    /**
     * Obtiene la descripción del estado basado en el código
     */
    public String getStatusDescription() {
        if (data == null || data.getStatus() == null) {
            return "Unknown";
        }
        
        switch (data.getStatus()) {
            case -1: return "Uploading";
            case 0: return "Processing";
            case 1: return "Failed";
            case 2: return "Successful";
            case 3: return "Queuing";
            case 4: return "Expired";
            default: return "Unknown";
        }
    }
    
    /**
     * Verifica si el modelo está listo para descargar
     */
    public boolean isReady() {
        return data != null && data.getStatus() != null && data.getStatus() == 2;
    }
    
    /**
     * Verifica si el modelo falló
     */
    public boolean isFailed() {
        return data != null && data.getStatus() != null && data.getStatus() == 1;
    }
    
    /**
     * Verifica si el modelo expiró
     */
    public boolean isExpired() {
        return data != null && data.getStatus() != null && data.getStatus() == 4;
    }
} 