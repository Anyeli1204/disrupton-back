package com.disrupton.collaborator.service;

import com.disrupton.collaborator.dto.*;
import com.disrupton.exception.ModerationRejectedException;
import com.disrupton.moderation.ModerationService;
import com.disrupton.user.dto.UserDto;
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
    private static final String COLLABORATOR_COMMENTS_COLLECTION = "collaborator_comments";

    private final ModerationService moderationService;

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
            data.put("region", dto.getRegion());
            data.put("descripcion", dto.getDescripcion());
            data.put("imagenesGaleria", dto.getImagenesGaleria());
            data.put("redesContacto", dto.getRedesContacto());
            data.put("precioAcceso", dto.getPrecioAcceso() != null ? dto.getPrecioAcceso() : 10.0);
            data.put("calificacion", 0.0);
            data.put("numeroResenas", 0);
            data.put("createdAt", Timestamp.now());

            firestore.collection(COLLABORATORS_COLLECTION).document(newId).set(data).get();

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
    public Page<CollaboratorDto> getCollaborators(String region, String tipo, String nombre, Pageable pageable, String currentUserId) {
        try {
            CollectionReference usersRef = firestore.collection(COLLABORATORS_COLLECTION);

            // Base query: obtener todos los documentos y filtrar después
            Query query = usersRef;

            // Aplicar filtros directos si están presentes (asumiendo que esos campos existen en el documento)
            if (region != null && !region.trim().isEmpty()) {
                query = query.whereEqualTo("region", region);
            }
            if (tipo != null && !tipo.trim().isEmpty()) {
                query = query.whereEqualTo("tipo", tipo);
            }

            // Obtener todos los documentos
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            log.info("📊 Documentos encontrados en colección {}: {}", COLLABORATORS_COLLECTION, documents.size());

            // Convertir y filtrar por rol y nombre
            List<CollaboratorDto> all = documents.stream()
                    .map(this::documentToDTO)
                    .filter(collab -> {
                        // Filtrar por rol (solo GUIDE y ARTISAN)
                        if (collab.getRole() == null || !Arrays.asList("GUIDE", "ARTISAN").contains(collab.getRole())) {
                            return false;
                        }
                        // Filtrar por nombre si se especifica
                        if (nombre != null && !nombre.trim().isEmpty()) {
                            return collab.getName() != null &&
                                    collab.getName().toLowerCase().contains(nombre.toLowerCase());
                        }
                        return true;
                    })
                    .sorted(Comparator.comparing(c -> Optional.ofNullable(c.getName()).orElse(""))) // orden por nombre
                    .map(collab -> {
                        boolean hasAccess = false;
                        if (currentUserId != null) {
                            // Si es premium globalmente o tiene acceso específico
                            hasAccess = isPremiumUser(currentUserId) || hasAccess(collab.getId(), currentUserId);
                        }
                        if (hasAccess) {
                            return CollaboratorDto.createPremiumView(collab);
                        } else {
                            return CollaboratorDto.createPublicView(collab);
                        }
                    })
                    .collect(Collectors.toList());

            // Paginación manual consistente con Pageable
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), all.size());
            List<CollaboratorDto> paginated = start < all.size() ? all.subList(start, end) : new ArrayList<>();

            return new PageImpl<>(paginated, pageable, all.size());

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al obtener agentes culturales", e);
        }
    }
    /**
     * Obtener detalle de agente cultural con validación de acceso
     */
    public CollaboratorDto getCollaboratorDetail(String userId, String currentUserId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLABORATORS_COLLECTION)
                    .document(userId)
                    .get().get();

            if (!doc.exists() || !Arrays.asList("GUIDE", "ARTISAN").contains(doc.getString("role"))) {
                throw new IllegalArgumentException("Colaborador no encontrado");
            }

            CollaboratorDto collaborator = documentToDTO(doc);

            // Verificar si el usuario actual tiene acceso a las redes de contacto
            boolean hasAccess = false;
            if (currentUserId != null) {
                // Si es premium globalmente O tiene acceso específico
                hasAccess = isPremiumUser(currentUserId) || hasAccess(userId, currentUserId);
            }

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
     * Actualizar campos de un colaborador existente
     */
    public CollaboratorDto updateCollaborator(String collaboratorId, CollaboratorDto updateDto) {
        try {
            // Verificar que el colaborador existe
            DocumentReference docRef = firestore.collection(COLLABORATORS_COLLECTION).document(collaboratorId);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                throw new IllegalArgumentException("El colaborador no existe");
            }

            String currentRole = snapshot.getString("role");
            if (!Arrays.asList("GUIDE", "ARTISAN").contains(currentRole)) {
                throw new IllegalArgumentException("El colaborador no tiene un rol válido");
            }

            // Preparar datos para actualizar (solo campos no nulos)
            Map<String, Object> updates = new HashMap<>();

            if (updateDto.getName() != null && !updateDto.getName().trim().isEmpty()) {
                updates.put("name", updateDto.getName().trim());
            }

            if (updateDto.getEmail() != null && !updateDto.getEmail().trim().isEmpty()) {
                updates.put("email", updateDto.getEmail().trim());
            }

            if (updateDto.getRole() != null && Arrays.asList("GUIDE", "ARTISAN").contains(updateDto.getRole())) {
                updates.put("role", updateDto.getRole());
            }

            if (updateDto.getRegion() != null && !updateDto.getRegion().trim().isEmpty()) {
                updates.put("region", updateDto.getRegion().trim());
            }

            if (updateDto.getDescripcion() != null && !updateDto.getDescripcion().trim().isEmpty()) {
                updates.put("descripcion", updateDto.getDescripcion().trim());
            }

            if (updateDto.getImagenesGaleria() != null) {
                updates.put("imagenesGaleria", updateDto.getImagenesGaleria());
            }

            if (updateDto.getRedesContacto() != null) {
                updates.put("redesContacto", updateDto.getRedesContacto());
            }

            if (updateDto.getPrecioAcceso() != null && updateDto.getPrecioAcceso() > 0) {
                updates.put("precioAcceso", updateDto.getPrecioAcceso());
            }

            // Solo actualizar si hay cambios
            if (updates.isEmpty()) {
                throw new IllegalArgumentException("No se proporcionaron campos válidos para actualizar");
            }

            // Actualizar documento en Firestore
            docRef.update(updates).get();

            log.info("Colaborador {} actualizado con campos: {}", collaboratorId, updates.keySet());

            // Obtener y retornar el colaborador actualizado
            DocumentSnapshot updatedSnapshot = docRef.get().get();
            return documentToDTO(updatedSnapshot);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error actualizando colaborador {}: {}", collaboratorId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar colaborador", e);
        }
    }
    /**
     * Desbloquear redes de contacto de agente cultural mediante pago (simulado)
     */
    public UnlockResponseDto unlockCollaborator(String agentId, String userId, UnlockRequestDto requestDto) {
        try {
            // 1. Usar una transacción para asegurar que todas las escrituras se completen o ninguna lo haga.
            return firestore.runTransaction(transaction -> {
                DocumentReference agentRef = firestore.collection(COLLABORATORS_COLLECTION).document(agentId);
                DocumentSnapshot agentDoc = transaction.get(agentRef).get();

                // 2. Usar "Guard Clauses" para validar y fallar rápido.
                if (!agentDoc.exists() || !Arrays.asList("GUIDE", "ARTISAN").contains(agentDoc.getString("role"))) {
                    throw new IllegalArgumentException("Colaborador no encontrado o rol inválido.");
                }

                if (hasAccess(agentId, userId)) {
                    throw new IllegalStateException("Ya tienes acceso a las redes de contacto de este agente.");
                }

                // 3. Lógica de negocio más limpia.
                double accessPrice = Optional.ofNullable(agentDoc.getDouble("precioAcceso")).orElse(1.0);
                String paymentId = "pay_" + UUID.randomUUID().toString().substring(0, 8);
                Timestamp now = Timestamp.now();

                // Registrar el pago
                DocumentReference paymentRef = firestore.collection(PAYMENTS_COLLECTION).document(paymentId);
                Map<String, Object> paymentRecord = createPaymentRecord(paymentId, userId, agentId, accessPrice, now);
                transaction.set(paymentRef, paymentRecord);

                // Registrar el acceso del usuario
                String accessId = userId + "_" + agentId;
                DocumentReference accessRef = firestore.collection(USER_ACCESS_COLLECTION).document(accessId);
                Map<String, Object> accessRecord = createAccessRecord(accessId, userId, agentId, paymentId, now);
                transaction.set(accessRef, accessRecord);

                log.info("Usuario {} desbloqueó al agente {} con pago {}", userId, agentId, paymentId);

                // 4. Devolver un DTO de respuesta en lugar de un Map.
                return new UnlockResponseDto(
                        true,
                        "Redes de contacto desbloqueadas exitosamente.",
                        paymentId,
                        true
                );
            }).get();

        } catch (Exception e) {
            // 5. Envolver excepciones específicas de Firestore en una excepción de runtime.
            log.error("Error en la transacción de desbloqueo para el agente {}: {}", agentId, e.getMessage(), e);
            // Si la excepción es una de nuestras excepciones de negocio, la volvemos a lanzar.
            if (e.getCause() instanceof IllegalArgumentException || e.getCause() instanceof IllegalStateException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException("Error procesando el desbloqueo. Inténtalo de nuevo.", e);
        }
    }

    // 6. Métodos de ayuda para crear los registros, mejorando la legibilidad.
    private Map<String, Object> createPaymentRecord(String paymentId, String userId, String agentId, double amount, Timestamp timestamp) {
        return Map.of(
                "id", paymentId,
                "userId", userId,
                "agentId", agentId,
                "amount", amount,
                "currency", "PEN",
                "status", "completed",
                "timestamp", timestamp
        );
    }

    private Map<String, Object> createAccessRecord(String accessId, String userId, String agentId, String paymentId, Timestamp timestamp) {
        return Map.of(
                "id", accessId,
                "userId", userId,
                "agentId", agentId,
                "grantedAt", timestamp,
                "paymentId", paymentId,
                "accessType", "contact_networks"
        );
    }
    public void deleteCollaborator(String collaboratorId) {
        try {
            DocumentReference docRef = firestore.collection(COLLABORATORS_COLLECTION).document(collaboratorId);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                throw new IllegalArgumentException("El colaborador no existe");
            }

            String role = snapshot.getString("role");
            if (!Arrays.asList("GUIDE", "ARTISAN").contains(role)) {
                throw new IllegalArgumentException("El usuario no es un colaborador válido");
            }

            docRef.delete().get();
            log.info("Colaborador eliminado: {}", collaboratorId);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al eliminar colaborador {}: {}", collaboratorId, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar colaborador", e);
        }
    }

    /**
     * Añadir comentario a un colaborador y aprobarlo
     */
    public CommentCollabResponseDto processAndSaveComment(String collaboratorId, CommentCollabRequestDto request) {
        String comentario = request.getComentario();
        String userId = request.getUserId();

        if (comentario == null || comentario.trim().isEmpty()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }

        if (request.getCalificacion() == null || request.getCalificacion() < 1.0 || request.getCalificacion() > 5.0) {
            throw new IllegalArgumentException("La calificación debe estar entre 1.0 y 5.0");
        }

        try {
            // Moderación automática
            boolean esSeguro = moderationService.isCommentSafe(comentario);
            if (!esSeguro) {
                String motivo = moderationService.getReasonIfUnsafe(comentario);
                throw new ModerationRejectedException(motivo != null ? motivo : "Contenido inapropiado");
            }

            // Verificar colaborador válido
            DocumentSnapshot collaboratorSnapshot = firestore.collection(COLLABORATORS_COLLECTION)
                    .document(collaboratorId).get().get();

            if (!collaboratorSnapshot.exists()) {
                throw new IllegalArgumentException("Colaborador no encontrado en la base de datos");
            }

            String role = collaboratorSnapshot.getString("role");
            if (role == null || !Arrays.asList("GUIDE", "ARTISAN").contains(role)) {
                throw new IllegalArgumentException("Rol no válido para comentarios: " + role);
            }

            // Obtener nombre del usuario
            DocumentSnapshot userSnapshot = firestore.collection("users").document(userId).get().get();
            String usuarioNombre = userSnapshot.exists() ? userSnapshot.getString("name") : "Anónimo";

            // Guardar comentario
            String commentId = UUID.randomUUID().toString();
            Timestamp now = Timestamp.now();

            Map<String, Object> commentData = Map.of(
                    "id", commentId,
                    "culturalAgentId", collaboratorId,
                    "authorUserId", userId,
                    "usuarioNombre", usuarioNombre,
                    "comentario", comentario,
                    "calificacion", request.getCalificacion(),
                    "fecha", now,
                    "isModerated", true,
                    "createdAt", now
            );

            firestore.collection(COLLABORATOR_COMMENTS_COLLECTION).document(commentId).set(commentData).get();

            // Actualizar calificación promedio del colaborador
            updateCollaboratorRating(collaboratorId);

            return CommentCollabResponseDto.builder()
                    .id(commentId)
                    .culturalAgentId(collaboratorId)
                    .authorUserId(userId)
                    .usuarioNombre(usuarioNombre)
                    .comentario(comentario)
                    .calificacion(request.getCalificacion())
                    .fecha(now)
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error al añadir comentario al colaborador {}: {}", collaboratorId, e.getMessage(), e);
            throw new RuntimeException("No se pudo guardar el comentario", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtener comentarios de un colaborador
     */
    public List<CommentCollabResponseDto> getCollaboratorComments(String collaboratorId) {
        try {
            // Verificar que el colaborador existe
            DocumentSnapshot collaboratorDoc = firestore.collection(COLLABORATORS_COLLECTION)
                    .document(collaboratorId).get().get();

            if (!collaboratorDoc.exists() || !Arrays.asList("GUIDE", "ARTISAN").contains(collaboratorDoc.getString("role"))) {
                throw new IllegalArgumentException("Colaborador no encontrado");
            }

            // Obtener comentarios ordenados por fecha (más recientes primero)
            Query commentsQuery = firestore.collection(COLLABORATOR_COMMENTS_COLLECTION)
                    .whereEqualTo("culturalAgentId", collaboratorId)
                    .orderBy("fecha", Query.Direction.DESCENDING);

            List<QueryDocumentSnapshot> commentDocs = commentsQuery.get().get().getDocuments();

            return commentDocs.stream()
                    .map(doc -> CommentCollabResponseDto.builder()
                            .id(doc.getString("id"))
                            .culturalAgentId(doc.getString("culturalAgentId"))
                            .authorUserId(doc.getString("authorUserId"))
                            .usuarioNombre(doc.getString("usuarioNombre"))
                            .comentario(doc.getString("comentario"))
                            .calificacion(doc.getDouble("calificacion"))
                            .fecha(doc.getTimestamp("fecha"))
                            .build())
                    .collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error obteniendo comentarios del colaborador {}: {}", collaboratorId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener comentarios", e);
        }
    }

    /**
     * Actualizar calificación promedio del colaborador basado en comentarios
     */
    private void updateCollaboratorRating(String collaboratorId) {
        try {
            // Obtener todos los comentarios del colaborador
            Query commentsQuery = firestore.collection(COLLABORATOR_COMMENTS_COLLECTION)
                    .whereEqualTo("culturalAgentId", collaboratorId);

            List<QueryDocumentSnapshot> commentDocs = commentsQuery.get().get().getDocuments();

            if (commentDocs.isEmpty()) {
                return; // No hay comentarios, mantener calificación actual
            }

            // Calcular promedio
            double totalRating = commentDocs.stream()
                    .mapToDouble(doc -> Optional.ofNullable(doc.getDouble("calificacion")).orElse(0.0))
                    .sum();

            double averageRating = totalRating / commentDocs.size();
            int numberOfReviews = commentDocs.size();

            // Actualizar colaborador
            DocumentReference collaboratorRef = firestore.collection(COLLABORATORS_COLLECTION).document(collaboratorId);
            Map<String, Object> updates = Map.of(
                    "calificacion", Math.round(averageRating * 10.0) / 10.0, // Redondear a 1 decimal
                    "numeroResenas", numberOfReviews
            );

            collaboratorRef.update(updates).get();
            log.info("Calificación actualizada para colaborador {}: {} ({} reseñas)",
                    collaboratorId, averageRating, numberOfReviews);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error actualizando calificación del colaborador {}: {}", collaboratorId, e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo principal
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
            DocumentSnapshot doc = firestore.collection(COLLABORATORS_COLLECTION)
                    .document(agentId)
                    .get().get();

            if (!doc.exists() || !Arrays.asList("GUIDE", "ARTISAN").contains(doc.getString("role"))) {
                throw new IllegalArgumentException("Colaborador no encontrado");
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
     * Verifica la expiracion del plan premium
     */

    private UserDto fetchUser(String userId) {
        try {
            DocumentSnapshot userDoc = firestore.collection("users").document(userId).get().get();
            if (!userDoc.exists()) return null;

            Boolean isPremium = userDoc.getBoolean("isPremium");
            Timestamp expiresAt = userDoc.getTimestamp("premiumExpiresAt");

            // Normalizar: si expiró, no es premium
            if (expiresAt != null && expiresAt.toDate().before(new Date())) {
                isPremium = false;
            }

            return new UserDto(
                    userDoc.getId(),
                    userDoc.getString("email"),
                    userDoc.getString("name"),
                    userDoc.getString("role"),
                    null, // profileImageUrl
                    userDoc.getBoolean("isActive"),
                    isPremium != null ? isPremium : false,
                    userDoc.getTimestamp("createdAt"),
                    userDoc.getTimestamp("updatedAt"),
                    expiresAt
            );
        } catch (InterruptedException | ExecutionException e) {
            log.warn("No se pudo obtener usuario {}: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si el usuario es premium o no
     */
    private boolean isPremiumUser(String userId) {
        UserDto user = fetchUser(userId);
        if (user == null) return false;

        if (Boolean.TRUE.equals(user.getIsPremium())) {
            // Si hay expiración, ya se verificó en fetchUser que no esté vencida
            return true;
        }
        return false;
    }


    /**
     * Convertir documento de Firebase a DTO
     */
    private CollaboratorDto documentToDTO(DocumentSnapshot doc) {
        return CollaboratorDto.builder()
                .id(doc.getId())
                .name(doc.getString("name"))
                .email(doc.getString("email"))
                .role(doc.getString("role"))
                .region(doc.getString("region"))
                .createdAt(doc.getTimestamp("createdAt"))
                .descripcion(doc.getString("descripcion"))
                .calificacion(doc.getDouble("calificacion") != null ? doc.getDouble("calificacion") : 0.0)
                .numeroResenas(doc.getLong("numeroResenas") != null ? doc.getLong("numeroResenas").intValue() : 0)
                .imagenesGaleria((List<String>) doc.get("imagenesGaleria"))
                .redesContacto((Map<String, String>) doc.get("redesContacto"))
                .precioAcceso(doc.getDouble("precioAcceso") != null ? doc.getDouble("precioAcceso") : 10.0)
                .build();
    }
};
