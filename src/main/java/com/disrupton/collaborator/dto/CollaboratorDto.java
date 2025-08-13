package com.disrupton.collaborator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CollaboratorDto {

    // Campos básicos de usuario
    private String id;
    private String name;
    private String email;
    private String role; 
    private Timestamp createdAt;

    private String descripcion; 
    private Double calificacion; 
    private List<String> imagenesGaleria; 
    private List<CommentCollabResponseDto> comentariosDestacados; 

    private Map<String, String> redesContacto; 
    private Boolean tieneAcceso; 
    private Double precioAcceso; 

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
                .build();
    }

    public static CollaboratorDto createPremiumView(CollaboratorDto full) {
        return full.toBuilder()
                .email(full.getEmail())
                .tieneAcceso(true)
                .build();
    }
}