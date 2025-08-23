package com.disrupton.event.dto;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventResponseDto {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String dateTime; // Formatted date time
    private String location;
    private Double latitude;
    private Double longitude;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private boolean isActive;
    private List<String> tags;
    private int attendeesCount;
    
    // Información adicional para el frontend
    private boolean isPastEvent; // Si el evento ya pasó
    private String timeUntilEvent; // Tiempo restante hasta el evento
}
