package com.disrupton.culturalObject.controller;

import com.disrupton.culturalObject.dto.CulturalObjectDto;
import com.disrupton.culturalObject.dto.CulturalObjectRequest;
import com.disrupton.culturalObject.model.CulturalObject;
import com.disrupton.culturalObject.service.CulturalObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cultural-objects")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CulturalObjectController {

    private final CulturalObjectService culturalObjectService;

    /**
     * Get all cultural objects
     */
    @GetMapping
    public ResponseEntity<List<CulturalObjectDto>> getAllObjects() {
        try {
            log.info("Getting all cultural objects");
            List<CulturalObjectDto> objects = culturalObjectService.getAllObjects();
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("Error getting all objects: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cultural object by ID
     */
    @GetMapping("/{objectId}")
    public ResponseEntity<CulturalObjectDto> getObjectById(@PathVariable String objectId) {
        try {
            log.info("Getting cultural object by ID: {}", objectId);
            CulturalObjectDto object = culturalObjectService.getObjectById(objectId);
            if (object != null) {
                return ResponseEntity.ok(object);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting object by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new cultural object
     */
    @PostMapping
    public ResponseEntity<CulturalObjectDto> createObject(@RequestBody CulturalObjectRequest request) {
        try {
            log.info("Creating new cultural object: {}", request.getName());
            CulturalObjectDto createdObject = culturalObjectService.createObject(request);
            return ResponseEntity.ok(createdObject);
        } catch (Exception e) {
            log.error("Error creating object: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update cultural object
     */
    @PutMapping("/{objectId}")
    public ResponseEntity<CulturalObjectDto> updateObject(
            @PathVariable String objectId,
            @RequestBody CulturalObjectRequest request) {
        try {
            log.info("Updating cultural object: {}", objectId);
            CulturalObjectDto updatedObject = culturalObjectService.updateObject(objectId, request);
            if (updatedObject != null) {
                return ResponseEntity.ok(updatedObject);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating object: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete cultural object
     */
    @DeleteMapping("/{objectId}")
    public ResponseEntity<?> deleteObject(@PathVariable String objectId) {
        try {
            log.info("Deleting cultural object: {}", objectId);
            boolean deleted = culturalObjectService.deleteObject(objectId);
            if (deleted) {
                return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Object deleted successfully\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting object: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get objects by cultural type
     */
    @GetMapping("/type/{culturalType}")
    public ResponseEntity<List<CulturalObjectDto>> getObjectsByType(@PathVariable String culturalType) {
        try {
            log.info("Getting objects by type: {}", culturalType);
            List<CulturalObjectDto> objects = culturalObjectService.getObjectsByType(culturalType);
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("Error getting objects by type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get objects by theme
     */
    @GetMapping("/theme/{theme}")
    public ResponseEntity<List<CulturalObjectDto>> getObjectsByTheme(@PathVariable String theme) {
        try {
            log.info("Getting objects by theme: {}", theme);
            List<CulturalObjectDto> objects = culturalObjectService.getObjectsByTheme(theme);
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("Error getting objects by theme: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get objects by period
     */
    @GetMapping("/period/{period}")
    public ResponseEntity<List<CulturalObjectDto>> getObjectsByPeriod(@PathVariable String period) {
        try {
            log.info("Getting objects by period: {}", period);
            List<CulturalObjectDto> objects = culturalObjectService.getObjectsByPeriod(period);
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("Error getting objects by period: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get objects by region
     */
    @GetMapping("/region/{region}")
    public ResponseEntity<List<CulturalObjectDto>> getObjectsByRegion(@PathVariable String region) {
        try {
            log.info("Getting objects by region: {}", region);
            List<CulturalObjectDto> objects = culturalObjectService.getObjectsByRegion(region);
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("Error getting objects by region: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search objects by name or description
     */
    @GetMapping("/search")
    public ResponseEntity<List<CulturalObjectDto>> searchObjects(@RequestParam String query) {
        try {
            log.info("Searching objects with query: {}", query);
            List<CulturalObjectDto> objects = culturalObjectService.searchObjects(query);
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("Error searching objects: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
