package com.weatherapp.service;

import com.weatherapp.entity.Location;
import com.weatherapp.repository.LocationRepository;
import com.weatherapp.client.OpenWeatherMapClient;
import com.weatherapp.dto.OpenWeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleLocationServiceTest {

    private LocationRepository locationRepository;
    private OpenWeatherMapClient weatherClient;
    private LocationService locationService;
    private String testApiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        // Create mock objects manually
        locationRepository = mock(LocationRepository.class);
        weatherClient = mock(OpenWeatherMapClient.class);
        
        // Create service instance manually
        locationService = new LocationService(locationRepository, weatherClient, testApiKey);
    }

    @Test
    void testGetAllLocations() {
        // Arrange
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));

        // Act
        List<Location> result = locationService.getAllLocations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("London", result.get(0).getCityName());
        assertEquals("Paris", result.get(1).getCityName());
        verify(locationRepository).findAll();
    }

    @Test
    void testGetLocationByIdFound() {
        // Arrange
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        // Act
        Optional<Location> result = locationService.getLocationById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("London", result.get().getCityName());
        verify(locationRepository).findById(1L);
    }

    @Test
    void testGetLocationByIdNotFound() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Location> result = locationService.getLocationById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(locationRepository).findById(1L);
    }

    @Test
    void testGetFavoriteLocations() {
        // Arrange
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        location1.setIsFavorite(true);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        location2.setIsFavorite(true);
        
        when(locationRepository.findByIsFavoriteTrue()).thenReturn(Arrays.asList(location1, location2));

        // Act
        List<Location> result = locationService.getFavoriteLocations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getIsFavorite());
        assertTrue(result.get(1).getIsFavorite());
        verify(locationRepository).findByIsFavoriteTrue();
    }

    @Test
    void testSearchLocations() {
        // Arrange
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        Location location2 = new Location("Londonderry", "GB", 54.9966, -7.3086);
        
        when(locationRepository.findBySearchTerm("Lon")).thenReturn(Arrays.asList(location1, location2));

        // Act
        List<Location> result = locationService.searchLocations("Lon");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCityName().contains("Lon"));
        assertTrue(result.get(1).getCityName().contains("Lon"));
        verify(locationRepository).findBySearchTerm("Lon");
    }

    @Test
    void testAddLocationSuccess() {
        // Arrange
        OpenWeatherResponse mockResponse = createMockWeatherResponse();
        
        when(locationRepository.existsByCityNameAndCountryCode("London", "GB")).thenReturn(false);
        when(weatherClient.getCurrentWeather("London,GB", testApiKey, "metric")).thenReturn(mockResponse);
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> {
            Location location = invocation.getArgument(0);
            location.setId(1L);
            return location;
        });

        // Act
        Location result = locationService.addLocation("London", "GB");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("London", result.getCityName());
        assertEquals("GB", result.getCountryCode());
        assertEquals(51.5074, result.getLatitude());
        assertEquals(-0.1278, result.getLongitude());
        assertEquals("London, GB", result.getDisplayName());
        
        // Verify interactions
        verify(locationRepository).existsByCityNameAndCountryCode("London", "GB");
        verify(weatherClient).getCurrentWeather("London,GB", testApiKey, "metric");
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void testAddLocationAlreadyExists() {
        // Arrange
        when(locationRepository.existsByCityNameAndCountryCode("London", "GB")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.addLocation("London", "GB");
        });

        // Verify no API calls were made
        verify(locationRepository).existsByCityNameAndCountryCode("London", "GB");
        verify(weatherClient, never()).getCurrentWeather(anyString(), anyString(), anyString());
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void testAddLocationApiFailure() {
        // Arrange
        when(locationRepository.existsByCityNameAndCountryCode("London", "GB")).thenReturn(false);
        when(weatherClient.getCurrentWeather("London,GB", testApiKey, "metric"))
            .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            locationService.addLocation("London", "GB");
        });

        // Verify interactions
        verify(locationRepository).existsByCityNameAndCountryCode("London", "GB");
        verify(weatherClient).getCurrentWeather("London,GB", testApiKey, "metric");
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void testUpdateLocationSuccess() {
        // Arrange
        Location existingLocation = new Location("London", "GB", 51.5074, -0.1278);
        existingLocation.setId(1L);
        existingLocation.setDisplayName("London");
        existingLocation.setIsFavorite(false);
        
        Location updateDetails = new Location();
        updateDetails.setDisplayName("London, UK");
        updateDetails.setIsFavorite(true);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Location result = locationService.updateLocation(1L, updateDetails);

        // Assert
        assertNotNull(result);
        assertEquals("London, UK", result.getDisplayName());
        assertTrue(result.getIsFavorite());
        assertEquals("London", result.getCityName()); // Should remain unchanged
        
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(existingLocation);
    }

    @Test
    void testUpdateLocationNotFound() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        Location updateDetails = new Location();
        updateDetails.setDisplayName("London, UK");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.updateLocation(1L, updateDetails);
        });

        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void testDeleteLocationSuccess() {
        // Arrange
        when(locationRepository.existsById(1L)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> locationService.deleteLocation(1L));
        
        verify(locationRepository).existsById(1L);
        verify(locationRepository).deleteById(1L);
    }

    @Test
    void testDeleteLocationNotFound() {
        // Arrange
        when(locationRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> locationService.deleteLocation(1L));
        
        verify(locationRepository).existsById(1L);
        verify(locationRepository, never()).deleteById(anyLong());
    }

    @Test
    void testToggleFavoriteFromFalseToTrue() {
        // Arrange
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        location.setIsFavorite(false);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Location result = locationService.toggleFavorite(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsFavorite());
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(location);
    }

    @Test
    void testToggleFavoriteFromTrueToFalse() {
        // Arrange
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        location.setIsFavorite(true);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Location result = locationService.toggleFavorite(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsFavorite());
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(location);
    }

    @Test
    void testToggleFavoriteNotFound() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> locationService.toggleFavorite(1L));
        
        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void testUpdateLastSyncTimeSuccess() {
        // Arrange
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() -> locationService.updateLastSyncTime(1L));
        
        // Verify sync time was updated
        assertNotNull(location.getLastSyncAt());
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(location);
    }

    @Test
    void testUpdateLastSyncTimeNotFound() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> locationService.updateLastSyncTime(1L));
        
        verify(locationRepository).findById(1L);
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void testGetLocationsNeedingSync() {
        // Arrange
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        
        when(locationRepository.findLocationsNeedingSync()).thenReturn(Arrays.asList(location1, location2));

        // Act
        List<Location> result = locationService.getLocationsNeedingSync();

        // Assert
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
