package com.disrupton.reaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDto {
    private String id; // ID del documento en Firestore
    private String objectId; // reference a cultural_objects
    private String userId; // reference a users
    private String type; // like, dislike, star
} 