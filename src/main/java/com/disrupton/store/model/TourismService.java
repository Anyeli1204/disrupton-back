package com.disrupton.store.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo para servicios turísticos en la tienda cultural
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourismService {
    
    @DocumentId
    private String id;
    
    // Información básica
    private String name;
    private String description;
    private String shortDescription; // Para la tarjeta
    
    // Precios
    private Double pricePerPerson;
    private Double groupPrice; // Precio por grupo completo
    private String currency;
    private String formattedPrice; // "$150 PEN por persona"
    
    // Imágenes
    private String mainImageUrl;
    private List<String> additionalImages;
    
    // Ubicación
    private String location; // "Cusco, Perú"
    private String department;
    private String province;
    private String district;
    private Double latitude;
    private Double longitude;
    private List<String> visitedPlaces; // ["Machu Picchu", "Sacsayhuamán"]
    
    // Categorización
    private ServiceCategory category;
    private ServiceType type;
    private List<String> tags; // ["historia", "arqueología", "cultura"]
    private DifficultyLevel difficulty;
    
    // Duración y horarios
    private String duration; // "Medio día (4 horas)"
    private String schedule; // "9:00 AM - 1:00 PM"
    private List<String> availableDays; // ["Lunes", "Martes", "Miércoles"]
    private Boolean isFlexibleSchedule;
    
    // Guía turístico
    private String guideId; // Referencia al agente cultural
    private String guideName;
    private String guideContact;
    private List<String> spokenLanguages;
    
    // Capacidad y requisitos
    private Integer minGroupSize;
    private Integer maxGroupSize;
    private String ageRestriction; // "Apto para todas las edades"
    private String physicalRequirement; // "Caminata moderada"
    private List<String> included; // ["Transporte", "Entrada", "Refrigerio"]
    private List<String> notIncluded; // ["Almuerzo", "Propinas"]
    
    // Estado
    private Boolean isAvailable;
    private Boolean requiresAdvanceBooking;
    private String advanceBookingTime; // "24 horas"
    
    // Métricas
    private Double rating;
    private Integer totalRatings;
    private Integer viewCount;
    private Integer bookingCount;
    
    // Metadatos
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    
    // Enums
    public enum ServiceCategory {
        CULTURAL("Tour Cultural"),
        HISTORICO("Tour Histórico"),
        ARQUEOLOGICO("Tour Arqueológico"),
        GASTRONOMICO("Tour Gastronómico"),
        AVENTURA("Tour de Aventura"),
        NATURALEZA("Tour de Naturaleza"),
        RELIGIOSO("Tour Religioso"),
        ARTISTICO("Tour Artístico"),
        FOTOGRAFICO("Tour Fotográfico"),
        PERSONALIZADO("Tour Personalizado");
        
        private final String displayName;
        
        ServiceCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ServiceType {
        PRIVADO("Tour Privado"),
        GRUPAL("Tour Grupal"),
        MIXTO("Tour Mixto"),
        VIRTUAL("Tour Virtual");
        
        private final String displayName;
        
        ServiceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum DifficultyLevel {
        FACIL("Fácil"),
        MODERADO("Moderado"),
        DIFICIL("Difícil"),
        EXTREMO("Extremo");
        
        private final String displayName;
        
        DifficultyLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Métodos de conveniencia
    public String getFormattedRating() {
        if (rating == null || totalRatings == null || totalRatings == 0) {
            return "Sin calificaciones";
        }
        return String.format("⭐ %.1f (%d)", rating, totalRatings);
    }
    
    public String getCategoryIcon() {
        switch (category) {
            case CULTURAL: return "🏛️";
            case HISTORICO: return "📚";
            case ARQUEOLOGICO: return "🏺";
            case GASTRONOMICO: return "🍽️";
            case AVENTURA: return "🏔️";
            case NATURALEZA: return "🌿";
            case RELIGIOSO: return "⛪";
            case ARTISTICO: return "🎨";
            case FOTOGRAFICO: return "📸";
            case PERSONALIZADO: return "✨";
            default: return "🗺️";
        }
    }
    
    public String getDifficultyIcon() {
        switch (difficulty) {
            case FACIL: return "🟢";
            case MODERADO: return "🟡";
            case DIFICIL: return "🟠";
            case EXTREMO: return "🔴";
            default: return "⚪";
        }
    }
    
    public String getGroupSizeText() {
        if (minGroupSize != null && maxGroupSize != null) {
            if (minGroupSize.equals(maxGroupSize)) {
                return minGroupSize + " personas";
            }
            return minGroupSize + "-" + maxGroupSize + " personas";
        }
        return "Consultar disponibilidad";
    }
}
