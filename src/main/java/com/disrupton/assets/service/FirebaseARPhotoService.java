package com.disrupton.assets.service;

import com.disrupton.assets.dto.ARPhotoDto;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseARPhotoService {

    private final Firestore db;
    private static final String COLLECTION_NAME = "ar_photos";

    /**
     * Guardar nueva foto AR
     */
    public ARPhotoDto saveARPhoto(ARPhotoDto arPhoto) throws ExecutionException, InterruptedException {
        log.info("📸 Guardando foto AR para objeto: {}", arPhoto.getObjectId());

        // Asignar timestamp si no existe
        if (arPhoto.getCreatedAt() == null) {
            arPhoto.setCreatedAt(Timestamp.now());
        }

        // Guardar en Firestore
        var docRef = db.collection(COLLECTION_NAME).document();
        String photoId = docRef.getId();
        arPhoto.setId(photoId);

        docRef.set(arPhoto).get();

        log.info("✅ Foto AR guardada con ID: {}", photoId);
        return arPhoto;
    }

    /**
     * Obtener fotos AR por objeto cultural
     */
    public List<ARPhotoDto> getARPhotosByObjectId(String objectId) throws ExecutionException, InterruptedException {
        log.info("📸 Obteniendo fotos AR para objeto: {}", objectId);

        var query = db.collection(COLLECTION_NAME)
                .whereEqualTo("objectId", objectId)
                .get();

        return query.get().getDocuments().stream()
                .map(document -> {
                    ARPhotoDto photo = document.toObject(ARPhotoDto.class);
                    photo.setId(document.getId());
                    return photo;
                })
                .toList();
    }

    /**
     * Obtener fotos AR por usuario
     */
    public List<ARPhotoDto> getARPhotosByUserId(String userId) throws ExecutionException, InterruptedException {
        log.info("📸 Obteniendo fotos AR del usuario: {}", userId);

        var query = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get();

        return query.get().getDocuments().stream()
                .map(document -> {
                    ARPhotoDto photo = document.toObject(ARPhotoDto.class);
                    photo.setId(document.getId());
                    return photo;
                })
                .toList();
    }

    /**
     * Eliminar foto AR
     */
    public boolean deleteARPhoto(String photoId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando foto AR: {}", photoId);

        var docRef = db.collection(COLLECTION_NAME).document(photoId);
        var document = docRef.get().get();

        if (document.exists()) {
            docRef.delete().get();
            log.info("✅ Foto AR eliminada: {}", photoId);
            return true;
        } else {
            log.warn("⚠️ Foto AR no encontrada: {}", photoId);
            return false;
        }
    }
} 