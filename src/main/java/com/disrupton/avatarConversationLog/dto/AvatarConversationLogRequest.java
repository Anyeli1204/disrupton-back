package com.disrupton.avatarConversationLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarConversationLogRequest {
    private String conversationId;
    private String avatarId;
    private String userId;
    private String userMessage;
    private String geminiResponse;
}
