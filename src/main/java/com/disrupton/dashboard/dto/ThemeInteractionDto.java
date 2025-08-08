package com.disrupton.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeInteractionDto {
    private String theme; // e.g., "Inca Culture", "Modern Art", "Colonial Period"
    private String culture; // e.g., "Inca", "Moche", "Chavin"
    private String period; // e.g., "Pre-Columbian", "Colonial", "Modern"
    private Long totalInteractions;
    private Long dailyInteractions;
    private Long weeklyInteractions;
    private Long monthlyInteractions;
    private Double averageEngagementTime; // in minutes
    private Long commentsCount;
    private Long reactionsCount;
    private Long sharesCount;
    private Long photosCount;
    private Double interactionScore; // calculated engagement score
}
