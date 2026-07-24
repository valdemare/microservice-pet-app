package com.quickbite.notificationservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderCreatedEvent {
    private String eventId;
    private Long orderId;
    private Long userId;
    private String description;
    private BigDecimal price;
}