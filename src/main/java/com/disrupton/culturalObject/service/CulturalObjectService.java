package com.disrupton.culturalObject.service;

import com.disrupton.culturalObject.dto.CulturalObjectDto;
import com.disrupton.culturalObject.dto.CulturalObjectRequest;
import com.disrupton.culturalObject.model.CulturalObject;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CulturalObjectService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "cultural_objects";

    /**
     * Get all cultural objects
     */
    public List<CulturalObjectDto> getAllObjects() throws ExecutionException, InterruptedException {
        List<CulturalObjectDto> objects = new ArrayList<>();
        
        firestore.collection(COLLECTION_NAME).get().get().getDocuments().forEach(document -> {
            CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
            objects.add(object);
        });
        
        return objects;
    }

    /**
     * Get cultural object by ID
     */
    public CulturalObjectDto getObjectById(String objectId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(objectId).get().get();
        
        if (document.exists()) {
            return document.toObject(CulturalObjectDto.class);
        } else {
            log.warn("Cultural object not found with ID: {}", objectId);
            return null;
        }
    }

    /**
     * Create new cultural object
     */
    public CulturalObjectDto createObject(CulturalObjectRequest request) throws ExecutionException, InterruptedException {
        CulturalObjectDto object = new CulturalObjectDto();
        object.setObjectId(UUID.randomUUID().toString());
        object.setName(request.getName());
        object.setDescription(request.getDescription());
        object.setCulturalType(request.getCulturalType());
        object.setTheme(request.getTheme());
        object.setCulture(request.getCulture());
        object.setPeriod(request.getPeriod());
        object.setRegion(request.getRegion());
        object.setLatitude(request.getLatitude());
        object.setLongitude(request.getLongitude());
        object.setImageUrl(request.getImageUrl());
        object.setModel3dUrl(request.getModel3dUrl());
        object.setAudioUrl(request.getAudioUrl());
        object.setVideoUrl(request.getVideoUrl());
        object.setAdditionalInfo(request.getAdditionalInfo());
        object.setIsActive(true);
        object.setCreatedAt(Timestamp.now());
        object.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(object.getObjectId());
        docRef.set(object).get();
        
        log.info("Cultural object created successfully: {}", object.getObjectId());
        return object;
    }

    /**
     * Update cultural object
     */
    public CulturalObjectDto updateObject(String objectId, CulturalObjectRequest request) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(objectId).get().get();
        
        if (!document.exists()) {
            log.warn("Cultural object not found for update: {}", objectId);
            return null;
        }
        
        CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
        object.setName(request.getName());
        object.setDescription(request.getDescription());
        object.setCulturalType(request.getCulturalType());
        object.setTheme(request.getTheme());
        object.setCulture(request.getCulture());
        object.setPeriod(request.getPeriod());
        object.setRegion(request.getRegion());
        object.setLatitude(request.getLatitude());
        object.setLongitude(request.getLongitude());
        object.setImageUrl(request.getImageUrl());
        object.setModel3dUrl(request.getModel3dUrl());
        object.setAudioUrl(request.getAudioUrl());
        object.setVideoUrl(request.getVideoUrl());
        object.setAdditionalInfo(request.getAdditionalInfo());
        object.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(objectId);
        docRef.set(object).get();
        
        log.info("Cultural object updated successfully: {}", objectId);
        return object;
    }

    /**
     * Delete cultural object
     */
    public boolean deleteObject(String objectId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(objectId);
        DocumentSnapshot document = docRef.get().get();
        
        if (document.exists()) {
            docRef.delete().get();
            log.info("Cultural object deleted successfully: {}", objectId);
            return true;
        } else {
            log.warn("Cultural object not found for deletion: {}", objectId);
            return false;
        }
    }

    /**
     * Get objects by cultural type
     */
    public List<CulturalObjectDto> getObjectsByType(String culturalType) throws ExecutionException, InterruptedException {
        List<CulturalObjectDto> objects = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("culturalType", culturalType)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
            objects.add(object);
        });
        
        return objects;
    }

    /**
     * Get objects by theme
     */
    public List<CulturalObjectDto> getObjectsByTheme(String theme) throws ExecutionException, InterruptedException {
        List<CulturalObjectDto> objects = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("theme", theme)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
            objects.add(object);
        });
        
        return objects;
    }

    /**
     * Get objects by period
     */
    public List<CulturalObjectDto> getObjectsByPeriod(String period) throws ExecutionException, InterruptedException {
        List<CulturalObjectDto> objects = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("period", period)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
            objects.add(object);
        });
        
        return objects;
    }

    /**
     * Get objects by region
     */
    public List<CulturalObjectDto> getObjectsByRegion(String region) throws ExecutionException, InterruptedException {
        List<CulturalObjectDto> objects = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("region", region)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
            objects.add(object);
        });
        
        return objects;
    }

    /**
     * Search objects by name or description
     */
    public List<CulturalObjectDto> searchObjects(String query) throws ExecutionException, InterruptedException {
        List<CulturalObjectDto> objects = new ArrayList<>();
        
        // Note: Firestore doesn't support full-text search natively
        // This is a simple implementation that searches in name and description
        // For production, consider using Algolia, Elasticsearch, or similar
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME).get().get();
        
        snapshot.getDocuments().forEach(document -> {
            CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
            if (object != null && 
                (object.getName().toLowerCase().contains(query.toLowerCase()) ||
                 object.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                objects.add(object);
            }
        });
        
        return objects;
    }
}
