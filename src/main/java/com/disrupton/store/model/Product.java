package com.disrupton.store.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo para productos artesanales en la tienda cultural
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @DocumentId
    private String id;
    
    // Información básica
    private String name;
    private String description;
    private String shortDescription; // Para la tarjeta
    
    // Precios
    private Double price;
    private String currency; // "PEN", "USD", etc.
    private String formattedPrice; // "$350 PEN"
    
    // Imágenes
    private String mainImageUrl;
    private List<String> additionalImages;
    
    // Ubicación y origen
    private String origin; // "Cusco, Perú"
    private String department;
    private String province;
    private String district;
    private Double latitude;
    private Double longitude;
    
    // Categorización
    private ProductCategory category;
    private ProductType type;
    private List<String> tags; // ["cerámica", "tradicional", "inca"]
    private List<String> materials; // ["arcilla", "pigmentos naturales"]
    
    // Artesano
    private String artisanId; // Referencia al agente cultural
    private String artisanName;
    private String artisanContact;
    
    // Estado y disponibilidad
    private Boolean isAvailable;
    private Integer stockQuantity;
    private Boolean isHandmade;
    private String craftingTime; // "2-3 semanas"
    
    // Métricas
    private Double rating;
    private Integer totalRatings;
    private Integer viewCount;
    private Integer purchaseCount;
    
    // Metadatos
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    
    // Enums
    public enum ProductCategory {
        CERAMICA("Cerámica"),
        TEXTILES("Textiles"),
        ORFEBRERIA("Orfebrería"),
        TALLADO("Tallado en Madera"),
        PINTURA("Pintura"),
        ESCULTURA("Escultura"),
        DECORACION("Decoración"),
        UTILITARIO("Utilitario"),
        JOYERIA("Joyería"),
        OTROS("Otros");
        
        private final String displayName;
        
        ProductCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ProductType {
        UNICO("Pieza Única"),
        LIMITADO("Edición Limitada"),
        PERSONALIZABLE("Personalizable"),
        SERIE("En Serie");
        
        private final String displayName;
        
        ProductType(String displayName) {
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
            case CERAMICA: return "🏺";
            case TEXTILES: return "🧵";
            case ORFEBRERIA: return "💍";
            case TALLADO: return "🪵";
            case PINTURA: return "🎨";
            case ESCULTURA: return "🗿";
            case DECORACION: return "🎭";
            case UTILITARIO: return "🔧";
            case JOYERIA: return "💎";
            default: return "🎨";
        }
    }
    
    public String getAvailabilityStatus() {
        if (!isAvailable) return "No disponible";
        if (stockQuantity == null || stockQuantity <= 0) return "Agotado";
        if (stockQuantity <= 3) return "Últimas unidades";
        return "Disponible";
    }
}
