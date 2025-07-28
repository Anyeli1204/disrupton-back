package com.disrupton.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusZone {
    private String zoneId;
    private String zoneName;
    private String description;
    private Double latitude;
    private Double longitude;
    private Double radius; // in meters
    private String zoneType; // "building", "plaza", "garden", "library", etc.
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
