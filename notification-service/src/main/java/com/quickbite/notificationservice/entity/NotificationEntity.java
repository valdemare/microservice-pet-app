package com.quickbite.notificationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;     // ID события из RabbitMQ (для связки)
    private Long orderId;       // ID заказа
    private Long userId;        // ID пользователя
    private String channel;     // EMAIL, SMS, TELEGRAM

    @Column(columnDefinition = "TEXT")
    private String message;     // Текст отправленного уведомления

    @Enumerated(EnumType.STRING)
    private NotificationStatus status; // SENT, FAILED

    private LocalDateTime createdAt;

    public enum NotificationStatus {
        SENT, FAILED
    }

    public NotificationEntity() {}

    public NotificationEntity(String eventId, Long orderId, Long userId, String channel, String message, NotificationStatus status) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.userId = userId;
        this.channel = channel;
        this.message = message;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}