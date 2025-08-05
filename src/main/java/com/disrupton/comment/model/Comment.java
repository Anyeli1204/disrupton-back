package com.disrupton.comment.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    
    private String id; // ID del documento en Firestore
    private String content;
    private LocalDateTime createdAt;
    private String authorId; // ID del usuario autor
    private String culturalObjectId; // ID del objeto cultural
    private Boolean isModerated;
    
    // Para respuestas anidadas
    private String parentCommentId;
} 