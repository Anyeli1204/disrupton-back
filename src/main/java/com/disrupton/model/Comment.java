package com.disrupton.model;

import com.google.cloud.firestore.annotation.PropertyName;
import lombok.Data;
import com.google.cloud.Timestamp;


@Data
public class Comment {

    private String id;

    private String content;

    private Timestamp createdAt;

    private boolean isModerated;

    private String culturalObjectId;

    private String authorUserId;

    private String parentCommentId; //Id del comentario padre si es una subrespuesta

    private String preguntaId;

    private String responseTimeMs;
}
