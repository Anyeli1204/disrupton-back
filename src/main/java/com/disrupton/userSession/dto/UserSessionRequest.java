package com.disrupton.userSession.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionRequest {
    private String userId;
    private String sessionType;
    private String location;
    private Double latitude;
    private Double longitude;
    private String deviceInfo;
    private String appVersion;
}
