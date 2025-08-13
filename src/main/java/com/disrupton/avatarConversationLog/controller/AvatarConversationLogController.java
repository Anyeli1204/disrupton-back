package com.disrupton.avatarConversationLog.controller;

import com.disrupton.avatarConversationLog.dto.AvatarConversationLogDto;
import com.disrupton.avatarConversationLog.dto.AvatarConversationLogRequest;
import com.disrupton.avatarConversationLog.service.AvatarConversationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avatar-conversation-logs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AvatarConversationLogController {

    private final AvatarConversationLogService avatarConversationLogService;

    /**
     * Get all avatar conversation logs
     */
    @GetMapping
    public ResponseEntity<List<AvatarConversationLogDto>> getAllLogs() {
        try {
            log.info("Getting all avatar conversation logs");
            List<AvatarConversationLogDto> logs = avatarConversationLogService.getAllLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error getting all logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get avatar conversation log by ID
     */
    @GetMapping("/{logId}")
    public ResponseEntity<AvatarConversationLogDto> getLogById(@PathVariable String logId) {
        try {
            log.info("Getting avatar conversation log by ID: {}", logId);
            AvatarConversationLogDto log = avatarConversationLogService.getLogById(logId);
            if (log != null) {
                return ResponseEntity.ok(log);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting log by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new avatar conversation log
     */
    @PostMapping
    public ResponseEntity<AvatarConversationLogDto> createLog(@RequestBody AvatarConversationLogRequest request) {
        try {
            log.info("Creating new avatar conversation log for user: {}", request.getUserId());
            AvatarConversationLogDto createdLog = avatarConversationLogService.createLog(request);
            return ResponseEntity.ok(createdLog);
        } catch (Exception e) {
            log.error("Error creating log: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update avatar conversation log
     */
    @PutMapping("/{logId}")
    public ResponseEntity<AvatarConversationLogDto> updateLog(
            @PathVariable String logId,
            @RequestBody AvatarConversationLogRequest request) {
        try {
            log.info("Updating avatar conversation log: {}", logId);
            AvatarConversationLogDto updatedLog = avatarConversationLogService.updateLog(logId, request);
            if (updatedLog != null) {
                return ResponseEntity.ok(updatedLog);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating log: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete avatar conversation log
     */
    @DeleteMapping("/{logId}")
    public ResponseEntity<?> deleteLog(@PathVariable String logId) {
        try {
            log.info("Deleting avatar conversation log: {}", logId);
            boolean deleted = avatarConversationLogService.deleteLog(logId);
            if (deleted) {
                return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Log deleted successfully\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting log: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get logs by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AvatarConversationLogDto>> getLogsByUserId(@PathVariable String userId) {
        try {
            log.info("Getting logs for user: {}", userId);
            List<AvatarConversationLogDto> logs = avatarConversationLogService.getLogsByUserId(userId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error getting logs by user ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get logs by avatar ID
     */
    @GetMapping("/avatar/{avatarId}")
    public ResponseEntity<List<AvatarConversationLogDto>> getLogsByAvatarId(@PathVariable String avatarId) {
        try {
            log.info("Getting logs for avatar: {}", avatarId);
            List<AvatarConversationLogDto> logs = avatarConversationLogService.getLogsByAvatarId(avatarId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error getting logs by avatar ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get logs by conversation ID
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<AvatarConversationLogDto> getLogByConversationId(@PathVariable String conversationId) {
        try {
            log.info("Getting log by conversation ID: {}", conversationId);
            AvatarConversationLogDto log = avatarConversationLogService.getLogByConversationId(conversationId);
            if (log != null) {
                return ResponseEntity.ok(log);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting log by conversation ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get conversation statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getConversationStats() {
        try {
            log.info("Getting conversation statistics");
            Object stats = avatarConversationLogService.getConversationStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting conversation stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
