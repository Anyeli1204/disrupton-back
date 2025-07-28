package com.disrupton.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CulturalObjectInteraction {
    private String interactionId;
    private String objectId;
    private String objectName;
    private String userId;
    private String interactionType; // "view", "explore", "comment", "reaction", "share", "photo"
    private LocalDateTime timestamp;
    private Double duration; // in seconds (for view/explore interactions)
    private String zoneId;
    private String culturalType;
    private String theme;
    private String culture;
    private String period;
    private String region;
    private String additionalData; // JSON string for extra interaction data
}
