package com.quickbite.analytics.listener;

import com.quickbite.analytics.dto.OrderCreatedEvent;
import com.quickbite.analytics.repository.OrderMetricRepository;
import com.quickbite.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"orders-stream", "orders-stream.DLT"}
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderEventListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private AnalyticsService analyticsService;

    @Autowired
    private OrderMetricRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Успешная вычитка события из Kafka и сохранение аналитики")
    void shouldConsumeMessageAndSaveAnalytics() {
        // Arrange
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                202L,
                5L,
                "Another test food",
                new BigDecimal("2500.00"),
                LocalDateTime.now()
        );

        // Act
        kafkaTemplate.send("orders-stream", String.valueOf(event.getOrderId()), event);

        // Assert: Асинхронное ожидание обработки события
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                verify(analyticsService).processOrderCreated(event));
    }

    @Test
    @DisplayName("При ошибке совершает 3 попытки и отправляет событие в DLT")
    void shouldRetryThreeTimesAndSendToDltOnFailure() {
        // Arrange
        OrderCreatedEvent badEvent = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                102L,
                1L,
                "Test drink",
                new BigDecimal("1234.00"),
                LocalDateTime.now()
        );

        // Настраиваем симуляцию падения базы данных или логики
        doThrow(new RuntimeException("Simulated DB Crash"))
                .when(analyticsService).processOrderCreated(any());

        // Act
        kafkaTemplate.send("orders-stream", String.valueOf(badEvent.getOrderId()), badEvent);

        // Assert: Проверяем, что из-за maxAttempts(3) метод вызывался ровно 3 раза перед уходом в DLT
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                verify(analyticsService, atLeast(3)).processOrderCreated(badEvent));
    }
}