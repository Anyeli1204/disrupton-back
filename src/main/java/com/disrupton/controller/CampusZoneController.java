package com.disrupton.controller;

import com.disrupton.dto.CampusZoneActivityDto;
import com.disrupton.model.CampusZone;
import com.disrupton.service.CampusZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/campus-zones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CampusZoneController {
    
    private final CampusZoneService campusZoneService;
    
    /**
     * Get all campus zones
     */
    @GetMapping
    public ResponseEntity<List<CampusZone>> getAllZones() {
        try {
            log.info("Getting all campus zones");
            
            List<CampusZone> zones = campusZoneService.getAllZones();
            
            return ResponseEntity.ok(zones);
            
        } catch (Exception e) {
            log.error("Error getting all zones: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get zone by ID
     */
    @GetMapping("/{zoneId}")
    public ResponseEntity<CampusZone> getZoneById(@PathVariable String zoneId) {
        try {
            log.info("Getting zone by ID: {}", zoneId);
            
            CampusZone zone = campusZoneService.getZoneById(zoneId);
            
            if (zone != null) {
                return ResponseEntity.ok(zone);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting zone by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create new campus zone
     */
    @PostMapping
    public ResponseEntity<CampusZone> createZone(@RequestBody CampusZoneRequest request) {
        try {
            log.info("Creating new campus zone: {}", request.getZoneName());
            
            CampusZone zone = new CampusZone();
            zone.setZoneId(UUID.randomUUID().toString());
            zone.setZoneName(request.getZoneName());
            zone.setDescription(request.getDescription());
            zone.setLatitude(request.getLatitude());
            zone.setLongitude(request.getLongitude());
            zone.setRadius(request.getRadius());
            zone.setZoneType(request.getZoneType());
            zone.setIsActive(true);
            zone.setCreatedAt(LocalDateTime.now());
            zone.setUpdatedAt(LocalDateTime.now());
            
            CampusZone savedZone = campusZoneService.createZone(zone);
            
            return ResponseEntity.ok(savedZone);
            
        } catch (Exception e) {
            log.error("Error creating zone: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update campus zone
     */
    @PutMapping("/{zoneId}")
    public ResponseEntity<CampusZone> updateZone(
            @PathVariable String zoneId, 
            @RequestBody CampusZoneRequest request) {
        try {
            log.info("Updating campus zone: {}", zoneId);
            
            CampusZone zone = campusZoneService.getZoneById(zoneId);
            if (zone == null) {
                return ResponseEntity.notFound().build();
            }
            
            zone.setZoneName(request.getZoneName());
            zone.setDescription(request.getDescription());
            zone.setLatitude(request.getLatitude());
            zone.setLongitude(request.getLongitude());
            zone.setRadius(request.getRadius());
            zone.setZoneType(request.getZoneType());
            zone.setUpdatedAt(LocalDateTime.now());
            
            CampusZone updatedZone = campusZoneService.updateZone(zone);
            
            return ResponseEntity.ok(updatedZone);
            
        } catch (Exception e) {
            log.error("Error updating zone: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete campus zone
     */
    @DeleteMapping("/{zoneId}")
    public ResponseEntity<?> deleteZone(@PathVariable String zoneId) {
        try {
            log.info("Deleting campus zone: {}", zoneId);
            
            boolean deleted = campusZoneService.deleteZone(zoneId);
            
            if (deleted) {
                return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Zone deleted successfully\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting zone: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Activate/Deactivate zone
     */
    @PatchMapping("/{zoneId}/status")
    public ResponseEntity<CampusZone> toggleZoneStatus(
            @PathVariable String zoneId,
            @RequestParam boolean isActive) {
        try {
            log.info("Toggling zone status: {} to {}", zoneId, isActive);
            
            CampusZone zone = campusZoneService.toggleZoneStatus(zoneId, isActive);
            
            if (zone != null) {
                return ResponseEntity.ok(zone);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error toggling zone status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get zones within radius of a location
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<CampusZone>> getZonesNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "100") Double radiusMeters) {
        try {
            log.info("Getting zones nearby location: {}, {} within {} meters", latitude, longitude, radiusMeters);
            
            List<CampusZone> zones = campusZoneService.getZonesNearby(latitude, longitude, radiusMeters);
            
            return ResponseEntity.ok(zones);
            
        } catch (Exception e) {
            log.error("Error getting nearby zones: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get zone activity metrics
     */
    @GetMapping("/{zoneId}/activity")
    public ResponseEntity<CampusZoneActivityDto> getZoneActivity(
            @PathVariable String zoneId,
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange) {
        try {
            log.info("Getting zone activity for: {} with time range: {}", zoneId, timeRange);
            
            CampusZoneActivityDto activity = campusZoneService.getZoneActivity(zoneId, timeRange);
            
            if (activity != null) {
                return ResponseEntity.ok(activity);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting zone activity: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Request DTO
    public static class CampusZoneRequest {
        private String zoneName;
        private String description;
        private Double latitude;
        private Double longitude;
        private Double radius;
        private String zoneType;
        
        // Getters and setters
        public String getZoneName() { return zoneName; }
        public void setZoneName(String zoneName) { this.zoneName = zoneName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Double getRadius() { return radius; }
        public void setRadius(Double radius) { this.radius = radius; }
        public String getZoneType() { return zoneType; }
        public void setZoneType(String zoneType) { this.zoneType = zoneType; }
    }
}
