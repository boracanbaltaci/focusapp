package com.focusapp.service;

import com.focusapp.dto.SessionResponse;
import com.focusapp.entity.User;
import com.focusapp.entity.WorkingSession;
import com.focusapp.exception.ResourceNotFoundException;
import com.focusapp.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SessionService {
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Transactional
    public SessionResponse startSession(User user, Boolean isBreak) {
        // Check for active sessions and end them
        List<WorkingSession> activeSessions = sessionRepository.findActiveSessionsByUser(user);
        for (WorkingSession activeSession : activeSessions) {
            endSessionInternal(activeSession);
        }
        
        WorkingSession session = new WorkingSession(LocalDateTime.now(), user, isBreak);
        session = sessionRepository.save(session);
        
        return toSessionResponse(session);
    }
    
    @Transactional
    public SessionResponse endSession(Long sessionId, User user) {
        WorkingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));
        
        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Session does not belong to user");
        }
        
        if (session.getEndTime() != null) {
            throw new IllegalStateException("Session already ended");
        }
        
        endSessionInternal(session);
        session = sessionRepository.save(session);
        
        return toSessionResponse(session);
    }
    
    private void endSessionInternal(WorkingSession session) {
        session.setEndTime(LocalDateTime.now());
        long seconds = Duration.between(session.getStartTime(), session.getEndTime()).getSeconds();
        session.setDurationSeconds(seconds);
    }
    
    public Map<String, Long> getWeeklyStats(User user) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7).truncatedTo(ChronoUnit.DAYS);
        
        List<WorkingSession> sessions = sessionRepository.findByUserAndDateRange(user, startDate, endDate);
        
        Map<String, Long> dailyDurations = new LinkedHashMap<>();
        
        // Initialize all days with 0
        for (int i = 0; i < 7; i++) {
            LocalDateTime day = startDate.plusDays(i);
            String dayKey = day.toLocalDate().toString();
            dailyDurations.put(dayKey, 0L);
        }
        
        // Aggregate durations
        for (WorkingSession session : sessions) {
            if (session.getDurationSeconds() != null && !session.getIsBreak()) {
                String dayKey = session.getStartTime().toLocalDate().toString();
                dailyDurations.merge(dayKey, session.getDurationSeconds(), Long::sum);
            }
        }
        
        return dailyDurations;
    }
    
    public Map<Integer, Long> getHourlyStats(User user) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusHours(24);
        
        List<WorkingSession> sessions = sessionRepository.findByUserAndDateRange(user, startDate, endDate);
        
        Map<Integer, Long> hourlyDurations = new LinkedHashMap<>();
        
        // Initialize all hours with 0
        for (int i = 0; i < 24; i++) {
            hourlyDurations.put(i, 0L);
        }
        
        // Aggregate durations by hour
        for (WorkingSession session : sessions) {
            if (session.getDurationSeconds() != null && !session.getIsBreak()) {
                int hour = session.getStartTime().getHour();
                hourlyDurations.merge(hour, session.getDurationSeconds(), Long::sum);
            }
        }
        
        return hourlyDurations;
    }
    
    private SessionResponse toSessionResponse(WorkingSession session) {
        return new SessionResponse(
            session.getId(),
            session.getStartTime(),
            session.getEndTime(),
            session.getDurationSeconds(),
            session.getIsBreak()
        );
    }
}
