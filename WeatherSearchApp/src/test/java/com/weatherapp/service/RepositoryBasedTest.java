package com.weatherapp.service;

import com.weatherapp.entity.Location;
import com.weatherapp.entity.WeatherSnapshot;
import com.weatherapp.repository.LocationRepository;
import com.weatherapp.repository.WeatherSnapshotRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoryBasedTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private WeatherSnapshotRepository weatherRepository;

    @MockBean
    private LocationServiceInterface locationService;

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
    void testLocationRepositoryOperations() {
        // Test saving and retrieving locations
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setDisplayName("London, UK");
        location.setIsFavorite(true);
        
        Location savedLocation = locationRepository.save(location);
        
        assertNotNull(savedLocation.getId());
        assertEquals("London", savedLocation.getCityName());
        assertEquals("GB", savedLocation.getCountryCode());
        assertEquals("London, UK", savedLocation.getDisplayName());
        assertTrue(savedLocation.getIsFavorite());

        // Test finding by ID
        Optional<Location> foundLocation = locationRepository.findById(savedLocation.getId());
        assertTrue(foundLocation.isPresent());
        assertEquals(savedLocation.getId(), foundLocation.get().getId());

        // Test finding all locations
        List<Location> allLocations = locationRepository.findAll();
        assertEquals(1, allLocations.size());

        // Test exists by city and country
        assertTrue(locationRepository.existsByCityNameAndCountryCode("London", "GB"));
        assertFalse(locationRepository.existsByCityNameAndCountryCode("Paris", "FR"));

        // Test favorite locations
        List<Location> favorites = locationRepository.findByIsFavoriteTrue();
        assertEquals(1, favorites.size());
        assertTrue(favorites.get(0).getIsFavorite());

        // Test search functionality
        Location location2 = new Location("Londonderry", "GB", 54.9966, -7.3086);
        locationRepository.save(location2);

        List<Location> searchResults = locationRepository.findBySearchTerm("Lon");
        assertEquals(2, searchResults.size());
    }

    @Test
    void testWeatherSnapshotRepositoryOperations() {
        // Create a location first
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        Location savedLocation = locationRepository.save(location);

        // Test saving weather snapshots
        WeatherSnapshot snapshot1 = new WeatherSnapshot();
        snapshot1.setLocation(savedLocation);
        snapshot1.setTemperature(15.5);
        snapshot1.setHumidity(65);
        snapshot1.setPressure(1013.0);
        snapshot1.setWindSpeed(5.2);
        snapshot1.setWindDirection(230);
        snapshot1.setVisibility(10000);
        snapshot1.setWeatherMain("Clouds");
        snapshot1.setWeatherDescription("scattered clouds");
        snapshot1.setWeatherIcon("03d");
        snapshot1.setDataTimestamp(LocalDateTime.now());
        snapshot1.setTimestamp(LocalDateTime.now());

        WeatherSnapshot savedSnapshot = weatherRepository.save(snapshot1);
        assertNotNull(savedSnapshot.getId());
        assertEquals(savedLocation, savedSnapshot.getLocation());

        // Test finding by location
        List<WeatherSnapshot> byLocation = weatherRepository.findByLocationIdOrderByTimestampDesc(savedLocation.getId());
        assertEquals(1, byLocation.size());

        // Test finding top by location
        Optional<WeatherSnapshot> topByLocation = weatherRepository.findTopByLocationIdOrderByTimestampDesc(savedLocation.getId());
        assertTrue(topByLocation.isPresent());

        // Test finding since timestamp
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<WeatherSnapshot> sinceTimestamp = weatherRepository.findByLocationIdSince(savedLocation.getId(), since);
        assertEquals(1, sinceTimestamp.size());

        // Test count by location
        long count = weatherRepository.countByLocationId(savedLocation.getId());
        assertEquals(1, count);
    }

    @Test
    void testLocationServiceWithRealRepository() {
        // Test LocationService with real repository but mocked API
        when(weatherClient.getCurrentWeather(eq("London,GB"), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        LocationService locationService = new LocationService(locationRepository, weatherClient, "test-api-key");

        // Test adding a location
        Location location = locationService.addLocation("London", "GB", null, null, null, null);

        assertNotNull(location.getId());
        assertEquals("London", location.getCityName());
        assertEquals("GB", location.getCountryCode());
        assertEquals(51.5074, location.getLatitude());
        assertEquals(-0.1278, location.getLongitude());

        // Test retrieving the location
        Optional<Location> retrieved = locationService.getLocationById(location.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(location.getId(), retrieved.get().getId());

        // Test updating the location
        Location updateDetails = new Location();
        updateDetails.setDisplayName("London, UK");
        updateDetails.setIsFavorite(true);

        Location updated = locationService.updateLocation(location.getId(), updateDetails);
        assertEquals("London, UK", updated.getDisplayName());
        assertTrue(updated.getIsFavorite());

        // Test toggling favorite
        Location toggled = locationService.toggleFavorite(location.getId());
        assertFalse(toggled.getIsFavorite());

        // Test getting all locations
        List<Location> allLocations = locationService.getAllLocations();
        assertEquals(1, allLocations.size());

        // Test search
        List<Location> searchResults = locationService.searchLocations("London");
        assertEquals(1, searchResults.size());

        verify(weatherClient).getCurrentWeather(eq("London,GB"), anyString(), anyString());
    }

    @Test
    void testWeatherServiceWithRealRepository() {
        // Create a location first
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        Location savedLocation = locationRepository.save(location);

        // Mock LocationServiceInterface to return our test location
        LocationServiceInterface mockLocationService = mock(LocationServiceInterface.class);
        when(mockLocationService.getLocationById(savedLocation.getId())).thenReturn(Optional.of(savedLocation));

        // Mock weather API
        when(weatherClient.getCurrentWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        WeatherService weatherService = new WeatherService(weatherRepository, mockLocationService, weatherClient, "test-api-key");

        // Test syncing weather data
        WeatherSnapshot snapshot = weatherService.syncWeatherData(savedLocation.getId());
        
        assertNotNull(snapshot.getId());
        assertEquals(savedLocation, snapshot.getLocation());
        assertEquals(15.5, snapshot.getTemperature());
        assertEquals(65, snapshot.getHumidity());
        assertEquals(1013, snapshot.getPressure());
        assertEquals(5.2, snapshot.getWindSpeed());
        assertEquals(230, snapshot.getWindDirection());
        assertEquals(10000, snapshot.getVisibility());
        assertEquals("Clouds", snapshot.getWeatherMain());
        assertEquals("scattered clouds", snapshot.getWeatherDescription());
        assertEquals("03d", snapshot.getWeatherIcon());

        // Test retrieving current weather
        WeatherSnapshot current = weatherService.getCurrentWeather(savedLocation.getId());
        assertEquals(snapshot.getId(), current.getId());

        // Test getting weather history
        List<WeatherSnapshot> history = weatherService.getWeatherHistory(savedLocation.getId());
        assertEquals(1, history.size());

        verify(weatherClient).getCurrentWeatherByCoordinates(eq(51.5074), eq(-0.1278), anyString(), eq("metric"));
        verify(mockLocationService).updateLastSyncTime(savedLocation.getId());
    }

    @Test
    void testErrorHandling() {
        LocationService locationService = new LocationService(locationRepository, weatherClient, "test-api-key");

        // Test adding duplicate location
        when(weatherClient.getCurrentWeather(eq("London,GB"), anyString(), anyString()))
            .thenReturn(mockWeatherResponse);

        Location location1 = locationService.addLocation("London", "GB",51.5074, -0.1278, "London", true);
        assertNotNull(location1.getId());

        // Try to add same location again
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.addLocation("London", "GB",51.5074, -0.1278, "London", true);
        });

        // Test API failure
        when(weatherClient.getCurrentWeather(eq("Paris,FR"), anyString(), anyString()))
            .thenThrow(new RuntimeException("API Error"));

        assertThrows(RuntimeException.class, () -> {
            locationService.addLocation("Paris", "FR",48.8566, 2.3522, "Paris", true);
        });

        // Test location not found
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.updateLocation(999L, new Location());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            locationService.deleteLocation(999L);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            locationService.toggleFavorite(999L);
        });
    }

    @Test
    void testCleanupOperations() {
        // Create old weather data
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        Location savedLocation = locationRepository.save(location);

        WeatherSnapshot oldSnapshot = new WeatherSnapshot();
        oldSnapshot.setLocation(savedLocation);
        oldSnapshot.setTemperature(10.0);
        oldSnapshot.setHumidity(50);
        oldSnapshot.setPressure(1010.0);
        oldSnapshot.setTimestamp(LocalDateTime.now().minusDays(35)); // 35 days old
        weatherRepository.save(oldSnapshot);

        WeatherSnapshot recentSnapshot = new WeatherSnapshot();
        recentSnapshot.setLocation(savedLocation);
        recentSnapshot.setTemperature(20.0);
        recentSnapshot.setHumidity(60);
        recentSnapshot.setPressure(1015.0);
        recentSnapshot.setTimestamp(LocalDateTime.now().minusDays(5)); // 5 days old
        weatherRepository.save(recentSnapshot);

        // Verify both snapshots exist
        List<WeatherSnapshot> allSnapshots = weatherRepository.findByLocationIdOrderByTimestampDesc(savedLocation.getId());
        assertEquals(2, allSnapshots.size());

        // Test cleanup (should delete old snapshot but keep recent one)
        LocationService locationService = new LocationService(locationRepository, weatherClient, "test-api-key");
        WeatherService weatherService = new WeatherService(weatherRepository, locationService, weatherClient, "test-api-key");

        weatherService.cleanupOldData();

        // Verify only recent snapshot remains
        List<WeatherSnapshot> afterCleanup = weatherRepository.findByLocationIdOrderByTimestampDesc(savedLocation.getId());
        assertEquals(1, afterCleanup.size());
        assertEquals(20.0, afterCleanup.get(0).getTemperature());
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
