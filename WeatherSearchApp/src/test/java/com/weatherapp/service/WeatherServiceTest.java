package com.weatherapp.service;

import com.weatherapp.entity.Location;
import com.weatherapp.entity.WeatherSnapshot;
import com.weatherapp.repository.WeatherSnapshotRepository;
import com.weatherapp.client.OpenWeatherMapClient;
import com.weatherapp.dto.OpenWeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherSnapshotRepository weatherRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private OpenWeatherMapClient weatherClient;

    @InjectMocks
    private WeatherService weatherService;

    private Location testLocation;
    private OpenWeatherResponse mockWeatherResponse;
    private String testApiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        testLocation = new Location("London", "GB", 51.5074, -0.1278);
        testLocation.setId(1L);

        mockWeatherResponse = new OpenWeatherResponse();
        mockWeatherResponse.setName("London");
        
        OpenWeatherResponse.Main main = new OpenWeatherResponse.Main();
        main.setTemp(15.5);
        main.setHumidity(65);
        main.setPressure(1013);
        mockWeatherResponse.setMain(main);

        OpenWeatherResponse.Weather weather = new OpenWeatherResponse.Weather();
        weather.setMain("Clouds");
        weather.setDescription("scattered clouds");
        weather.setIcon("03d");
        mockWeatherResponse.setWeather(java.util.List.of(weather));

        OpenWeatherResponse.Wind wind = new OpenWeatherResponse.Wind();
        wind.setSpeed(5.2);
        wind.setDeg(230);
        mockWeatherResponse.setWind(wind);

        mockWeatherResponse.setVisibility(10000);
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
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(testLocation));
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);
        when(weatherRepository.save(any(WeatherSnapshot.class))).thenAnswer(invocation -> {
            WeatherSnapshot snapshot = invocation.getArgument(0);
            snapshot.setId(1L);
            return snapshot;
        });

        WeatherSnapshot result = weatherService.syncWeatherData(1L);

        assertNotNull(result);
        assertEquals(testLocation, result.getLocation());
        assertEquals(15.5, result.getTemperature());
        assertEquals(65, result.getHumidity());
        assertEquals(1013, result.getPressure());
        assertEquals(5.2, result.getWindSpeed());
        assertEquals(230, result.getWindDirection());
        assertEquals(10000, result.getVisibility());
        assertEquals("Clouds", result.getWeatherMain());
        assertEquals("scattered clouds", result.getWeatherDescription());
        assertEquals("03d", result.getWeatherIcon());

        verify(locationService).getLocationById(1L);
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
}
