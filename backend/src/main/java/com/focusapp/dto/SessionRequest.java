package com.focusapp.dto;

public class SessionRequest {
    
    private Boolean isBreak;
    
    // Constructors
    public SessionRequest() {
    }
    
    public SessionRequest(Boolean isBreak) {
        this.isBreak = isBreak;
    }
    
    // Getters and Setters
    public Boolean getIsBreak() {
        return isBreak != null ? isBreak : false;
    }
    
    public void setIsBreak(Boolean isBreak) {
        this.isBreak = isBreak;
    }
}
