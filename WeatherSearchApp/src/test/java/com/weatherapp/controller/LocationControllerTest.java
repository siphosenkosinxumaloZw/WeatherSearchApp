package com.weatherapp.controller;

import com.weatherapp.entity.Location;
import com.weatherapp.service.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllLocations_Success() throws Exception {
        Location location1 = new Location("London", "GB", 51.5074, -0.1278);
        Location location2 = new Location("Paris", "FR", 48.8566, 2.3522);
        
        when(locationService.getAllLocations()).thenReturn(Arrays.asList(location1, location2));

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].cityName").value("London"))
                .andExpect(jsonPath("$[1].cityName").value("Paris"));
    }

    @Test
    void getLocationById_Success() throws Exception {
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        
        when(locationService.getLocationById(1L)).thenReturn(Optional.of(location));

        mockMvc.perform(get("/api/locations/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("London"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getLocationById_NotFound() throws Exception {
        when(locationService.getLocationById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/locations/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFavoriteLocations_Success() throws Exception {
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setIsFavorite(true);
        
        when(locationService.getFavoriteLocations()).thenReturn(Arrays.asList(location));

        mockMvc.perform(get("/api/locations/favorites"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].cityName").value("London"))
                .andExpect(jsonPath("$[0].isFavorite").value(true));
    }

    @Test
    void addLocation_Success() throws Exception {
        Location newLocation = new Location("London", "GB", 51.5074, -0.1278);
        newLocation.setId(1L);
        
        when(locationService.addLocation("London", "GB")).thenReturn(newLocation);

        LocationController.LocationRequest request = new LocationController.LocationRequest();
        request.setCityName("London");
        request.setCountryCode("GB");

        mockMvc.perform(post("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("London"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateLocation_Success() throws Exception {
        Location existingLocation = new Location("London", "GB", 51.5074, -0.1278);
        existingLocation.setId(1L);
        existingLocation.setDisplayName("London, UK");
        
        when(locationService.updateLocation(anyLong(), any(Location.class))).thenReturn(existingLocation);

        Location updateRequest = new Location();
        updateRequest.setDisplayName("London, UK");

        mockMvc.perform(put("/api/locations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.displayName").value("London, UK"));
    }

    @Test
    void deleteLocation_Success() throws Exception {
        mockMvc.perform(delete("/api/locations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void toggleFavorite_Success() throws Exception {
        Location location = new Location("London", "GB", 51.5074, -0.1278);
        location.setId(1L);
        location.setIsFavorite(true);
        
        when(locationService.toggleFavorite(1L)).thenReturn(location);

        mockMvc.perform(post("/api/locations/1/toggle-favorite"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isFavorite").value(true));
    }
}
