package com.disrupton.model;

import lombok.Data;
import java.time.LocalDateTime;


@Data
public class Comment {

    private String id;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isModerated;


    private CulturalObject culturalObject;

    private User author;
    // Para respuestas anidadas
    private Long parentCommentId;
} 