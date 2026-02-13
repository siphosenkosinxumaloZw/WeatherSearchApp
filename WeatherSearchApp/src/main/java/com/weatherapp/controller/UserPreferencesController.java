package com.weatherapp.controller;

import com.weatherapp.entity.UserPreferences;
import com.weatherapp.service.UserPreferencesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@CrossOrigin(origins = "*")
public class UserPreferencesController {
    
    private final UserPreferencesService preferencesService;
    
    @Autowired
    public UserPreferencesController(UserPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserPreferences> getUserPreferences(@PathVariable String userId) {
        UserPreferences preferences = preferencesService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }
    
    @GetMapping
    public ResponseEntity<UserPreferences> getDefaultPreferences() {
        UserPreferences preferences = preferencesService.getUserPreferences("default");
        return ResponseEntity.ok(preferences);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<UserPreferences> updateUserPreferences(
            @PathVariable String userId,
            @Valid @RequestBody UserPreferences preferences) {
        UserPreferences updated = preferencesService.updateUserPreferences(userId, preferences);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping
    public ResponseEntity<UserPreferences> updateDefaultPreferences(
            @Valid @RequestBody UserPreferences preferences) {
        UserPreferences updated = preferencesService.updateUserPreferences("default", preferences);
        return ResponseEntity.ok(updated);
    }
}
