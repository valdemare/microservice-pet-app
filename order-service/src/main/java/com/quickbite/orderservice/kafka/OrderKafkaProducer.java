package com.quickbite.orderservice.kafka;

import com.quickbite.orderservice.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private static final String TOPIC_ORDERS = "orders-stream";
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        String key = String.valueOf(event.getOrderId());

        log.info("Отправка события OrderCreatedEvent в Kafka [topic={}, orderId={}]: {}",
                TOPIC_ORDERS, key, event);

        kafkaTemplate.send(TOPIC_ORDERS, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Событие успешно доставлено в Kafka: offset={}",
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Ошибка при отправке события в Kafka for orderId={}", key, ex);
                    }
                });
    }
}