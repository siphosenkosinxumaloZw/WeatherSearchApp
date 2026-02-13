package com.weatherapp.controller;

import com.weatherapp.dto.ForecastResponse;
import com.weatherapp.entity.WeatherSnapshot;
import com.weatherapp.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {
    
    private final WeatherService weatherService;
    
    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    
    @GetMapping("/current/{locationId}")
    public ResponseEntity<WeatherSnapshot> getCurrentWeather(@PathVariable Long locationId) {
        try {
            WeatherSnapshot weather = weatherService.getCurrentWeather(locationId);
            return ResponseEntity.ok(weather);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/history/{locationId}")
    public ResponseEntity<List<WeatherSnapshot>> getWeatherHistory(@PathVariable Long locationId) {
        List<WeatherSnapshot> history = weatherService.getWeatherHistory(locationId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/history/{locationId}/since")
    public ResponseEntity<List<WeatherSnapshot>> getWeatherHistorySince(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        List<WeatherSnapshot> history = weatherService.getWeatherHistorySince(locationId, since);
        return ResponseEntity.ok(history);
    }
    
    @PostMapping("/sync/{locationId}")
    public ResponseEntity<WeatherSnapshot> syncWeatherData(@PathVariable Long locationId) {
        try {
            WeatherSnapshot weather = weatherService.syncWeatherData(locationId);
            return ResponseEntity.ok(weather);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/sync/all")
    public ResponseEntity<Void> syncAllLocations() {
        weatherService.syncAllLocations();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/forecast/{locationId}")
    public ResponseEntity<ForecastResponse> getForecast(@PathVariable Long locationId) {
        try {
            ForecastResponse forecast = weatherService.getForecast(locationId);
            return ResponseEntity.ok(forecast);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupOldData() {
        weatherService.cleanupOldData();
        return ResponseEntity.ok().build();
    }
}
