package com.disrupton.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reaction {
    
    private String id; // ID del documento en Firestore
    private String type; 
    private LocalDateTime createdAt;
    private String userId; // ID del usuario
    private String culturalObjectId; // ID del objeto cultural
    
    public enum ReactionType {
        LIKE, LOVE, WOW, INTERESTING, EDUCATIONAL, CULTURAL_HERITAGE
    }
} 