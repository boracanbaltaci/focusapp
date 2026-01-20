package com.focusapp.controller;

import com.focusapp.dto.*;
import com.focusapp.entity.User;
import com.focusapp.repository.UserRepository;
import com.focusapp.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/start")
    public ResponseEntity<SessionResponse> startSession(
            @Valid @RequestBody SessionRequest request,
            Authentication authentication) {
        
        User user = getUserFromAuthentication(authentication);
        SessionResponse response = sessionService.startSession(user, request.getIsBreak());
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/end")
    public ResponseEntity<SessionResponse> endSession(
            @PathVariable Long id,
            Authentication authentication) {
        
        User user = getUserFromAuthentication(authentication);
        SessionResponse response = sessionService.endSession(id, user);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/weekly")
    public ResponseEntity<WeeklyStatsResponse> getWeeklyStats(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Map<String, Long> stats = sessionService.getWeeklyStats(user);
        
        return ResponseEntity.ok(new WeeklyStatsResponse(stats));
    }
    
    @GetMapping("/stats/hourly")
    public ResponseEntity<HourlyStatsResponse> getHourlyStats(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Map<Integer, Long> stats = sessionService.getHourlyStats(user);
        
        return ResponseEntity.ok(new HourlyStatsResponse(stats));
    }
    
    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
