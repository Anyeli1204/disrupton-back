package com.disrupton.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDto {
    private List<CulturalObjectViewMetricDto> mostViewedObjects;
    private List<CampusZoneActivityDto> campusZoneActivity;
    private List<ThemeInteractionDto> themeInteractions;
    private OverallStatsDto overallStats;
    private LocalDateTime generatedAt;
}
