package com.disrupton.culturalObject.service;

import com.disrupton.culturalObject.dto.CulturalObjectDto;
import com.disrupton.culturalObject.dto.CulturalObjectRequest;
import com.disrupton.culturalObject.model.CulturalObject;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.*;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage.SignUrlOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CulturalObjectService {

    private final Firestore firestore;
    private final Storage storage; // Injected Storage client with credentials
    private static final String COLLECTION_NAME = "cultural_objects";

    // Constructor to inject Firestore and Storage clients
    public CulturalObjectService(Firestore firestore, Storage storage) {
        this.firestore = firestore;
        this.storage = storage;
        log.info("✅ Firebase Storage client initialized successfully with credentials.");
    }

    /**
     * Get all cultural objects
     */
    public List<CulturalObjectDto> getAllObjects() throws ExecutionException, InterruptedException {
        List<CulturalObjectDto> objects = new ArrayList<>();

        firestore.collection(COLLECTION_NAME).get().get().getDocuments().forEach(document -> {
            CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
            // Ensure model3dUrl is a signed URL if it's a Firebase Storage path
            if (object.getModel3dUrl() != null && !object.getModel3dUrl().isEmpty()) {
                object.setModel3dUrl(generateSignedUrlIfNecessary(object.getModel3dUrl()));
            }
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
            CulturalObjectDto object = document.toObject(CulturalObjectDto.class);
            // Ensure model3dUrl is a signed URL if it's a Firebase Storage path
            if (object.getModel3dUrl() != null && !object.getModel3dUrl().isEmpty()) {
                object.setModel3dUrl(generateSignedUrlIfNecessary(object.getModel3dUrl()));
            }
            return object;
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

        // Process model3dUrl to generate a signed URL if it's a Firebase Storage path
        String modelUrl = request.getModel3dUrl();
        if (modelUrl != null && !modelUrl.isEmpty()) {
            String signedUrl = generateSignedUrlIfNecessary(modelUrl);
            if (signedUrl != null) {
                object.setModel3dUrl(signedUrl);
                log.info("Generated signed URL for model: {}", signedUrl);
            } else {
                log.warn("Could not generate signed URL for model: {}", modelUrl);
                object.setModel3dUrl(modelUrl); // Fallback to original URL
            }
        } else {
            object.setModel3dUrl(modelUrl); // Set to null or empty if not provided
        }

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

        // Process model3dUrl to generate a signed URL if it's a Firebase Storage path
        String modelUrl = request.getModel3dUrl();
        if (modelUrl != null && !modelUrl.isEmpty()) {
            String signedUrl = generateSignedUrlIfNecessary(modelUrl);
            if (signedUrl != null) {
                object.setModel3dUrl(signedUrl);
                log.info("Generated signed URL for model: {}", signedUrl);
            } else {
                log.warn("Could not generate signed URL for model: {}", modelUrl);
                object.setModel3dUrl(modelUrl); // Fallback to original URL
            }
        } else {
            object.setModel3dUrl(modelUrl); // Set to null or empty if not provided
        }

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
            // Ensure model3dUrl is a signed URL if it's a Firebase Storage path
            if (object.getModel3dUrl() != null && !object.getModel3dUrl().isEmpty()) {
                object.setModel3dUrl(generateSignedUrlIfNecessary(object.getModel3dUrl()));
            }
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
            // Ensure model3dUrl is a signed URL if it's a Firebase Storage path
            if (object.getModel3dUrl() != null && !object.getModel3dUrl().isEmpty()) {
                object.setModel3dUrl(generateSignedUrlIfNecessary(object.getModel3dUrl()));
            }
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
            // Ensure model3dUrl is a signed URL if it's a Firebase Storage path
            if (object.getModel3dUrl() != null && !object.getModel3dUrl().isEmpty()) {
                object.setModel3dUrl(generateSignedUrlIfNecessary(object.getModel3dUrl()));
            }
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
            // Ensure model3dUrl is a signed URL if it's a Firebase Storage path
            if (object.getModel3dUrl() != null && !object.getModel3dUrl().isEmpty()) {
                object.setModel3dUrl(generateSignedUrlIfNecessary(object.getModel3dUrl()));
            }
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
                // Ensure model3dUrl is a signed URL if it's a Firebase Storage path
                if (object.getModel3dUrl() != null && !object.getModel3dUrl().isEmpty()) {
                    object.setModel3dUrl(generateSignedUrlIfNecessary(object.getModel3dUrl()));
                }
                objects.add(object);
            }
        });

        return objects;
    }

    /**
     * Upload a 3D model file to Firebase Storage in the /models folder
     */
    /*public String uploadModelToStorage(MultipartFile file, String objectName) throws IOException {
        try {
            log.info("📥 Received upload request");
            log.info("📦 Object name: {}", objectName);
            log.info("📦 File name: {}", file.getOriginalFilename());
            log.info("📦 File size: {} bytes", file.getSize());

            String fileName = objectName != null ? objectName : UUID.randomUUID().toString();
            if (!fileName.endsWith(".glb")) {
                fileName = fileName + ".glb";
            }

            String bucketName = "disrupton-new.firebasestorage.app";
            String objectPath = "models/" + fileName;

            BlobId blobId = BlobId.of(bucketName, objectPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("model/gltf-binary")
                    .build();

            storage.create(blobInfo, file.getBytes());

            String gsUrl = "gs://" + bucketName + "/" + objectPath;
            log.info("✅ Model uploaded successfully to: {}", gsUrl);

            return gsUrl;
        } catch (Exception e) {
            log.error("❌ Upload error: {}", e.getMessage(), e);
            throw new IOException("Failed to upload model to Firebase Storage", e);
        }
    }*/

    /**
     * Helper method to generate a signed URL for Firebase Storage objects.
     * If the URL is a gs:// path, it generates a signed URL. Otherwise, it returns the original URL.
     */
    private String generateSignedUrlIfNecessary(String modelUrl) {
        if (modelUrl == null || modelUrl.isEmpty()) {
            return null;
        }

        try {
            if (modelUrl.startsWith("gs://")) {
                // Extraer bucket y path del formato gs://
                String path = modelUrl.substring("gs://".length());
                String bucketName = path.substring(0, path.indexOf('/'));
                String objectPath = path.substring(path.indexOf('/') + 1);

                // Generar URL pública (sin expiración) en lugar de signed URL
                String publicUrl = String.format(
                        "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                        bucketName,
                        objectPath.replace("/", "%2F")
                );

                log.info("✅ Generated public URL (no expiration) for gs://{}/{}: {}", bucketName, objectPath, publicUrl);
                return publicUrl;

            } else if (modelUrl.startsWith("https://firebasestorage.googleapis.com")) {
                // Ya es una URL pública de Firebase Storage
                log.info("ℹ️ URL is already a public Firebase Storage URL: {}", modelUrl);
                return modelUrl;

            } else if (modelUrl.startsWith("https://storage.googleapis.com")) {
                // Es una signed URL antigua, intentar convertir a pública
                log.warn("⚠️ Found old signed URL, returning as-is (will expire): {}", modelUrl.substring(0, Math.min(100, modelUrl.length())));
                return modelUrl;

            } else {
                log.warn("⚠️ Unknown URL format, returning as-is: {}", modelUrl);
                return modelUrl;
            }

        } catch (Exception e) {
            log.error("❌ Error generating public URL for {}: {}", modelUrl, e.getMessage(), e);
            return null;
        }
    }}
