package com.weatherapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String userId = "default";
    
    @Column
    private String temperatureUnit = "celsius";
    
    @Column
    private String windSpeedUnit = "kmh";
    
    @Column
    private String pressureUnit = "hPa";
    
    @Column
    private Integer refreshInterval = 30;
    
    @Column
    private Boolean autoRefresh = false;
    
    @Column
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public UserPreferences() {}
    
    public UserPreferences(String userId) {
        this.userId = userId;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTemperatureUnit() {
        return temperatureUnit;
    }
    
    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }
    
    public String getWindSpeedUnit() {
        return windSpeedUnit;
    }
    
    public void setWindSpeedUnit(String windSpeedUnit) {
        this.windSpeedUnit = windSpeedUnit;
    }
    
    public String getPressureUnit() {
        return pressureUnit;
    }
    
    public void setPressureUnit(String pressureUnit) {
        this.pressureUnit = pressureUnit;
    }
    
    public Integer getRefreshInterval() {
        return refreshInterval;
    }
    
    public void setRefreshInterval(Integer refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
    
    public Boolean getAutoRefresh() {
        return autoRefresh;
    }
    
    public void setAutoRefresh(Boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
