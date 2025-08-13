package com.disrupton.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusZoneActivityDto {
    private String zoneId;
    private String zoneName;
    private String zoneDescription;
    private Double latitude;
    private Double longitude;
    private Long totalArSessions;
    private Long dailyArSessions;
    private Long weeklyArSessions;
    private Long monthlyArSessions;
    private Double averageSessionDuration; // in minutes
    private Long uniqueVisitors;
    private String mostActiveTimeSlot; // e.g., "morning", "afternoon", "evening"
}
