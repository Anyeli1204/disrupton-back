package com.disrupton.model;

import com.google.cloud.firestore.annotation.PropertyName;
import lombok.Data;
import com.google.cloud.Timestamp;

@Data
public class Comment {

    private String id;

    @PropertyName("content")
    private String content;

    @PropertyName("createdAt")
    private Timestamp createdAt;

    private boolean isModerated;

    @PropertyName("culturalObjectId")
    private String culturalObjectId;

    @PropertyName("authorUserId")
    private String authorUserId;

    @PropertyName("parentCommentId")
    private String parentCommentId;

    @PropertyName("preguntaId")
    private String preguntaId;

}
