package com.quickbite.notificationservice.repository;

import com.quickbite.notificationservice.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    // Метод для получения всех уведомлений конкретного пользователя
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}