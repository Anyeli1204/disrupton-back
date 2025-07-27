package com.disrupton.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reaction {

    private String id;
    private String type; 
    private LocalDateTime createdAt;

    private String culturalObject;

    private String user;
    public enum ReactionType {
        LIKE, LOVE, WOW, INTERESTING, EDUCATIONAL, CULTURAL_HERITAGE
    }
} 