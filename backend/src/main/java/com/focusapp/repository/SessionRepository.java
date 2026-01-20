package com.focusapp.repository;

import com.focusapp.entity.User;
import com.focusapp.entity.WorkingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<WorkingSession, Long> {
    
    List<WorkingSession> findByUserOrderByStartTimeDesc(User user);
    
    @Query("SELECT s FROM WorkingSession s WHERE s.user = :user AND s.startTime >= :startDate AND s.startTime <= :endDate ORDER BY s.startTime")
    List<WorkingSession> findByUserAndDateRange(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT s FROM WorkingSession s WHERE s.user = :user AND s.endTime IS NULL")
    List<WorkingSession> findActiveSessionsByUser(@Param("user") User user);
}
