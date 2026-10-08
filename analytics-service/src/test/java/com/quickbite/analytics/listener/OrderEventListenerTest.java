package com.quickbite.analytics.listener;

import com.quickbite.analytics.dto.OrderCreatedEvent;
import com.quickbite.analytics.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private OrderEventListener orderEventListener;

    @Test
    @DisplayName("Должен передать событие в AnalyticsService при получении сообщения из Kafka")
    void shouldPassEventToAnalyticsService() {
        // Arrange
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                101L,
                1L,
                "Test food",
                new BigDecimal("1500.00"),
                LocalDateTime.now()
        );

        // Act
        orderEventListener.handleOrderCreated(event);

        // Assert
        verify(analyticsService).processOrderCreated(event);
    }
}