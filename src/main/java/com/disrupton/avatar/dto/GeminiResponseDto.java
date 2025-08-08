package com.disrupton.avatar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para las respuestas de Gemini API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponseDto {
    
    @JsonProperty("candidates")
    private List<Candidate> candidates;
    
    @JsonProperty("promptFeedback")
    private PromptFeedback promptFeedback;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        @JsonProperty("content")
        private Content content;
        
        @JsonProperty("finishReason")
        private String finishReason;
        
        @JsonProperty("index")
        private Integer index;
        
        @JsonProperty("safetyRatings")
        private List<SafetyRating> safetyRatings;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        @JsonProperty("parts")
        private List<Part> parts;
        
        @JsonProperty("role")
        private String role;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        @JsonProperty("text")
        private String text;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyRating {
        @JsonProperty("category")
        private String category;
        
        @JsonProperty("probability")
        private String probability;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptFeedback {
        @JsonProperty("safetyRatings")
        private List<SafetyRating> safetyRatings;
    }
}
