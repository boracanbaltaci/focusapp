package com.focusapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String passwordHash;
    
    // User preferences
    @Column(name = "language")
    private String language = "en";
    
    @Column(name = "clock_type")
    private String clockType = "digital"; // digital or analog
    
    @Column(name = "style")
    private String style = "default";
    
    @Column(name = "background")
    private String background = "default";
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkingSession> sessions = new ArrayList<>();
    
    // Constructors
    public User() {
    }
    
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getClockType() {
        return clockType;
    }
    
    public void setClockType(String clockType) {
        this.clockType = clockType;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
    
    public String getBackground() {
        return background;
    }
    
    public void setBackground(String background) {
        this.background = background;
    }
    
    public List<WorkingSession> getSessions() {
        return sessions;
    }
    
    public void setSessions(List<WorkingSession> sessions) {
        this.sessions = sessions;
    }
}
