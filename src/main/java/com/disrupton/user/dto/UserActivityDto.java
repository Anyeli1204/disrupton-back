package com.disrupton.user.dto;

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
    private LocalDateTime lastActivity;
    private Long totalSessions;
    private Double totalExplorationTime; // in minutes
    private Long totalInteractions;
    private Long commentsCount;
    private Long reactionsCount;
    private Long sharesCount;
    private Long photosCount;
    private String favoriteTheme;
    private String favoriteCulture;
    private String mostVisitedZone;
    private LocalDateTime firstVisit;
    private String userType; // e.g., "student", "teacher", "visitor"
}
