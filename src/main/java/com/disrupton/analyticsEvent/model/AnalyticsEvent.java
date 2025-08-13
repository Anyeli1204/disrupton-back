package com.disrupton.analyticsEvent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {
    private String eventId;
    private String userId;
    private String sessionId;
    private String eventType;
    private String eventName;
    private Map<String, Object> eventData;
    private String location;
    private Double latitude;
    private Double longitude;
    private String deviceInfo;
    private String appVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
