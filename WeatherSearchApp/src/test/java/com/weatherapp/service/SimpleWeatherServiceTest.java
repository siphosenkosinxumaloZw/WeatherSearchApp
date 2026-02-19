package com.weatherapp.service;

import com.weatherapp.entity.Location;
import com.weatherapp.entity.WeatherSnapshot;
import com.weatherapp.repository.WeatherSnapshotRepository;
import com.weatherapp.client.OpenWeatherMapClient;
import com.weatherapp.dto.OpenWeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleWeatherServiceTest {

    private WeatherSnapshotRepository weatherRepository;
    private LocationServiceInterface locationService;
    private OpenWeatherMapClient weatherClient;
    private WeatherService weatherService;
    private String testApiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        // Create mock objects manually
        weatherRepository = mock(WeatherSnapshotRepository.class);
        locationService = mock(LocationServiceInterface.class);
        weatherClient = mock(OpenWeatherMapClient.class);
        
        // Create service instance manually
        weatherService = new WeatherService(weatherRepository, locationService, weatherClient, testApiKey);
        
        // Setup common test data
        Location testLocation = new Location("London", "GB", 51.5074, -0.1278);
        testLocation.setId(1L);
        
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(testLocation));
    }

    @Test
    void testGetCurrentWeatherSuccess() {
        // Arrange
        WeatherSnapshot expectedSnapshot = new WeatherSnapshot();
        expectedSnapshot.setId(1L);
        expectedSnapshot.setTemperature(20.0);
        
        when(weatherRepository.findTopByLocationIdOrderByTimestampDesc(1L))
            .thenReturn(Optional.of(expectedSnapshot));

        // Act
        WeatherSnapshot result = weatherService.getCurrentWeather(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(20.0, result.getTemperature());
        verify(weatherRepository).findTopByLocationIdOrderByTimestampDesc(1L);
    }

    @Test
    void testGetCurrentWeatherNotFound() {
        // Arrange
        when(weatherRepository.findTopByLocationIdOrderByTimestampDesc(1L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> weatherService.getCurrentWeather(1L));
        verify(weatherRepository).findTopByLocationIdOrderByTimestampDesc(1L);
    }

    @Test
    void testSyncWeatherDataSuccess() {
        // Arrange
        OpenWeatherResponse mockResponse = createMockWeatherResponse();
        
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockResponse);
        
        when(weatherRepository.save(any(WeatherSnapshot.class))).thenAnswer(invocation -> {
            WeatherSnapshot snapshot = invocation.getArgument(0);
            snapshot.setId(1L);
            return snapshot;
        });

        // Act
        WeatherSnapshot result = weatherService.syncWeatherData(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(15.5, result.getTemperature());
        assertEquals(65, result.getHumidity());
        assertEquals(1013, result.getPressure());
        assertEquals(5.2, result.getWindSpeed());
        assertEquals(230, result.getWindDirection());
        assertEquals(10000, result.getVisibility());
        assertEquals("Clouds", result.getWeatherMain());
        assertEquals("scattered clouds", result.getWeatherDescription());
        assertEquals("03d", result.getWeatherIcon());

        // Verify interactions
        verify(weatherClient).getCurrentWeatherByCoordinates(51.5074, -0.1278, testApiKey, "metric");
        verify(weatherRepository).save(any(WeatherSnapshot.class));
        verify(locationService).updateLastSyncTime(1L);
    }

    @Test
    void testSyncWeatherDataLocationNotFound() {
        // Arrange
        when(locationService.getLocationById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> weatherService.syncWeatherData(999L));
        
        // Verify no API calls were made
        verify(weatherClient, never()).getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString());
        verify(weatherRepository, never()).save(any(WeatherSnapshot.class));
    }

    @Test
    void testGetWeatherHistory() {
        // Arrange
        WeatherSnapshot snapshot1 = new WeatherSnapshot();
        WeatherSnapshot snapshot2 = new WeatherSnapshot();
        
        when(weatherRepository.findByLocationIdOrderByTimestampDesc(1L))
            .thenReturn(Arrays.asList(snapshot1, snapshot2));

        // Act
        List<WeatherSnapshot> result = weatherService.getWeatherHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(weatherRepository).findByLocationIdOrderByTimestampDesc(1L);
    }

    @Test
    void testGetWeatherHistorySince() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        WeatherSnapshot snapshot1 = new WeatherSnapshot();
        
        when(weatherRepository.findByLocationIdSince(eq(1L), eq(since)))
            .thenReturn(Arrays.asList(snapshot1));

        // Act
        List<WeatherSnapshot> result = weatherService.getWeatherHistorySince(1L, since);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(weatherRepository).findByLocationIdSince(1L, since);
    }

    @Test
    void testCleanupOldData() {
        // Arrange
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        location1.setId(1L);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        location2.setId(2L);
        
        when(locationService.getAllLocations()).thenReturn(Arrays.asList(location1, location2));

        // Act
        weatherService.cleanupOldData();

        // Assert
        verify(locationService).getAllLocations();
        verify(weatherRepository).deleteByLocationIdAndTimestampBefore(eq(1L), any(LocalDateTime.class));
        verify(weatherRepository).deleteByLocationIdAndTimestampBefore(eq(2L), any(LocalDateTime.class));
    }

    @Test
    void testSyncAllLocationsSuccess() {
        // Arrange
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        location1.setId(1L);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        location2.setId(2L);
        
        when(locationService.getAllLocations()).thenReturn(Arrays.asList(location1, location2));
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(location1));
        when(locationService.getLocationById(2L)).thenReturn(Optional.of(location2));
        
        OpenWeatherResponse mockResponse = createMockWeatherResponse();
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockResponse);
        when(weatherRepository.save(any(WeatherSnapshot.class))).thenReturn(new WeatherSnapshot());

        // Act
        weatherService.syncAllLocations();

        // Assert
        verify(locationService).getAllLocations();
        verify(locationService).getLocationById(1L);
        verify(locationService).getLocationById(2L);
        verify(weatherClient, times(2)).getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString());
        verify(weatherRepository, times(2)).save(any(WeatherSnapshot.class));
        verify(locationService, times(2)).updateLastSyncTime(anyLong());
    }

    @Test
    void testSyncAllLocationsWithErrors() {
        // Arrange
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        location1.setId(1L);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        location2.setId(2L);
        
        when(locationService.getAllLocations()).thenReturn(Arrays.asList(location1, location2));
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(location1));
        when(locationService.getLocationById(2L)).thenReturn(Optional.of(location2));
        
        OpenWeatherResponse mockResponse = createMockWeatherResponse();
        when(weatherClient.getCurrentWeatherByCoordinates(51.5074, -0.1278, testApiKey, "metric"))
            .thenReturn(mockResponse);
        when(weatherClient.getCurrentWeatherByCoordinates(48.8566, 2.3522, testApiKey, "metric"))
            .thenThrow(new RuntimeException("API Error"));
        when(weatherRepository.save(any(WeatherSnapshot.class))).thenReturn(new WeatherSnapshot());

        // Act & Assert - should not throw exception, should continue processing
        assertDoesNotThrow(() -> weatherService.syncAllLocations());

        // Verify that first location was processed but second failed
        verify(weatherClient, times(2)).getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString());
        verify(weatherRepository, times(1)).save(any(WeatherSnapshot.class));
        verify(locationService, times(1)).updateLastSyncTime(anyLong());
    }

    private OpenWeatherResponse createMockWeatherResponse() {
        OpenWeatherResponse response = new OpenWeatherResponse();
        response.setName("London");
        
        OpenWeatherResponse.Main main = new OpenWeatherResponse.Main();
        main.setTemp(15.5);
        main.setHumidity(65);
        main.setPressure(1013);
        response.setMain(main);

        OpenWeatherResponse.Weather weather = new OpenWeatherResponse.Weather();
        weather.setMain("Clouds");
        weather.setDescription("scattered clouds");
        weather.setIcon("03d");
        response.setWeather(Arrays.asList(weather));

        OpenWeatherResponse.Wind wind = new OpenWeatherResponse.Wind();
        wind.setSpeed(5.2);
        wind.setDeg(230);
        response.setWind(wind);
        
        response.setVisibility(10000);
        response.setDt(System.currentTimeMillis() / 1000);
        
        return response;
    }
}
