package com.weatherapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "locations")
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "City name is required")
    @Column(nullable = false)
    private String cityName;
    
    @NotBlank(message = "Country code is required")
    @Column(nullable = false, length = 2)
    private String countryCode;
    
    @NotNull(message = "Latitude is required")
    @Column(nullable = false)
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @Column(nullable = false)
    private Double longitude;
    
    @Column
    private String displayName;
    
    @Column
    private Boolean isFavorite = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime lastSyncAt;
    
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WeatherSnapshot> weatherSnapshots;
    
    public Location() {}
    
    public Location(String cityName, String countryCode, Double latitude, Double longitude) {
        this.cityName = cityName;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = cityName + ", " + countryCode;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public Boolean getIsFavorite() {
        return isFavorite;
    }
    
    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }
    
    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }
    
    public List<WeatherSnapshot> getWeatherSnapshots() {
        return weatherSnapshots;
    }
    
    public void setWeatherSnapshots(List<WeatherSnapshot> weatherSnapshots) {
        this.weatherSnapshots = weatherSnapshots;
    }
}
