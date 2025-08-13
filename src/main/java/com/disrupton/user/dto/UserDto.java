package com.disrupton.user.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String userId;
    private String email;
    private String name;
    private String role;
    private String profileImageUrl;
    private Boolean isActive;
    private Boolean isPremium;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp premiumExpiresAt;
}