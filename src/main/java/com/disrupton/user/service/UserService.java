package com.disrupton.user.service;

import com.disrupton.user.dto.UserDto;
import com.disrupton.user.dto.UserRequest;
import com.disrupton.user.model.User;
import com.google.api.core.ApiFuture;
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
public class UserService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "users";

    /**
     * Guarda un nuevo usuario en Firestore
     */
    public UserDto saveUser(UserDto user) throws ExecutionException, InterruptedException {
        log.info("💾 Guardando usuario: {}", user.getEmail());
        
        // Establecer timestamp de creación si no existe
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Timestamp.now());
        }
        
        // Crear documento con ID automático de Firestore
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        String userId = docRef.getId();
        
        // Asignar el ID generado al usuario
        user.setUserId(userId);
        
        ApiFuture<WriteResult> future = docRef.set(user);
        WriteResult result = future.get();
        
        log.info("✅ Usuario guardado exitosamente con ID: {}. Timestamp: {}", userId, result.getUpdateTime());
        return user;
    }

    /**
     * Crea un nuevo usuario desde UserRequest
     */
    public UserDto createUser(UserRequest request) throws ExecutionException, InterruptedException {
        log.info("👤 Creando nuevo usuario: {}", request.getEmail());
        
        UserDto user = new UserDto();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setIsActive(true);
        user.setIsPremium(false); // Usuario gratuito por defecto
        user.setCreatedAt(Timestamp.now());
        user.setUpdatedAt(Timestamp.now());
        
        return saveUser(user);
    }

    /**
     * Obtiene un usuario por ID
     */
    public UserDto getUserById(String userId) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando usuario con ID: {}", userId);
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            UserDto user = document.toObject(UserDto.class);
            // Asignar el ID del documento
            user.setUserId(document.getId());
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
        
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            UserDto user = document.toObject(UserDto.class);
            // Asignar el ID del documento
            user.setUserId(document.getId());
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
        
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        QuerySnapshot querySnapshot = future.get();
        
        List<UserDto> users = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            UserDto user = document.toObject(UserDto.class);
            // Asignar el ID del documento
            user.setUserId(document.getId());
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
        
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("role", role)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<UserDto> users = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            UserDto user = document.toObject(UserDto.class);
            user.setUserId(document.getId());
            users.add(user);
        }
        
        log.info("✅ {} usuarios encontrados con rol {}", users.size(), role);
        return users;
    }

    /**
     * Busca usuarios por nombre o email
     */
    public List<UserDto> searchUsers(String query) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando usuarios con query: {}", query);
        
        List<UserDto> users = new ArrayList<>();
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME).get().get();
        
        snapshot.getDocuments().forEach(document -> {
            UserDto user = document.toObject(UserDto.class);
            if (user != null && 
                (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                 user.getEmail().toLowerCase().contains(query.toLowerCase()))) {
                user.setUserId(document.getId());
                users.add(user);
            }
        });
        
        log.info("✅ {} usuarios encontrados en búsqueda", users.size());
        return users;
    }

    /**
     * Actualiza un usuario existente
     */
    public UserDto updateUser(String userId, UserRequest request) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando usuario: {}", userId);
        
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(userId).get().get();
        
        if (!document.exists()) {
            log.warn("⚠️ Usuario no encontrado para actualizar: {}", userId);
            return null;
        }
        
        UserDto user = document.toObject(UserDto.class);
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setProfileImageUrl(request.getProfileImageUrl());
        user.setUpdatedAt(Timestamp.now());
        
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME)
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
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        DocumentSnapshot document = docRef.get().get();
        
        if (document.exists()) {
            ApiFuture<WriteResult> future = docRef.delete();
            WriteResult result = future.get();
            log.info("✅ Usuario eliminado exitosamente. Timestamp: {}", result.getUpdateTime());
            return true;
        } else {
            log.warn("⚠️ Usuario no encontrado para eliminar: {}", userId);
            return false;
        }
    }

    /**
     * Verifica si un usuario existe
     */
    public boolean userExists(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        return document.exists();
    }

    /**
     * Obtiene actividad del usuario
     */
    public Object getUserActivity(String userId) throws ExecutionException, InterruptedException {
        log.info("📊 Obteniendo actividad del usuario: {}", userId);
        
        UserDto user = getUserById(userId);
        if (user != null) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("userId", user.getUserId());
            activity.put("lastActivity", user.getUpdatedAt());
            activity.put("status", "active");
            activity.put("isPremium", user.getIsPremium());
            activity.put("premiumExpiresAt", user.getPremiumExpiresAt());
            
            log.info("✅ Actividad del usuario obtenida: {}", userId);
            return activity;
        } else {
            log.warn("⚠️ Usuario no encontrado para obtener actividad: {}", userId);
            return null;
        }
    }

    /**
     * Habilita acceso premium para usuario (cuando pagan por tour guide)
     */
    public UserDto enablePremiumAccess(String userId, int daysDuration) throws ExecutionException, InterruptedException {
        log.info("⭐ Habilitando acceso premium para usuario: {} por {} días", userId, daysDuration);
        
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(userId).get().get();
        
        if (!document.exists()) {
            log.warn("⚠️ Usuario no encontrado para activación premium: {}", userId);
            return null;
        }
        
        UserDto user = document.toObject(UserDto.class);
        user.setIsPremium(true);
        
        // Calcular fecha de expiración (días desde ahora)
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + (daysDuration * 24L * 60L * 60L * 1000L);
        user.setPremiumExpiresAt(Timestamp.of(new java.util.Date(expirationTime)));
        user.setUpdatedAt(Timestamp.now());
        
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME)
                .document(userId)
                .set(user);
        
        WriteResult result = future.get();
        log.info("✅ Acceso premium habilitado para usuario: {} por {} días. Timestamp: {}", 
                userId, daysDuration, result.getUpdateTime());
        
        return user;
    }

    /**
     * Verifica si un usuario tiene acceso premium activo
     */
    public boolean hasActivePremiumAccess(String userId) throws ExecutionException, InterruptedException {
        log.info("🔍 Verificando acceso premium para usuario: {}", userId);
        
        UserDto user = getUserById(userId);
        if (user == null || !user.getIsPremium()) {
            log.info("❌ Usuario no tiene acceso premium: {}", userId);
            return false;
        }
        
        // Verificar si el premium no ha expirado
        if (user.getPremiumExpiresAt() != null) {
            long currentTime = System.currentTimeMillis();
            long expirationTime = user.getPremiumExpiresAt().toDate().getTime();
            boolean isActive = currentTime < expirationTime;
            
            log.info("{} Acceso premium {} para usuario: {}", 
                    isActive ? "✅" : "❌", 
                    isActive ? "activo" : "expirado", 
                    userId);
            
            return isActive;
        }
        
        log.info("❌ Usuario no tiene fecha de expiración premium: {}", userId);
        return false;
    }



    /**
     * Actualiza el rol de un usuario (solo para admin)
     */
    public UserDto updateUserRole(String userId, String newRole) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando rol del usuario {} a {}", userId, newRole);
        
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(userId).get().get();
        
        if (!document.exists()) {
            log.warn("⚠️ Usuario no encontrado para actualizar rol: {}", userId);
            return null;
        }
        
        UserDto user = document.toObject(UserDto.class);
        user.setRole(newRole);
        user.setUpdatedAt(Timestamp.now());
        
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME)
                .document(userId)
                .set(user);
        
        WriteResult result = future.get();
        log.info("✅ Rol actualizado para usuario: {}. Nuevo rol: {}. Timestamp: {}", 
                userId, newRole, result.getUpdateTime());
        
        return user;
    }

    /**
     * Actualiza el estado de un usuario (solo para admin)
     */
    public UserDto updateUserStatus(String userId, boolean active) throws ExecutionException, InterruptedException {
        log.info("🔄 {} usuario: {}", active ? "Activando" : "Desactivando", userId);
        
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(userId).get().get();
        
        if (!document.exists()) {
            log.warn("⚠️ Usuario no encontrado para actualizar estado: {}", userId);
            return null;
        }
        
        UserDto user = document.toObject(UserDto.class);
        user.setIsActive(active);
        user.setUpdatedAt(Timestamp.now());
        
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME)
                .document(userId)
                .set(user);
        
        WriteResult result = future.get();
        log.info("✅ Estado actualizado para usuario: {}. Activo: {}. Timestamp: {}", 
                userId, active, result.getUpdateTime());
        
        return user;
    }
}
