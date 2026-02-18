package com.weatherapp;

import com.weatherapp.dto.OpenWeatherResponse;
import com.weatherapp.entity.Location;
import com.weatherapp.entity.WeatherSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BasicFunctionalTest {

    @Test
    void testLocationEntityCreation() {
        // Test basic Location entity functionality
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        
        assertEquals("London", location.getCityName());
        assertEquals("GB", location.getCountryCode());
        assertEquals(51.5074, location.getLatitude());
        assertEquals(-0.1278, location.getLongitude());
        assertEquals("London, GB", location.getDisplayName());
        assertFalse(location.getIsFavorite());
        assertNotNull(location.getCreatedAt());
        
        // Test setters
        location.setId(1L);
        location.setDisplayName("London, UK");
        location.setIsFavorite(true);
        
        assertEquals(1L, location.getId());
        assertEquals("London, UK", location.getDisplayName());
        assertTrue(location.getIsFavorite());
    }

    @Test
    void testWeatherSnapshotEntityCreation() {
        // Test basic WeatherSnapshot entity functionality
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        WeatherSnapshot snapshot = new WeatherSnapshot();
        
        snapshot.setLocation(location);
        snapshot.setTemperature(15.5);
        snapshot.setHumidity(65);
        snapshot.setPressure(1013.0);
        snapshot.setWindSpeed(5.2);
        snapshot.setWindDirection(230);
        snapshot.setVisibility(10000);
        snapshot.setWeatherMain("Clouds");
        snapshot.setWeatherDescription("scattered clouds");
        snapshot.setWeatherIcon("03d");
        snapshot.setDataTimestamp(LocalDateTime.now());
        snapshot.setTimestamp(LocalDateTime.now());
        
        assertEquals(location, snapshot.getLocation());
        assertEquals(15.5, snapshot.getTemperature());
        assertEquals(65, snapshot.getHumidity());
        assertEquals(1013.0, snapshot.getPressure());
        assertEquals(5.2, snapshot.getWindSpeed());
        assertEquals(230, snapshot.getWindDirection());
        assertEquals(10000, snapshot.getVisibility());
        assertEquals("Clouds", snapshot.getWeatherMain());
        assertEquals("scattered clouds", snapshot.getWeatherDescription());
        assertEquals("03d", snapshot.getWeatherIcon());
        assertNotNull(snapshot.getDataTimestamp());
        assertNotNull(snapshot.getTimestamp());
    }

    @Test
    void testOpenWeatherResponseDTO() {
        // Test OpenWeatherResponse DTO functionality
        OpenWeatherResponse response = new OpenWeatherResponse();
        
        // Test main weather data
        OpenWeatherResponse.Main main = new OpenWeatherResponse.Main();
        main.setTemp(15.5);
        main.setHumidity(65);
        main.setPressure(1013);
        response.setMain(main);
        
        // Test weather conditions
        OpenWeatherResponse.Weather weather = new OpenWeatherResponse.Weather();
        weather.setMain("Clouds");
        weather.setDescription("scattered clouds");
        weather.setIcon("03d");
        response.setWeather(java.util.List.of(weather));
        
        // Test wind data
        OpenWeatherResponse.Wind wind = new OpenWeatherResponse.Wind();
        wind.setSpeed(5.2);
        wind.setDeg(230);
        response.setWind(wind);
        
        // Test visibility
        response.setVisibility(10000);
        
        // Test timestamp
        response.setDt(System.currentTimeMillis() / 1000);
        
        // Test coordinates
        OpenWeatherResponse.Coord coord = new OpenWeatherResponse.Coord();
        coord.setLat(51.5074);
        coord.setLon(-0.1278);
        response.setCoord(coord);
        
        // Test system data
        OpenWeatherResponse.Sys sys = new OpenWeatherResponse.Sys();
        sys.setCountry("GB");
        response.setSys(sys);
        
        // Verify all getters work
        assertEquals(15.5, response.getMain().getTemp());
        assertEquals(65, response.getMain().getHumidity());
        assertEquals(1013, response.getMain().getPressure());
        assertEquals("Clouds", response.getWeather().get(0).getMain());
        assertEquals("scattered clouds", response.getWeather().get(0).getDescription());
        assertEquals("03d", response.getWeather().get(0).getIcon());
        assertEquals(5.2, response.getWind().getSpeed());
        assertEquals(230, response.getWind().getDeg());
        assertEquals(10000, response.getVisibility());
        assertEquals(51.5074, response.getCoord().getLat());
        assertEquals(-0.1278, response.getCoord().getLon());
        assertEquals("GB", response.getSys().getCountry());
        assertNotNull(response.getDataTimestamp());
    }

    @Test
    void testOpenWeatherResponseDataTimestampConversion() {
        // Test timestamp conversion functionality
        OpenWeatherResponse response = new OpenWeatherResponse();
        
        long timestamp = System.currentTimeMillis() / 1000;
        response.setDt(timestamp);
        
        LocalDateTime convertedTime = response.getDataTimestamp();
        assertNotNull(convertedTime);
        
        // Verify it's within a reasonable range (current time +/- 1 second)
        LocalDateTime now = LocalDateTime.now();
        assertTrue(convertedTime.isAfter(now.minusSeconds(1)));
        assertTrue(convertedTime.isBefore(now.plusSeconds(1)));
    }

    @Test
    void testLocationDisplayNameGeneration() {
        // Test different location display name scenarios
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        assertEquals("London, GB", location1.getDisplayName());
        
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        assertEquals("Paris, FR", location2.getDisplayName());
        
        // Test custom display name
        Location location3 = new Location("New York", "US", 40.7128, -74.0060);
        location3.setDisplayName("The Big Apple");
        assertEquals("The Big Apple", location3.getDisplayName());
    }

    @Test
    void testWeatherDataValidation() {
        // Test weather data validation scenarios
        WeatherSnapshot snapshot = new WeatherSnapshot();
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        
        // Test valid data
        assertDoesNotThrow(() -> {
            snapshot.setLocation(location);
            snapshot.setTemperature(15.5);
            snapshot.setHumidity(65);
            snapshot.setPressure(1013.0);
        });
        
        // Test edge cases
        assertDoesNotThrow(() -> {
            snapshot.setTemperature(-50.0); // Very cold
            snapshot.setHumidity(0); // No humidity
            snapshot.setHumidity(100); // Maximum humidity
            snapshot.setPressure(800.0); // Low pressure
            snapshot.setPressure(1200.0); // High pressure
            snapshot.setWindSpeed(0.0); // No wind
            snapshot.setWindSpeed(50.0); // Very strong wind
            snapshot.setWindDirection(0); // North
            snapshot.setWindDirection(360); // North again
            snapshot.setVisibility(0); // No visibility
            snapshot.setVisibility(10000); // Excellent visibility
        });
    }

    @Test
    void testLocationFavoriteToggle() {
        // Test favorite toggle functionality
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        
        // Default should be false
        assertFalse(location.getIsFavorite());
        
        // Toggle to true
        location.setIsFavorite(true);
        assertTrue(location.getIsFavorite());
        
        // Toggle to false
        location.setIsFavorite(false);
        assertFalse(location.getIsFavorite());
        
        // Test null handling
        location.setIsFavorite(null);
        assertNull(location.getIsFavorite());
    }

    @Test
    void testWeatherSnapshotTimestamps() {
        // Test timestamp handling in weather snapshots
        WeatherSnapshot snapshot = new WeatherSnapshot();
        
        LocalDateTime dataTimestamp = LocalDateTime.of(2023, 12, 25, 10, 30, 0);
        LocalDateTime recordTimestamp = LocalDateTime.of(2023, 12, 25, 10, 31, 0);
        
        snapshot.setDataTimestamp(dataTimestamp);
        snapshot.setTimestamp(recordTimestamp);
        
        assertEquals(dataTimestamp, snapshot.getDataTimestamp());
        assertEquals(recordTimestamp, snapshot.getTimestamp());
        
        // Test that timestamps can be different
        assertNotEquals(snapshot.getDataTimestamp(), snapshot.getTimestamp());
    }

    @Test
    void testOpenWeatherResponseNullHandling() {
        // Test null handling in OpenWeatherResponse
        OpenWeatherResponse response = new OpenWeatherResponse();
        
        // All should return null when not set
        assertNull(response.getMain());
        assertNull(response.getWeather());
        assertNull(response.getWind());
        assertNull(response.getVisibility());
        assertNull(response.getCoord());
        assertNull(response.getSys());
        assertNull(response.getDataTimestamp());
        
        // Test setting null values
        response.setMain(null);
        response.setWeather(null);
        response.setWind(null);
        response.setVisibility(null);
        
        // Should still return null
        assertNull(response.getMain());
        assertNull(response.getWeather());
        assertNull(response.getWind());
        assertNull(response.getVisibility());
    }

    @Test
    void testLocationCoordinatesValidation() {
        // Test coordinate validation
        assertDoesNotThrow(() -> {
            new Location("London", "GB", 51.5074, -0.1278); // Normal coordinates
            new Location("North Pole", "US", 90.0, 0.0); // North pole
            new Location("South Pole", "AQ", -90.0, 0.0); // South pole
            new Location("International Date Line", "FJ", 0.0, 180.0); // Date line
            new Location("International Date Line West", "FJ", 0.0, -180.0); // Date line west
        });
        
        // Test extreme coordinates
        Location extremeLocation = new Location("Extreme", "XX", 91.0, 181.0);
        assertEquals(91.0, extremeLocation.getLatitude());
        assertEquals(181.0, extremeLocation.getLongitude());
    }
}
