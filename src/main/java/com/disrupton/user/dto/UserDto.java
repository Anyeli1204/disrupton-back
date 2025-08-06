package com.disrupton.user.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id; // ID del documento en Firestore
    private String name;
    private String email;
    private String role; // student, moderator, admin
    private Timestamp createdAt;

    private Boolean isPremium; // si tiene premium activo
    private Timestamp premiumExpiresAt; // opcional: cuándo expira
}