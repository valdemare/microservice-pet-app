package com.quickbite.orderservice.controller;

import com.quickbite.common.security.UserContext;
import com.quickbite.orderservice.client.UserClient;
import com.quickbite.orderservice.entity.OrderEntity;
import com.quickbite.orderservice.repository.OrderRepository;
import com.quickbite.orderservice.repository.OutboxRepository;
import com.quickbite.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderEntity createOrder(@Valid @RequestBody OrderEntity order,
                                   @AuthenticationPrincipal UserContext userContext,
                                   HttpServletRequest request) {
        log.info("[DIAG-3] Order-Service принял traceparent: {}", request.getHeader("traceparent"));
        log.info("[DIAG-3] Order-Service принял X-B3-TraceId: {}", request.getHeader("X-B3-TraceId"));
        if (userContext == null || userContext.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не аутентифицирован");
        }
        return orderService.createOrder(order, userContext.getUserId());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }
}