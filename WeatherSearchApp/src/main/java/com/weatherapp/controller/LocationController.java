package com.weatherapp.controller;

import com.weatherapp.entity.Location;
import com.weatherapp.service.LocationServiceInterface;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {
    
    private final LocationServiceInterface locationService;

    @Autowired
    public LocationController(LocationServiceInterface locationService) {
        this.locationService = locationService;
    }
    
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        Optional<Location> location = locationService.getLocationById(id);
        return location.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/favorites")
    public ResponseEntity<List<Location>> getFavoriteLocations() {
        List<Location> favorites = locationService.getFavoriteLocations();
        return ResponseEntity.ok(favorites);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Location>> searchLocations(@RequestParam String q) {
        List<Location> results = locationService.searchLocations(q);
        return ResponseEntity.ok(results);
    }
    
    @PostMapping
    public ResponseEntity<Location> addLocation(@Valid @RequestBody LocationRequest request) {
        try {
            Location location = locationService.addLocation(
                request.getCityName(),
                request.getCountryCode(),
                request.getLatitude(),
                request.getLongitude(),
                request.getDisplayName(),
                request.getIsFavorite()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(location);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id,
                                                   @RequestBody Location locationDetails) {
        try {
            Location updatedLocation = locationService.updateLocation(id, locationDetails);
            return ResponseEntity.ok(updatedLocation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        try {
            locationService.deleteLocation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/toggle-favorite")
    public ResponseEntity<Location> toggleFavorite(@PathVariable Long id) {
        try {
            Location location = locationService.toggleFavorite(id);
            return ResponseEntity.ok(location);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    public static class LocationRequest {
        @NotBlank(message = "City name is required")
        private String cityName;

        @NotBlank(message = "Country code is required")
        private String countryCode;

        private Double latitude;
        private Double longitude;
        private String displayName;
        private Boolean isFavorite;

        public String getCityName() { return cityName; }
        public void setCityName(String cityName) { this.cityName = cityName; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public Boolean getIsFavorite() { return isFavorite; }
        public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }
    }
}
