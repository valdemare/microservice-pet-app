package com.quickbite.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private String eventId;
    private Long orderId;
    private Long userId;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdAt;
}