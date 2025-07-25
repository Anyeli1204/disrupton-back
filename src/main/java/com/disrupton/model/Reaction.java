package com.disrupton.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reaction {
    
    private Long id;
    private String type; 
    private LocalDateTime createdAt;
    private User user;
    private CulturalObject culturalObject;
    
    public enum ReactionType {
        LIKE, LOVE, WOW, INTERESTING, EDUCATIONAL, CULTURAL_HERITAGE
    }
} 