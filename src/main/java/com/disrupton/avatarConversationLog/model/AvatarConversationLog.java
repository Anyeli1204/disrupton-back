package com.disrupton.avatarConversationLog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarConversationLog {
    private String logId;
    private String conversationId;
    private String avatarId;
    private String userId;
    private String userMessage;
    private String geminiResponse;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
