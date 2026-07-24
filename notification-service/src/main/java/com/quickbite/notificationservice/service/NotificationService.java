package com.quickbite.notificationservice.service;

import com.quickbite.notificationservice.dto.OrderCreatedEvent;

public interface NotificationService {
    void processOrderNotification(OrderCreatedEvent event);
}