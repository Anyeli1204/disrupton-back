package com.disrupton.kiriengine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KiriEngineResponse {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("msg")
    private String message;
    
    @JsonProperty("data")
    private ResponseData data;
    
    @JsonProperty("ok")
    private Boolean ok;
    
    @Data
    public static class ResponseData {
        @JsonProperty("serialize")
        private String serialize;
        
        @JsonProperty("calculateType")
        private Integer calculateType;
    }
} 