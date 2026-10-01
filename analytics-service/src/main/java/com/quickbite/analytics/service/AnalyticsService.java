package com.quickbite.analytics.service;

import com.quickbite.analytics.dto.OrderCreatedEvent;
import com.quickbite.analytics.entity.OrderMetric;
import com.quickbite.analytics.repository.OrderMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderMetricRepository repository;

    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {
        // Защита от дубликатов (Идемпотентность)
        if (repository.existsByOrderId(event.getOrderId())) {
            log.warn("Метрика для заказа id={} уже была обработана", event.getOrderId());
            return;
        }

        OrderMetric metric = OrderMetric.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .totalPrice(event.getTotalPrice())
                .createdAt(event.getCreatedAt())
                .build();

        repository.save(metric);
        log.info("Сохранена метрика по заказу id={}", event.getOrderId());
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", repository.count());
        summary.put("totalRevenue", repository.getTotalRevenue());
        return summary;
    }
}