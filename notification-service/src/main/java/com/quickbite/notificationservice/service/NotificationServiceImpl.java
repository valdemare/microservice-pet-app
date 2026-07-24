package com.quickbite.notificationservice.service;

import com.quickbite.notificationservice.dto.OrderCreatedEvent;
import com.quickbite.notificationservice.entity.NotificationEntity;
import com.quickbite.notificationservice.entity.ProcessedEvent;
import com.quickbite.notificationservice.repository.NotificationRepository;
import com.quickbite.notificationservice.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private ProcessedEventRepository processedEventRepository;
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(ProcessedEventRepository processedEventRepository, NotificationRepository notificationRepository) {
        this.processedEventRepository = processedEventRepository;
        this.notificationRepository=notificationRepository;
    }
    @Override
    @Transactional
    public void processOrderNotification(OrderCreatedEvent event) {
        String eventId = event.getEventId();

        // Проверяем, обрабатывали ли мы это событие ранее
        if (processedEventRepository.existsById(eventId)) {
            log.warn("⚠️ [ДУБЛИКАТ] Событие с eventId={} уже было обработано ранее. Пропускаем.", eventId);
            return; // Выходим из метода — повторная отправка письма/SMS не выполняется!
        }
        log.info("===> Начало обработки уведомления для заказа #{}", event.getOrderId());
        // Временная имитация падения сервиса
        // throw new RuntimeException("Упс! Сервис упал при обработке!");

        // Формируем текст уведомления
        String emailMessage = String.format("Уважаемый клиент #%d, ваш заказ #%d (%s) на сумму %.2f руб. успешно создан!",
                event.getUserId(), event.getOrderId(), event.getDescription(), event.getPrice());
        // Имитация формирования и отправки сообщения
        sendEmail(event.getUserId(), emailMessage);
        // Сохраняем бизнес-историю уведомления в БД
        NotificationEntity notification = new NotificationEntity(
                eventId,
                event.getOrderId(),
                event.getUserId(),
                "EMAIL",
                emailMessage,
                NotificationEntity.NotificationStatus.SENT
        );
        notificationRepository.save(notification);
        
        // Сохраняем eventId в БД, фиксируя успешную обработку
        processedEventRepository.save(new ProcessedEvent(eventId));
        // Дополнительные действия (например, метрики или push)
        log.info("<=== Уведомление по заказу #{} успешно отправлено!", event.getOrderId());
    }

    private void sendEmail(Long userId, String text) {
        log.info("Отправка Email пользователю # {}: '{}'", userId, text);
    }

    private void sendEmail(OrderCreatedEvent event) {
        // Здесь в будущем можно подключить JavaMailSender
        log.info("Отправка Email клиенту (ID: {}). Сумма заказа: {}",
                event.getUserId(),
                event.getPrice());
    }
}
