package com.disrupton.socialInteraction.service;

import com.disrupton.socialInteraction.dto.SocialInteractionDto;
import com.disrupton.socialInteraction.dto.SocialInteractionRequest;
import com.disrupton.socialInteraction.model.SocialInteraction;
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
public class SocialInteractionService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "social_interactions";

    /**
     * Get all social interactions
     */
    public List<SocialInteractionDto> getAllInteractions() throws ExecutionException, InterruptedException {
        List<SocialInteractionDto> interactions = new ArrayList<>();
        
        firestore.collection(COLLECTION_NAME).get().get().getDocuments().forEach(document -> {
            SocialInteractionDto interaction = document.toObject(SocialInteractionDto.class);
            interactions.add(interaction);
        });
        
        return interactions;
    }

    /**
     * Get social interaction by ID
     */
    public SocialInteractionDto getInteractionById(String interactionId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(interactionId).get().get();
        
        if (document.exists()) {
            return document.toObject(SocialInteractionDto.class);
        } else {
            log.warn("Social interaction not found with ID: {}", interactionId);
            return null;
        }
    }

    /**
     * Create new social interaction
     */
    public SocialInteractionDto createInteraction(SocialInteractionRequest request) throws ExecutionException, InterruptedException {
        SocialInteractionDto interaction = new SocialInteractionDto();
        interaction.setInteractionId(UUID.randomUUID().toString());
        interaction.setUserId(request.getUserId());
        interaction.setTargetId(request.getTargetId());
        interaction.setTargetType(request.getTargetType());
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
        
        log.info("Social interaction created successfully: {}", interaction.getInteractionId());
        return interaction;
    }

    /**
     * Update social interaction
     */
    public SocialInteractionDto updateInteraction(String interactionId, SocialInteractionRequest request) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(interactionId).get().get();
        
        if (!document.exists()) {
            log.warn("Social interaction not found for update: {}", interactionId);
            return null;
        }
        
        SocialInteractionDto interaction = document.toObject(SocialInteractionDto.class);
        interaction.setTargetId(request.getTargetId());
        interaction.setTargetType(request.getTargetType());
        interaction.setInteractionType(request.getInteractionType());
        interaction.setContent(request.getContent());
        interaction.setRating(request.getRating());
        interaction.setLocation(request.getLocation());
        interaction.setLatitude(request.getLatitude());
        interaction.setLongitude(request.getLongitude());
        interaction.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(interactionId);
        docRef.set(interaction).get();
        
        log.info("Social interaction updated successfully: {}", interactionId);
        return interaction;
    }

    /**
     * Delete social interaction
     */
    public boolean deleteInteraction(String interactionId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(interactionId);
        DocumentSnapshot document = docRef.get().get();
        
        if (document.exists()) {
            docRef.delete().get();
            log.info("Social interaction deleted successfully: {}", interactionId);
            return true;
        } else {
            log.warn("Social interaction not found for deletion: {}", interactionId);
            return false;
        }
    }

    /**
     * Get interactions by user ID
     */
    public List<SocialInteractionDto> getInteractionsByUserId(String userId) throws ExecutionException, InterruptedException {
        List<SocialInteractionDto> interactions = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            SocialInteractionDto interaction = document.toObject(SocialInteractionDto.class);
            interactions.add(interaction);
        });
        
        return interactions;
    }

    /**
     * Get interactions by type
     */
    public List<SocialInteractionDto> getInteractionsByType(String interactionType) throws ExecutionException, InterruptedException {
        List<SocialInteractionDto> interactions = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("interactionType", interactionType)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            SocialInteractionDto interaction = document.toObject(SocialInteractionDto.class);
            interactions.add(interaction);
        });
        
        return interactions;
    }

    /**
     * Get interactions by target ID
     */
    public List<SocialInteractionDto> getInteractionsByTargetId(String targetId) throws ExecutionException, InterruptedException {
        List<SocialInteractionDto> interactions = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("targetId", targetId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            SocialInteractionDto interaction = document.toObject(SocialInteractionDto.class);
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
            SocialInteractionDto interaction = document.toObject(SocialInteractionDto.class);
            String type = interaction.getInteractionType();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        stats.put("interactionsByType", typeCounts);
        
        // Calculate average rating
        double totalRating = 0;
        int interactionsWithRating = 0;
        
        for (QueryDocumentSnapshot document : allInteractions) {
            SocialInteractionDto interaction = document.toObject(SocialInteractionDto.class);
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
