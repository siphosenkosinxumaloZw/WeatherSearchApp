package com.weatherapp.service;

import com.weatherapp.dto.OpenWeatherResponse;
import com.weatherapp.entity.Location;
import com.weatherapp.repository.LocationRepository;
import com.weatherapp.client.OpenWeatherMapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService implements LocationServiceInterface {
    
    private final LocationRepository locationRepository;
    private final OpenWeatherMapClient weatherClient;
    private final String apiKey;
    
    @Autowired
    public LocationService(LocationRepository locationRepository, 
                          OpenWeatherMapClient weatherClient,
                          String apiKey) {
        this.locationRepository = locationRepository;
        this.weatherClient = weatherClient;
        this.apiKey = apiKey;
    }
    
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
    
    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }
    
    public List<Location> getFavoriteLocations() {
        return locationRepository.findByIsFavoriteTrue();
    }
    
    public List<Location> searchLocations(String searchTerm) {
        return locationRepository.findBySearchTerm(searchTerm);
    }
    
    @Transactional
    public Location addLocation(String cityName, String countryCode, Double latitude, Double longitude, String displayName, Boolean isFavorite) {
        if (locationRepository.existsByCityNameAndCountryCode(cityName, countryCode)) {
            throw new IllegalArgumentException("Location already exists");
        }

        Location location = new Location();

        // If latitude and longitude are provided, use them directly
        if (latitude != null && longitude != null) {
            location.setCityName(cityName);
            location.setCountryCode(countryCode);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setDisplayName(displayName != null ? displayName : cityName + ", " + countryCode);
            location.setIsFavorite(isFavorite != null ? isFavorite : false);
        } else {
            // Otherwise, fetch from the API
            try {
                OpenWeatherResponse weatherResponse = weatherClient.getCurrentWeather(
                    cityName + "," + countryCode, apiKey, "metric");

                location.setCityName(weatherResponse.getName());
                location.setCountryCode(weatherResponse.getSys().getCountry());
                location.setLatitude(weatherResponse.getCoord().getLat());
                location.setLongitude(weatherResponse.getCoord().getLon());
                location.setDisplayName(displayName != null ? displayName : weatherResponse.getName() + ", " + weatherResponse.getSys().getCountry());
                location.setIsFavorite(isFavorite != null ? isFavorite : false);
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch location data from API: " + e.getMessage(), e);
            }
        }

        return locationRepository.save(location);
    }
    
    @Transactional
    public Location updateLocation(Long id, Location locationDetails) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
        
        if (locationDetails.getDisplayName() != null) {
            location.setDisplayName(locationDetails.getDisplayName());
        }
        if (locationDetails.getIsFavorite() != null) {
            location.setIsFavorite(locationDetails.getIsFavorite());
        }
        
        return locationRepository.save(location);
    }
    
    
    @Transactional
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new IllegalArgumentException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }
    
    @Transactional
    public Location toggleFavorite(Long id) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + id));
        
        location.setIsFavorite(!location.getIsFavorite());
        return locationRepository.save(location);
    }
    
    @Transactional
    public void updateLastSyncTime(Long locationId) {
        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + locationId));
        
        location.setLastSyncAt(LocalDateTime.now());
        locationRepository.save(location);
    }
    
    public List<Location> getLocationsNeedingSync() {
        return locationRepository.findLocationsNeedingSync();
    }
}
