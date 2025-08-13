package com.disrupton.userSession.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDto {
    private String sessionId;
    private String userId;
    private String sessionType;
    private String status;
    private Timestamp startTime;
    private Timestamp endTime;
    private Long duration;
    private String location;
    private Double latitude;
    private Double longitude;
    private String deviceInfo;
    private String appVersion;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
