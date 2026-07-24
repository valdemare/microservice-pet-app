package com.quickbite.orderservice.repository;

import com.quickbite.orderservice.entity.OutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity, String> {
    // Находим первые N неотправленных событий
    List<OutboxEntity> findByStatusOrderByCreatedAtAsc(OutboxEntity.OutboxStatus status, Pageable pageable);
}