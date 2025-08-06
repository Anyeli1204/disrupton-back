package com.disrupton.cultural.dto;

import lombok.Data;

@Data
public class CulturalObjectRequest {
    
    private String name;
    private String description;
    private String origin;
    private String culturalType;
    private String localPhrases;
    private String story;
    private String contributorId;
    private Integer numberOfImages;
    private String captureNotes;
    private String region;
} 