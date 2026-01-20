package com.focusapp.dto;

import java.util.Map;

public class HourlyStatsResponse {
    
    private Map<Integer, Long> hourlyDurations; // hour -> total seconds
    
    // Constructors
    public HourlyStatsResponse() {
    }
    
    public HourlyStatsResponse(Map<Integer, Long> hourlyDurations) {
        this.hourlyDurations = hourlyDurations;
    }
    
    // Getters and Setters
    public Map<Integer, Long> getHourlyDurations() {
        return hourlyDurations;
    }
    
    public void setHourlyDurations(Map<Integer, Long> hourlyDurations) {
        this.hourlyDurations = hourlyDurations;
    }
}
