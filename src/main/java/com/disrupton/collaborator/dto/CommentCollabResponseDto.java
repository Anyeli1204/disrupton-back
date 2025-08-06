package com.disrupton.collaborator.dto;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class CommentCollabResponseDto {
    private String id;
    private String culturalAgentId;
    private String authorUserId;
    private String usuarioNombre;
    private String comentario;
    private Double calificacion;
    private Timestamp fecha;
}
