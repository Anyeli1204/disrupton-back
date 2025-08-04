package com.disrupton.dto;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private String id;

    @PropertyName("text")
    private String text;

    @PropertyName("createdAt")
    private Timestamp createdAt;

    @PropertyName("culturalObjectId")
    private String culturalObjectId;

    @PropertyName("userId")
    private String userId;

    @PropertyName("parentCommentId")
    private String parentCommentId;

    @PropertyName("preguntaId")
    private String preguntaId;
} 