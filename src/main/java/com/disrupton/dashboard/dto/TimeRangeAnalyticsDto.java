package com.disrupton.dashboard.dto;

import com.disrupton.analytics.dto.CulturalObjectViewMetricDto;
import com.disrupton.analytics.dto.UserActivityDto;
import com.disrupton.campus.dto.CampusZoneActivityDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeRangeAnalyticsDto {
    private String timeRange; // "daily", "weekly", "monthly"
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<CulturalObjectViewMetricDto> topViewedObjects;
    private List<CampusZoneActivityDto> topActiveZones;
    private List<ThemeInteractionDto> topThemes;
    private List<UserActivityDto> topActiveUsers;
    private OverallStatsDto stats;
}
