package com.disrupton.userAccess.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessRequest {
    private String userId;
    private String agentId;
    private String accessType;
    private String paymentId;
    private String description;
    private Double price;
    private String currency;
    private Integer durationDays; // Duración en días
}

