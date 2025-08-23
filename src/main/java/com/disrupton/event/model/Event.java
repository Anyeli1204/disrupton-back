package com.disrupton.event.model;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Event {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private Timestamp dateTime;
    private String location;
    private Double latitude;  // Coordenadas opcionales
    private Double longitude; // Coordenadas opcionales
    private String createdBy; // ID del admin que lo creó
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isActive;
    private List<String> tags; // Tags opcionales para categorizar
    private int attendeesCount; // Contador de interesados
}
