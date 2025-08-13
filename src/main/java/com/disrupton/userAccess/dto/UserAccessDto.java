package com.disrupton.userAccess.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessDto {
    private String id;
    private String userId;
    private String agentId;
    private String accessType;
    private String paymentId;
    private Timestamp grantedAt;
    private Timestamp expiresAt;
    private String status;
    private String description;
    private Double price;
    private String currency;
}

