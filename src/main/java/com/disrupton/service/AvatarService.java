package com.disrupton.service;

import com.disrupton.dto.AvatarDto;
import com.disrupton.model.Avatar;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Servicio simplificado para gestionar los 3 tipos de avatares
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "avatars";

    /**
     * Inicializa los 3 avatares disponibles si no existen
     */
    public void initializeDefaultAvatars() throws ExecutionException, InterruptedException {
        List<AvatarDto> existingAvatars = getAllAvatars();
        
        // Si no hay avatares, crear los 3 predefinidos
        if (existingAvatars.isEmpty()) {
            // Vicuña
            createDefaultAvatar(
                "VICUNA", 
                "Vicuña", 
                "https://storage.googleapis.com/cultural-app-assets/avatars/3d/vicuna.glb"
            );
            
            // Perro Peruano
            createDefaultAvatar(
                "PERUVIAN_DOG", 
                "Perro Peruano", 
                "https://storage.googleapis.com/cultural-app-assets/avatars/3d/peruvian_dog.glb"
            );
            
            // Gallito de las Rocas
            createDefaultAvatar(
                "COCK_OF_THE_ROCK", 
                "Gallito de las Rocas", 
                "https://storage.googleapis.com/cultural-app-assets/avatars/3d/cock_of_the_rock.glb"
            );
            
            log.info("✅ Avatares predeterminados creados exitosamente");
        }
    }
    
    /**
     * Método auxiliar para crear un avatar predeterminado
     */
    private void createDefaultAvatar(String type, String displayName, String modelUrl) 
            throws ExecutionException, InterruptedException {
        
        AvatarDto avatar = new AvatarDto();
        avatar.setAvatarId(UUID.randomUUID().toString());
        avatar.setType(type);
        avatar.setDisplayName(displayName);
        avatar.setAvatar3DModelUrl(modelUrl);
        avatar.setCreatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(avatar.getAvatarId());
        docRef.set(avatar).get();
    }
    
    /**
     * Obtiene todos los avatares disponibles
     */
    public List<AvatarDto> getAllAvatars() throws ExecutionException, InterruptedException {
        List<AvatarDto> avatars = new ArrayList<>();
        
        firestore.collection(COLLECTION_NAME).get().get().getDocuments().forEach(document -> {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        });
        
        return avatars;
    }
    
    /**
     * Obtiene un avatar por su ID
     */
    public AvatarDto getAvatarById(String avatarId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(avatarId).get().get();
        
        if (document.exists()) {
            return document.toObject(AvatarDto.class);
        }
        
        return null;
    }
}
