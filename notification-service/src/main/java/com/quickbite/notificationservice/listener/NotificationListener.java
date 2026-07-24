package com.quickbite.notificationservice.listener;

import com.quickbite.notificationservice.dto.OrderCreatedEvent;
import com.quickbite.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleNotification(OrderCreatedEvent event) {
        log.info("Получено сообщение из очереди RabbitMQ: {}", event);

        // Вызываем бизнес-логику
        notificationService.processOrderNotification(event);
    }

}