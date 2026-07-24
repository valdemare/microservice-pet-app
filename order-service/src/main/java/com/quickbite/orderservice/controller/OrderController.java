package com.quickbite.orderservice.controller;

import com.quickbite.orderservice.client.UserClient;
import com.quickbite.orderservice.config.RabbitMQConfig;
import com.quickbite.orderservice.dto.OrderCreatedEvent;
import com.quickbite.orderservice.dto.UserDto;
import com.quickbite.orderservice.entity.OrderEntity;
import com.quickbite.orderservice.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping
    public OrderEntity createOrder(@RequestBody OrderEntity order) {
        // 1. Проверяем существование пользователя в user-service
        try {
            UserDto user = userClient.getUserById(order.getUserId());
            System.out.println("Создаем заказ для пользователя: " + user.getName());
        } catch (FeignException.NotFound e) {
            // 404 только если user-service ответил, что ЮЗЕРА НЕТ
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Пользователь с ID " + order.getUserId() + " не найден!"
            );
        } catch (Exception e) {
            // 503 если user-service недоступен / упал / не отвечает
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "user-service временно недоступен: " + e.getMessage()
            );
        }

        // 2. Сохраняем заказ в базу данных
        OrderEntity savedOrder = orderRepository.save(order);

        // 3. Публикуем событие в RabbitMQ (Асинхронно)
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getDescription(),
                savedOrder.getPrice()
        );

        rabbitTemplate.convertAndSend("",RabbitMQConfig.QUEUE_NAME, event);
        System.out.println("Событие OrderCreatedEvent отправлено в RabbitMQ!");

        return savedOrder;
    }

    @GetMapping
    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }
}