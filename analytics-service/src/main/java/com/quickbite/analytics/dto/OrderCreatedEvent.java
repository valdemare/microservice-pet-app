package com.quickbite.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String eventId;
    private Long orderId;
    private Long userId;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdAt;
}