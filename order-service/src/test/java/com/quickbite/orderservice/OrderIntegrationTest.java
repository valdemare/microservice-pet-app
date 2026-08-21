package com.quickbite.orderservice;

import com.quickbite.orderservice.client.UserClient;
import com.quickbite.orderservice.dto.UserDto;
import com.quickbite.orderservice.entity.OrderEntity;
import com.quickbite.orderservice.entity.OutboxEntity;
import com.quickbite.orderservice.exception.UserServiceException;
import com.quickbite.orderservice.repository.OrderRepository;
import com.quickbite.orderservice.repository.OutboxRepository;
import com.quickbite.orderservice.scheduler.OutboxScheduler;
import com.quickbite.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("local-test") // Использует application-test.yml
class OrderIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxRepository outboxRepository;
    @MockitoBean
    private UserClient userClient;
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        outboxRepository.deleteAll();
        UserDto mockUser = new UserDto();
        mockUser.setId(1L);
        when(userClient.getUserById(anyLong())).thenReturn(mockUser);
    }

    @Test
    void createOrder_ShouldSaveOrderAndCreateOutboxEvent() {
        // Given
        Long userId = 1L;
        OrderEntity order = new OrderEntity();
        order.setPrice(new BigDecimal("1500.00"));
        order.setDescription("Тестовый набор еды.");

        // When
        OrderEntity savedOrder = orderService.createOrder(order, userId);

        // Then
        // 1. Проверяем, что заказ успешно сохранился в БД
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(orderRepository.findById(savedOrder.getId())).isPresent();

        // 2. Проверяем транзакционность (Transactional Outbox Pattern):
        // событие для RabbitMQ должно было автоматически записаться в Outbox-таблицу
        assertThat(outboxRepository.findAll()).hasSize(1);
        assertThat(outboxRepository.findAll().get(0).getPayload()).contains("1500.00");
    }
    @Autowired
    private OutboxScheduler outboxScheduler;

    @Test
    void processOutboxEvents_ShouldSendToRabbitAndMarkAsProcessed() {
        // Given: Создаем заказ, который порождает запись в outbox со статусом NEW
        OrderEntity order = new OrderEntity();
        order.setPrice(new BigDecimal("2000.00"));
        order.setDescription("Тестовый сет");
        orderService.createOrder(order, 1L);

        assertThat(outboxRepository.findAll()).hasSize(1);

        // When: Принудительно вызываем метод планировщика
        outboxScheduler.processOutboxMessages();

        // Then: Проверяем, что статус события изменился или событие удалено
        var outboxList = outboxRepository.findAll();

        // Если вы обновляете статус:
        assertThat(outboxList.get(0).getStatus()).isEqualTo(OutboxEntity.OutboxStatus.PROCESSED);
    }

    @Test
    void createOrder_WhenUserNotFound_ShouldThrowExceptionAndNotSaveOrder() {
        // Given: Мокаем отказ user-service
        when(userClient.getUserById(999L))
                .thenThrow(new UserServiceException("Пользователь не найден"));

        OrderEntity order = new OrderEntity();
        order.setPrice(new BigDecimal("500.00"));
        order.setDescription("Невалидный заказ");

        // When & Then: Проверяем выброс исключения
        org.junit.jupiter.api.Assertions.assertThrows(UserServiceException.class, () -> {
            orderService.createOrder(order, 999L);
        });

        // Проверяем, что ничего не сохранено в БД и Outbox (откат транзакции)
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(outboxRepository.findAll()).isEmpty();
    }
}