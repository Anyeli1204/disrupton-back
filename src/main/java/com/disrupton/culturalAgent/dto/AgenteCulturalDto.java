package com.disrupton.culturalAgent.dto;

import com.disrupton.culturalAgent.model.AgenteCultural.AgentType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgenteCulturalDto {
    
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
    private String workPhotoHighlight; // Primera foto destacada
    private Boolean isActive;
    private String department;
    private String province;
    private String district;
    
    // Campos derivados para la UI
    private String typeIcon;
    private String typeLabel;
    private String primaryContact;
    private String fullLocation;
    private String formattedRating;
    
    // Campo específico principal según tipo
    private String mainSpecialty; // Técnica principal para artesano o tipo de turismo para guía
    private List<String> spokenLanguages; // Solo para guías
    private String experienceDisplay; // Años de experiencia formateado
}
