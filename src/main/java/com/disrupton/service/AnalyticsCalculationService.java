package com.disrupton.service;

import com.disrupton.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsCalculationService {
    
    /**
     * Calculate engagement score for cultural objects
     */
    public Double calculateEngagementScore(CulturalObjectViewMetricDto metric) {
        if (metric == null) return 0.0;
        
        double score = 0.0;
        
        // Views contribute to score (weight: 1.0)
        score += metric.getTotalViews() * 1.0;
        
        // Comments have higher weight (weight: 3.0)
        score += metric.getTotalComments() * 3.0;
        
        // Reactions contribute (weight: 2.0)
        score += metric.getTotalReactions() * 2.0;
        
        // Shares are highly valuable (weight: 5.0)
        score += metric.getTotalShares() * 5.0;
        
        // Average exploration time contributes (weight: 0.1 per second)
        score += (metric.getAverageExplorationTime() != null ? 
                 metric.getAverageExplorationTime() * 0.1 : 0.0);
        
        return score;
    }
    
    /**
     * Calculate zone popularity score
     */
    public Double calculateZonePopularityScore(CampusZoneActivityDto activity) {
        if (activity == null) return 0.0;
        
        double score = 0.0;
        
        // Total sessions contribute (weight: 1.0)
        score += activity.getTotalArSessions() * 1.0;
        
        // Unique visitors are important (weight: 2.0)
        score += activity.getUniqueVisitors() * 2.0;
        
        // Average session duration contributes (weight: 0.5 per minute)
        score += (activity.getAverageSessionDuration() != null ? 
                 activity.getAverageSessionDuration() * 0.5 : 0.0);
        
        // Recent activity is more valuable
        score += activity.getDailyArSessions() * 5.0; // Higher weight for recent activity
        score += activity.getWeeklyArSessions() * 2.0;
        
        return score;
    }
    
    /**
     * Calculate theme interaction score
     */
    public Double calculateThemeInteractionScore(ThemeInteractionDto interaction) {
        if (interaction == null) return 0.0;
        
        double score = 0.0;
        
        // Base interactions (weight: 1.0)
        score += interaction.getTotalInteractions() * 1.0;
        
        // Comments are highly valuable (weight: 4.0)
        score += interaction.getCommentsCount() * 4.0;
        
        // Reactions contribute (weight: 2.0)
        score += interaction.getReactionsCount() * 2.0;
        
        // Shares are very valuable (weight: 6.0)
        score += interaction.getSharesCount() * 6.0;
        
        // Photos show engagement (weight: 3.0)
        score += interaction.getPhotosCount() * 3.0;
        
        // Average engagement time (weight: 0.2 per minute)
        score += (interaction.getAverageEngagementTime() != null ? 
                 interaction.getAverageEngagementTime() * 0.2 : 0.0);
        
        return score;
    }
    
    /**
     * Calculate user engagement level
     */
    public String calculateUserEngagementLevel(UserActivityDto user) {
        if (user == null) return "Low";
        
        double score = 0.0;
        
        // Session frequency
        score += user.getTotalSessions() * 2.0;
        
        // Total exploration time (minutes)
        score += (user.getTotalExplorationTime() != null ? 
                 user.getTotalExplorationTime() * 0.1 : 0.0);
        
        // Interactions
        score += user.getTotalInteractions() * 1.5;
        score += user.getCommentsCount() * 3.0;
        score += user.getReactionsCount() * 2.0;
        score += user.getSharesCount() * 4.0;
        score += user.getPhotosCount() * 2.5;
        
        if (score >= 100) return "Very High";
        else if (score >= 50) return "High";
        else if (score >= 20) return "Medium";
        else if (score >= 5) return "Low";
        else return "Very Low";
    }
    
    /**
     * Calculate content optimization recommendations
     */
    public List<String> generateContentRecommendations(DashboardMetricsDto metrics) {
        List<String> recommendations = new ArrayList<>();
        
        if (metrics == null) return recommendations;
        
        // Analyze most viewed objects
        if (metrics.getMostViewedObjects() != null && !metrics.getMostViewedObjects().isEmpty()) {
            CulturalObjectViewMetricDto topObject = metrics.getMostViewedObjects().get(0);
            
            if (topObject.getAverageExplorationTime() != null && topObject.getAverageExplorationTime() < 30) {
                recommendations.add("Consider adding more interactive elements to cultural objects to increase exploration time");
            }
            
            if (topObject.getTotalComments() < topObject.getTotalViews() * 0.1) {
                recommendations.add("Add prompts or questions to encourage user comments and discussions");
            }
            
            // Find popular cultural types
            Map<String, Long> typePopularity = metrics.getMostViewedObjects().stream()
                    .collect(Collectors.groupingBy(
                            obj -> obj.getCulturalType() != null ? obj.getCulturalType() : "Unknown",
                            Collectors.summingLong(CulturalObjectViewMetricDto::getTotalViews)
                    ));
            
            String mostPopularType = typePopularity.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");
            
            recommendations.add("Focus on creating more content related to " + mostPopularType + 
                              " as it shows high user engagement");
        }
        
        // Analyze zone activity
        if (metrics.getCampusZoneActivity() != null && !metrics.getCampusZoneActivity().isEmpty()) {
            CampusZoneActivityDto topZone = metrics.getCampusZoneActivity().get(0);
            
            recommendations.add("Consider expanding AR experiences in " + topZone.getZoneName() + 
                              " due to high user activity");
            
            // Find underutilized zones
            List<CampusZoneActivityDto> lowActivityZones = metrics.getCampusZoneActivity().stream()
                    .filter(zone -> zone.getTotalArSessions() < 10)
                    .collect(Collectors.toList());
            
            if (!lowActivityZones.isEmpty()) {
                recommendations.add("Improve content or add promotional campaigns for underutilized zones: " +
                                  lowActivityZones.stream()
                                          .map(CampusZoneActivityDto::getZoneName)
                                          .collect(Collectors.joining(", ")));
            }
        }
        
        // Analyze theme interactions
        if (metrics.getThemeInteractions() != null && !metrics.getThemeInteractions().isEmpty()) {
            ThemeInteractionDto topTheme = metrics.getThemeInteractions().get(0);
            
            recommendations.add("Create thematic routes or collections around " + topTheme.getTheme() + 
                              " to leverage high user interest");
            
            // Find themes with low interaction scores
            List<ThemeInteractionDto> lowEngagementThemes = metrics.getThemeInteractions().stream()
                    .filter(theme -> theme.getInteractionScore() < 50)
                    .collect(Collectors.toList());
            
            if (!lowEngagementThemes.isEmpty()) {
                recommendations.add("Review and enhance content for themes with low engagement: " +
                                  lowEngagementThemes.stream()
                                          .map(ThemeInteractionDto::getTheme)
                                          .collect(Collectors.joining(", ")));
            }
        }
        
        // Analyze overall stats
        if (metrics.getOverallStats() != null) {
            OverallStatsDto stats = metrics.getOverallStats();
            
            if (stats.getAverageSessionDuration() != null && stats.getAverageSessionDuration() < 10) {
                recommendations.add("Sessions are relatively short. Consider adding guided tours or challenges to increase engagement time");
            }
            
            if (stats.getSocialFunctionUsage() != null && stats.getTotalViews() != null && 
                stats.getSocialFunctionUsage() < stats.getTotalViews() * 0.2) {
                recommendations.add("Social features are underutilized. Add incentives for sharing, commenting, and taking photos");
            }
            
            if (stats.getUserRetentionRate() != null && stats.getUserRetentionRate() < 50) {
                recommendations.add("User retention is low. Consider implementing notification systems and regular content updates");
            }
        }
        
        return recommendations.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Generate insights from analytics data
     */
    public List<String> generateInsights(DashboardMetricsDto metrics) {
        List<String> insights = new ArrayList<>();
        
        if (metrics == null) return insights;
        
        // Peak activity analysis
        if (metrics.getCampusZoneActivity() != null && !metrics.getCampusZoneActivity().isEmpty()) {
            long totalSessions = metrics.getCampusZoneActivity().stream()
                    .mapToLong(CampusZoneActivityDto::getTotalArSessions)
                    .sum();
            
            if (totalSessions > 0) {
                insights.add("Total AR sessions across all zones: " + totalSessions);
                
                CampusZoneActivityDto mostActive = metrics.getCampusZoneActivity().get(0);
                double percentage = (mostActive.getTotalArSessions().doubleValue() / totalSessions) * 100;
                insights.add(String.format("%.1f%% of all AR activity happens in %s", percentage, mostActive.getZoneName()));
            }
        }
        
        // Cultural interest patterns
        if (metrics.getThemeInteractions() != null && !metrics.getThemeInteractions().isEmpty()) {
            Map<String, Long> culturePopularity = metrics.getThemeInteractions().stream()
                    .collect(Collectors.groupingBy(
                            theme -> theme.getCulture() != null ? theme.getCulture() : "Unknown",
                            Collectors.summingLong(ThemeInteractionDto::getTotalInteractions)
                    ));
            
            String topCulture = culturePopularity.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");
            
            insights.add("Most popular cultural topic: " + topCulture);
        }
        
        // Engagement patterns
        if (metrics.getMostViewedObjects() != null && !metrics.getMostViewedObjects().isEmpty()) {
            double avgExplorationTime = metrics.getMostViewedObjects().stream()
                    .mapToDouble(obj -> obj.getAverageExplorationTime() != null ? obj.getAverageExplorationTime() : 0.0)
                    .average()
                    .orElse(0.0);
            
            insights.add(String.format("Average exploration time per object: %.1f seconds", avgExplorationTime));
            
            // Social engagement rate
            long totalViews = metrics.getMostViewedObjects().stream()
                    .mapToLong(CulturalObjectViewMetricDto::getTotalViews)
                    .sum();
            
            long totalSocialInteractions = metrics.getMostViewedObjects().stream()
                    .mapToLong(obj -> obj.getTotalComments() + obj.getTotalReactions() + obj.getTotalShares())
                    .sum();
            
            if (totalViews > 0) {
                double socialEngagementRate = ((double) totalSocialInteractions / totalViews) * 100;
                insights.add(String.format("Social engagement rate: %.1f%%", socialEngagementRate));
            }
        }
        
        // Usage trends
        if (metrics.getOverallStats() != null) {
            OverallStatsDto stats = metrics.getOverallStats();
            
            if (stats.getActiveUsersToday() != null && stats.getActiveUsersThisWeek() != null && 
                stats.getActiveUsersThisWeek() > 0) {
                double dailyEngagementRate = (stats.getActiveUsersToday().doubleValue() / stats.getActiveUsersThisWeek().doubleValue()) * 100;
                insights.add(String.format("Daily engagement rate: %.1f%% of weekly active users", dailyEngagementRate));
            }
            
            if (stats.getTotalUsers() != null && stats.getActiveUsersThisMonth() != null && 
                stats.getTotalUsers() > 0) {
                double monthlyEngagementRate = (stats.getActiveUsersThisMonth().doubleValue() / stats.getTotalUsers()) * 100;
                insights.add(String.format("Monthly engagement rate: %.1f%% of total users", monthlyEngagementRate));
            }
        }
        
        return insights;
    }
    
    /**
     * Calculate growth rate between two periods
     */
    public Double calculateGrowthRate(Long currentValue, Long previousValue) {
        if (previousValue == null || previousValue == 0) return 0.0;
        if (currentValue == null) return -100.0;
        
        return ((currentValue.doubleValue() - previousValue.doubleValue()) / previousValue.doubleValue()) * 100.0;
    }
    
    /**
     * Predict future trends based on current data
     */
    public Map<String, Double> generateTrendPredictions(DashboardMetricsDto metrics) {
        Map<String, Double> predictions = new HashMap<>();
        
        if (metrics == null || metrics.getOverallStats() == null) return predictions;
        
        OverallStatsDto stats = metrics.getOverallStats();
        
        // Simple linear projection based on daily vs weekly growth
        if (stats.getActiveUsersToday() != null && stats.getActiveUsersThisWeek() != null) {
            double dailyAverage = stats.getActiveUsersThisWeek().doubleValue() / 7.0;
            double growthFactor = stats.getActiveUsersToday().doubleValue() / dailyAverage;
            
            predictions.put("nextWeekActiveUsers", stats.getActiveUsersThisWeek() * growthFactor);
            predictions.put("nextMonthActiveUsers", stats.getActiveUsersThisMonth() != null ? 
                    stats.getActiveUsersThisMonth() * growthFactor : stats.getActiveUsersThisWeek() * 4 * growthFactor);
        }
        
        // Predict social engagement growth
        if (stats.getSocialFunctionUsage() != null && stats.getTotalViews() != null) {
            double currentSocialRate = stats.getSocialFunctionUsage().doubleValue() / stats.getTotalViews().doubleValue();
            predictions.put("projectedSocialEngagementRate", Math.min(currentSocialRate * 1.1, 0.5)); // Cap at 50%
        }
        
        return predictions;
    }
}
