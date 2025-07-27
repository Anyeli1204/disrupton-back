package com.disrupton.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CulturalObjectDto {
    private String title;
    private String description;
    private String modelUrl; // URL en Firebase Storage del modelo 3D
    private String createdBy; // reference a documento en users
    private Timestamp createdAt;
    private String status; // pending, approved, rejected
    private String region; // Región de origen
    
    // Información de ubicación
    private Double latitude;
    private Double longitude;
    private String department;
    private String district;
    private String street;
    private String city;
    private String country;
    private String postalCode;
    private String fullAddress;
} 