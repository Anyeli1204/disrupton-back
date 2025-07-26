package com.disrupton.reaction.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reaction {
    
    private Long id;
    private String type; 
    private LocalDateTime createdAt;
    private com.disrupton.user.model.User user;
    private com.disrupton.cultural.model.CulturalObject culturalObject;
    
    public enum ReactionType {
        LIKE, LOVE, WOW, INTERESTING, EDUCATIONAL, CULTURAL_HERITAGE
    }
} 