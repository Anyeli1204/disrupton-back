package com.disrupton.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private User author;
    private CulturalObject culturalObject;
    private Boolean isModerated;
    
    // Para respuestas anidadas
    private Long parentCommentId;
} 