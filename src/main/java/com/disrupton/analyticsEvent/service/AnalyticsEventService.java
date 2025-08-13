package com.disrupton.analyticsEvent.service;

import com.disrupton.analyticsEvent.dto.AnalyticsEventDto;
import com.disrupton.analyticsEvent.dto.AnalyticsEventRequest;
import com.disrupton.analyticsEvent.model.AnalyticsEvent;
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
public class AnalyticsEventService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "analytics_events";

    /**
     * Get all analytics events
     */
    public List<AnalyticsEventDto> getAllEvents() throws ExecutionException, InterruptedException {
        List<AnalyticsEventDto> events = new ArrayList<>();
        
        firestore.collection(COLLECTION_NAME).get().get().getDocuments().forEach(document -> {
            AnalyticsEventDto event = document.toObject(AnalyticsEventDto.class);
            events.add(event);
        });
        
        return events;
    }

    /**
     * Get analytics event by ID
     */
    public AnalyticsEventDto getEventById(String eventId) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(eventId).get().get();
        
        if (document.exists()) {
            return document.toObject(AnalyticsEventDto.class);
        } else {
            log.warn("Analytics event not found with ID: {}", eventId);
            return null;
        }
    }

    /**
     * Create new analytics event
     */
    public AnalyticsEventDto createEvent(AnalyticsEventRequest request) throws ExecutionException, InterruptedException {
        AnalyticsEventDto event = new AnalyticsEventDto();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(request.getUserId());
        event.setSessionId(request.getSessionId());
        event.setEventType(request.getEventType());
        event.setEventName(request.getEventName());
        event.setEventData(request.getEventData());
        event.setLocation(request.getLocation());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setDeviceInfo(request.getDeviceInfo());
        event.setAppVersion(request.getAppVersion());
        event.setCreatedAt(Timestamp.now());
        event.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(event.getEventId());
        docRef.set(event).get();
        
        log.info("Analytics event created successfully: {}", event.getEventId());
        return event;
    }

    /**
     * Update analytics event
     */
    public AnalyticsEventDto updateEvent(String eventId, AnalyticsEventRequest request) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(eventId).get().get();
        
        if (!document.exists()) {
            log.warn("Analytics event not found for update: {}", eventId);
            return null;
        }
        
        AnalyticsEventDto event = document.toObject(AnalyticsEventDto.class);
        event.setSessionId(request.getSessionId());
        event.setEventType(request.getEventType());
        event.setEventName(request.getEventName());
        event.setEventData(request.getEventData());
        event.setLocation(request.getLocation());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setDeviceInfo(request.getDeviceInfo());
        event.setAppVersion(request.getAppVersion());
        event.setUpdatedAt(Timestamp.now());
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(eventId);
        docRef.set(event).get();
        
        log.info("Analytics event updated successfully: {}", eventId);
        return event;
    }

    /**
     * Delete analytics event
     */
    public boolean deleteEvent(String eventId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(eventId);
        DocumentSnapshot document = docRef.get().get();
        
        if (document.exists()) {
            docRef.delete().get();
            log.info("Analytics event deleted successfully: {}", eventId);
            return true;
        } else {
            log.warn("Analytics event not found for deletion: {}", eventId);
            return false;
        }
    }

    /**
     * Get events by user ID
     */
    public List<AnalyticsEventDto> getEventsByUserId(String userId) throws ExecutionException, InterruptedException {
        List<AnalyticsEventDto> events = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            AnalyticsEventDto event = document.toObject(AnalyticsEventDto.class);
            events.add(event);
        });
        
        return events;
    }

    /**
     * Get events by event type
     */
    public List<AnalyticsEventDto> getEventsByType(String eventType) throws ExecutionException, InterruptedException {
        List<AnalyticsEventDto> events = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("eventType", eventType)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            AnalyticsEventDto event = document.toObject(AnalyticsEventDto.class);
            events.add(event);
        });
        
        return events;
    }

    /**
     * Get events by session ID
     */
    public List<AnalyticsEventDto> getEventsBySessionId(String sessionId) throws ExecutionException, InterruptedException {
        List<AnalyticsEventDto> events = new ArrayList<>();
        
        QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .get().get();
        
        snapshot.getDocuments().forEach(document -> {
            AnalyticsEventDto event = document.toObject(AnalyticsEventDto.class);
            events.add(event);
        });
        
        return events;
    }

    /**
     * Get event statistics
     */
    public Map<String, Object> getEventStats() throws ExecutionException, InterruptedException {
        Map<String, Object> stats = new HashMap<>();
        
        QuerySnapshot allEvents = firestore.collection(COLLECTION_NAME).get().get();
        
        stats.put("totalEvents", allEvents.size());
        
        // Count by event type
        Map<String, Integer> typeCounts = new HashMap<>();
        for (QueryDocumentSnapshot document : allEvents) {
            AnalyticsEventDto event = document.toObject(AnalyticsEventDto.class);
            String type = event.getEventType();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        stats.put("eventsByType", typeCounts);
        
        // Count by event name
        Map<String, Integer> nameCounts = new HashMap<>();
        for (QueryDocumentSnapshot document : allEvents) {
            AnalyticsEventDto event = document.toObject(AnalyticsEventDto.class);
            String name = event.getEventName();
            nameCounts.put(name, nameCounts.getOrDefault(name, 0) + 1);
        }
        stats.put("eventsByName", nameCounts);
        
        return stats;
    }
}
