package com.quickbite.notificationservice.service;

import com.quickbite.notificationservice.dto.OrderCreatedEvent;
import com.quickbite.notificationservice.entity.ProcessedEvent;
import com.quickbite.notificationservice.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private ProcessedEventRepository processedEventRepository;

    public NotificationServiceImpl(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }
    @Override
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
        // Имитация формирования и отправки сообщения
        sendEmail(event);
        // Сохраняем eventId в БД, фиксируя успешную обработку
        processedEventRepository.save(new ProcessedEvent(eventId));
        // Дополнительные действия (например, метрики или push)
        log.info("<=== Уведомление по заказу #{} успешно отправлено!", event.getOrderId());
    }

    private void sendEmail(OrderCreatedEvent event) {
        // Здесь в будущем можно подключить JavaMailSender
        log.info("Отправка Email клиенту (ID: {}). Сумма заказа: {}",
                event.getUserId(),
                event.getPrice());
    }
}
