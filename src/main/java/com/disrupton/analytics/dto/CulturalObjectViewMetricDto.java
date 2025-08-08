package com.disrupton.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CulturalObjectViewMetricDto {
    private String objectId;
    private String objectName;
    private String objectDescription;
    private String objectType;
    private String culturalType;
    private String theme;
    private String campusZone;
    private Long totalViews;
    private Long dailyViews;
    private Long weeklyViews;
    private Long monthlyViews;
    private Long totalComments;
    private Long totalReactions;
    private Long totalShares;
    private Double averageViewDuration; // in minutes
    private Double averageExplorationTime; // in minutes
    private Double engagementScore;
    private Integer uniqueViewers;
    private LocalDateTime lastViewedAt;
    private LocalDateTime createdAt;
}
