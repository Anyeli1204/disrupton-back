package com.disrupton.comment.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private com.disrupton.user.model.User author;
    private com.disrupton.cultural.model.CulturalObject culturalObject;
    private Boolean isModerated;
    
    // Para respuestas anidadas
    private Long parentCommentId;
} 