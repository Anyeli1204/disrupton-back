package com.disrupton.dto;

import lombok.Data;
import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * DTO para Sesiones de Interacción con Avatar
 * Representa una sesión completa de interacción entre usuario y avatar
 */
@Data
public class AvatarSessionDto {
    
    // Identificación de sesión
    private String sessionId;
    private String avatarId;
    private String userId;
    private String deviceType; // MOBILE, TABLET, WEB, AR_GLASSES, VR_HEADSET
    
    // Información temporal
    private Timestamp startTime;
    private Timestamp endTime;
    private long durationSeconds; // Duración de la sesión en segundos
    
    // Contexto de la sesión
    private String sessionType; // CULTURAL_TOUR, Q_A_SESSION, STORYTELLING, OBJECT_EXPLORATION
    private String campusZone; // Zona del campus donde ocurrió
    private String culturalTheme; // Tema cultural principal
    private List<String> culturalObjectsExplored; // Objetos culturales explorados
    
    // Ubicación geográfica
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private List<Map<String, Double>> pathCoordinates; // Ruta seguida durante la sesión
    
    // Estadísticas de interacción
    private int totalMessages; // Total de mensajes intercambiados
    private int userQuestions; // Preguntas del usuario
    private int avatarResponses; // Respuestas del avatar
    private int storiesTold; // Historias contadas
    private int recommendationsMade; // Recomendaciones hechas
    
    // Contenido multimedia usado
    private int imagesShown; // Imágenes mostradas
    private int videosPlayed; // Videos reproducidos
    private int audioClipsPlayed; // Clips de audio reproducidos
    private int model3DViewed; // Modelos 3D visualizados
    
    // Idiomas utilizados
    private List<String> languagesUsed; // Idiomas utilizados en la sesión
    private int translationsPerformed; // Traducciones realizadas
    private String primaryLanguage; // Idioma principal
    
    // Evaluación de la sesión
    private Double userSatisfactionScore; // Satisfacción del usuario (1-5)
    private String sessionRating; // EXCELLENT, GOOD, AVERAGE, POOR
    private String userFeedback; // Comentarios del usuario
    private boolean sessionCompleted; // Si la sesión se completó normalmente
    
    // Aprendizaje y objetivos
    private List<String> learningObjectivesAchieved; // Objetivos de aprendizaje logrados
    private List<String> culturalConceptsLearned; // Conceptos culturales aprendidos
    private String knowledgeLevelBefore; // Nivel de conocimiento antes (1-5)
    private String knowledgeLevelAfter; // Nivel de conocimiento después (1-5)
    
    // Interacciones sociales
    private boolean sharedContent; // Si compartió contenido
    private int likesGiven; // Likes dados durante la sesión
    private int commentsPosted; // Comentarios publicados
    private List<String> friendsInvited; // Amigos invitados a la experiencia
    
    // Configuración técnica
    private String appVersion; // Versión de la aplicación
    private String deviceModel; // Modelo del dispositivo
    private String operatingSystem; // Sistema operativo
    private boolean arModeUsed; // Si se usó modo AR
    private boolean vrModeUsed; // Si se usó modo VR
    
    // Problemas técnicos
    private List<String> errorsEncountered; // Errores encontrados
    private boolean connectionIssues; // Problemas de conexión
    private boolean audioIssues; // Problemas de audio
    private boolean visualIssues; // Problemas visuales
    
    // Datos de comportamiento
    private Map<String, Integer> topicTimeSpent; // Tiempo dedicado por tema
    private String mostEngagingTopic; // Tema más atractivo
    private String preferredInteractionStyle; // Estilo de interacción preferido
    private List<String> questionsAsked; // Preguntas específicas realizadas
    
    // Metadatos
    private String sessionStatus; // ACTIVE, COMPLETED, INTERRUPTED, ERROR
    private Timestamp lastActivityTime; // Última actividad
    private String endReason; // Razón del fin de sesión
    private boolean dataProcessed; // Si los datos fueron procesados para analytics
}
