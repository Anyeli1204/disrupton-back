package com.disrupton.comment.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String id;
    private String UserName;

    //@PropertyName("text")
    private String text;
    //@PropertyName("createdAt")
    private Timestamp createdAt;

    //@PropertyName("culturalObjectId")
    private String culturalObjectId; //en caso que sea un comentario de un objeto cultural

    //@PropertyName("userId")
    private String userId;

    //@PropertyName("parentCommentId")
    private String parentCommentId;

    //@PropertyName("preguntaId")
    private String preguntaId; //en caso que sea un comentario de una pregunta del mural

    //@PropertyName("imageUrls")
    private List<String> imageUrls; //URLs de las imágenes adjuntas al comentario

    // Campos para conteo de reacciones
    private Integer likeCount = 0;
    private Integer dislikeCount = 0;

    // Indica si el usuario actual ha reaccionado
    private String userReaction; // "like", "dislike", o null
}