package com.disrupton.service;

import com.disrupton.dto.AvatarDto;
import com.disrupton.dto.AvatarConversationDto;
import com.disrupton.dto.AvatarSessionDto;
import com.disrupton.dto.AvatarKnowledgeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Servicio principal para la funcionalidad de Avatar Cultural Interactivo
 * Coordina todas las operaciones relacionadas con avatares, conversaciones y conocimiento
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarCulturalService {
    
    private final FirebaseAvatarService avatarService;
    private final FirebaseAvatarConversationService conversationService;
    private final FirebaseAvatarSessionService sessionService;
    private final FirebaseAvatarKnowledgeService knowledgeService;
    private final FirebaseCulturalObjectService culturalObjectService;
    
    // ===== OPERACIONES DE AVATAR =====
    
    /**
     * Crea un nuevo avatar cultural completo
     */
    public AvatarDto createCulturalAvatar(AvatarDto avatarData) throws ExecutionException, InterruptedException {
        log.info("🎭 Creando nuevo avatar cultural: {}", avatarData.getName());
        
        // Validar datos del avatar
        validateAvatarData(avatarData);
        
        // Establecer valores por defecto
        setDefaultAvatarValues(avatarData);
        
        // Guardar avatar en Firebase
        AvatarDto savedAvatar = avatarService.saveAvatar(avatarData);
        
        log.info("✅ Avatar cultural creado exitosamente: {}", savedAvatar.getAvatarId());
        return savedAvatar;
    }
    
    /**
     * Obtiene avatares recomendados para un usuario basado en su ubicación y preferencias
     */
    public List<AvatarDto> getRecommendedAvatars(String userId, String campusZone, String culturalInterest) throws ExecutionException, InterruptedException {
        log.info("🎯 Obteniendo avatares recomendados para usuario {} en zona {}", userId, campusZone);
        
        List<AvatarDto> recommendations = new ArrayList<>();
        
        // Obtener avatares de la zona del campus
        if (campusZone != null) {
            List<AvatarDto> zoneAvatars = avatarService.getAvatarsByCampusZone(campusZone);
            recommendations.addAll(zoneAvatars);
        }
        
        // Obtener avatares por interés cultural
        if (culturalInterest != null) {
            List<AvatarDto> interestAvatars = avatarService.getAvatarsByKnowledgeArea(culturalInterest);
            recommendations.addAll(interestAvatars);
        }
        
        // Obtener avatares populares como respaldo
        List<AvatarDto> popularAvatars = avatarService.getPopularAvatars(5);
        recommendations.addAll(popularAvatars);
        
        // Eliminar duplicados y limitar resultados
        List<AvatarDto> uniqueRecommendations = recommendations.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
        
        log.info("✅ {} avatares recomendados encontrados", uniqueRecommendations.size());
        return uniqueRecommendations;
    }
    
    /**
     * Busca avatares inteligentemente basado en criterios múltiples
     */
    public List<AvatarDto> smartSearchAvatars(String query, String region, String language, String expertise) throws ExecutionException, InterruptedException {
        log.info("🔍 Búsqueda inteligente de avatares: {}", query);
        
        List<AvatarDto> results = new ArrayList<>();
        
        // Búsqueda por texto
        if (query != null && !query.trim().isEmpty()) {
            List<AvatarDto> textResults = avatarService.searchAvatars(query);
            results.addAll(textResults);
        }
        
        // Filtrar por región si se especifica
        if (region != null) {
            List<AvatarDto> regionResults = avatarService.getAvatarsByRegion(region);
            if (results.isEmpty()) {
                results.addAll(regionResults);
            } else {
                results = results.stream()
                        .filter(avatar -> region.equals(avatar.getCulturalRegion()))
                        .collect(Collectors.toList());
            }
        }
        
        // Filtrar por idioma si se especifica
        if (language != null) {
            List<AvatarDto> languageResults = avatarService.getAvatarsByLanguage(language);
            if (results.isEmpty()) {
                results.addAll(languageResults);
            } else {
                results = results.stream()
                        .filter(avatar -> avatar.getLanguages() != null && avatar.getLanguages().contains(language))
                        .collect(Collectors.toList());
            }
        }
        
        // Filtrar por área de expertise si se especifica
        if (expertise != null) {
            List<AvatarDto> expertiseResults = avatarService.getAvatarsByKnowledgeArea(expertise);
            if (results.isEmpty()) {
                results.addAll(expertiseResults);
            } else {
                results = results.stream()
                        .filter(avatar -> avatar.getKnowledgeAreas() != null && avatar.getKnowledgeAreas().contains(expertise))
                        .collect(Collectors.toList());
            }
        }
        
        // Eliminar duplicados y ordenar por relevancia
        List<AvatarDto> uniqueResults = results.stream()
                .distinct()
                .sorted((a, b) -> {
                    // Ordenar por rating y popularidad
                    int ratingCompare = Double.compare(
                            b.getRating() != null ? b.getRating() : 0.0,
                            a.getRating() != null ? a.getRating() : 0.0
                    );
                    if (ratingCompare != 0) return ratingCompare;
                    
                    return Long.compare(
                            b.getTotalInteractions() != null ? b.getTotalInteractions() : 0L,
                            a.getTotalInteractions() != null ? a.getTotalInteractions() : 0L
                    );
                })
                .limit(20)
                .collect(Collectors.toList());
        
        log.info("✅ {} avatares encontrados en búsqueda inteligente", uniqueResults.size());
        return uniqueResults;
    }
    
    // ===== OPERACIONES DE CONVERSACIÓN =====
    
    /**
     * Inicia una nueva conversación con un avatar
     */
    public AvatarSessionDto startConversation(String avatarId, String userId, String deviceType, String campusZone) throws ExecutionException, InterruptedException {
        log.info("💬 Iniciando conversación entre avatar {} y usuario {}", avatarId, userId);
        
        // Crear nueva sesión
        AvatarSessionDto session = new AvatarSessionDto();
        session.setAvatarId(avatarId);
        session.setUserId(userId);
        session.setDeviceType(deviceType);
        session.setCampusZone(campusZone);
        session.setSessionType("FREE_CONVERSATION");
        session.setStartTime(com.google.cloud.Timestamp.now());
        
        // Guardar sesión
        AvatarSessionDto savedSession = sessionService.createSession(session);
        
        // Incrementar interacciones del avatar
        avatarService.incrementAvatarInteractions(avatarId);
        
        // Crear mensaje de saludo inicial
        createWelcomeMessage(savedSession);
        
        log.info("✅ Conversación iniciada con ID de sesión: {}", savedSession.getSessionId());
        return savedSession;
    }
    
    /**
     * Procesa un mensaje del usuario y genera respuesta del avatar
     */
    public AvatarConversationDto processUserMessage(String sessionId, String message, String messageType) throws ExecutionException, InterruptedException {
        log.info("📝 Procesando mensaje del usuario en sesión: {}", sessionId);
        
        // Obtener información de la sesión
        AvatarSessionDto session = sessionService.getSessionById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Sesión no encontrada: " + sessionId);
        }
        
        // Guardar mensaje del usuario
        AvatarConversationDto userMessage = new AvatarConversationDto();
        userMessage.setSessionId(sessionId);
        userMessage.setAvatarId(session.getAvatarId());
        userMessage.setUserId(session.getUserId());
        userMessage.setMessageType("USER_MESSAGE");
        userMessage.setContent(message);
        userMessage.setTimestamp(com.google.cloud.Timestamp.now());
        userMessage.setMessageSource(messageType);
        userMessage.setCampusLocation(session.getCampusZone());
        
        AvatarConversationDto savedUserMessage = conversationService.saveConversation(userMessage);
        
        // Generar respuesta del avatar
        AvatarConversationDto avatarResponse = generateAvatarResponse(session, message);
        AvatarConversationDto savedAvatarResponse = conversationService.saveConversation(avatarResponse);
        
        // Actualizar estadísticas de sesión
        sessionService.updateSessionStats(sessionId, 2, 1, 0);
        
        log.info("✅ Mensaje procesado y respuesta generada");
        return savedAvatarResponse;
    }
    
    /**
     * Finaliza una conversación y calcula métricas
     */
    public void endConversation(String sessionId, String endReason, Double userSatisfaction, String feedback) throws ExecutionException, InterruptedException {
        log.info("🔚 Finalizando conversación de sesión: {}", sessionId);
        
        // Finalizar sesión
        sessionService.endSession(sessionId, endReason);
        
        // Actualizar satisfacción si se proporciona
        if (userSatisfaction != null) {
            sessionService.updateSessionSatisfaction(sessionId, userSatisfaction, feedback);
            
            // Actualizar rating del avatar
            AvatarSessionDto session = sessionService.getSessionById(sessionId);
            if (session != null) {
                avatarService.updateAvatarRating(session.getAvatarId(), userSatisfaction);
            }
        }
        
        log.info("✅ Conversación finalizada exitosamente");
    }
    
    // ===== OPERACIONES DE CONOCIMIENTO =====
    
    /**
     * Agrega nuevo conocimiento cultural a un avatar
     */
    public AvatarKnowledgeDto addKnowledgeToAvatar(String avatarId, AvatarKnowledgeDto knowledgeData) throws ExecutionException, InterruptedException {
        log.info("📚 Agregando conocimiento al avatar {}: {}", avatarId, knowledgeData.getTitle());
        
        // Validar que el avatar existe
        AvatarDto avatar = avatarService.getAvatarById(avatarId);
        if (avatar == null) {
            throw new IllegalArgumentException("Avatar no encontrado: " + avatarId);
        }
        
        // Establecer el ID del avatar
        knowledgeData.setAvatarId(avatarId);
        
        // Establecer valores por defecto
        setDefaultKnowledgeValues(knowledgeData);
        
        // Guardar conocimiento
        AvatarKnowledgeDto savedKnowledge = knowledgeService.saveKnowledge(knowledgeData);
        
        log.info("✅ Conocimiento agregado exitosamente: {}", savedKnowledge.getKnowledgeId());
        return savedKnowledge;
    }
    
    /**
     * Busca conocimiento relevante para responder a una pregunta
     */
    public List<AvatarKnowledgeDto> findRelevantKnowledge(String avatarId, String query, String culturalContext) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando conocimiento relevante para: {}", query);
        
        List<AvatarKnowledgeDto> relevantKnowledge = new ArrayList<>();
        
        // Obtener todo el conocimiento del avatar
        List<AvatarKnowledgeDto> avatarKnowledge = knowledgeService.getKnowledgeByAvatarId(avatarId);
        
        // Buscar por palabras clave en el query
        String[] queryWords = query.toLowerCase().split("\\s+");
        List<String> keywords = Arrays.asList(queryWords);
        List<AvatarKnowledgeDto> keywordResults = knowledgeService.searchKnowledgeByKeywords(keywords);
        
        // Combinar resultados
        relevantKnowledge.addAll(avatarKnowledge);
        relevantKnowledge.addAll(keywordResults);
        
        // Si hay contexto cultural, filtrar por él
        if (culturalContext != null) {
            relevantKnowledge = relevantKnowledge.stream()
                    .filter(knowledge -> culturalContext.equals(knowledge.getCulturalRegion()) ||
                                       (knowledge.getRelatedCultures() != null && knowledge.getRelatedCultures().contains(culturalContext)))
                    .collect(Collectors.toList());
        }
        
        // Eliminar duplicados y ordenar por relevancia
        List<AvatarKnowledgeDto> uniqueResults = relevantKnowledge.stream()
                .distinct()
                .sorted((a, b) -> Double.compare(
                        b.getRelevanceScore() != null ? b.getRelevanceScore() : 0.0,
                        a.getRelevanceScore() != null ? a.getRelevanceScore() : 0.0
                ))
                .limit(5)
                .collect(Collectors.toList());
        
        log.info("✅ {} elementos de conocimiento relevante encontrados", uniqueResults.size());
        return uniqueResults;
    }
    
    // ===== OPERACIONES DE ANÁLISIS =====
    
    /**
     * Genera reporte de analytics para un avatar
     */
    public Map<String, Object> getAvatarAnalytics(String avatarId, String period) throws ExecutionException, InterruptedException {
        log.info("📊 Generando analytics del avatar {} para período {}", avatarId, period);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Obtener información básica del avatar
        AvatarDto avatar = avatarService.getAvatarById(avatarId);
        if (avatar == null) {
            throw new IllegalArgumentException("Avatar no encontrado: " + avatarId);
        }
        
        analytics.put("avatarInfo", avatar);
        
        // Obtener sesiones del período
        List<AvatarSessionDto> sessions = sessionService.getSessionsByPeriod(period);
        List<AvatarSessionDto> avatarSessions = sessions.stream()
                .filter(session -> avatarId.equals(session.getAvatarId()))
                .collect(Collectors.toList());
        
        analytics.put("totalSessions", avatarSessions.size());
        
        // Calcular estadísticas de sesiones
        int completedSessions = (int) avatarSessions.stream()
                .filter(session -> Boolean.TRUE.equals(session.getSessionCompleted()))
                .count();
        
        double averageDuration = avatarSessions.stream()
                .filter(session -> session.getDurationSeconds() != null)
                .mapToLong(AvatarSessionDto::getDurationSeconds)
                .average()
                .orElse(0.0);
        
        double averageSatisfaction = avatarSessions.stream()
                .filter(session -> session.getUserSatisfactionScore() != null)
                .mapToDouble(AvatarSessionDto::getUserSatisfactionScore)
                .average()
                .orElse(0.0);
        
        analytics.put("completedSessions", completedSessions);
        analytics.put("averageDurationSeconds", averageDuration);
        analytics.put("averageSatisfaction", averageSatisfaction);
        
        // Obtener conversaciones del período
        List<AvatarConversationDto> conversations = conversationService.getConversationAnalytics(avatarId, period);
        analytics.put("totalConversations", conversations.size());
        
        // Analizar temas más populares
        Map<String, Integer> topicFrequency = conversations.stream()
                .filter(conv -> conv.getCulturalTopic() != null)
                .collect(Collectors.groupingBy(
                        AvatarConversationDto::getCulturalTopic,
                        Collectors.summingInt(conv -> 1)
                ));
        
        analytics.put("popularTopics", topicFrequency);
        
        // Obtener estadísticas de conocimiento
        List<AvatarKnowledgeDto> knowledge = knowledgeService.getKnowledgeByAvatarId(avatarId);
        analytics.put("totalKnowledgeItems", knowledge.size());
        
        long verifiedKnowledge = knowledge.stream()
                .filter(k -> "VERIFIED".equals(k.getVerificationStatus()))
                .count();
        
        analytics.put("verifiedKnowledge", verifiedKnowledge);
        
        log.info("✅ Analytics generado para avatar {}", avatarId);
        return analytics;
    }
    
    /**
     * Obtiene resumen general del sistema de avatares
     */
    public Map<String, Object> getSystemSummary() throws ExecutionException, InterruptedException {
        log.info("📈 Generando resumen general del sistema de avatares");
        
        Map<String, Object> summary = new HashMap<>();
        
        // Estadísticas básicas
        List<AvatarDto> activeAvatars = avatarService.getActiveAvatars();
        summary.put("totalActiveAvatars", activeAvatars.size());
        
        List<AvatarSessionDto> activeSessions = sessionService.getActiveSessions();
        summary.put("currentActiveSessions", activeSessions.size());
        
        // Avatares más populares
        List<AvatarDto> popularAvatars = avatarService.getPopularAvatars(5);
        summary.put("topAvatars", popularAvatars);
        
        // Conocimiento más accedido
        List<AvatarKnowledgeDto> popularKnowledge = knowledgeService.getPopularKnowledge(10);
        summary.put("popularKnowledge", popularKnowledge);
        
        // Distribución por regiones
        Map<String, Long> regionDistribution = activeAvatars.stream()
                .filter(avatar -> avatar.getCulturalRegion() != null)
                .collect(Collectors.groupingBy(
                        AvatarDto::getCulturalRegion,
                        Collectors.counting()
                ));
        
        summary.put("avatarsByRegion", regionDistribution);
        
        // Distribución por tipos
        Map<String, Long> typeDistribution = activeAvatars.stream()
                .filter(avatar -> avatar.getType() != null)
                .collect(Collectors.groupingBy(
                        AvatarDto::getType,
                        Collectors.counting()
                ));
        
        summary.put("avatarsByType", typeDistribution);
        
        log.info("✅ Resumen del sistema generado");
        return summary;
    }
    
    // ===== MÉTODOS AUXILIARES PRIVADOS =====
    
    private void validateAvatarData(AvatarDto avatar) {
        if (avatar.getName() == null || avatar.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del avatar es requerido");
        }
        if (avatar.getType() == null) {
            throw new IllegalArgumentException("El tipo del avatar es requerido");
        }
        if (avatar.getCulturalRegion() == null) {
            throw new IllegalArgumentException("La región cultural es requerida");
        }
    }
    
    private void setDefaultAvatarValues(AvatarDto avatar) {
        if (avatar.getPersonality() == null) {
            avatar.setPersonality("FRIENDLY");
        }
        if (avatar.getVoiceType() == null) {
            avatar.setVoiceType("NEUTRAL");
        }
        if (avatar.getIsActive() == null) {
            avatar.setIsActive(true);
        }
        if (avatar.getInteractionLevel() == null) {
            avatar.setInteractionLevel(3);
        }
        if (avatar.getCanAnswerQuestions() == null) {
            avatar.setCanAnswerQuestions(true);
        }
        if (avatar.getCanTellStories() == null) {
            avatar.setCanTellStories(true);
        }
        if (avatar.getCanRecommendContent() == null) {
            avatar.setCanRecommendContent(true);
        }
        if (avatar.getTotalInteractions() == null) {
            avatar.setTotalInteractions(0L);
        }
        if (avatar.getPopularity() == null) {
            avatar.setPopularity(0);
        }
        if (avatar.getRating() == null) {
            avatar.setRating(5.0);
        }
    }
    
    private void setDefaultKnowledgeValues(AvatarKnowledgeDto knowledge) {
        if (knowledge.getDifficultyLevel() == null) {
            knowledge.setDifficultyLevel("INTERMEDIATE");
        }
        if (knowledge.getTargetAudience() == null) {
            knowledge.setTargetAudience("GENERAL_PUBLIC");
        }
        if (knowledge.getOriginalLanguage() == null) {
            knowledge.setOriginalLanguage("es");
        }
        if (knowledge.getRelevanceScore() == null) {
            knowledge.setRelevanceScore(1.0);
        }
        if (knowledge.getTimesAccessed() == null) {
            knowledge.setTimesAccessed(0L);
        }
        if (knowledge.getIsPublic() == null) {
            knowledge.setIsPublic(true);
        }
    }
    
    private void createWelcomeMessage(AvatarSessionDto session) throws ExecutionException, InterruptedException {
        AvatarConversationDto welcomeMessage = new AvatarConversationDto();
        welcomeMessage.setSessionId(session.getSessionId());
        welcomeMessage.setAvatarId(session.getAvatarId());
        welcomeMessage.setUserId(session.getUserId());
        welcomeMessage.setMessageType("AVATAR_RESPONSE");
        welcomeMessage.setResponseType("GREETING");
        welcomeMessage.setContent("¡Hola! Soy tu guía cultural interactivo. Estoy aquí para compartir contigo la riqueza de nuestra herencia cultural. ¿En qué te puedo ayudar hoy?");
        welcomeMessage.setTimestamp(com.google.cloud.Timestamp.now());
        welcomeMessage.setCampusLocation(session.getCampusZone());
        
        conversationService.saveConversation(welcomeMessage);
    }
    
    private AvatarConversationDto generateAvatarResponse(AvatarSessionDto session, String userMessage) throws ExecutionException, InterruptedException {
        // Buscar conocimiento relevante
        List<AvatarKnowledgeDto> relevantKnowledge = findRelevantKnowledge(
                session.getAvatarId(), 
                userMessage, 
                session.getCulturalTheme()
        );
        
        // Generar respuesta basada en el conocimiento
        String response = generateResponseFromKnowledge(relevantKnowledge, userMessage);
        
        AvatarConversationDto avatarResponse = new AvatarConversationDto();
        avatarResponse.setSessionId(session.getSessionId());
        avatarResponse.setAvatarId(session.getAvatarId());
        avatarResponse.setUserId(session.getUserId());
        avatarResponse.setMessageType("AVATAR_RESPONSE");
        avatarResponse.setResponseType(determineResponseType(userMessage));
        avatarResponse.setContent(response);
        avatarResponse.setTimestamp(com.google.cloud.Timestamp.now());
        avatarResponse.setCampusLocation(session.getCampusZone());
        
        return avatarResponse;
    }
    
    private String generateResponseFromKnowledge(List<AvatarKnowledgeDto> knowledge, String userMessage) {
        if (knowledge.isEmpty()) {
            return "Es una pregunta muy interesante. Aunque no tengo información específica sobre ese tema en este momento, te animo a explorar más sobre nuestra rica herencia cultural. ¿Hay algún otro aspecto cultural que te gustaría conocer?";
        }
        
        // Usar el conocimiento más relevante para generar la respuesta
        AvatarKnowledgeDto mostRelevant = knowledge.get(0);
        
        String response = mostRelevant.getSummary() != null ? mostRelevant.getSummary() : mostRelevant.getDescription();
        
        if (response == null || response.trim().isEmpty()) {
            response = "Te puedo contar sobre " + mostRelevant.getTitle() + ". " + mostRelevant.getContent();
        }
        
        // Agregar pregunta de seguimiento si hay temas relacionados
        if (mostRelevant.getFollowUpTopics() != null && !mostRelevant.getFollowUpTopics().isEmpty()) {
            response += "\n\n¿Te gustaría saber más sobre " + mostRelevant.getFollowUpTopics().get(0) + "?";
        }
        
        return response;
    }
    
    private String determineResponseType(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("historia") || message.contains("cuento") || message.contains("leyenda")) {
            return "STORYTELLING";
        } else if (message.contains("recomienda") || message.contains("sugiere")) {
            return "RECOMMENDATION";
        } else if (message.contains("qué") || message.contains("cómo") || message.contains("cuándo") || message.contains("dónde")) {
            return "INFORMATIVE";
        } else if (message.contains("enseña") || message.contains("aprend")) {
            return "EDUCATIONAL";
        } else {
            return "INFORMATIVE";
        }
    }
}
