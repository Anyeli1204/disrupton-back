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
    
    // Campos adicionales para el mural
    private String userName;
    private String userReaction; // 'like', 'dislike', null
    private Integer likeCount;
    private Integer dislikeCount;
    private List<String> imageUrls;
} 