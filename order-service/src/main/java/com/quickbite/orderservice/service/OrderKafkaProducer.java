package com.quickbite.orderservice.service;

import com.quickbite.orderservice.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void sendOrderEvent(OrderCreatedEvent event) {
        // Отправка в топик "orders-stream", где ключом является orderId для соблюдения порядка
        kafkaTemplate.send("orders-stream", String.valueOf(event.getOrderId()), event);
    }
}