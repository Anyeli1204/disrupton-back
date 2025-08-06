package com.disrupton.KiriEngine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ModelDownloadResponse {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("msg")
    private String message;
    
    @JsonProperty("data")
    private DownloadData data;
    
    @JsonProperty("ok")
    private Boolean ok;
    
    @Data
    public static class DownloadData {
        @JsonProperty("modelUrl")
        private String modelUrl;
        
        @JsonProperty("serialize")
        private String serialize;
    }
} 