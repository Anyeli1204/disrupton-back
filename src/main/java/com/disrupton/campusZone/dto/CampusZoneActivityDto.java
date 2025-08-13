package com.disrupton.campusZone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusZoneActivityDto {
    private String zoneId;
    private String zoneName;
    private Long totalArSessions;
    private Long uniqueVisitors;
    private Double averageSessionDuration; // in minutes
    private Long dailyArSessions;
    private Long weeklyArSessions;
    private Long monthlyArSessions;
    private Double popularityScore;
}
