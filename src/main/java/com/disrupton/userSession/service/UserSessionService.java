package com.disrupton.userSession.service;

import com.disrupton.userSession.dto.UserSessionDto;
import com.disrupton.userSession.dto.UserSessionRequest;
import com.disrupton.userSession.model.UserSession;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "user_sessions";

    /**
     * Get all user sessions
     */
    public List<UserSessionDto> getAllSessions() throws ExecutionException, InterruptedException {
        List<UserSessionDto> sessions = new ArrayList<>();
        
        firestore.collection(COLLECTION_NAME).get().get().getDocuments().forEach(document -> {
            UserSessionDto session = document.toObject(UserSessionDto.class);
            sessions.add(session);
        });
        
        return sessions;
    }

    /**
     * Get user session by ID
     */
    public UserSessionDto getSessionById(String sessionId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(sessionId).get().get();
        
        if (document.exists()) {
            return document.toObject(UserSessionDto.class);
        } else {
            log.warn("User session not found with ID: {}", sessionId);
            return null;
        }
    }

    /**
     * Create new user session
     */
    public UserSessionDto createSession(UserSessionRequest request) throws ExecutionException, InterruptedException {
        UserSessionDto session = new UserSessionDto();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(request.getUserId());
        session.setSessionType(request.getSessionType());
        session.setStatus("active");
        session.setStartTime(Timestamp.now());
        session.setLocation(request.getLocation());
        session.setLatitude(request.getLatitude());
        session.setLongitude(request.getLongitude());
        session.setDeviceInfo(request.getDeviceInfo());
        session.setAppVersion(request.getAppVersion());
        session.setCreatedAt(Timestamp.now());
        session.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(session.getSessionId());
        docRef.set(session).get();
        
        log.info("User session created successfully: {}", session.getSessionId());
        return session;
    }

    /**
     * Update user session
     */
    public UserSessionDto updateSession(String sessionId, UserSessionRequest request) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(sessionId).get().get();
        
        if (!document.exists()) {
            log.warn("User session not found for update: {}", sessionId);
            return null;
        }
        
        UserSessionDto session = document.toObject(UserSessionDto.class);
        session.setSessionType(request.getSessionType());
        session.setLocation(request.getLocation());
        session.setLatitude(request.getLatitude());
        session.setLongitude(request.getLongitude());
        session.setDeviceInfo(request.getDeviceInfo());
        session.setAppVersion(request.getAppVersion());
        session.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(sessionId);
        docRef.set(session).get();
        
        log.info("User session updated successfully: {}", sessionId);
        return session;
    }

    /**
     * End user session
     */
    public UserSessionDto endSession(String sessionId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(sessionId).get().get();
        
        if (!document.exists()) {
            log.warn("User session not found for ending: {}", sessionId);
            return null;
        }
        
        UserSessionDto session = document.toObject(UserSessionDto.class);
        session.setStatus("ended");
        session.setEndTime(Timestamp.now());
        
        // Calculate duration in milliseconds
        if (session.getStartTime() != null && session.getEndTime() != null) {
            long duration = session.getEndTime().toDate().getTime() - session.getStartTime().toDate().getTime();
            session.setDuration(duration);
        }
        
        session.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(sessionId);
        docRef.set(session).get();
        
        log.info("User session ended successfully: {}", sessionId);
        return session;
    }

    /**
     * Get sessions by user ID
     */
    public List<UserSessionDto> getSessionsByUserId(String userId) throws ExecutionException, InterruptedException {
        List<UserSessionDto> sessions = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            UserSessionDto session = document.toObject(UserSessionDto.class);
            sessions.add(session);
        });
        
        return sessions;
    }

    /**
     * Get active sessions
     */
    public List<UserSessionDto> getActiveSessions() throws ExecutionException, InterruptedException {
        List<UserSessionDto> sessions = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", "active")
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            UserSessionDto session = document.toObject(UserSessionDto.class);
            sessions.add(session);
        });
        
        return sessions;
    }

    /**
     * Get session statistics
     */
    public Map<String, Object> getSessionStats() throws ExecutionException, InterruptedException {
        Map<String, Object> stats = new HashMap<>();
        
        QuerySnapshot allSessions = firestore.collection(COLLECTION_NAME).get().get();
        QuerySnapshot activeSessions = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", "active")
                .get().get();
        
        stats.put("totalSessions", allSessions.size());
        stats.put("activeSessions", activeSessions.size());
        stats.put("endedSessions", allSessions.size() - activeSessions.size());
        
        // Calculate average session duration
        long totalDuration = 0;
        int sessionsWithDuration = 0;
        
        for (QueryDocumentSnapshot document : allSessions) {
            UserSessionDto session = document.toObject(UserSessionDto.class);
            if (session.getDuration() != null && session.getDuration() > 0) {
                totalDuration += session.getDuration();
                sessionsWithDuration++;
            }
        }
        
        if (sessionsWithDuration > 0) {
            stats.put("averageSessionDuration", totalDuration / sessionsWithDuration);
        } else {
            stats.put("averageSessionDuration", 0);
        }
        
        return stats;
    }
}
