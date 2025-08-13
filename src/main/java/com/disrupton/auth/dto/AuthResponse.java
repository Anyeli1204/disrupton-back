package com.disrupton.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String refreshToken;
    private String userId;
    private String email;
    private String displayName;
    private String message;
    private boolean success;
    
    public static AuthResponse success(String token, String refreshToken, String userId, String email, String displayName) {
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(userId)
                .email(email)
                .displayName(displayName)
                .success(true)
                .message("Autenticación exitosa")
                .build();
    }
    
    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
