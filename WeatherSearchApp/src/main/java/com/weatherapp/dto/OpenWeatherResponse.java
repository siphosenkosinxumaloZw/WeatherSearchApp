package com.weatherapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse {
    
    private Coord coord;
    private List<Weather> weather;
    private Main main;
    private Wind wind;
    private Clouds clouds;
    private Rain rain;
    private Snow snow;
    private Sys sys;
    private Long dt;
    private Long timezone;
    private Long id;
    private String name;
    private Integer cod;
    private Integer visibility;
    
    public static class Coord {
        private Double lon;
        private Double lat;
        
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
    }
    
    public static class Weather {
        private Integer id;
        private String main;
        private String description;
        private String icon;
        
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getMain() { return main; }
        public void setMain(String main) { this.main = main; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
    
    public static class Main {
        private Double temp;
        private Double feels_like;
        private Double temp_min;
        private Double temp_max;
        private Integer pressure;
        private Integer humidity;
        private Integer sea_level;
        private Integer grnd_level;
        
        public Double getTemp() { return temp; }
        public void setTemp(Double temp) { this.temp = temp; }
        public Double getFeels_like() { return feels_like; }
        public void setFeels_like(Double feels_like) { this.feels_like = feels_like; }
        public Double getTemp_min() { return temp_min; }
        public void setTemp_min(Double temp_min) { this.temp_min = temp_min; }
        public Double getTemp_max() { return temp_max; }
        public void setTemp_max(Double temp_max) { this.temp_max = temp_max; }
        public Integer getPressure() { return pressure; }
        public void setPressure(Integer pressure) { this.pressure = pressure; }
        public Integer getHumidity() { return humidity; }
        public void setHumidity(Integer humidity) { this.humidity = humidity; }
        public Integer getSea_level() { return sea_level; }
        public void setSea_level(Integer sea_level) { this.sea_level = sea_level; }
        public Integer getGrnd_level() { return grnd_level; }
        public void setGrnd_level(Integer grnd_level) { this.grnd_level = grnd_level; }
    }
    
    public static class Wind {
        private Double speed;
        private Integer deg;
        private Double gust;
        
        public Double getSpeed() { return speed; }
        public void setSpeed(Double speed) { this.speed = speed; }
        public Integer getDeg() { return deg; }
        public void setDeg(Integer deg) { this.deg = deg; }
        public Double getGust() { return gust; }
        public void setGust(Double gust) { this.gust = gust; }
    }
    
    public static class Clouds {
        private Integer all;
        
        public Integer getAll() { return all; }
        public void setAll(Integer all) { this.all = all; }
    }
    
    public static class Rain {
        private Double oneHour;
        private Double threeHours;
        
        public Double getOneHour() { return oneHour; }
        public void setOneHour(Double oneHour) { this.oneHour = oneHour; }
        public Double getThreeHours() { return threeHours; }
        public void setThreeHours(Double threeHours) { this.threeHours = threeHours; }
    }
    
    public static class Snow {
        private Double oneHour;
        private Double threeHours;
        
        public Double getOneHour() { return oneHour; }
        public void setOneHour(Double oneHour) { this.oneHour = oneHour; }
        public Double getThreeHours() { return threeHours; }
        public void setThreeHours(Double threeHours) { this.threeHours = threeHours; }
    }
    
    public static class Sys {
        private Integer type;
        private Long id;
        private String country;
        private Long sunrise;
        private Long sunset;
        
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public Long getSunrise() { return sunrise; }
        public void setSunrise(Long sunrise) { this.sunrise = sunrise; }
        public Long getSunset() { return sunset; }
        public void setSunset(Long sunset) { this.sunset = sunset; }
    }
    
    public Coord getCoord() { return coord; }
    public void setCoord(Coord coord) { this.coord = coord; }
    public List<Weather> getWeather() { return weather; }
    public void setWeather(List<Weather> weather) { this.weather = weather; }
    public Main getMain() { return main; }
    public void setMain(Main main) { this.main = main; }
    public Wind getWind() { return wind; }
    public void setWind(Wind wind) { this.wind = wind; }
    public Clouds getClouds() { return clouds; }
    public void setClouds(Clouds clouds) { this.clouds = clouds; }
    public Rain getRain() { return rain; }
    public void setRain(Rain rain) { this.rain = rain; }
    public Snow getSnow() { return snow; }
    public void setSnow(Snow snow) { this.snow = snow; }
    public Sys getSys() { return sys; }
    public void setSys(Sys sys) { this.sys = sys; }
    public Long getDt() { return dt; }
    public void setDt(Long dt) { this.dt = dt; }
    public Long getTimezone() { return timezone; }
    public void setTimezone(Long timezone) { this.timezone = timezone; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCod() { return cod; }
    public void setCod(Integer cod) { this.cod = cod; }
    public Integer getVisibility() { return visibility; }
    public void setVisibility(Integer visibility) { this.visibility = visibility; }
    
    public LocalDateTime getDataTimestamp() {
        if (dt != null) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneId.systemDefault());
        }
        return null;
    }
}
