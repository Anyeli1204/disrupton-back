package com.disrupton.comment.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String id; // ID del documento en Firestore
    private String objectId; // reference a cultural_objects
    private String userId; // reference a users
    private String text;
    private Timestamp createdAt;
} 