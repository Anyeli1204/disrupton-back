package com.disrupton.userAccess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccess {
    private String id;
    private String userId;
    private String agentId;
    private String accessType;
    private String paymentId;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private String status; // "active", "expired", "revoked"
    private String description;
    private Double price;
    private String currency;
}

