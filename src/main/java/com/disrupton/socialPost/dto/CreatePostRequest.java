package com.disrupton.socialPost.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {
    
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String description;
    
    private String location;
    
    private Double latitude;
    
    private Double longitude;
    
    private String department;
    
    private List<String> tags;
    
    private List<String> mentionedCulturalObjects;
    
    @Builder.Default
    private PostPrivacy privacy = PostPrivacy.PUBLIC;
    
    private List<String> imageUrls; // URLs de las imágenes ya subidas
    
    public enum PostPrivacy {
        PUBLIC,
        FRIENDS,
        PRIVATE
    }
}
