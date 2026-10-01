package com.quickbite.analytics.repository;

import com.quickbite.analytics.entity.OrderMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface OrderMetricRepository extends JpaRepository<OrderMetric, Long> {

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM OrderMetric o")
    BigDecimal getTotalRevenue();

    boolean existsByOrderId(Long orderId);
}