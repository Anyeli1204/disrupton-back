package com.disrupton.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String region; 
    private String role; 
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Boolean isActive;
    
    public enum Role {
        STUDENT, MODERATOR, ADMIN
    }
} 