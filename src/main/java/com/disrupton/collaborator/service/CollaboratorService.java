package com.disrupton.collaborator.service;

import com.disrupton.collaborator.dto.CollaboratorDto;
import com.disrupton.collaborator.dto.CommentCollabRequestDto;
import com.disrupton.collaborator.dto.CommentCollabResponseDto;
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
            data.put("descripcion", dto.getDescripcion());
            data.put("imagenesGaleria", dto.getImagenesGaleria());
            data.put("redesContacto", dto.getRedesContacto());
            data.put("precioAcceso", dto.getPrecioAcceso() != null ? dto.getPrecioAcceso() : 10.0);
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
                        // Filtrar por rol (GUIDE, ARTISAN, AGENTE_CULTURAL)
                        if (collab.getRole() == null || !Arrays.asList("GUIDE", "ARTISAN", "AGENTE_CULTURAL").contains(collab.getRole())) {
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

            if (!doc.exists() || !Arrays.asList("GUIDE", "ARTISAN", "AGENTE_CULTURAL").contains(doc.getString("role"))) {
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
     * Desbloquear redes de contacto de agente cultural mediante pago (simulado)
     */
    public Map<String, Object> unlockCollaborator(String agentId, String userId, Map<String, Object> paymentData) {
        try {
            // Verificar que el agente cultural existe
            DocumentSnapshot agentDoc = firestore.collection(COLLABORATORS_COLLECTION)
                    .document(agentId)
                    .get().get();

            if (!agentDoc.exists() || !Arrays.asList("GUIDE", "ARTISAN", "AGENTE_CULTURAL").contains(agentDoc.getString("role"))) {
                return Map.of("error", "Colaborador no encontrado");
            }

            // Verificar si ya tiene acceso
            if (hasAccess(agentId, userId)) {
                return Map.of("error", "Ya tienes acceso a las redes de contacto de este agente");
            }

            // Obtener precio de acceso (por defecto 1 soles)
            Double precioAcceso = agentDoc.getDouble("precioAcceso");
            if (precioAcceso == null) {
                precioAcceso = 1.0;
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

    public void deleteCollaborator(String collaboratorId) {
        try {
            DocumentReference docRef = firestore.collection(COLLABORATORS_COLLECTION).document(collaboratorId);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                throw new IllegalArgumentException("El colaborador no existe");
            }

            String role = snapshot.getString("role");
            if (!"AGENTE_CULTURAL".equals(role)) {
                throw new IllegalArgumentException("El usuario no es un agente cultural");
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
            if (!collaboratorSnapshot.exists() ||
                    !"AGENTE_CULTURAL".equals(collaboratorSnapshot.getString("role"))) {
                throw new IllegalArgumentException("Agente cultural no encontrado");
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

            firestore.collection("collaborator_comments").document(commentId).set(commentData).get();

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

            if (!doc.exists() || !Arrays.asList("GUIDE", "ARTISAN", "AGENTE_CULTURAL").contains(doc.getString("role"))) {
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
        // Obtener comentarios destacados (simulados por ahora)
        List<CommentCollabResponseDto> comentarios = List.of(
                CommentCollabResponseDto.builder()
                        .id("com1")
                        .usuarioNombre("Ana García")
                        .comentario("Excelente guía, muy conocedor de la cultura inca")
                        .calificacion(5.0)
                        .fecha(Timestamp.now())
                        .build(),
                CommentCollabResponseDto.builder()
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
