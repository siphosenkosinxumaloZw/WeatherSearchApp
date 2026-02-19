package com.weatherapp.service;

import com.weatherapp.entity.Location;

import java.util.List;
import java.util.Optional;

public interface LocationServiceInterface {
    
    List<Location> getAllLocations();
    
    Optional<Location> getLocationById(Long id);
    
    List<Location> getFavoriteLocations();
    
    List<Location> searchLocations(String searchTerm);
    
    Location addLocation(String cityName, String countryCode, Double latitude, Double longitude, String displayName, Boolean isFavorite);
    
    Location updateLocation(Long id, Location locationDetails);
    
    void deleteLocation(Long id);
    
    Location toggleFavorite(Long id);
    
    void updateLastSyncTime(Long locationId);
    
    List<Location> getLocationsNeedingSync();
}
