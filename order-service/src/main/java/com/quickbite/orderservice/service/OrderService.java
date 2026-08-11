package com.quickbite.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.orderservice.client.UserClient;
import com.quickbite.orderservice.dto.OrderCreatedEvent;
import com.quickbite.orderservice.dto.UserDto;
import com.quickbite.orderservice.entity.OrderEntity;
import com.quickbite.orderservice.entity.OutboxEntity;
import com.quickbite.orderservice.repository.OrderRepository;
import com.quickbite.orderservice.repository.OutboxRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final UserClient userClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderEntity createOrder(OrderEntity order, Long userIdFromToken) {
        // 1. Принудительно устанавливаем userId из JWT-токена (защита от подмены)
        order.setUserId(userIdFromToken);

        // 2. Валидируем пользователя через user-service
        validateUserExistence(order.getUserId());

        // 3. Сохраняем сам заказ
        OrderEntity savedOrder = orderRepository.save(order);

        // 4. Формируем событие
        String eventId = UUID.randomUUID().toString();
        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getDescription(),
                savedOrder.getPrice()
        );

        // 5. Сохраняем запись в Outbox
        OutboxEntity outbox = createOutboxEntity(eventId, savedOrder.getId(), event);
        outboxRepository.save(outbox);

        log.info("Заказ #{} и событие Outbox [{}] успешно сохранены", savedOrder.getId(), eventId);
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }

    private void validateUserExistence(Long userId) {
        try {
            UserDto user = userClient.getUserById(userId);
            log.info("Создаем заказ для пользователя ID: {}, Name: {}", userId, user.getName());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Пользователь с ID " + userId + " не найден!"
            );
        } catch (Exception e) {
            log.error("Ошибка обращения к user-service для userId={}: {}", userId, e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "user-service временно недоступен: " + e.getMessage()
            );
        }
    }

    private OutboxEntity createOutboxEntity(String eventId, Long orderId, OrderCreatedEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            return new OutboxEntity(eventId, "ORDER", orderId, jsonPayload);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации события Outbox для orderId={}", orderId, e);
            throw new IllegalStateException("Ошибка сериализации события в Outbox", e);
        }
    }
}