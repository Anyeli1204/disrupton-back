package com.disrupton.socialInteraction.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialInteractionDto {
    private String interactionId;
    private String userId;
    private String targetId;
    private String targetType;
    private String interactionType;
    private String content;
    private Integer rating;
    private String location;
    private Double latitude;
    private Double longitude;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
