package com.disrupton.culturalObjectInteraction.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CulturalObjectInteractionDto {
    private String interactionId;
    private String userId;
    private String objectId;
    private String interactionType;
    private String content;
    private Integer rating;
    private String location;
    private Double latitude;
    private Double longitude;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
