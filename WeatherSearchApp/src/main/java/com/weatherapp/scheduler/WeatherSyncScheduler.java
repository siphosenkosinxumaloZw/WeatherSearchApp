package com.weatherapp.scheduler;

import com.weatherapp.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeatherSyncScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherSyncScheduler.class);
    
    private final WeatherService weatherService;
    
    @Autowired
    public WeatherSyncScheduler(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    
    @Scheduled(cron = "0 0 */6 * * *")
    public void syncAllLocations() {
        logger.info("Starting scheduled weather sync for all locations");
        try {
            weatherService.syncAllLocations();
            logger.info("Completed scheduled weather sync");
        } catch (Exception e) {
            logger.error("Failed to complete scheduled weather sync", e);
        }
    }
    
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldData() {
        logger.info("Starting cleanup of old weather data");
        try {
            weatherService.cleanupOldData();
            logger.info("Completed cleanup of old weather data");
        } catch (Exception e) {
            logger.error("Failed to cleanup old weather data", e);
        }
    }
}
