package com.weatherapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeatherApiConfig {
    
    @Value("${weather.api.key}")
    private String apiKey;
    
    @Bean
    public String weatherApiKey() {
        return apiKey;
    }
}
