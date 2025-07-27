package com.disrupton.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class User {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String region; 
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Boolean isActive;

    private List<String> contributedObjects;

     private List<String> moderatedObjects;

    public enum Role {
        STUDENT, MODERATOR, ADMIN, AGENTE_CULTURAL
    }
} 