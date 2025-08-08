package com.disrupton.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDto {
    private String userId;
    private String username;
    private String email;
    private String userType; // "student", "professor", "admin"
    private Long totalArSessions;
    private Long totalSessions;
    private Long dailyArSessions;
    private Long weeklyArSessions;
    private Long monthlyArSessions;
    private Double averageSessionDuration; // in minutes
    private Double totalExplorationTime; // in minutes
    private Long totalInteractions;
    private Integer culturalObjectsViewed;
    private Integer commentsPosted;
    private Integer commentsCount;
    private Integer reactionsGiven;
    private Integer reactionsCount;
    private Integer sharesCount;
    private Integer photosCount;
    private String engagementLevel; // "low", "medium", "high"
    private LocalDateTime lastActivityAt;
    private LocalDateTime registeredAt;
}
