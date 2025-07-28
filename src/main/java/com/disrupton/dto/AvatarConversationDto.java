package com.disrupton.dto;

import lombok.Data;
import com.google.cloud.Timestamp;

/**
 * DTO para Conversaciones con Avatar
 * Representa una conversación entre un usuario y un avatar cultural
 */
@Data
public class AvatarConversationDto {
    
    // Identificación
    private String conversationId;
    private String avatarId;
    private String userId;
    private String sessionId; // ID de la sesión de conversación
    
    // Contenido de la conversación
    private String messageType; // USER_MESSAGE, AVATAR_RESPONSE, SYSTEM_MESSAGE
    private String content; // Contenido del mensaje
    private String language; // Idioma del mensaje
    private String translatedContent; // Contenido traducido si aplica
    
    // Contexto cultural
    private String culturalTopic; // Tema cultural discutido
    private String culturalObjectId; // ID del objeto cultural relacionado
    private String culturalRegion; // Región cultural del contexto
    
    // Metadatos de interacción
    private Timestamp timestamp;
    private String messageSource; // TEXT, VOICE, GESTURE
    private String responseType; // INFORMATIVE, STORYTELLING, QUESTION, RECOMMENDATION
    private String sentimentScore; // Análisis de sentimiento del mensaje
    
    // Audio y multimedia
    private String audioUrl; // URL del audio si es mensaje de voz
    private String imageUrl; // URL de imagen si aplica
    private String videoUrl; // URL de video si aplica
    private String attachmentType; // Tipo de archivo adjunto
    
    // Contexto geográfico
    private String campusLocation; // Ubicación en el campus
    private Double latitude;
    private Double longitude;
    
    // Estado de la conversación
    private boolean isRead; // Si el mensaje fue leído
    private boolean isArchived; // Si está archivado
    private String conversationStatus; // ACTIVE, PAUSED, ENDED
    private int messageOrder; // Orden del mensaje en la conversación
    
    // Calidad de interacción
    private Double userSatisfaction; // Satisfacción del usuario (1-5)
    private boolean wasHelpful; // Si fue útil
    private String feedback; // Comentarios del usuario
    
    // Análisis de IA
    private String intentDetected; // Intención detectada por IA
    private Double confidenceScore; // Nivel de confianza de la respuesta
    private String entityExtracted; // Entidades extraídas del mensaje
    private String nextSuggestedAction; // Siguiente acción sugerida
}
