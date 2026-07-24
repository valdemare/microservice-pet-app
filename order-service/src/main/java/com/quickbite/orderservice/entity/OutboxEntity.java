package com.quickbite.orderservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
public class OutboxEntity {

    @Id
    private String id; // Идентификатор события (eventId/UUID)

    private String aggregateType; // Имя сущности, например, "ORDER"

    private Long aggregateId; // ID заказа (orderId)

    @Column(columnDefinition = "TEXT")
    private String payload; // JSON с содержимым события (OrderCreatedEvent)

    @Enumerated(EnumType.STRING)
    private OutboxStatus status; // PENDING, PROCESSED, FAILED

    private LocalDateTime createdAt;

    public enum OutboxStatus {
        PENDING, PROCESSED, FAILED
    }

    public OutboxEntity() {}

    public OutboxEntity(String id, String aggregateType, Long aggregateId, String payload) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public Long getAggregateId() { return aggregateId; }
    public void setAggregateId(Long aggregateId) { this.aggregateId = aggregateId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public OutboxStatus getStatus() { return status; }
    public void setStatus(OutboxStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}