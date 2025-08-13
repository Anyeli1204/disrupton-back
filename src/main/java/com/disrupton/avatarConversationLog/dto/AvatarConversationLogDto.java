package com.disrupton.avatarConversationLog.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarConversationLogDto {
    private String logId;
    private String conversationId;
    private String avatarId;
    private String userId;
    private String userMessage;
    private String geminiResponse;
    private Timestamp timestamp;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
