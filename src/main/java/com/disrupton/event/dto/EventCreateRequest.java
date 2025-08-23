package com.disrupton.event.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventCreateRequest {
    private String title;
    private String description;
    private String imageUrl;
    private String dateTime; // ISO 8601 format
    private String location;
    private Double latitude;
    private Double longitude;
    private List<String> tags;
}
