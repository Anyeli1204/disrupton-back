package com.disrupton.dto;

import lombok.Data;
import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * DTO para Avatar Cultural Interactivo
 * Representa un avatar virtual que puede interactuar con usuarios
 * y proporcionar información sobre objetos culturales
 */
@Data
public class AvatarDto {
    
    // Identificación del avatar
    private String avatarId;
    private String name;
    private String description;
    private String type; // CULTURAL_GUIDE, HISTORICAL_FIGURE, MYTHOLOGICAL_CHARACTER, CONTEMPORARY_EXPERT
    
    // Información cultural
    private String culturalRegion; // Región cultural que representa (Andes, Costa, Selva, etc.)
    private String culturalPeriod; // Período histórico (Inca, Colonial, Republicano, Contemporáneo)
    private String expertise; // Área de especialización (Textiles, Cerámica, Arquitectura, etc.)
    private List<String> languages; // Idiomas que habla (Español, Quechua, Aymara, etc.)
    
    // Personalidad y características
    private String personality; // FRIENDLY, WISE, ENTHUSIASTIC, SCHOLARLY, MYSTICAL
    private String voiceType; // MALE, FEMALE, NEUTRAL
    private String ageGroup; // YOUNG, ADULT, ELDER
    private String communication_style; // FORMAL, CASUAL, STORYTELLING, ACADEMIC
    
    // Apariencia visual
    private String avatarImageUrl; // URL de la imagen del avatar
    private String avatar3DModelUrl; // URL del modelo 3D del avatar
    private String traditionaldress; // Vestimenta tradicional
    private Map<String, String> visualAttributes; // Atributos visuales adicionales
    
    // Conocimiento y contenido
    private List<String> knowledgeAreas; // Áreas de conocimiento cultural
    private Map<String, String> culturalStories; // Historias culturales que puede contar
    private List<String> interactiveQuestions; // Preguntas que puede hacer al usuario
    private Map<String, String> responses; // Respuestas predefinidas
    
    // Interacción
    private boolean isActive; // Si el avatar está activo
    private String currentMood; // Estado de ánimo actual
    private int interactionLevel; // Nivel de interacción (1-5)
    private List<String> availableActions; // Acciones disponibles
    
    // Contexto geográfico
    private String campusZone; // Zona del campus donde está presente
    private Double latitude; // Ubicación geográfica
    private Double longitude;
    private String associatedObjects; // IDs de objetos culturales asociados
    
    // Configuración de IA
    private String aiModelType; // Tipo de modelo de IA utilizado
    private Map<String, Object> aiParameters; // Parámetros de configuración de IA
    private boolean learningEnabled; // Si puede aprender de las interacciones
    
    // Metadatos
    private String createdBy; // Usuario que creó el avatar
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String status; // ACTIVE, INACTIVE, MAINTENANCE, TRAINING
    private int popularity; // Popularidad basada en interacciones
    private Double rating; // Calificación promedio
    
    // Estadísticas de uso
    private long totalInteractions; // Total de interacciones
    private long todayInteractions; // Interacciones de hoy
    private Map<String, Integer> topicInteractions; // Interacciones por tema
    private List<String> frequentUsers; // Usuarios frecuentes
    
    // Contenido multimedia
    private List<String> audioClips; // Clips de audio del avatar
    private List<String> videoIntroductions; // Videos de presentación
    private Map<String, String> gestureAnimations; // Animaciones de gestos
    
    // Capacidades conversacionales
    private boolean canAnswerQuestions; // Puede responder preguntas
    private boolean canTellStories; // Puede contar historias
    private boolean canProvideDirections; // Puede dar direcciones
    private boolean canRecommendContent; // Puede recomendar contenido
    private boolean canTranslate; // Puede traducir idiomas
    
    // Configuración de horarios
    private Map<String, String> availableHours; // Horarios de disponibilidad
    private String timezone; // Zona horaria
    private List<String> specialEvents; // Eventos especiales donde aparece
}
