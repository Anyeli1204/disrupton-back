package com.disrupton.culturalObjectInteraction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CulturalObjectInteractionRequest {
    private String userId;
    private String objectId;
    private String interactionType;
    private String content;
    private Integer rating;
    private String location;
    private Double latitude;
    private Double longitude;
}
