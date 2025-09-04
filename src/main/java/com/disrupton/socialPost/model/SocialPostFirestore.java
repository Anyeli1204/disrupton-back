package com.disrupton.socialPost.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import com.google.cloud.Timestamp;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialPostFirestore {
    
    private String id;
    private String userId;
    private String userName;
    private String userRole;
    private String userProfileImageUrl;
    private String description;
    private List<Map<String, Object>> images;
    private String location;
    private Double latitude;
    private Double longitude;
    private String department;
    private List<String> tags;
    private List<String> mentionedCulturalObjects;
    private String privacy; // PUBLIC, FRIENDS, PRIVATE
    
    @Builder.Default
    private Integer likesCount = 0;
    
    @Builder.Default
    private Integer commentsCount = 0;
    
    @Builder.Default
    private Integer savesCount = 0;
    
    @Builder.Default
    private Integer sharesCount = 0;
    
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    public enum PostPrivacy {
        PUBLIC,
        FRIENDS,
        PRIVATE
    }
}
