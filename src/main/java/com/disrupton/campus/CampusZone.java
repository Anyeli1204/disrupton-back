package com.disrupton.campus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

/**
 * Representa una zona del campus con coordenadas geográficas
 */
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
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
