package com.disrupton.culturalObjectInteraction.controller;

import com.disrupton.culturalObjectInteraction.dto.CulturalObjectInteractionDto;
import com.disrupton.culturalObjectInteraction.dto.CulturalObjectInteractionRequest;
import com.disrupton.culturalObjectInteraction.service.CulturalObjectInteractionService;
import com.disrupton.service.FirebaseAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cultural-object-interactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CulturalObjectInteractionController {

    private final CulturalObjectInteractionService culturalObjectInteractionService;
    private final FirebaseAnalyticsService firebaseAnalyticsService;

    /**
     * Get all cultural object interactions
     */
    @GetMapping
    public ResponseEntity<List<CulturalObjectInteractionDto>> getAllInteractions() {
        try {
            log.info("Getting all cultural object interactions");
            List<CulturalObjectInteractionDto> interactions = culturalObjectInteractionService.getAllInteractions();
            return ResponseEntity.ok(interactions);
        } catch (Exception e) {
            log.error("Error getting all interactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cultural object interaction by ID
     */
    @GetMapping("/{interactionId}")
    public ResponseEntity<CulturalObjectInteractionDto> getInteractionById(@PathVariable String interactionId) {
        try {
            log.info("Getting cultural object interaction by ID: {}", interactionId);
            CulturalObjectInteractionDto interaction = culturalObjectInteractionService.getInteractionById(interactionId);
            if (interaction != null) {
                return ResponseEntity.ok(interaction);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting interaction by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new cultural object interaction
     */
    @PostMapping
    public ResponseEntity<CulturalObjectInteractionDto> createInteraction(@RequestBody CulturalObjectInteractionRequest request) {
        try {
            log.info("Creating new cultural object interaction for user: {}", request.getUserId());
            CulturalObjectInteractionDto createdInteraction = culturalObjectInteractionService.createInteraction(request);
            
            // Track analytics event
            firebaseAnalyticsService.saveSocialInteraction(createdInteraction);
            firebaseAnalyticsService.logEvent("cultural_object_interaction_created", createdInteraction);
            
            return ResponseEntity.ok(createdInteraction);
        } catch (Exception e) {
            log.error("Error creating interaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update cultural object interaction
     */
    @PutMapping("/{interactionId}")
    public ResponseEntity<CulturalObjectInteractionDto> updateInteraction(
            @PathVariable String interactionId,
            @RequestBody CulturalObjectInteractionRequest request) {
        try {
            log.info("Updating cultural object interaction: {}", interactionId);
            CulturalObjectInteractionDto updatedInteraction = culturalObjectInteractionService.updateInteraction(interactionId, request);
            if (updatedInteraction != null) {
                return ResponseEntity.ok(updatedInteraction);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating interaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete cultural object interaction
     */
    @DeleteMapping("/{interactionId}")
    public ResponseEntity<?> deleteInteraction(@PathVariable String interactionId) {
        try {
            log.info("Deleting cultural object interaction: {}", interactionId);
            boolean deleted = culturalObjectInteractionService.deleteInteraction(interactionId);
            if (deleted) {
                return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Interaction deleted successfully\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting interaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get interactions by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CulturalObjectInteractionDto>> getInteractionsByUserId(@PathVariable String userId) {
        try {
            log.info("Getting interactions for user: {}", userId);
            List<CulturalObjectInteractionDto> interactions = culturalObjectInteractionService.getInteractionsByUserId(userId);
            return ResponseEntity.ok(interactions);
        } catch (Exception e) {
            log.error("Error getting interactions by user ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get interactions by cultural object ID
     */
    @GetMapping("/object/{objectId}")
    public ResponseEntity<List<CulturalObjectInteractionDto>> getInteractionsByObjectId(@PathVariable String objectId) {
        try {
            log.info("Getting interactions for cultural object: {}", objectId);
            List<CulturalObjectInteractionDto> interactions = culturalObjectInteractionService.getInteractionsByObjectId(objectId);
            return ResponseEntity.ok(interactions);
        } catch (Exception e) {
            log.error("Error getting interactions by object ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get interactions by interaction type
     */
    @GetMapping("/type/{interactionType}")
    public ResponseEntity<List<CulturalObjectInteractionDto>> getInteractionsByType(@PathVariable String interactionType) {
        try {
            log.info("Getting interactions by type: {}", interactionType);
            List<CulturalObjectInteractionDto> interactions = culturalObjectInteractionService.getInteractionsByType(interactionType);
            return ResponseEntity.ok(interactions);
        } catch (Exception e) {
            log.error("Error getting interactions by type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get interaction statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getInteractionStats() {
        try {
            log.info("Getting interaction statistics");
            Object stats = culturalObjectInteractionService.getInteractionStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting interaction stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
