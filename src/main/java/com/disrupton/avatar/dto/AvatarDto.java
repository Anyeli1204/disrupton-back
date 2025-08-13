package com.disrupton.avatar.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarDto {
    private String avatarId;
    private String type;
    private String displayName;
    private String avatar3DModelUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
