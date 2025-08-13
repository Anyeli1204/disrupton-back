package com.disrupton.userSession.controller;

import com.disrupton.userSession.dto.UserSessionDto;
import com.disrupton.userSession.dto.UserSessionRequest;
import com.disrupton.userSession.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-sessions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserSessionController {

    private final UserSessionService userSessionService;

    /**
     * Get all user sessions
     */
    @GetMapping
    public ResponseEntity<List<UserSessionDto>> getAllSessions() {
        try {
            log.info("Getting all user sessions");
            List<UserSessionDto> sessions = userSessionService.getAllSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error getting all sessions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user session by ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<UserSessionDto> getSessionById(@PathVariable String sessionId) {
        try {
            log.info("Getting user session by ID: {}", sessionId);
            UserSessionDto session = userSessionService.getSessionById(sessionId);
            if (session != null) {
                return ResponseEntity.ok(session);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting session by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new user session
     */
    @PostMapping
    public ResponseEntity<UserSessionDto> createSession(@RequestBody UserSessionRequest request) {
        try {
            log.info("Creating new user session for user: {}", request.getUserId());
            UserSessionDto createdSession = userSessionService.createSession(request);
            return ResponseEntity.ok(createdSession);
        } catch (Exception e) {
            log.error("Error creating session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update user session
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<UserSessionDto> updateSession(
            @PathVariable String sessionId,
            @RequestBody UserSessionRequest request) {
        try {
            log.info("Updating user session: {}", sessionId);
            UserSessionDto updatedSession = userSessionService.updateSession(sessionId, request);
            if (updatedSession != null) {
                return ResponseEntity.ok(updatedSession);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * End user session
     */
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<UserSessionDto> endSession(@PathVariable String sessionId) {
        try {
            log.info("Ending user session: {}", sessionId);
            UserSessionDto endedSession = userSessionService.endSession(sessionId);
            if (endedSession != null) {
                return ResponseEntity.ok(endedSession);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error ending session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get sessions by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSessionDto>> getSessionsByUserId(@PathVariable String userId) {
        try {
            log.info("Getting sessions for user: {}", userId);
            List<UserSessionDto> sessions = userSessionService.getSessionsByUserId(userId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error getting sessions by user ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active sessions
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserSessionDto>> getActiveSessions() {
        try {
            log.info("Getting active sessions");
            List<UserSessionDto> sessions = userSessionService.getActiveSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error getting active sessions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get session statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getSessionStats() {
        try {
            log.info("Getting session statistics");
            Object stats = userSessionService.getSessionStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting session stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
