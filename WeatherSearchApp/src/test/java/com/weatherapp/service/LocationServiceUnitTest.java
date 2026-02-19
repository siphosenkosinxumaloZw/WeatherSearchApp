package com.weatherapp.service;

import com.weatherapp.entity.Location;
import com.weatherapp.repository.LocationRepository;
import com.weatherapp.client.OpenWeatherMapClient;
import com.weatherapp.dto.OpenWeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceUnitTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private OpenWeatherMapClient weatherClient;

    private LocationService locationService;
    private String testApiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        locationService = new LocationService(locationRepository, weatherClient, testApiKey);
    }

    @Test
    void getAllLocations_Success() {
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        
        when(locationRepository.findAll()).thenReturn(List.of(location1, location2));

        List<Location> result = locationService.getAllLocations();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("London", result.get(0).getCityName());
        assertEquals("Paris", result.get(1).getCityName());
        verify(locationRepository).findAll();
    }

    @Test
    void getLocationById_Found() {
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        Optional<Location> result = locationService.getLocationById(1L);

        assertTrue(result.isPresent());
        assertEquals("London", result.get().getCityName());
        verify(locationRepository).findById(1L);
    }

    @Test
    void getLocationById_NotFound() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Location> result = locationService.getLocationById(1L);

        assertFalse(result.isPresent());
        verify(locationRepository).findById(1L);
    }

    @Test
    void getFavoriteLocations_Success() {
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        location1.setIsFavorite(true);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        location2.setIsFavorite(true);
        
        when(locationRepository.findByIsFavoriteTrue()).thenReturn(List.of(location1, location2));

        List<Location> result = locationService.getFavoriteLocations();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getIsFavorite());
        assertTrue(result.get(1).getIsFavorite());
        verify(locationRepository).findByIsFavoriteTrue();
    }

    @Test
    void searchLocations_Success() {
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        Location location2 = new Location("Londonderry", "GB", 54.9966, -7.3086);
        
        when(locationRepository.findBySearchTerm("Lon")).thenReturn(List.of(location1, location2));

        List<Location> result = locationService.searchLocations("Lon");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCityName().contains("Lon"));
        assertTrue(result.get(1).getCityName().contains("Lon"));
        verify(locationRepository).findBySearchTerm("Lon");
    }

    @Test
    void addLocation_Success() {
        OpenWeatherResponse mockResponse = createMockWeatherResponse();
        
        when(locationRepository.existsByCityNameAndCountryCode("London", "GB")).thenReturn(false);
        when(weatherClient.getCurrentWeather("London,GB", testApiKey, "metric")).thenReturn(mockResponse);
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> {
            Location location = invocation.getArgument(0);
            location.setId(1L);
            return location;
        });

        Location result = locationService.addLocation("London", "GB",51.5074, -0.1278, "London", true);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("London", result.getCityName());
        assertEquals("GB", result.getCountryCode());
        assertEquals(51.5074, result.getLatitude());
        assertEquals(-0.1278, result.getLongitude());
        assertEquals("London, GB", result.getDisplayName());
        
        verify(locationRepository).existsByCityNameAndCountryCode("London", "GB");
        verify(weatherClient).getCurrentWeather("London,GB", testApiKey, "metric");
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void addLocation_AlreadyExists_ThrowsException() {
        when(locationRepository.existsByCityNameAndCountryCode("London", "GB")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            locationService.addLocation("London", "GB",51.5074, -0.1278, "London", true);
        });

        verify(locationRepository).existsByCityNameAndCountryCode("London", "GB");
        verify(weatherClient, never()).getCurrentWeather(anyString(), anyString(), anyString());
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void addLocation_ApiFailure_ThrowsException() {
        when(locationRepository.existsByCityNameAndCountryCode("London", "GB")).thenReturn(false);
        when(weatherClient.getCurrentWeather("London,GB", testApiKey, "metric"))
            .thenThrow(new RuntimeException("API Error"));

        assertThrows(RuntimeException.class, () -> {
            locationService.addLocation("London", "GB",51.5074, -0.1278, "London", true);
        });

        verify(locationRepository).existsByCityNameAndCountryCode("London", "GB");
        verify(weatherClient).getCurrentWeather("London,GB", testApiKey, "metric");
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void updateLocation_Success() {
        Location existingLocation = new Location("London", "GB", 51.5074, -0.1278);
        existingLocation.setId(1L);
        existingLocation.setDisplayName("London");
        existingLocation.setIsFavorite(false);
        
        Location updateDetails = new Location();
        updateDetails.setDisplayName("London, UK");
        updateDetails.setIsFavorite(true);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Location result = locationService.updateLocation(1L, updateDetails);

        assertNotNull(result);
        assertEquals("London, UK", result.getDisplayName());
        assertTrue(result.getIsFavorite());
        assertEquals("London", result.getCityName()); // Should remain unchanged
        
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(existingLocation);
    }

    @Test
    void updateLocation_NotFound_ThrowsException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        Location updateDetails = new Location();
        updateDetails.setDisplayName("London, UK");

        assertThrows(IllegalArgumentException.class, () -> {
            locationService.updateLocation(1L, updateDetails);
        });

        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void deleteLocation_Success() {
        when(locationRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> locationService.deleteLocation(1L));
        
        verify(locationRepository).existsById(1L);
        verify(locationRepository).deleteById(1L);
    }

    @Test
    void deleteLocation_NotFound_ThrowsException() {
        when(locationRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> locationService.deleteLocation(1L));
        
        verify(locationRepository).existsById(1L);
        verify(locationRepository, never()).deleteById(anyLong());
    }

    @Test
    void toggleFavorite_Success_FromFalseToTrue() {
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        location.setIsFavorite(false);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Location result = locationService.toggleFavorite(1L);

        assertNotNull(result);
        assertTrue(result.getIsFavorite());
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(location);
    }

    @Test
    void toggleFavorite_Success_FromTrueToFalse() {
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        location.setIsFavorite(true);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Location result = locationService.toggleFavorite(1L);

        assertNotNull(result);
        assertFalse(result.getIsFavorite());
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(location);
    }

    @Test
    void toggleFavorite_NotFound_ThrowsException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> locationService.toggleFavorite(1L));
        
        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void updateLastSyncTime_Success() {
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> locationService.updateLastSyncTime(1L));
        
        assertNotNull(location.getLastSyncAt());
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(location);
    }

    @Test
    void updateLastSyncTime_NotFound_ThrowsException() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> locationService.updateLastSyncTime(1L));
        
        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void getLocationsNeedingSync_Success() {
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        
        when(locationRepository.findLocationsNeedingSync()).thenReturn(List.of(location1, location2));

        List<Location> result = locationService.getLocationsNeedingSync();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(locationRepository).findLocationsNeedingSync();
    }

    private OpenWeatherResponse createMockWeatherResponse() {
        OpenWeatherResponse response = new OpenWeatherResponse();
        response.setName("London");
        
        OpenWeatherResponse.Sys sys = new OpenWeatherResponse.Sys();
        sys.setCountry("GB");
        response.setSys(sys);
        
        OpenWeatherResponse.Coord coord = new OpenWeatherResponse.Coord();
        coord.setLat(51.5074);
        coord.setLon(-0.1278);
        response.setCoord(coord);
        
        return response;
    }
}
