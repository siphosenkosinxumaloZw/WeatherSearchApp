package com.weatherapp.client;

import com.weatherapp.dto.ForecastResponse;
import com.weatherapp.dto.OpenWeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "openweathermap", url = "${weather.api.base-url}")
public interface OpenWeatherMapClient {
    
    @GetMapping("/weather")
    OpenWeatherResponse getCurrentWeather(
        @RequestParam("q") String cityName,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units
    );
    
    @GetMapping("/weather")
    OpenWeatherResponse getCurrentWeatherByCoordinates(
        @RequestParam("lat") double latitude,
        @RequestParam("lon") double longitude,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units
    );
    
    @GetMapping("/forecast")
    ForecastResponse getForecast(
        @RequestParam("q") String cityName,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units
    );
    
    @GetMapping("/forecast")
    ForecastResponse getForecastByCoordinates(
        @RequestParam("lat") double latitude,
        @RequestParam("lon") double longitude,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units
    );
}
