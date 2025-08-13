package com.disrupton.socialInteraction.controller;

import com.disrupton.socialInteraction.dto.SocialInteractionDto;
import com.disrupton.socialInteraction.dto.SocialInteractionRequest;
import com.disrupton.socialInteraction.service.SocialInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social-interactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SocialInteractionController {

    private final SocialInteractionService socialInteractionService;

    /**
     * Get all social interactions
     */
    @GetMapping
    public ResponseEntity<List<SocialInteractionDto>> getAllInteractions() {
        try {
            log.info("Getting all social interactions");
            List<SocialInteractionDto> interactions = socialInteractionService.getAllInteractions();
            return ResponseEntity.ok(interactions);
        } catch (Exception e) {
            log.error("Error getting all interactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get social interaction by ID
     */
    @GetMapping("/{interactionId}")
    public ResponseEntity<SocialInteractionDto> getInteractionById(@PathVariable String interactionId) {
        try {
            log.info("Getting social interaction by ID: {}", interactionId);
            SocialInteractionDto interaction = socialInteractionService.getInteractionById(interactionId);
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
     * Create new social interaction
     */
    @PostMapping
    public ResponseEntity<SocialInteractionDto> createInteraction(@RequestBody SocialInteractionRequest request) {
        try {
            log.info("Creating new social interaction for user: {}", request.getUserId());
            SocialInteractionDto createdInteraction = socialInteractionService.createInteraction(request);
            return ResponseEntity.ok(createdInteraction);
        } catch (Exception e) {
            log.error("Error creating interaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update social interaction
     */
    @PutMapping("/{interactionId}")
    public ResponseEntity<SocialInteractionDto> updateInteraction(
            @PathVariable String interactionId,
            @RequestBody SocialInteractionRequest request) {
        try {
            log.info("Updating social interaction: {}", interactionId);
            SocialInteractionDto updatedInteraction = socialInteractionService.updateInteraction(interactionId, request);
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
     * Delete social interaction
     */
    @DeleteMapping("/{interactionId}")
    public ResponseEntity<?> deleteInteraction(@PathVariable String interactionId) {
        try {
            log.info("Deleting social interaction: {}", interactionId);
            boolean deleted = socialInteractionService.deleteInteraction(interactionId);
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
    public ResponseEntity<List<SocialInteractionDto>> getInteractionsByUserId(@PathVariable String userId) {
        try {
            log.info("Getting interactions for user: {}", userId);
            List<SocialInteractionDto> interactions = socialInteractionService.getInteractionsByUserId(userId);
            return ResponseEntity.ok(interactions);
        } catch (Exception e) {
            log.error("Error getting interactions by user ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get interactions by type
     */
    @GetMapping("/type/{interactionType}")
    public ResponseEntity<List<SocialInteractionDto>> getInteractionsByType(@PathVariable String interactionType) {
        try {
            log.info("Getting interactions by type: {}", interactionType);
            List<SocialInteractionDto> interactions = socialInteractionService.getInteractionsByType(interactionType);
            return ResponseEntity.ok(interactions);
        } catch (Exception e) {
            log.error("Error getting interactions by type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get interactions by target ID
     */
    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<SocialInteractionDto>> getInteractionsByTargetId(@PathVariable String targetId) {
        try {
            log.info("Getting interactions by target ID: {}", targetId);
            List<SocialInteractionDto> interactions = socialInteractionService.getInteractionsByTargetId(targetId);
            return ResponseEntity.ok(interactions);
        } catch (Exception e) {
            log.error("Error getting interactions by target ID: {}", e.getMessage(), e);
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
            Object stats = socialInteractionService.getInteractionStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting interaction stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
