package com.disrupton.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverallStatsDto {
    private Long totalUsers;
    private Long activeUsersToday;
    private Long activeUsersThisWeek;
    private Long activeUsersThisMonth;
    private Long totalCulturalObjects;
    private Long totalViews;
    private Long totalComments;
    private Long totalReactions;
    private Long totalShares;
    private Long totalPhotos;
    private Double averageSessionDuration; // in minutes
    private Double averageExplorationTimePerObject; // in minutes
    private Long socialFunctionUsage; // total social interactions
    private Double userRetentionRate; // percentage
}
