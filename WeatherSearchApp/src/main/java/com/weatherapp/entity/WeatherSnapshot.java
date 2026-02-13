package com.weatherapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_snapshots")
public class WeatherSnapshot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @NotNull
    @Column(nullable = false)
    private Double temperature;
    
    @NotNull
    @Column(nullable = false)
    private Integer humidity;
    
    @NotNull
    @Column(nullable = false)
    private Double pressure;
    
    @Column
    private Double windSpeed;
    
    @Column
    private Integer windDirection;
    
    @Column
    private Integer visibility;
    
    @Column
    private Double uvIndex;
    
    @Column(length = 50)
    private String weatherMain;
    
    @Column(length = 200)
    private String weatherDescription;
    
    @Column
    private String weatherIcon;
    
    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Column
    private LocalDateTime dataTimestamp;
    
    public WeatherSnapshot() {}
    
    public WeatherSnapshot(Location location, Double temperature, Integer humidity, Double pressure) {
        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Integer getHumidity() {
        return humidity;
    }
    
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }
    
    public Double getPressure() {
        return pressure;
    }
    
    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }
    
    public Double getWindSpeed() {
        return windSpeed;
    }
    
    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }
    
    public Integer getWindDirection() {
        return windDirection;
    }
    
    public void setWindDirection(Integer windDirection) {
        this.windDirection = windDirection;
    }
    
    public Integer getVisibility() {
        return visibility;
    }
    
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
    
    public Double getUvIndex() {
        return uvIndex;
    }
    
    public void setUvIndex(Double uvIndex) {
        this.uvIndex = uvIndex;
    }
    
    public String getWeatherMain() {
        return weatherMain;
    }
    
    public void setWeatherMain(String weatherMain) {
        this.weatherMain = weatherMain;
    }
    
    public String getWeatherDescription() {
        return weatherDescription;
    }
    
    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }
    
    public String getWeatherIcon() {
        return weatherIcon;
    }
    
    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public LocalDateTime getDataTimestamp() {
        return dataTimestamp;
    }
    
    public void setDataTimestamp(LocalDateTime dataTimestamp) {
        this.dataTimestamp = dataTimestamp;
    }
}
