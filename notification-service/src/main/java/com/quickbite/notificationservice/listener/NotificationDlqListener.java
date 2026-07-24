package com.quickbite.notificationservice.listener;

import com.quickbite.notificationservice.config.RabbitMQConfig;
import com.quickbite.notificationservice.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationDlqListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationDlqListener.class);

    @RabbitListener(queues = RabbitMQConfig.DLQ_NAME)
    public void processFailedNotification(OrderCreatedEvent event, Message message) {
        log.error("=================== DLQ ALERT ===================");
        log.error("Сообщение попало в Dead Letter Queue!");
        log.error("Данные заказа: Order ID = {}, User ID = {}, Price = {}",
                event.getOrderId(), event.getUserId(), event.getPrice());

        // Извлекаем служебную информацию об ошибках из заголовков AMQP
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        if (headers.containsKey("x-death")) {
            log.error("Служебная информация о сбое (x-death): {}", headers.get("x-death"));
        }

        // 1. Здесь можно отправить алерт в Telegram / Slack / Sentry
        // 2. Или сохранить упавшее событие в БД (таблица failed_messages) для ручного разбора
        sendAlertToDevOps(event, headers);
        log.error("=================================================");
    }

    private void sendAlertToDevOps(OrderCreatedEvent event, Map<String, Object> headers) {
        // Имитация отправки алерта
        log.warn("🚨 [ALERT] Уведомление разработчикам: Не удалось обработать заказ #{}", event.getOrderId());
    }
}