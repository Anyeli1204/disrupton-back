package com.disrupton.socialInteraction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialInteractionRequest {
    private String userId;
    private String targetId;
    private String targetType;
    private String interactionType;
    private String content;
    private Integer rating;
    private String location;
    private Double latitude;
    private Double longitude;
}
