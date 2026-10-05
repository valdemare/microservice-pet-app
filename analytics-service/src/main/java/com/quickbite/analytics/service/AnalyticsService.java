package com.quickbite.analytics.service;

import com.quickbite.analytics.dto.AnalyticsSummaryDto;
import com.quickbite.analytics.dto.OrderCreatedEvent;
import com.quickbite.analytics.entity.OrderMetric;
import com.quickbite.analytics.repository.OrderMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderMetricRepository repository;

    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {
        // Защита от дубликатов (Идемпотентность)
        if (repository.existsByEventId(event.getEventId())) {
            log.warn("Метрика для заказа id={} уже была обработана", event.getOrderId());
            return;
        }

        OrderMetric metric = OrderMetric.builder()
                .eventId(event.getEventId())
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .totalPrice(event.getPrice())
                .createdAt((event.getCreatedAt() != null) ? event.getCreatedAt() : LocalDateTime.now())
                .build();

        repository.save(metric);
        log.info("Сохранена метрика по заказу orderId={} (eventId={})",
                event.getOrderId(), event.getEventId());
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getSummaryDto() {
        long totalOrders = repository.count();
        BigDecimal totalRevenue = repository.getTotalRevenue();

        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AnalyticsSummaryDto.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .build();
    }
}