package com.disrupton.analyticsEvent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEventRequest {
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
}
