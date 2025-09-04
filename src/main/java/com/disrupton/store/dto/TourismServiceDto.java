package com.disrupton.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para servicios turísticos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourismServiceDto {
    
    private String id;
    
    // Información básica
    private String name;
    private String description;
    private String shortDescription;
    
    // Precio
    private Double pricePerPerson;
    private Double groupPrice;
    private String currency;
    private String formattedPrice;
    
    // Imágenes
    private String mainImageUrl;
    private List<String> additionalImages;
    
    // Ubicación
    private String location;
    private String department;
    private String province;
    private String district;
    private Double latitude;
    private Double longitude;
    private List<String> visitedPlaces;
    
    // Categorización
    private String category;
    private String categoryIcon;
    private String type;
    private List<String> tags;
    private String difficulty;
    private String difficultyIcon;
    
    // Duración y horarios
    private String duration;
    private String schedule;
    private List<String> availableDays;
    private Boolean isFlexibleSchedule;
    
    // Guía
    private String guideId;
    private String guideName;
    private String guideContact;
    private List<String> spokenLanguages;
    
    // Capacidad
    private Integer minGroupSize;
    private Integer maxGroupSize;
    private String groupSizeText;
    private String ageRestriction;
    private String physicalRequirement;
    private List<String> included;
    private List<String> notIncluded;
    
    // Estado
    private Boolean isAvailable;
    private Boolean requiresAdvanceBooking;
    private String advanceBookingTime;
    
    // Métricas
    private Double rating;
    private Integer totalRatings;
    private String formattedRating;
    private Integer viewCount;
    private Integer bookingCount;
    
    // Metadatos
    private String createdAt;
    private String updatedAt;
    private String createdBy;
}
