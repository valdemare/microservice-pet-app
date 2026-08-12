package com.quickbite.orderservice.controller;

import com.quickbite.common.security.UserContext;
import com.quickbite.orderservice.client.UserClient;
import com.quickbite.orderservice.entity.OrderEntity;
import com.quickbite.orderservice.repository.OrderRepository;
import com.quickbite.orderservice.repository.OutboxRepository;
import com.quickbite.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Transactional
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderEntity createOrder(@RequestBody OrderEntity order,
                                   @AuthenticationPrincipal UserContext userContext) {

        // Передаем в сервис тело заказа И настоящий userId, извлеченный фильтром из заголовков Gateway
        Long currentUserId = (userContext != null) ? userContext.getUserId() : order.getUserId();

        return orderService.createOrder(order, currentUserId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }
}