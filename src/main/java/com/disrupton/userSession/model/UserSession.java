package com.disrupton.userSession.model;

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
    private String sessionType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String location;
    private Double latitude;
    private Double longitude;
    private String deviceInfo;
    private String appVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
