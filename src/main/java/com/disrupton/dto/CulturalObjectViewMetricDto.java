package com.disrupton.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CulturalObjectViewMetricDto {
    private String objectId;
    private String objectName;
    private String culturalType;
    private String region;
    private Long totalViews;
    private Long dailyViews;
    private Long weeklyViews;
    private Long monthlyViews;
    private Double averageExplorationTime; // in seconds
    private Long totalComments;
    private Long totalReactions;
    private Long totalShares;
}
