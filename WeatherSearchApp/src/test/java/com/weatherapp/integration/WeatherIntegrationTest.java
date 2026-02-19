package com.weatherapp.integration;

import com.weatherapp.entity.Location;
import com.weatherapp.entity.WeatherSnapshot;
import com.weatherapp.repository.LocationRepository;
import com.weatherapp.repository.WeatherSnapshotRepository;
import com.weatherapp.service.LocationService;
import com.weatherapp.service.WeatherService;
import com.weatherapp.client.OpenWeatherMapClient;
import com.weatherapp.dto.OpenWeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WeatherIntegrationTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private WeatherSnapshotRepository weatherRepository;

    @MockBean
    private OpenWeatherMapClient weatherClient;

    private OpenWeatherResponse mockWeatherResponse;

    @BeforeEach
    void setUp() {
        weatherRepository.deleteAll();
        locationRepository.deleteAll();
        
        mockWeatherResponse = createMockWeatherResponse();
    }

    @Test
    void testCompleteWeatherWorkflow() {
        // Step 1: Add a new location
        when(weatherClient.getCurrentWeather(eq("London,GB"), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        Location location = locationService.addLocation("London", "GB");

        assertNotNull(location);
        assertNotNull(location.getId());
        assertEquals("London", location.getCityName());
        assertEquals("GB", location.getCountryCode());
        assertEquals(51.5074, location.getLatitude());
        assertEquals(-0.1278, location.getLongitude());

        verify(weatherClient).getCurrentWeather(eq("London,GB"), anyString(), anyString());

        // Step 2: Sync weather data for the location
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        WeatherSnapshot snapshot = weatherService.syncWeatherData(location.getId());

        assertNotNull(snapshot);
        assertNotNull(snapshot.getId());
        assertEquals(location, snapshot.getLocation());
        assertEquals(15.5, snapshot.getTemperature());
        assertEquals(65, snapshot.getHumidity());
        assertEquals(1013, snapshot.getPressure());
        assertEquals(5.2, snapshot.getWindSpeed());
        assertEquals(230, snapshot.getWindDirection());
        assertEquals(10000, snapshot.getVisibility());
        assertEquals("Clouds", snapshot.getWeatherMain());
        assertEquals("scattered clouds", snapshot.getWeatherDescription());
        assertEquals("03d", snapshot.getWeatherIcon());

        verify(weatherClient).getCurrentWeatherByCoordinates(
            eq(51.5074), eq(-0.1278), anyString(), eq("metric"));

        // Step 3: Retrieve current weather
        WeatherSnapshot currentWeather = weatherService.getCurrentWeather(location.getId());

        assertNotNull(currentWeather);
        assertEquals(snapshot.getId(), currentWeather.getId());
        assertEquals(snapshot.getTemperature(), currentWeather.getTemperature());

        // Step 4: Get weather history
        List<WeatherSnapshot> history = weatherService.getWeatherHistory(location.getId());

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(snapshot.getId(), history.get(0).getId());

        // Step 5: Get weather history since a specific time
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<WeatherSnapshot> recentHistory = weatherService.getWeatherHistorySince(location.getId(), since);

        assertNotNull(recentHistory);
        assertEquals(1, recentHistory.size());
    }

    @Test
    void testMultipleLocationsManagement() {
        // Setup mock responses for different cities
        OpenWeatherResponse londonResponse = createMockWeatherResponse();
        londonResponse.setName("London");
        
        OpenWeatherResponse parisResponse = createMockWeatherResponse();
        parisResponse.setName("Paris");
        
        OpenWeatherResponse.Sys parisSys = new OpenWeatherResponse.Sys();
        parisSys.setCountry("FR");
        parisResponse.setSys(parisSys);
        
        OpenWeatherResponse.Coord parisCoord = new OpenWeatherResponse.Coord();
        parisCoord.setLat(48.8566);
        parisCoord.setLon(2.3522);
        parisResponse.setCoord(parisCoord);

        when(weatherClient.getCurrentWeather(eq("London,GB"), anyString(), anyString()))
            .thenReturn(londonResponse);
        when(weatherClient.getCurrentWeather(eq("Paris,FR"), anyString(), anyString()))
            .thenReturn(parisResponse);

        // Add multiple locations
        Location london = locationService.addLocation("London", "GB");
        Location paris = locationService.addLocation("Paris", "FR");

        // Verify both locations were added
        List<Location> allLocations = locationService.getAllLocations();
        assertEquals(2, allLocations.size());

        // Test search functionality
        List<Location> searchResults = locationService.searchLocations("Lon");
        assertEquals(1, searchResults.size());
        assertEquals("London", searchResults.get(0).getCityName());

        // Test favorite functionality
        Location favoritedLondon = locationService.toggleFavorite(london.getId());
        assertTrue(favoritedLondon.getIsFavorite());

        List<Location> favorites = locationService.getFavoriteLocations();
        assertEquals(1, favorites.size());
        assertEquals("London", favorites.get(0).getCityName());

        // Sync weather for both locations
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(londonResponse)
            .thenReturn(parisResponse);

        weatherService.syncAllLocations();

        // Verify weather data exists for both locations
        List<WeatherSnapshot> londonHistory = weatherService.getWeatherHistory(london.getId());
        List<WeatherSnapshot> parisHistory = weatherService.getWeatherHistory(paris.getId());

        assertEquals(1, londonHistory.size());
        assertEquals(1, parisHistory.size());
    }

    @Test
    void testLocationUpdateAndDeletion() {
        // Add a location first
        when(weatherClient.getCurrentWeather(eq("London,GB"), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        Location location = locationService.addLocation("London", "GB");

        // Update location
        Location updateDetails = new Location();
        updateDetails.setDisplayName("London, UK");
        updateDetails.setIsFavorite(true);

        Location updatedLocation = locationService.updateLocation(location.getId(), updateDetails);

        assertEquals("London, UK", updatedLocation.getDisplayName());
        assertTrue(updatedLocation.getIsFavorite());

        // Verify update persisted
        Optional<Location> retrieved = locationService.getLocationById(location.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("London, UK", retrieved.get().getDisplayName());
        assertTrue(retrieved.get().getIsFavorite());

        // Delete location
        locationService.deleteLocation(location.getId());

        // Verify deletion
        Optional<Location> afterDeletion = locationService.getLocationById(location.getId());
        assertFalse(afterDeletion.isPresent());
    }

    @Test
    void testErrorHandling() {
        // Test adding duplicate location
        when(weatherClient.getCurrentWeather(eq("London,GB"), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        locationService.addLocation("London", "GB");

        // Attempt to add same location again
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.addLocation("London", "GB");
        });

        // Test API failure during location addition
        when(weatherClient.getCurrentWeather(eq("Paris,FR"), anyString(), anyString()))
            .thenThrow(new RuntimeException("API Error"));

        assertThrows(RuntimeException.class, () -> {
            locationService.addLocation("Paris", "FR");
        });

        // Test API failure during weather sync
        OpenWeatherResponse berlinResponse = createMockWeatherResponse();
        berlinResponse.setName("Berlin");
        OpenWeatherResponse.Sys berlinSys = new OpenWeatherResponse.Sys();
        berlinSys.setCountry("DE");
        berlinResponse.setSys(berlinSys);
        when(weatherClient.getCurrentWeather(eq("Berlin,DE"), anyString(), anyString()))
            .thenReturn(berlinResponse);

        Location location = locationService.addLocation("Berlin", "DE");
        
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Weather API Error"));

        assertThrows(RuntimeException.class, () -> {
            weatherService.syncWeatherData(location.getId());
        });
    }

    @Test
    void testWeatherDataCleanup() {
        // Add location and sync some old weather data
        when(weatherClient.getCurrentWeather(eq("London,GB"), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        Location location = locationService.addLocation("London", "GB");

        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        // Sync weather data
        weatherService.syncWeatherData(location.getId());

        // Verify data exists
        List<WeatherSnapshot> history = weatherService.getWeatherHistory(location.getId());
        assertEquals(1, history.size());

        // Run cleanup (should not delete recent data)
        weatherService.cleanupOldData();

        // Verify data still exists
        history = weatherService.getWeatherHistory(location.getId());
        assertEquals(1, history.size());
    }

    @Test
    void testLocationSyncTimeUpdate() {
        when(weatherClient.getCurrentWeather(eq("London,GB"), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        Location location = locationService.addLocation("London", "GB");
        assertNull(location.getLastSyncAt());

        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        weatherService.syncWeatherData(location.getId());

        // Verify sync time was updated
        Optional<Location> updatedLocation = locationService.getLocationById(location.getId());
        assertTrue(updatedLocation.isPresent());
        assertNotNull(updatedLocation.get().getLastSyncAt());
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
