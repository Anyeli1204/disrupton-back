package com.disrupton.culturalObject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CulturalObject {
    private String objectId;
    private String name;
    private String description;
    private String culturalType;
    private String theme;
    private String culture;
    private String period;
    private String region;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String model3dUrl;
    private String audioUrl;
    private String videoUrl;
    private String additionalInfo;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
