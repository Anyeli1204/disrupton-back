package com.disrupton.collaborator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorDto {

    // Campos básicos de usuario
    private String id;
    private String name;
    private String email;
    private String role; // "student", "moderator", "admin", "AGENTE_CULTURAL"
    private Timestamp createdAt;

    // Campos específicos para AGENTE_CULTURAL (siempre visibles)
    private String descripcion; // Breve descripción o experiencia
    private Double calificacion; // Rating promedio
    private List<String> imagenesGaleria; // Galería breve de imágenes
    private List<CommentCollabResponseDto> comentariosDestacados; // Los mejores comentarios

    // Información premium (solo visible tras pago)
    private Map<String, String> redesContacto; // WhatsApp, Instagram, Facebook, etc.
    private Boolean tieneAcceso; // Si el usuario actual tiene acceso a las redes
    private Double precioAcceso; // Precio para desbloquear contacto

    // Factory methods para controlar acceso
    public static CollaboratorDto createPublicView(CollaboratorDto full) {
        return CollaboratorDto.builder()
                .id(full.getId())
                .name(full.getName())
                .role(full.getRole())
                .createdAt(full.getCreatedAt())
                .descripcion(full.getDescripcion())
                .calificacion(full.getCalificacion())
                .imagenesGaleria(full.getImagenesGaleria())
                .comentariosDestacados(full.getComentariosDestacados())
                .precioAcceso(full.getPrecioAcceso())
                .tieneAcceso(false)
                // redesContacto se omite intencionalmente
                .build();
    }

    public static CollaboratorDto createPremiumView(CollaboratorDto full) {
        return full.toBuilder()
                .email(full.getEmail())
                .tieneAcceso(true)
                .build();
    }
}