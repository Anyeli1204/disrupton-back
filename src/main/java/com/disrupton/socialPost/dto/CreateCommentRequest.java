package com.disrupton.socialPost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {
    
    @NotBlank(message = "El contenido del comentario es requerido")
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String content;
    
    private String parentCommentId; // Para respuestas a comentarios
}
