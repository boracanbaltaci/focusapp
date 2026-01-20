package com.focusapp.service;

import com.focusapp.entity.User;
import com.focusapp.entity.WorkingSession;
import com.focusapp.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    
    @Mock
    private SessionRepository sessionRepository;
    
    @InjectMocks
    private SessionService sessionService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "hashedPassword");
        testUser.setId(1L);
    }
    
    @Test
    void testStartSession_Success() {
        // Arrange
        when(sessionRepository.findActiveSessionsByUser(testUser)).thenReturn(new ArrayList<>());
        when(sessionRepository.save(any(WorkingSession.class))).thenAnswer(invocation -> {
            WorkingSession session = invocation.getArgument(0);
            session.setId(1L);
            return session;
        });
        
        // Act
        var response = sessionService.startSession(testUser, false);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertNotNull(response.getStartTime());
        assertNull(response.getEndTime());
        assertFalse(response.getIsBreak());
        
        verify(sessionRepository, times(1)).save(any(WorkingSession.class));
    }
    
    @Test
    void testGetWeeklyStats_ReturnsCorrectData() {
        // Arrange
        List<WorkingSession> sessions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        WorkingSession session1 = new WorkingSession(now.minusDays(1), testUser, false);
        session1.setEndTime(now.minusDays(1).plusHours(2));
        session1.setDurationSeconds(7200L); // 2 hours
        sessions.add(session1);
        
        when(sessionRepository.findByUserAndDateRange(eq(testUser), any(), any())).thenReturn(sessions);
        
        // Act
        Map<String, Long> stats = sessionService.getWeeklyStats(testUser);
        
        // Assert
        assertNotNull(stats);
        assertEquals(7, stats.size());
        assertTrue(stats.values().stream().anyMatch(v -> v == 7200L));
    }
    
    @Test
    void testGetHourlyStats_ReturnsCorrectData() {
        // Arrange
        List<WorkingSession> sessions = new ArrayList<>();
        
        when(sessionRepository.findByUserAndDateRange(eq(testUser), any(), any())).thenReturn(sessions);
        
        // Act
        Map<Integer, Long> stats = sessionService.getHourlyStats(testUser);
        
        // Assert
        assertNotNull(stats);
        assertEquals(24, stats.size());
        for (int i = 0; i < 24; i++) {
            assertTrue(stats.containsKey(i));
        }
    }
}
