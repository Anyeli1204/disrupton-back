package com.disrupton.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para productos artesanales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    private String id;
    
    // Información básica
    private String name;
    private String description;
    private String shortDescription;
    
    // Precio
    private Double price;
    private String currency;
    private String formattedPrice;
    
    // Imágenes
    private String mainImageUrl;
    private List<String> additionalImages;
    
    // Ubicación
    private String origin;
    private String department;
    private String province;
    private String district;
    private Double latitude;
    private Double longitude;
    
    // Categorización
    private String category;
    private String categoryIcon;
    private String type;
    private List<String> tags;
    private List<String> materials;
    
    // Artesano
    private String artisanId;
    private String artisanName;
    private String artisanContact;
    
    // Estado
    private Boolean isAvailable;
    private Integer stockQuantity;
    private String availabilityStatus;
    private Boolean isHandmade;
    private String craftingTime;
    
    // Métricas
    private Double rating;
    private Integer totalRatings;
    private String formattedRating;
    private Integer viewCount;
    private Integer purchaseCount;
    
    // Metadatos
    private String createdAt;
    private String updatedAt;
    private String createdBy;
}
