package com.disrupton.service;

import com.disrupton.dto.UserDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseUserService {

    private final Firestore db;
    private static final String COLLECTION_NAME = "users";

    /**
     * Guarda un nuevo usuario en Firestore
     */
    public UserDto saveUser(UserDto user) throws ExecutionException, InterruptedException {
        log.info("💾 Guardando usuario: {}", user.getEmail());
        
        // Establecer timestamp de creación si no existe
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(com.google.cloud.Timestamp.now());
        }
        
        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String userId = docRef.getId();
        
        // Asignar el ID generado al usuario
        user.setId(userId);
        
        ApiFuture<WriteResult> future = docRef.set(user);
        
        WriteResult result = future.get();
        log.info("✅ Usuario guardado exitosamente con ID: {}. Timestamp: {}", userId, result.getUpdateTime());
        
        return user;
    }

    /**
     * Obtiene un usuario por ID
     */
    public UserDto getUserById(String userId) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando usuario con ID: {}", userId);
        
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            UserDto user = document.toObject(UserDto.class);
            // Asignar el ID del documento
            user.setId(document.getId());
            log.info("✅ Usuario encontrado: {}", user.getEmail());
            return user;
        } else {
            log.warn("⚠️ Usuario no encontrado con ID: {}", userId);
            return null;
        }
    }

    /**
     * Obtiene un usuario por email
     */
    public UserDto getUserByEmail(String email) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando usuario por email: {}", email);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            UserDto user = document.toObject(UserDto.class);
            // Asignar el ID del documento
            user.setId(document.getId());
            log.info("✅ Usuario encontrado por email: {}", email);
            return user;
        } else {
            log.warn("⚠️ Usuario no encontrado con email: {}", email);
            return null;
        }
    }

    /**
     * Obtiene todos los usuarios
     */
    public List<UserDto> getAllUsers() throws ExecutionException, InterruptedException {
        log.info("📋 Obteniendo todos los usuarios");
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        QuerySnapshot querySnapshot = future.get();
        
        List<UserDto> users = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            UserDto user = document.toObject(UserDto.class);
            // Asignar el ID del documento
            user.setId(document.getId());
            users.add(user);
        }
        
        log.info("✅ {} usuarios encontrados", users.size());
        return users;
    }

    /**
     * Obtiene usuarios por rol
     */
    public List<UserDto> getUsersByRole(String role) throws ExecutionException, InterruptedException {
        log.info("👥 Buscando usuarios con rol: {}", role);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("role", role)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<UserDto> users = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            UserDto user = document.toObject(UserDto.class);
            users.add(user);
        }
        
        log.info("✅ {} usuarios encontrados con rol {}", users.size(), role);
        return users;
    }

    /**
     * Actualiza un usuario existente
     */
    public UserDto updateUser(String userId, UserDto user) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando usuario: {}", userId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(userId)
                .set(user);
        
        WriteResult result = future.get();
        log.info("✅ Usuario actualizado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return user;
    }

    /**
     * Elimina un usuario
     */
    public boolean deleteUser(String userId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando usuario: {}", userId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(userId)
                .delete();
        
        WriteResult result = future.get();
        log.info("✅ Usuario eliminado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return true;
    }

    /**
     * Verifica si un usuario existe
     */
    public boolean userExists(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        return document.exists();
    }
} 