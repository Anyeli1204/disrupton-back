package com.disrupton.culturalAgent.model;

import com.google.cloud.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgenteCultural {
    
    private String id;
    private String name;
    private String imageUrl;
    private String region;
    private String description;
    private String expertise;
    private List<String> specialties;
    private AgentType type;
    private String phone;
    private String whatsapp;
    private String email;
    private Double rating;
    private Integer totalRatings;
    private String location;
    private Double latitude;
    private Double longitude;
    private List<String> workPhotos;
    private Boolean isActive;
    private String department;
    private String province;
    private String district;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Campos específicos para artesanos
    private String craftTechnique; // Técnica artesanal
    private String mainMaterials;  // Materiales principales
    private Integer experienceYears; // Años de experiencia
    
    // Campos específicos para guías turísticos  
    private List<String> spokenLanguages; // Idiomas hablados
    private String guideLicense; // Licencia de guía
    private List<String> touristZones; // Zonas turísticas que maneja
    private String tourismType; // Tipo de turismo (Cultural, Aventura, etc.)
    
    public enum AgentType {
        ARTISAN,
        TOURIST_GUIDE
    }
    
    // Métodos de conveniencia
    public String getFullName() {
        return name != null ? name : "";
    }
    
    public String getPrimaryContact() {
        return whatsapp != null && !whatsapp.isEmpty() ? whatsapp : phone;
    }
    
    public String getFullLocation() {
        if (location != null && !location.isEmpty()) {
            return location;
        }
        return district + ", " + province + ", " + department;
    }
    
    public String getTypeIcon() {
        return type == AgentType.ARTISAN ? "🎨" : "🗺️";
    }
    
    public String getTypeLabel() {
        return type == AgentType.ARTISAN ? "Artesano" : "Guía Turístico";
    }
}
