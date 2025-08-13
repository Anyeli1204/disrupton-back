package com.disrupton.analyticsEvent.controller;

import com.disrupton.analyticsEvent.dto.AnalyticsEventDto;
import com.disrupton.analyticsEvent.dto.AnalyticsEventRequest;
import com.disrupton.analyticsEvent.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics-events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalyticsEventController {

    private final AnalyticsEventService analyticsEventService;

    /**
     * Get all analytics events
     */
    @GetMapping
    public ResponseEntity<List<AnalyticsEventDto>> getAllEvents() {
        try {
            log.info("Getting all analytics events");
            List<AnalyticsEventDto> events = analyticsEventService.getAllEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting all events: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get analytics event by ID
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<AnalyticsEventDto> getEventById(@PathVariable String eventId) {
        try {
            log.info("Getting analytics event by ID: {}", eventId);
            AnalyticsEventDto event = analyticsEventService.getEventById(eventId);
            if (event != null) {
                return ResponseEntity.ok(event);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting event by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new analytics event
     */
    @PostMapping
    public ResponseEntity<AnalyticsEventDto> createEvent(@RequestBody AnalyticsEventRequest request) {
        try {
            log.info("Creating new analytics event for user: {}", request.getUserId());
            AnalyticsEventDto createdEvent = analyticsEventService.createEvent(request);
            return ResponseEntity.ok(createdEvent);
        } catch (Exception e) {
            log.error("Error creating event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update analytics event
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<AnalyticsEventDto> updateEvent(
            @PathVariable String eventId,
            @RequestBody AnalyticsEventRequest request) {
        try {
            log.info("Updating analytics event: {}", eventId);
            AnalyticsEventDto updatedEvent = analyticsEventService.updateEvent(eventId, request);
            if (updatedEvent != null) {
                return ResponseEntity.ok(updatedEvent);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete analytics event
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable String eventId) {
        try {
            log.info("Deleting analytics event: {}", eventId);
            boolean deleted = analyticsEventService.deleteEvent(eventId);
            if (deleted) {
                return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Event deleted successfully\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get events by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnalyticsEventDto>> getEventsByUserId(@PathVariable String userId) {
        try {
            log.info("Getting events for user: {}", userId);
            List<AnalyticsEventDto> events = analyticsEventService.getEventsByUserId(userId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting events by user ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get events by event type
     */
    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<AnalyticsEventDto>> getEventsByType(@PathVariable String eventType) {
        try {
            log.info("Getting events by type: {}", eventType);
            List<AnalyticsEventDto> events = analyticsEventService.getEventsByType(eventType);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting events by type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get events by session ID
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AnalyticsEventDto>> getEventsBySessionId(@PathVariable String sessionId) {
        try {
            log.info("Getting events for session: {}", sessionId);
            List<AnalyticsEventDto> events = analyticsEventService.getEventsBySessionId(sessionId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error getting events by session ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get event statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getEventStats() {
        try {
            log.info("Getting event statistics");
            Object stats = analyticsEventService.getEventStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting event stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
