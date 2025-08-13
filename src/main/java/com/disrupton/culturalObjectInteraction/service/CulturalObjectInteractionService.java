package com.disrupton.culturalObjectInteraction.service;

import com.disrupton.culturalObjectInteraction.dto.CulturalObjectInteractionDto;
import com.disrupton.culturalObjectInteraction.dto.CulturalObjectInteractionRequest;
import com.disrupton.culturalObjectInteraction.model.CulturalObjectInteraction;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CulturalObjectInteractionService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "cultural_object_interactions";

    /**
     * Get all cultural object interactions
     */
    public List<CulturalObjectInteractionDto> getAllInteractions() throws ExecutionException, InterruptedException {
        List<CulturalObjectInteractionDto> interactions = new ArrayList<>();
        
        firestore.collection(COLLECTION_NAME).get().get().getDocuments().forEach(document -> {
            CulturalObjectInteractionDto interaction = document.toObject(CulturalObjectInteractionDto.class);
            interactions.add(interaction);
        });
        
        return interactions;
    }

    /**
     * Get cultural object interaction by ID
     */
    public CulturalObjectInteractionDto getInteractionById(String interactionId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(interactionId).get().get();
        
        if (document.exists()) {
            return document.toObject(CulturalObjectInteractionDto.class);
        } else {
            log.warn("Cultural object interaction not found with ID: {}", interactionId);
            return null;
        }
    }

    /**
     * Create new cultural object interaction
     */
    public CulturalObjectInteractionDto createInteraction(CulturalObjectInteractionRequest request) throws ExecutionException, InterruptedException {
        CulturalObjectInteractionDto interaction = new CulturalObjectInteractionDto();
        interaction.setInteractionId(UUID.randomUUID().toString());
        interaction.setUserId(request.getUserId());
        interaction.setObjectId(request.getObjectId());
        interaction.setInteractionType(request.getInteractionType());
        interaction.setContent(request.getContent());
        interaction.setRating(request.getRating());
        interaction.setLocation(request.getLocation());
        interaction.setLatitude(request.getLatitude());
        interaction.setLongitude(request.getLongitude());
        interaction.setCreatedAt(Timestamp.now());
        interaction.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(interaction.getInteractionId());
        docRef.set(interaction).get();
        
        log.info("Cultural object interaction created successfully: {}", interaction.getInteractionId());
        return interaction;
    }

    /**
     * Update cultural object interaction
     */
    public CulturalObjectInteractionDto updateInteraction(String interactionId, CulturalObjectInteractionRequest request) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(interactionId).get().get();
        
        if (!document.exists()) {
            log.warn("Cultural object interaction not found for update: {}", interactionId);
            return null;
        }
        
        CulturalObjectInteractionDto interaction = document.toObject(CulturalObjectInteractionDto.class);
        interaction.setObjectId(request.getObjectId());
        interaction.setInteractionType(request.getInteractionType());
        interaction.setContent(request.getContent());
        interaction.setRating(request.getRating());
        interaction.setLocation(request.getLocation());
        interaction.setLatitude(request.getLatitude());
        interaction.setLongitude(request.getLongitude());
        interaction.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(interactionId);
        docRef.set(interaction).get();
        
        log.info("Cultural object interaction updated successfully: {}", interactionId);
        return interaction;
    }

    /**
     * Delete cultural object interaction
     */
    public boolean deleteInteraction(String interactionId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(interactionId);
        DocumentSnapshot document = docRef.get().get();
        
        if (document.exists()) {
            docRef.delete().get();
            log.info("Cultural object interaction deleted successfully: {}", interactionId);
            return true;
        } else {
            log.warn("Cultural object interaction not found for deletion: {}", interactionId);
            return false;
        }
    }

    /**
     * Get interactions by user ID
     */
    public List<CulturalObjectInteractionDto> getInteractionsByUserId(String userId) throws ExecutionException, InterruptedException {
        List<CulturalObjectInteractionDto> interactions = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            CulturalObjectInteractionDto interaction = document.toObject(CulturalObjectInteractionDto.class);
            interactions.add(interaction);
        });
        
        return interactions;
    }

    /**
     * Get interactions by cultural object ID
     */
    public List<CulturalObjectInteractionDto> getInteractionsByObjectId(String objectId) throws ExecutionException, InterruptedException {
        List<CulturalObjectInteractionDto> interactions = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("objectId", objectId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            CulturalObjectInteractionDto interaction = document.toObject(CulturalObjectInteractionDto.class);
            interactions.add(interaction);
        });
        
        return interactions;
    }

    /**
     * Get interactions by interaction type
     */
    public List<CulturalObjectInteractionDto> getInteractionsByType(String interactionType) throws ExecutionException, InterruptedException {
        List<CulturalObjectInteractionDto> interactions = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("interactionType", interactionType)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            CulturalObjectInteractionDto interaction = document.toObject(CulturalObjectInteractionDto.class);
            interactions.add(interaction);
        });
        
        return interactions;
    }

    /**
     * Get interaction statistics
     */
    public Map<String, Object> getInteractionStats() throws ExecutionException, InterruptedException {
        Map<String, Object> stats = new HashMap<>();
        
        QuerySnapshot allInteractions = firestore.collection(COLLECTION_NAME).get().get();
        
        stats.put("totalInteractions", allInteractions.size());
        
        // Count by interaction type
        Map<String, Integer> typeCounts = new HashMap<>();
        for (QueryDocumentSnapshot document : allInteractions) {
            CulturalObjectInteractionDto interaction = document.toObject(CulturalObjectInteractionDto.class);
            String type = interaction.getInteractionType();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        stats.put("interactionsByType", typeCounts);
        
        // Calculate average rating
        double totalRating = 0;
        int interactionsWithRating = 0;
        
        for (QueryDocumentSnapshot document : allInteractions) {
            CulturalObjectInteractionDto interaction = document.toObject(CulturalObjectInteractionDto.class);
            if (interaction.getRating() != null && interaction.getRating() > 0) {
                totalRating += interaction.getRating();
                interactionsWithRating++;
            }
        }
        
        if (interactionsWithRating > 0) {
            stats.put("averageRating", totalRating / interactionsWithRating);
        } else {
            stats.put("averageRating", 0.0);
        }
        
        return stats;
    }
}
