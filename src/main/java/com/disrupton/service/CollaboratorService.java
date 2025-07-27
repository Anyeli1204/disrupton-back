package com.disrupton.service;

import com.disrupton.dto.CollaboratorDto;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CollaboratorService {

    private final Firestore firestore;
    private static final String COLLABORATORS_COLLECTION = "collaborators";
    private static final String USER_ACCESS_COLLECTION = "user_access";
    private static final String PAYMENTS_COLLECTION = "payments";

    /**
     * Crear un nuevo agente cultural en Firestore
     */
    public CollaboratorDto createCollaborator(CollaboratorDto dto) {
        try {
            String newId = UUID.randomUUID().toString(); // ID único

            Map<String, Object> data = new HashMap<>();
            data.put("name", dto.getName());
            data.put("email", dto.getEmail());
            data.put("role", dto.getRole());
            data.put("descripcion", dto.getDescripcion());
            data.put("calificacion", dto.getCalificacion());
            data.put("imagenesGaleria", dto.getImagenesGaleria());
            data.put("redesContacto", dto.getRedesContacto());
            data.put("precioAcceso", dto.getPrecioAcceso() != null ? dto.getPrecioAcceso() : 10.0);
            data.put("createdAt", Timestamp.now());

            firestore.collection("users").document(newId).set(data).get();

            dto.setId(newId);
            dto.setCreatedAt(Timestamp.now());

            return dto;

        } catch (Exception e) {
            log.error("Error creando colaborador: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo crear el colaborador", e);
        }
    }


    /**
     * Obtener agentes culturales con filtros y paginación
     */
    public Page<CollaboratorDto> getCollaborators(String region, String tipo, String nombre, Pageable pageable) {
        try {
            // Solo obtener usuarios con rol AGENTE_CULTURAL
            CollectionReference usersRef = firestore.collection("users");
            Query query = usersRef.whereEqualTo("role", "AGENTE_CULTURAL");

            // Ejecutar consulta
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

            // Filtrar por nombre si se especifica
            List<CollaboratorDto> collaborators = documents.stream()
                    .map(this::documentToDTO)
                    .filter(collab -> nombre == null || nombre.trim().isEmpty() ||
                            collab.getName().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());

            // Aplicar paginación manual
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), collaborators.size());
            List<CollaboratorDto> paginatedList = start < collaborators.size() ?
                    collaborators.subList(start, end) : new ArrayList<>();

            return new PageImpl<>(paginatedList, pageable, collaborators.size());

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error obteniendo agentes culturales: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener agentes culturales", e);
        }
    }

    /**
     * Obtener detalle de agente cultural con validación de acceso
     */
    public CollaboratorDto getCollaboratorDetail(String userId, String currentUserId) {
        try {
            DocumentSnapshot doc = firestore.collection("users")
                    .document(userId)
                    .get().get();

            if (!doc.exists() || !"AGENTE_CULTURAL".equals(doc.getString("role"))) {
                throw new IllegalArgumentException("Agente cultural no encontrado");
            }

            CollaboratorDto collaborator = documentToDTO(doc);

            // Verificar si el usuario actual tiene acceso a las redes de contacto
            boolean hasAccess = hasAccess(userId, currentUserId);

            // Retornar vista apropiada según el acceso
            if (hasAccess) {
                return CollaboratorDto.createPremiumView(collaborator);
            } else {
                return CollaboratorDto.createPublicView(collaborator);
            }

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error obteniendo detalle del agente cultural {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener detalle del agente cultural", e);
        }
    }

    /**
     * Desbloquear redes de contacto de agente cultural mediante pago (simulado)
     */
    public Map<String, Object> unlockCollaborator(String agentId, String userId, Map<String, Object> paymentData) {
        try {
            // Verificar que el agente cultural existe
            DocumentSnapshot agentDoc = firestore.collection("users")
                    .document(agentId)
                    .get().get();

            if (!agentDoc.exists() || !"AGENTE_CULTURAL".equals(agentDoc.getString("role"))) {
                return Map.of("error", "Agente cultural no encontrado");
            }

            // Verificar si ya tiene acceso
            if (hasAccess(agentId, userId)) {
                return Map.of("error", "Ya tienes acceso a las redes de contacto de este agente");
            }

            // Obtener precio de acceso (por defecto 10 soles)
            Double precioAcceso = agentDoc.getDouble("precioAcceso");
            if (precioAcceso == null) {
                precioAcceso = 10.0;
            }

            // Simular procesamiento de pago
            String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 8);
            Timestamp now = Timestamp.now();

            // Registrar el pago
            Map<String, Object> paymentRecord = Map.of(
                    "id", paymentId,
                    "userId", userId,
                    "agentId", agentId,
                    "amount", precioAcceso,
                    "currency", "PEN",
                    "status", "completed",
                    "timestamp", now
            );

            firestore.collection(PAYMENTS_COLLECTION).document(paymentId).set(paymentRecord);

            // Registrar el acceso del usuario
            String accessId = userId + "_" + agentId;
            Map<String, Object> accessRecord = Map.of(
                    "id", accessId,
                    "userId", userId,
                    "agentId", agentId,
                    "grantedAt", now,
                    "paymentId", paymentId,
                    "accessType", "contact_networks"
            );

            firestore.collection(USER_ACCESS_COLLECTION).document(accessId).set(accessRecord);

            log.info("Usuario {} desbloqueó redes de contacto del agente {} con pago {}", userId, agentId, paymentId);

            return Map.of(
                    "success", true,
                    "message", "Redes de contacto desbloqueadas exitosamente",
                    "paymentId", paymentId,
                    "accessGranted", true
            );

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error desbloqueando agente {}: {}", agentId, e.getMessage(), e);
            return Map.of("error", "Error procesando el desbloqueo");
        }
    }

    /**
     * Verificar si un usuario tiene acceso a las redes de contacto de un agente
     */
    public boolean hasAccess(String agentId, String userId) {
        if (userId == null) {
            return false;
        }

        try {
            String accessId = userId + "_" + agentId;
            DocumentSnapshot accessDoc = firestore.collection(USER_ACCESS_COLLECTION)
                    .document(accessId)
                    .get().get();

            return accessDoc.exists();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error verificando acceso para usuario {} y agente {}: {}",
                    userId, agentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtener estadísticas de un agente cultural
     */
    public Map<String, Object> getCollaboratorStats(String agentId) {
        try {
            // Obtener información básica del agente
            DocumentSnapshot doc = firestore.collection("users")
                    .document(agentId)
                    .get().get();

            if (!doc.exists() || !"AGENTE_CULTURAL".equals(doc.getString("role"))) {
                throw new IllegalArgumentException("Agente cultural no encontrado");
            }

            // Contar accesos pagados
            Query accessQuery = firestore.collection(USER_ACCESS_COLLECTION)
                    .whereEqualTo("agentId", agentId);
            int totalAccesses = accessQuery.get().get().getDocuments().size();

            Double precioAcceso = doc.getDouble("precioAcceso");
            if (precioAcceso == null) {
                precioAcceso = 10.0;
            }

            return Map.of(
                    "agentId", agentId,
                    "totalAccesses", totalAccesses,
                    "revenue", totalAccesses * precioAcceso,
                    "calificacion", doc.getDouble("calificacion") != null ? doc.getDouble("calificacion") : 0.0,
                    "lastUpdated", Timestamp.now()
            );

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error obteniendo estadísticas del agente {}: {}", agentId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas", e);
        }
    }

    /**
     * Incrementar contador de visualizaciones
     */
    private void incrementViewCount(String collaboratorId) {
        try {
            DocumentReference docRef = firestore.collection(COLLABORATORS_COLLECTION).document(collaboratorId);
            firestore.runTransaction(transaction -> {
                DocumentSnapshot doc = transaction.get(docRef).get();
                Long currentViews = doc.getLong("totalVisualizaciones");
                transaction.update(docRef, "totalVisualizaciones", (currentViews != null ? currentViews : 0L) + 1);
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Error incrementando visualizaciones para colaborador {}: {}", collaboratorId, e.getMessage());
        }
    }

    /**
     * Convertir documento de Firebase a DTO
     */
    private CollaboratorDto documentToDTO(DocumentSnapshot doc) {
        // Obtener comentarios destacados (simulados por ahora)
        List<CollaboratorDto.ComentarioDto> comentarios = List.of(
                CollaboratorDto.ComentarioDto.builder()
                        .id("com1")
                        .usuarioNombre("Ana García")
                        .comentario("Excelente guía, muy conocedor de la cultura inca")
                        .calificacion(5.0)
                        .fecha(Timestamp.now())
                        .build(),
                CollaboratorDto.ComentarioDto.builder()
                        .id("com2")
                        .usuarioNombre("Carlos Mendoza")
                        .comentario("Una experiencia única, recomendado 100%")
                        .calificacion(4.8)
                        .fecha(Timestamp.now())
                        .build()
        );

        return CollaboratorDto.builder()
                .id(doc.getId())
                .name(doc.getString("name"))
                .email(doc.getString("email"))
                .role(doc.getString("role"))
                .createdAt(doc.getTimestamp("createdAt"))
                .descripcion(doc.getString("descripcion"))
                .calificacion(doc.getDouble("calificacion"))
                .imagenesGaleria((List<String>) doc.get("imagenesGaleria"))
                .comentariosDestacados(comentarios)
                .redesContacto((Map<String, String>) doc.get("redesContacto"))
                .precioAcceso(doc.getDouble("precioAcceso") != null ? doc.getDouble("precioAcceso") : 10.0)
                .build();
    }
};
