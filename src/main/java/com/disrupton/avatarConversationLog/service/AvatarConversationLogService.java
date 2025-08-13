package com.disrupton.avatarConversationLog.service;

import com.disrupton.avatarConversationLog.dto.AvatarConversationLogDto;
import com.disrupton.avatarConversationLog.dto.AvatarConversationLogRequest;
import com.disrupton.avatarConversationLog.model.AvatarConversationLog;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarConversationLogService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "avatar_conversation_logs";

    /**
     * Get all avatar conversation logs
     */
    public List<AvatarConversationLogDto> getAllLogs() throws ExecutionException, InterruptedException {
        List<AvatarConversationLogDto> logs = new ArrayList<>();
        
        firestore.collection(COLLECTION_NAME).get().get().getDocuments().forEach(document -> {
            AvatarConversationLogDto logEntry = document.toObject(AvatarConversationLogDto.class);
            logs.add(logEntry);
        });
        
        return logs;
    }

    /**
     * Get avatar conversation log by ID
     */
    public AvatarConversationLogDto getLogById(String logId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(logId).get().get();
        
        if (document.exists()) {
            return document.toObject(AvatarConversationLogDto.class);
        } else {
            log.warn("Avatar conversation log not found with ID: {}", logId);
            return null;
        }
    }

    /**
     * Create new avatar conversation log
     */
    public AvatarConversationLogDto createLog(AvatarConversationLogRequest request) throws ExecutionException, InterruptedException {
        AvatarConversationLogDto logEntry = new AvatarConversationLogDto();
        logEntry.setLogId(UUID.randomUUID().toString());
        logEntry.setConversationId(request.getConversationId());
        logEntry.setAvatarId(request.getAvatarId());
        logEntry.setUserId(request.getUserId());
        logEntry.setUserMessage(request.getUserMessage());
        logEntry.setGeminiResponse(request.getGeminiResponse());
        logEntry.setTimestamp(Timestamp.now());
        logEntry.setCreatedAt(Timestamp.now());
        logEntry.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(logEntry.getLogId());
        docRef.set(logEntry).get();
        
        log.info("Avatar conversation log created successfully: {}", logEntry.getLogId());
        return logEntry;
    }

    /**
     * Update avatar conversation log
     */
    public AvatarConversationLogDto updateLog(String logId, AvatarConversationLogRequest request) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(logId).get().get();
        
        if (!document.exists()) {
            log.warn("Avatar conversation log not found for update: {}", logId);
            return null;
        }
        
        AvatarConversationLogDto logEntry = document.toObject(AvatarConversationLogDto.class);
        logEntry.setConversationId(request.getConversationId());
        logEntry.setAvatarId(request.getAvatarId());
        logEntry.setUserId(request.getUserId());
        logEntry.setUserMessage(request.getUserMessage());
        logEntry.setGeminiResponse(request.getGeminiResponse());
        logEntry.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(logId);
        docRef.set(logEntry).get();
        
        log.info("Avatar conversation log updated successfully: {}", logId);
        return logEntry;
    }

    /**
     * Delete avatar conversation log
     */
    public boolean deleteLog(String logId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(logId);
        DocumentSnapshot document = docRef.get().get();
        
        if (document.exists()) {
            docRef.delete().get();
            log.info("Avatar conversation log deleted successfully: {}", logId);
            return true;
        } else {
            log.warn("Avatar conversation log not found for deletion: {}", logId);
            return false;
        }
    }

    /**
     * Get logs by user ID
     */
    public List<AvatarConversationLogDto> getLogsByUserId(String userId) throws ExecutionException, InterruptedException {
        List<AvatarConversationLogDto> logs = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            AvatarConversationLogDto logEntry = document.toObject(AvatarConversationLogDto.class);
            logs.add(logEntry);
        });
        
        return logs;
    }

    /**
     * Get logs by avatar ID
     */
    public List<AvatarConversationLogDto> getLogsByAvatarId(String avatarId) throws ExecutionException, InterruptedException {
        List<AvatarConversationLogDto> logs = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("avatarId", avatarId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            AvatarConversationLogDto logEntry = document.toObject(AvatarConversationLogDto.class);
            logs.add(logEntry);
        });
        
        return logs;
    }

    /**
     * Get log by conversation ID
     */
    public AvatarConversationLogDto getLogByConversationId(String conversationId) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("conversationId", conversationId)
                .get().get();
        
        if (!snapshot.isEmpty()) {
            return snapshot.getDocuments().get(0).toObject(AvatarConversationLogDto.class);
        } else {
            log.warn("Avatar conversation log not found with conversation ID: {}", conversationId);
            return null;
        }
    }

    /**
     * Get conversation statistics
     */
    public Map<String, Object> getConversationStats() throws ExecutionException, InterruptedException {
        Map<String, Object> stats = new HashMap<>();
        
        QuerySnapshot allLogs = firestore.collection(COLLECTION_NAME).get().get();
        
        stats.put("totalConversations", allLogs.size());
        
        // Count by avatar
        Map<String, Integer> avatarCounts = new HashMap<>();
        for (QueryDocumentSnapshot document : allLogs) {
            AvatarConversationLogDto logEntry = document.toObject(AvatarConversationLogDto.class);
            String avatarId = logEntry.getAvatarId();
            avatarCounts.put(avatarId, avatarCounts.getOrDefault(avatarId, 0) + 1);
        }
        stats.put("conversationsByAvatar", avatarCounts);
        
        // Count by user
        Map<String, Integer> userCounts = new HashMap<>();
        for (QueryDocumentSnapshot document : allLogs) {
            AvatarConversationLogDto logEntry = document.toObject(AvatarConversationLogDto.class);
            String userId = logEntry.getUserId();
            userCounts.put(userId, userCounts.getOrDefault(userId, 0) + 1);
        }
        stats.put("conversationsByUser", userCounts);
        
        return stats;
    }
}
