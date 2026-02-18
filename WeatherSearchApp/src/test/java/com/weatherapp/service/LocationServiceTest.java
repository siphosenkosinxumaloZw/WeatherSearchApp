package com.weatherapp.service;

import com.weatherapp.entity.Location;
import com.weatherapp.repository.LocationRepository;
import com.weatherapp.client.OpenWeatherMapClient;
import com.weatherapp.dto.OpenWeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class LocationServiceTest {

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private OpenWeatherMapClient weatherClient;

    @Autowired
    private LocationService locationService;

    private OpenWeatherResponse mockWeatherResponse;
    private String testApiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        mockWeatherResponse = new OpenWeatherResponse();
        mockWeatherResponse.setName("London");
        
        OpenWeatherResponse.Sys sys = new OpenWeatherResponse.Sys();
        sys.setCountry("GB");
        mockWeatherResponse.setSys(sys);
        
        OpenWeatherResponse.Coord coord = new OpenWeatherResponse.Coord();
        coord.setLat(51.5074);
        coord.setLon(-0.1278);
        mockWeatherResponse.setCoord(coord);
    }

    @Test
    void addLocation_Success() {
        when(locationRepository.existsByCityNameAndCountryCode("London", "GB")).thenReturn(false);
        when(weatherClient.getCurrentWeather(anyString(), anyString(), anyString())).thenReturn(mockWeatherResponse);
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> {
            Location location = invocation.getArgument(0);
            location.setId(1L);
            return location;
        });

        Location result = locationService.addLocation("London", "GB");

        assertNotNull(result);
        assertEquals("London", result.getCityName());
        assertEquals("GB", result.getCountryCode());
        assertEquals(51.5074, result.getLatitude());
        assertEquals(-0.1278, result.getLongitude());
        assertEquals("London, GB", result.getDisplayName());
        
        verify(locationRepository).save(any(Location.class));
        verify(weatherClient).getCurrentWeather("London,GB", testApiKey, "metric");
    }

    @Test
    void addLocation_AlreadyExists_ThrowsException() {
        when(locationRepository.existsByCityNameAndCountryCode("London", "GB")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            locationService.addLocation("London", "GB");
        });

        verify(locationRepository, never()).save(any(Location.class));
        verify(weatherClient, never()).getCurrentWeather(anyString(), anyString(), anyString());
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
    void toggleFavorite_Success() {
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        location.setIsFavorite(false);
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Location result = locationService.toggleFavorite(1L);

        assertTrue(result.getIsFavorite());
        verify(locationRepository).save(location);
    }

    @Test
    void deleteLocation_Success() {
        when(locationRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> locationService.deleteLocation(1L));
        
        verify(locationRepository).deleteById(1L);
    }

    @Test
    void deleteLocation_NotFound_ThrowsException() {
        when(locationRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> locationService.deleteLocation(1L));
        
        verify(locationRepository, never()).deleteById(1L);
    }
}
