package com.disrupton.dto;

import lombok.Data;
import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * DTO para Conocimiento Cultural del Avatar
 * Representa la base de conocimientos culturales de un avatar
 */
@Data
public class AvatarKnowledgeDto {
    
    // Identificación
    private String knowledgeId;
    private String avatarId;
    private String category; // HISTORY, TRADITIONS, ART, ARCHITECTURE, MUSIC, CUISINE, LANGUAGE
    
    // Contenido del conocimiento
    private String topic; // Tema específico
    private String title; // Título del conocimiento
    private String description; // Descripción detallada
    private String content; // Contenido completo
    private String summary; // Resumen corto
    
    // Contexto cultural
    private String culturalRegion; // Región cultural
    private String culturalPeriod; // Período histórico
    private String ethnicity; // Grupo étnico relacionado
    private List<String> relatedCultures; // Culturas relacionadas
    
    // Información académica
    private String difficultyLevel; // BASIC, INTERMEDIATE, ADVANCED, EXPERT
    private List<String> keywords; // Palabras clave
    private List<String> relatedTopics; // Temas relacionados
    private String academicSource; // Fuente académica
    private String bibliography; // Bibliografía
    
    // Contenido multimedia
    private List<String> imageUrls; // URLs de imágenes
    private List<String> videoUrls; // URLs de videos
    private List<String> audioUrls; // URLs de audio
    private String threeDModelUrl; // URL de modelo 3D
    private Map<String, String> attachments; // Archivos adjuntos
    
    // Metadatos narrativos
    private String storytellingStyle; // LEGEND, HISTORICAL_FACT, PERSONAL_STORY, MYTH
    private String targetAudience; // CHILDREN, TEENS, ADULTS, SCHOLARS, TOURISTS
    private int estimatedReadingTime; // Tiempo estimado de lectura en minutos
    private String emotionalTone; // INSPIRING, EDUCATIONAL, MYSTERIOUS, JOYFUL, SOLEMN
    
    // Interactividad
    private List<String> relatedQuestions; // Preguntas relacionadas
    private List<String> followUpTopics; // Temas de seguimiento
    private Map<String, String> interactiveElements; // Elementos interactivos
    private boolean hasQuiz; // Si tiene un quiz asociado
    private List<String> practicalActivities; // Actividades prácticas
    
    // Localización e idiomas
    private Map<String, String> translations; // Traducciones a otros idiomas
    private String originalLanguage; // Idioma original
    private List<String> availableLanguages; // Idiomas disponibles
    private Map<String, String> culturalTerms; // Términos culturales específicos
    
    // Validación y calidad
    private String verificationStatus; // VERIFIED, PENDING, REJECTED, NEEDS_REVIEW
    private String verifiedBy; // ID del experto que verificó
    private Timestamp verificationDate; // Fecha de verificación
    private Double accuracyScore; // Puntuación de precisión (1-5)
    private String sourceReliability; // Confiabilidad de la fuente
    
    // Uso y estadísticas
    private long timesAccessed; // Veces que se accedió
    private Double averageRating; // Calificación promedio
    private int totalRatings; // Total de calificaciones
    private long shareCount; // Veces compartido
    private Timestamp lastAccessed; // Último acceso
    
    // Conexiones con objetos culturales
    private List<String> relatedCulturalObjects; // Objetos culturales relacionados
    private List<String> relatedAvatars; // Otros avatares con conocimiento relacionado
    private String primaryCulturalObject; // Objeto cultural principal
    
    // Configuración de IA
    private Map<String, Object> aiTags; // Tags para procesamiento de IA
    private String intentCategory; // Categoría de intención para chatbot
    private List<String> triggerPhrases; // Frases que activan este conocimiento
    private Double relevanceScore; // Puntuación de relevancia
    
    // Metadatos del sistema
    private String createdBy; // Usuario que creó el conocimiento
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String status; // ACTIVE, INACTIVE, ARCHIVED, DRAFT
    private String version; // Versión del contenido
    private boolean isPublic; // Si es público o privado
}
