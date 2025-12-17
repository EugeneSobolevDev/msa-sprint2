package com.hotelio.booking.repositories;

import com.hotelio.booking.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Query(value = """
        SELECT * FROM outbox_event 
        WHERE status = 'PENDING' 
        ORDER BY created_at ASC, id ASC 
        LIMIT 1 
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    Optional<OutboxEvent> findOldestPending();

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'SENT', e.sentAt = :sentAt WHERE e.id = :id")
    void markAsSent(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);

    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = 'FAILED', e.errorMessage = :error WHERE e.id = :id")
    void markAsFailed(@Param("id") Long id, @Param("error") String error);
}
