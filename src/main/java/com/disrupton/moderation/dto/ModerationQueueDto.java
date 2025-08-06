package com.disrupton.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationQueueDto {
    private String id; // ID del documento en Firestore
    private String objectId; // reference a cultural_objects
    private String submittedBy; // reference a users
    private String status; // pending, in_review, approved, rejected
    private String notes;
} 