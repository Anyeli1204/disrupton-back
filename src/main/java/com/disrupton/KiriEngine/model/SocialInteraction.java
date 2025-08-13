package com.disrupton.KiriEngine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialInteraction {
    private String interactionId;
    private String userId;
    private String targetId; // culturalObjectId, commentId, etc.
    private String targetType; // "cultural_object", "comment", "user"
    private String interactionType; // "comment", "reaction", "share", "photo", "like", "dislike"
    private String content; // comment text, reaction emoji, etc.
    private LocalDateTime timestamp;
    private String zoneId;
    private String culturalTheme;
    private String metadata; // JSON string for additional data
}
