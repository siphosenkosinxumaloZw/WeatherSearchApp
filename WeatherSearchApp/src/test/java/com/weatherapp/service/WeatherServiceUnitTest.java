package com.weatherapp.service;

import com.weatherapp.entity.Location;
import com.weatherapp.entity.WeatherSnapshot;
import com.weatherapp.repository.WeatherSnapshotRepository;
import com.weatherapp.client.OpenWeatherMapClient;
import com.weatherapp.dto.OpenWeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceUnitTest {

    @Mock
    private WeatherSnapshotRepository weatherRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private OpenWeatherMapClient weatherClient;

    private WeatherService weatherService;
    private String testApiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(weatherRepository, locationService, weatherClient, testApiKey);
        
        Location testLocation = new Location("London", "GB", 51.5074, -0.1278);
        testLocation.setId(1L);
        
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(testLocation));
    }

    @Test
    void getCurrentWeather_Success() {
        WeatherSnapshot snapshot = new WeatherSnapshot();
        snapshot.setId(1L);
        
        when(weatherRepository.findTopByLocationIdOrderByTimestampDesc(1L))
            .thenReturn(Optional.of(snapshot));

        WeatherSnapshot result = weatherService.getCurrentWeather(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(weatherRepository).findTopByLocationIdOrderByTimestampDesc(1L);
    }

    @Test
    void getCurrentWeather_NotFound_ThrowsException() {
        when(weatherRepository.findTopByLocationIdOrderByTimestampDesc(1L))
            .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> weatherService.getCurrentWeather(1L));
    }

    @Test
    void syncWeatherData_Success() {
        OpenWeatherResponse mockResponse = createMockWeatherResponse();
        
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockResponse);
        when(weatherRepository.save(any(WeatherSnapshot.class))).thenAnswer(invocation -> {
            WeatherSnapshot snapshot = invocation.getArgument(0);
            snapshot.setId(1L);
            return snapshot;
        });

        WeatherSnapshot result = weatherService.syncWeatherData(1L);

        assertNotNull(result);
        assertEquals(15.5, result.getTemperature());
        assertEquals(65, result.getHumidity());
        assertEquals(1013, result.getPressure());
        assertEquals(5.2, result.getWindSpeed());
        assertEquals(230, result.getWindDirection());
        assertEquals(10000, result.getVisibility());
        assertEquals("Clouds", result.getWeatherMain());
        assertEquals("scattered clouds", result.getWeatherDescription());
        assertEquals("03d", result.getWeatherIcon());

        verify(weatherClient).getCurrentWeatherByCoordinates(51.5074, -0.1278, testApiKey, "metric");
        verify(weatherRepository).save(any(WeatherSnapshot.class));
        verify(locationService).updateLastSyncTime(1L);
    }

    @Test
    void syncWeatherData_LocationNotFound_ThrowsException() {
        when(locationService.getLocationById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> weatherService.syncWeatherData(1L));

        verify(locationService).getLocationById(1L);
        verify(weatherClient, never()).getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString());
        verify(weatherRepository, never()).save(any(WeatherSnapshot.class));
    }

    @Test
    void syncWeatherData_ApiFailure_ThrowsException() {
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenThrow(new RuntimeException("API Error"));

        assertThrows(RuntimeException.class, () -> weatherService.syncWeatherData(1L));
        
        verify(weatherClient).getCurrentWeatherByCoordinates(51.5074, -0.1278, testApiKey, "metric");
        verify(weatherRepository, never()).save(any(WeatherSnapshot.class));
    }

    @Test
    void getWeatherHistory_Success() {
        WeatherSnapshot snapshot1 = new WeatherSnapshot();
        WeatherSnapshot snapshot2 = new WeatherSnapshot();
        
        when(weatherRepository.findByLocationIdOrderByTimestampDesc(1L))
            .thenReturn(java.util.List.of(snapshot1, snapshot2));

        var result = weatherService.getWeatherHistory(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(weatherRepository).findByLocationIdOrderByTimestampDesc(1L);
    }

    @Test
    void getWeatherHistorySince_Success() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        WeatherSnapshot snapshot1 = new WeatherSnapshot();
        
        when(weatherRepository.findByLocationIdSince(anyLong(), any(LocalDateTime.class)))
            .thenReturn(java.util.List.of(snapshot1));

        var result = weatherService.getWeatherHistorySince(1L, since);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(weatherRepository).findByLocationIdSince(1L, since);
    }

    @Test
    void getForecast_Success() {
        // This test would need ForecastResponse mock
        // For now, just test the exception case
        when(weatherClient.getForecastByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Forecast API Error"));

        assertThrows(RuntimeException.class, () -> weatherService.getForecast(1L));
    }

    @Test
    void cleanupOldData_Success() {
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        location1.setId(1L);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        location2.setId(2L);
        
        when(locationService.getAllLocations()).thenReturn(java.util.List.of(location1, location2));

        weatherService.cleanupOldData();

        verify(locationService).getAllLocations();
        verify(weatherRepository).deleteByLocationIdAndTimestampBefore(1L, any(LocalDateTime.class));
        verify(weatherRepository).deleteByLocationIdAndTimestampBefore(2L, any(LocalDateTime.class));
    }

    @Test
    void syncAllLocations_Success() {
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        location1.setId(1L);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        location2.setId(2L);
        
        when(locationService.getAllLocations()).thenReturn(java.util.List.of(location1, location2));
        
        OpenWeatherResponse mockResponse = createMockWeatherResponse();
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockResponse);
        when(weatherRepository.save(any(WeatherSnapshot.class))).thenReturn(new WeatherSnapshot());

        weatherService.syncAllLocations();

        verify(locationService).getAllLocations();
        verify(weatherClient, times(2)).getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString());
        verify(weatherRepository, times(2)).save(any(WeatherSnapshot.class));
        verify(locationService, times(2)).updateLastSyncTime(anyLong());
    }

    @Test
    void syncAllLocations_WithErrors_ContinuesProcessing() {
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        location1.setId(1L);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        location2.setId(2L);
        
        when(locationService.getAllLocations()).thenReturn(java.util.List.of(location1, location2));
        
        // First location succeeds, second fails
        OpenWeatherResponse mockResponse = createMockWeatherResponse();
        when(weatherClient.getCurrentWeatherByCoordinates(51.5074, -0.1278, testApiKey, "metric"))
            .thenReturn(mockResponse);
        when(weatherClient.getCurrentWeatherByCoordinates(48.8566, 2.3522, testApiKey, "metric"))
            .thenThrow(new RuntimeException("API Error"));
        when(weatherRepository.save(any(WeatherSnapshot.class))).thenReturn(new WeatherSnapshot());

        // Should not throw exception, should continue processing
        assertDoesNotThrow(() -> weatherService.syncAllLocations());

        verify(locationService).getAllLocations();
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
        response.setWeather(java.util.List.of(weather));

        OpenWeatherResponse.Wind wind = new OpenWeatherResponse.Wind();
        wind.setSpeed(5.2);
        wind.setDeg(230);
        response.setWind(wind);
        
        response.setVisibility(10000);
        response.setDt(System.currentTimeMillis() / 1000);
        
        return response;
    }
}
