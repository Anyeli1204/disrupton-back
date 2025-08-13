package com.disrupton.culturalObject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CulturalObjectRequest {
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
}
