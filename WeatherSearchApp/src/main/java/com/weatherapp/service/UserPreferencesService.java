package com.weatherapp.service;

import com.weatherapp.entity.UserPreferences;
import com.weatherapp.repository.UserPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserPreferencesService {
    
    private final UserPreferencesRepository preferencesRepository;
    
    @Autowired
    public UserPreferencesService(UserPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }
    
    public UserPreferences getUserPreferences(String userId) {
        return preferencesRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreferences(userId));
    }
    
    public UserPreferences updateUserPreferences(String userId, UserPreferences preferences) {
        UserPreferences existing = preferencesRepository.findByUserId(userId)
            .orElseGet(() -> {
                preferences.setUserId(userId);
                preferences.setCreatedAt(LocalDateTime.now());
                return preferences;
            });
        
        if (preferences.getTemperatureUnit() != null) {
            existing.setTemperatureUnit(preferences.getTemperatureUnit());
        }
        if (preferences.getWindSpeedUnit() != null) {
            existing.setWindSpeedUnit(preferences.getWindSpeedUnit());
        }
        if (preferences.getPressureUnit() != null) {
            existing.setPressureUnit(preferences.getPressureUnit());
        }
        if (preferences.getRefreshInterval() != null) {
            existing.setRefreshInterval(preferences.getRefreshInterval());
        }
        if (preferences.getAutoRefresh() != null) {
            existing.setAutoRefresh(preferences.getAutoRefresh());
        }
        
        existing.setUpdatedAt(LocalDateTime.now());
        
        return preferencesRepository.save(existing);
    }
    
    private UserPreferences createDefaultPreferences(String userId) {
        UserPreferences preferences = new UserPreferences(userId);
        preferences.setTemperatureUnit("celsius");
        preferences.setWindSpeedUnit("kmh");
        preferences.setPressureUnit("hPa");
        preferences.setRefreshInterval(30);
        preferences.setAutoRefresh(false);
        
        return preferencesRepository.save(preferences);
    }
}
