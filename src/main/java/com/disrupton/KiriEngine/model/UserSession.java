package com.disrupton.KiriEngine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    private String sessionId;
    private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double duration; // in minutes
    private String zoneId;
    private String zoneName;
    private Double latitude;
    private Double longitude;
    private String deviceType; // "mobile", "tablet", "ar_glasses"
    private String sessionType; // "ar_exploration", "social_interaction", "content_creation"
}
