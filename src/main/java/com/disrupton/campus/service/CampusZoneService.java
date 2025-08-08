package com.disrupton.campus.service;

import com.disrupton.campus.dto.CampusZoneActivityDto;
import com.disrupton.campus.model.CampusZone;
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
            firestore.collection(CAMPUS_ZONES_COLLECTION)
                    .document(zoneId)
                    .delete()
                    .get();
            
            log.info("Zone deleted successfully: {}", zoneId);
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting zone: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Toggle zone status
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
     * Get zones nearby a location
     */
    public List<CampusZone> getZonesNearby(Double latitude, Double longitude, Double radiusMeters) {
        try {
            List<CampusZone> allZones = getAllZones();
            List<CampusZone> nearbyZones = new ArrayList<>();
            
            for (CampusZone zone : allZones) {
                if (zone.getLatitude() != null && zone.getLongitude() != null) {
                    double distance = calculateDistance(
                            latitude, longitude,
                            zone.getLatitude(), zone.getLongitude()
                    );
                    
                    if (distance <= radiusMeters) {
                        nearbyZones.add(zone);
                    }
                }
            }
            
            log.info("Found {} zones nearby location ({}, {}) within {} meters", 
                    nearbyZones.size(), latitude, longitude, radiusMeters);
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
            CampusZone zone = getZoneById(zoneId);
            if (zone == null) {
                return null;
            }
            
            // Get activity data from analytics service
            List<CampusZoneActivityDto> activities = firebaseAnalyticsService.getCampusZoneActivity(timeRange, 100);
            
            return activities.stream()
                    .filter(activity -> zoneId.equals(activity.getZoneId()))
                    .findFirst()
                    .orElse(createEmptyActivityDto(zone));
            
        } catch (Exception e) {
            log.error("Error getting zone activity: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get zones by type
     */
    public List<CampusZone> getZonesByType(String zoneType) {
        try {
            Query query = firestore.collection(CAMPUS_ZONES_COLLECTION)
                    .whereEqualTo("zoneType", zoneType);
            
            QuerySnapshot snapshot = query.get().get();
            
            List<CampusZone> zones = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                CampusZone zone = document.toObject(CampusZone.class);
                zones.add(zone);
            }
            
            log.info("Retrieved {} zones of type: {}", zones.size(), zoneType);
            return zones;
            
        } catch (Exception e) {
            log.error("Error getting zones by type: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get active zones only
     */
    public List<CampusZone> getActiveZones() {
        try {
            Query query = firestore.collection(CAMPUS_ZONES_COLLECTION)
                    .whereEqualTo("isActive", true);
            
            QuerySnapshot snapshot = query.get().get();
            
            List<CampusZone> zones = new ArrayList<>();
            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                CampusZone zone = document.toObject(CampusZone.class);
                zones.add(zone);
            }
            
            log.info("Retrieved {} active zones", zones.size());
            return zones;
            
        } catch (Exception e) {
            log.error("Error getting active zones: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // Helper methods
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371000; // Earth radius in meters
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in meters
    }
    
    /**
     * Create empty activity DTO for zones with no activity
     */
    private CampusZoneActivityDto createEmptyActivityDto(CampusZone zone) {
        CampusZoneActivityDto activity = new CampusZoneActivityDto();
        activity.setZoneId(zone.getZoneId());
        activity.setZoneName(zone.getZoneName());
        activity.setZoneDescription(zone.getDescription());
        activity.setLatitude(zone.getLatitude());
        activity.setLongitude(zone.getLongitude());
        activity.setTotalArSessions(0L);
        activity.setDailyArSessions(0L);
        activity.setWeeklyArSessions(0L);
        activity.setMonthlyArSessions(0L);
        activity.setAverageSessionDuration(0.0);
        activity.setUniqueVisitors(0L);
        activity.setMostActiveTimeSlot("none");
        
        return activity;
    }
}
