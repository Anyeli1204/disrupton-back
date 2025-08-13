package com.disrupton.userAccess.service;

import com.disrupton.userAccess.dto.UserAccessDto;
import com.disrupton.userAccess.dto.UserAccessRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccessService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "user_access";

    /**
     * Obtiene todos los accesos de usuario
     */
    public List<UserAccessDto> getAllUserAccess() throws ExecutionException, InterruptedException {
        log.info("📋 Obteniendo todos los accesos de usuario");
        
        List<UserAccessDto> accesses = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        QuerySnapshot querySnapshot = future.get();
        
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            UserAccessDto access = document.toObject(UserAccessDto.class);
            access.setId(document.getId());
            accesses.add(access);
        }
        
        log.info("✅ {} accesos de usuario encontrados", accesses.size());
        return accesses;
    }

    /**
     * Obtiene un acceso por ID
     */
    public UserAccessDto getUserAccessById(String accessId) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando acceso de usuario con ID: {}", accessId);
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(accessId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            UserAccessDto access = document.toObject(UserAccessDto.class);
            access.setId(document.getId());
            log.info("✅ Acceso de usuario encontrado: {}", accessId);
            return access;
        } else {
            log.warn("⚠️ Acceso de usuario no encontrado con ID: {}", accessId);
            return null;
        }
    }

    /**
     * Crea un nuevo acceso de usuario
     */
    public UserAccessDto createUserAccess(UserAccessRequest request) throws ExecutionException, InterruptedException {
        log.info("🔓 Creando nuevo acceso para usuario: {} - Tipo: {}", request.getUserId(), request.getAccessType());
        
        UserAccessDto access = new UserAccessDto();
        access.setId(UUID.randomUUID().toString());
        access.setUserId(request.getUserId());
        access.setAgentId(request.getAgentId());
        access.setAccessType(request.getAccessType());
        access.setPaymentId(request.getPaymentId());
        access.setDescription(request.getDescription());
        access.setPrice(request.getPrice());
        access.setCurrency(request.getCurrency());
        access.setStatus("active");
        access.setGrantedAt(Timestamp.now());
        
        // Calcular fecha de expiración si se especifica duración
        if (request.getDurationDays() != null && request.getDurationDays() > 0) {
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(request.getDurationDays());
            access.setExpiresAt(Timestamp.of(java.util.Date.from(
                expiresAt.atZone(ZoneId.systemDefault()).toInstant()
            )));
        }
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(access.getId());
        ApiFuture<WriteResult> future = docRef.set(access);
        WriteResult result = future.get();
        
        log.info("✅ Acceso de usuario creado exitosamente: {}. Timestamp: {}", access.getId(), result.getUpdateTime());
        return access;
    }

    /**
     * Actualiza un acceso de usuario
     */
    public UserAccessDto updateUserAccess(String accessId, UserAccessRequest request) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando acceso de usuario: {}", accessId);
        
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(accessId).get().get();
        
        if (!document.exists()) {
            log.warn("⚠️ Acceso de usuario no encontrado para actualizar: {}", accessId);
            return null;
        }
        
        UserAccessDto access = document.toObject(UserAccessDto.class);
        access.setAgentId(request.getAgentId());
        access.setAccessType(request.getAccessType());
        access.setPaymentId(request.getPaymentId());
        access.setDescription(request.getDescription());
        access.setPrice(request.getPrice());
        access.setCurrency(request.getCurrency());
        
        // Recalcular fecha de expiración si se especifica nueva duración
        if (request.getDurationDays() != null && request.getDurationDays() > 0) {
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(request.getDurationDays());
            access.setExpiresAt(Timestamp.of(java.util.Date.from(
                expiresAt.atZone(ZoneId.systemDefault()).toInstant()
            )));
        }
        
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME)
                .document(accessId)
                .set(access);
        
        WriteResult result = future.get();
        log.info("✅ Acceso de usuario actualizado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return access;
    }

    /**
     * Elimina un acceso de usuario
     */
    public boolean deleteUserAccess(String accessId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando acceso de usuario: {}", accessId);
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(accessId);
        DocumentSnapshot document = docRef.get().get();
        
        if (document.exists()) {
            ApiFuture<WriteResult> future = docRef.delete();
            WriteResult result = future.get();
            log.info("✅ Acceso de usuario eliminado exitosamente. Timestamp: {}", result.getUpdateTime());
            return true;
        } else {
            log.warn("⚠️ Acceso de usuario no encontrado para eliminar: {}", accessId);
            return false;
        }
    }

    /**
     * Obtiene accesos por usuario
     */
    public List<UserAccessDto> getUserAccessByUserId(String userId) throws ExecutionException, InterruptedException {
        log.info("👤 Buscando accesos para usuario: {}", userId);
        
        List<UserAccessDto> accesses = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            UserAccessDto access = document.toObject(UserAccessDto.class);
            access.setId(document.getId());
            accesses.add(access);
        }
        
        log.info("✅ {} accesos encontrados para usuario: {}", accesses.size(), userId);
        return accesses;
    }

    /**
     * Obtiene accesos por tipo
     */
    public List<UserAccessDto> getUserAccessByType(String accessType) throws ExecutionException, InterruptedException {
        log.info("🔑 Buscando accesos por tipo: {}", accessType);
        
        List<UserAccessDto> accesses = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("accessType", accessType)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            UserAccessDto access = document.toObject(UserAccessDto.class);
            access.setId(document.getId());
            accesses.add(access);
        }
        
        log.info("✅ {} accesos encontrados de tipo: {}", accesses.size(), accessType);
        return accesses;
    }

    /**
     * Obtiene accesos activos
     */
    public List<UserAccessDto> getActiveUserAccess() throws ExecutionException, InterruptedException {
        log.info("✅ Buscando accesos activos");
        
        List<UserAccessDto> accesses = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", "active")
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            UserAccessDto access = document.toObject(UserAccessDto.class);
            access.setId(document.getId());
            accesses.add(access);
        }
        
        log.info("✅ {} accesos activos encontrados", accesses.size());
        return accesses;
    }

    /**
     * Verifica si un usuario tiene acceso activo a un tipo específico
     */
    public boolean hasActiveAccess(String userId, String accessType) throws ExecutionException, InterruptedException {
        log.info("🔍 Verificando acceso activo para usuario: {} - Tipo: {}", userId, accessType);
        
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("accessType", accessType)
                .whereEqualTo("status", "active")
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        if (!querySnapshot.isEmpty()) {
            UserAccessDto access = querySnapshot.getDocuments().get(0).toObject(UserAccessDto.class);
            
            // Verificar si no ha expirado
            if (access.getExpiresAt() != null) {
                long currentTime = System.currentTimeMillis();
                long expirationTime = access.getExpiresAt().toDate().getTime();
                boolean isActive = currentTime < expirationTime;
                
                log.info("{} Acceso {} para usuario: {} - Tipo: {}", 
                        isActive ? "✅" : "❌", 
                        isActive ? "activo" : "expirado", 
                        userId, accessType);
                
                return isActive;
            } else {
                log.info("✅ Acceso activo sin expiración para usuario: {} - Tipo: {}", userId, accessType);
                return true;
            }
        } else {
            log.info("❌ No se encontró acceso activo para usuario: {} - Tipo: {}", userId, accessType);
            return false;
        }
    }

    /**
     * Obtiene estadísticas de accesos
     */
    public Map<String, Object> getUserAccessStats() throws ExecutionException, InterruptedException {
        log.info("📊 Obteniendo estadísticas de accesos de usuario");
        
        Map<String, Object> stats = new HashMap<>();
        
        QuerySnapshot allAccess = firestore.collection(COLLECTION_NAME).get().get();
        QuerySnapshot activeAccess = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", "active")
                .get().get();
        
        stats.put("totalAccess", allAccess.size());
        stats.put("activeAccess", activeAccess.size());
        stats.put("expiredAccess", allAccess.size() - activeAccess.size());
        
        // Contar por tipo de acceso
        Map<String, Integer> typeCounts = new HashMap<>();
        for (QueryDocumentSnapshot document : allAccess) {
            UserAccessDto access = document.toObject(UserAccessDto.class);
            String type = access.getAccessType();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        stats.put("accessByType", typeCounts);
        
        // Calcular ingresos totales
        double totalRevenue = 0;
        for (QueryDocumentSnapshot document : allAccess) {
            UserAccessDto access = document.toObject(UserAccessDto.class);
            if (access.getPrice() != null) {
                totalRevenue += access.getPrice();
            }
        }
        stats.put("totalRevenue", totalRevenue);
        
        log.info("✅ Estadísticas de accesos obtenidas");
        return stats;
    }
}

