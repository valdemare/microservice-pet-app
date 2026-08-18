package com.quickbite.orderservice.repository;

import com.quickbite.orderservice.entity.OutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity, String> {
    // Находим первые N неотправленных событий
    List<OutboxEntity> findByStatusOrderByCreatedAtAsc(OutboxEntity.OutboxStatus status, Pageable pageable);
    @Query(value = """
            SELECT * FROM outbox 
            WHERE status = 'PENDING' 
            ORDER BY created_at ASC 
            LIMIT :limit 
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEntity> findPendingForUpdate(@Param("limit") int limit);
}