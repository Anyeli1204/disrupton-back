package com.disrupton.dto;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ARPhotoDto {
    private String userId; // reference a users
    private String objectId; // reference a cultural_objects
    private String photoUrl; // link Firebase Storage
    private GeoPoint location; // geopoint (si usas ubicación)
    private Timestamp createdAt;
} 