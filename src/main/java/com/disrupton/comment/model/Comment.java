package com.disrupton.comment.model;

import com.google.cloud.Timestamp;
import lombok.Data;

import java.util.List;

@Data
public class Comment {

    private String id; // ID del documento en Firestore
    private String content;
    private Timestamp createdAt;
    private String authorId; // ID del usuario autor
    private String culturalObjectId; // ID del objeto cultural
    private Boolean isModerated;

    // Para respuestas anidadas
    private String parentCommentId;
    private String preguntaId;

    // URLs de las imágenes adjuntas
    private List<String> imageUrls;
}