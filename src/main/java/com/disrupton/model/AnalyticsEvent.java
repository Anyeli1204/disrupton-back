package com.disrupton.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {
    private String eventId;
    private String userId;
    private String sessionId;
    private String eventType; // "app_open", "ar_start", "object_view", "zone_enter", "zone_exit", etc.
    private String objectId;
    private String zoneId;
    private LocalDateTime timestamp;
    private Double duration; // for events with duration
    private String eventData; // JSON string with additional event data
    private String deviceInfo; // device type, OS, app version, etc.
    private Double latitude;
    private Double longitude;
}
