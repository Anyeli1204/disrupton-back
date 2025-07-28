package com.disrupton.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Comment {

    private String id; // Firestore document ID (se puede asignar desde backend o dejar que Firebase lo genere)
    private String content;
    private LocalDateTime createdAt;
    private boolean isModerated;

    // Referencias por ID
    private String culturalObjectId;  // ID del objeto cultural relacionado
    private String authorUserId;      // ID del usuario que escribió el comentario

    // Para comentarios anidados
    private String parentCommentId;   // ID del comentario padre, si es una respuesta
}
