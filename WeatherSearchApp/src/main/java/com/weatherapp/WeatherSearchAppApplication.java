package com.weatherapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WeatherSearchAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherSearchAppApplication.class, args);
    }

}
