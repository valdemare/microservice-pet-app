package com.quickbite.orderservice.controller;

import com.quickbite.common.security.UserContext;
import com.quickbite.orderservice.dto.OrderCreateDto;
import com.quickbite.orderservice.entity.OrderEntity;
import com.quickbite.orderservice.repository.OrderRepository;
import com.quickbite.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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
    public OrderEntity createOrder(@Valid @RequestBody OrderCreateDto dto,
                                   @RequestHeader("X-User-Id") Long userId,
                                   @AuthenticationPrincipal UserContext userContext,
                                   HttpServletRequest request) {
        log.info("[DIAG-3] Order-Service принял traceparent: {}", request.getHeader("traceparent"));
        log.info("[DIAG-3] Order-Service принял X-B3-TraceId: {}", request.getHeader("X-B3-TraceId"));
        if (userContext == null || userContext.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не аутентифицирован");
        }
        OrderEntity order = new OrderEntity();
        order.setDescription(dto.description());
        order.setPrice(dto.price());
        return orderService.createOrder(order, userId);// userContext.getUserId());
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderEntity getOrderById(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String userRole) {

        return orderService.getOrderById(id, userId, userRole);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }
}