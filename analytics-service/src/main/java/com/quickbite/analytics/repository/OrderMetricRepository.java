package com.quickbite.analytics.repository;

import com.quickbite.analytics.entity.OrderMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrderMetricRepository extends JpaRepository<OrderMetric, Long> {

    // Проверка идемпотентности по уникальному ID события
    boolean existsByEventId(String eventId);

    // Подсчет общей выручки
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM OrderMetric o")
    BigDecimal getTotalRevenue();
}