package com.weatherapp.service;

import com.weatherapp.dto.ForecastResponse;
import com.weatherapp.dto.OpenWeatherResponse;
import com.weatherapp.entity.Location;
import com.weatherapp.entity.WeatherSnapshot;
import com.weatherapp.repository.WeatherSnapshotRepository;
import com.weatherapp.client.OpenWeatherMapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WeatherService {
    
    private final WeatherSnapshotRepository weatherRepository;
    private final LocationService locationService;
    private final OpenWeatherMapClient weatherClient;
    private final String apiKey;
    
    @Autowired
    public WeatherService(WeatherSnapshotRepository weatherRepository,
                         LocationService locationService,
                         OpenWeatherMapClient weatherClient,
                         String apiKey) {
        this.weatherRepository = weatherRepository;
        this.locationService = locationService;
        this.weatherClient = weatherClient;
        this.apiKey = apiKey;
    }
    
    public WeatherSnapshot getCurrentWeather(Long locationId) {
        return weatherRepository.findTopByLocationIdOrderByTimestampDesc(locationId)
            .orElseThrow(() -> new IllegalArgumentException("No weather data found for location"));
    }
    
    public List<WeatherSnapshot> getWeatherHistory(Long locationId) {
        return weatherRepository.findByLocationIdOrderByTimestampDesc(locationId);
    }
    
    public List<WeatherSnapshot> getWeatherHistorySince(Long locationId, LocalDateTime since) {
        return weatherRepository.findByLocationIdSince(locationId, since);
    }
    
    public WeatherSnapshot syncWeatherData(Long locationId) {
        Location location = locationService.getLocationById(locationId)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + locationId));
        
        try {
            OpenWeatherResponse weatherResponse = weatherClient.getCurrentWeatherByCoordinates(
                location.getLatitude(), location.getLongitude(), apiKey, "metric");
            
            WeatherSnapshot snapshot = convertToWeatherSnapshot(weatherResponse, location);
            snapshot = weatherRepository.save(snapshot);
            
            locationService.updateLastSyncTime(locationId);
            
            return snapshot;
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync weather data: " + e.getMessage(), e);
        }
    }
    
    public ForecastResponse getForecast(Long locationId) {
        Location location = locationService.getLocationById(locationId)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + locationId));
        
        try {
            return weatherClient.getForecastByCoordinates(
                location.getLatitude(), location.getLongitude(), apiKey, "metric");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch forecast data: " + e.getMessage(), e);
        }
    }
    
    public void cleanupOldData() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Location> allLocations = locationService.getAllLocations();
        
        for (Location location : allLocations) {
            weatherRepository.deleteByLocationIdAndTimestampBefore(location.getId(), cutoff);
        }
    }
    
    private WeatherSnapshot convertToWeatherSnapshot(OpenWeatherResponse response, Location location) {
        WeatherSnapshot snapshot = new WeatherSnapshot();
        snapshot.setLocation(location);
        snapshot.setTemperature(response.getMain().getTemp());
        snapshot.setHumidity(response.getMain().getHumidity());
        snapshot.setPressure(response.getMain().getPressure());
        
        if (response.getWind() != null) {
            snapshot.setWindSpeed(response.getWind().getSpeed());
            snapshot.setWindDirection(response.getWind().getDeg());
        }
        
        if (response.getVisibility() != null) {
            snapshot.setVisibility(response.getVisibility());
        }
        
        if (response.getWeather() != null && !response.getWeather().isEmpty()) {
            var weather = response.getWeather().get(0);
            snapshot.setWeatherMain(weather.getMain());
            snapshot.setWeatherDescription(weather.getDescription());
            snapshot.setWeatherIcon(weather.getIcon());
        }
        
        snapshot.setDataTimestamp(response.getDataTimestamp());
        snapshot.setTimestamp(LocalDateTime.now());
        
        return snapshot;
    }
    
    public void syncAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        for (Location location : locations) {
            try {
                syncWeatherData(location.getId());
            } catch (Exception e) {
                System.err.println("Failed to sync weather for location " + location.getId() + ": " + e.getMessage());
            }
        }
    }
}
