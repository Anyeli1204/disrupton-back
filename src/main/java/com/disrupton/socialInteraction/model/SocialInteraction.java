package com.disrupton.socialInteraction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialInteraction {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
