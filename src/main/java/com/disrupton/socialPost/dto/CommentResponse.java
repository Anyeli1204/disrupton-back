package com.disrupton.socialPost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    
    private String id;
    private String postId;
    private String userId;
    private String userName;
    private String userRole;
    private String userProfileImageUrl;
    private String content;
    private String parentCommentId;
    private Integer likesCount;
    private Integer repliesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Campo específico para el usuario actual
    private Boolean isOwnComment;
}
