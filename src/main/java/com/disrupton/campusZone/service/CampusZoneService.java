package com.disrupton.campusZone.service;

import com.disrupton.campusZone.dto.CampusZoneActivityDto;
import com.disrupton.campusZone.model.CampusZone;
import com.disrupton.service.FirebaseAnalyticsService;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampusZoneService {
    
    private final Firestore firestore;
    private final FirebaseAnalyticsService firebaseAnalyticsService;
    
    private static final String CAMPUS_ZONES_COLLECTION = "campus_zones";
    
    /**
     * Get all campus zones
     */
    public List<CampusZone> getAllZones() {
        try {
            QuerySnapshot snapshot = firestore.collection(CAMPUS_ZONES_COLLECTION).get().get();
            
            List<CampusZone> zones = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                CampusZone zone = document.toObject(CampusZone.class);
                zones.add(zone);
            }
            
            log.info("Retrieved {} campus zones", zones.size());
            return zones;
            
        } catch (Exception e) {
            log.error("Error getting all zones: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get zone by ID
     */
    public CampusZone getZoneById(String zoneId) {
        try {
            DocumentSnapshot document = firestore.collection(CAMPUS_ZONES_COLLECTION)
                    .document(zoneId)
                    .get()
                    .get();
            
            if (document.exists()) {
                return document.toObject(CampusZone.class);
            } else {
                log.warn("Zone not found with ID: {}", zoneId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error getting zone by ID: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Create new zone
     */
    public CampusZone createZone(CampusZone zone) {
        try {
            firestore.collection(CAMPUS_ZONES_COLLECTION)
                    .document(zone.getZoneId())
                    .set(zone)
                    .get();
            
            log.info("Zone created successfully: {}", zone.getZoneId());
            return zone;
            
        } catch (Exception e) {
            log.error("Error creating zone: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create zone", e);
        }
    }
    
    /**
     * Update zone
     */
    public CampusZone updateZone(CampusZone zone) {
        try {
            firestore.collection(CAMPUS_ZONES_COLLECTION)
                    .document(zone.getZoneId())
                    .set(zone)
                    .get();
            
            log.info("Zone updated successfully: {}", zone.getZoneId());
            return zone;
            
        } catch (Exception e) {
            log.error("Error updating zone: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update zone", e);
        }
    }
    
    /**
     * Delete zone
     */
    public boolean deleteZone(String zoneId) {
        try {
            DocumentReference docRef = firestore.collection(CAMPUS_ZONES_COLLECTION)
                    .document(zoneId);
            
            DocumentSnapshot document = docRef.get().get();
            if (document.exists()) {
                docRef.delete().get();
                log.info("Zone deleted successfully: {}", zoneId);
                return true;
            } else {
                log.warn("Zone not found for deletion: {}", zoneId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error deleting zone: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Toggle zone status (active/inactive)
     */
    public CampusZone toggleZoneStatus(String zoneId, boolean isActive) {
        try {
            CampusZone zone = getZoneById(zoneId);
            if (zone == null) {
                return null;
            }
            
            zone.setIsActive(isActive);
            zone.setUpdatedAt(LocalDateTime.now());
            
            return updateZone(zone);
            
        } catch (Exception e) {
            log.error("Error toggling zone status: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get zones within radius of a location
     */
    public List<CampusZone> getZonesNearby(Double latitude, Double longitude, Double radiusMeters) {
        try {
            List<CampusZone> allZones = getAllZones();
            List<CampusZone> nearbyZones = new ArrayList<>();
            
            for (CampusZone zone : allZones) {
                if (zone.getLatitude() != null && zone.getLongitude() != null) {
                    double distance = calculateDistance(latitude, longitude, 
                                                     zone.getLatitude(), zone.getLongitude());
                    
                    if (distance <= radiusMeters) {
                        nearbyZones.add(zone);
                    }
                }
            }
            
            log.info("Found {} zones within {} meters of ({}, {})", 
                    nearbyZones.size(), radiusMeters, latitude, longitude);
            
            return nearbyZones;
            
        } catch (Exception e) {
            log.error("Error getting nearby zones: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get zone activity metrics
     */
    public CampusZoneActivityDto getZoneActivity(String zoneId, String timeRange) {
        try {
            // This would typically integrate with analytics service
            // For now, return mock data
            CampusZone zone = getZoneById(zoneId);
            if (zone == null) {
                return null;
            }
            
            CampusZoneActivityDto activity = new CampusZoneActivityDto();
            activity.setZoneId(zoneId);
            activity.setZoneName(zone.getZoneName());
            activity.setTotalArSessions(100L);
            activity.setUniqueVisitors(50L);
            activity.setAverageSessionDuration(15.5);
            activity.setDailyArSessions(10L);
            activity.setWeeklyArSessions(45L);
            activity.setMonthlyArSessions(180L);
            activity.setPopularityScore(85.5);
            
            return activity;
            
        } catch (Exception e) {
            log.error("Error getting zone activity: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
