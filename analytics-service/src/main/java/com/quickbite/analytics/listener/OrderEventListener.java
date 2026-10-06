package com.quickbite.analytics.listener;

import com.quickbite.analytics.dto.OrderCreatedEvent;
import com.quickbite.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "orders-stream", groupId = "analytics-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Получено событие заказа из Kafka: orderId={}, amount={}", event.getOrderId(), event.getPrice());
        analyticsService.processOrderCreated(event);
    }

    @DltHandler
    public void handleDltMessage(
            OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("СООБЩЕНИЕ ПОПАЛО В DLQ! Топик: {}, OrderID: {}, Причина: {}",
                topic, event.getOrderId(), exceptionMessage);
    }
}