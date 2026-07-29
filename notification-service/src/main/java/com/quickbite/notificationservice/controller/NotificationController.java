package com.quickbite.notificationservice.controller;

import com.quickbite.notificationservice.entity.NotificationEntity;
import com.quickbite.notificationservice.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Получить историю всех уведомлений
    @GetMapping
    public List<NotificationEntity> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // Получить истории уведомлений конкретного пользователя
    @GetMapping("/user/{userId}")
    public List<NotificationEntity> getUserNotifications(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}