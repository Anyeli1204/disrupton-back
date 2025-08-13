package com.disrupton.collaborator.dto;

import lombok.Data;

@Data
public class CommentCollabRequestDto {
    private String userId;
    private String comentario;
    private Double calificacion;
}