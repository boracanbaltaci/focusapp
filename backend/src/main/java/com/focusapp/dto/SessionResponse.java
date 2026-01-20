package com.focusapp.dto;

import java.time.LocalDateTime;

public class SessionResponse {
    
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationSeconds;
    private Boolean isBreak;
    
    // Constructors
    public SessionResponse() {
    }
    
    public SessionResponse(Long id, LocalDateTime startTime, LocalDateTime endTime, Long durationSeconds, Boolean isBreak) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
        this.isBreak = isBreak;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Long getDurationSeconds() {
        return durationSeconds;
    }
    
    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    
    public Boolean getIsBreak() {
        return isBreak;
    }
    
    public void setIsBreak(Boolean isBreak) {
        this.isBreak = isBreak;
    }
}
