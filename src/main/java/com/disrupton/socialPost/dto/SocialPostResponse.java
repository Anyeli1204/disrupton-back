package com.disrupton.socialPost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import com.google.cloud.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialPostResponse {
    
    private String id;
    private String userId;
    private String userName;
    private String userRole;
    private String userProfileImageUrl;
    private String description;
    private List<PostImageDto> images;
    private String location;
    private Double latitude;
    private Double longitude;
    private String department;
    private List<String> tags;
    private List<String> mentionedCulturalObjects;
    private String privacy;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer savesCount;
    private Integer sharesCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Campos específicos para el usuario actual
    private Boolean isLikedByCurrentUser;
    private Boolean isSavedByCurrentUser;
    private Boolean isOwnPost;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostImageDto {
        private String id;
        private String imageUrl;
        private String caption;
        private Integer displayOrder;
        private String originalFileName;
        private Long fileSize;
        private String mimeType;
    }
}
