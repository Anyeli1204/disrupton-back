package com.disrupton.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {
    private String id;
    private String topic;
    private String type;
    private String date_created;
    private String user_id;
    private Long version;
    private String live_mode;
    private Object data;
}