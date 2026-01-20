package com.focusapp.dto;

import java.util.Map;

public class WeeklyStatsResponse {
    
    private Map<String, Long> dailyDurations; // day -> total seconds
    
    // Constructors
    public WeeklyStatsResponse() {
    }
    
    public WeeklyStatsResponse(Map<String, Long> dailyDurations) {
        this.dailyDurations = dailyDurations;
    }
    
    // Getters and Setters
    public Map<String, Long> getDailyDurations() {
        return dailyDurations;
    }
    
    public void setDailyDurations(Map<String, Long> dailyDurations) {
        this.dailyDurations = dailyDurations;
    }
}
