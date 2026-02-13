package com.weatherapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastResponse {
    
    private String cod;
    private Long message;
    private Integer cnt;
    private List<ForecastItem> list;
    private City city;
    
    public static class ForecastItem {
        private Long dt;
        private Main main;
        private List<Weather> weather;
        private Clouds clouds;
        private Wind wind;
        private Integer visibility;
        private Double pop;
        private Rain rain;
        private Snow snow;
        private Sys sys;
        private String dt_txt;
        
        public Long getDt() { return dt; }
        public void setDt(Long dt) { this.dt = dt; }
        public Main getMain() { return main; }
        public void setMain(Main main) { this.main = main; }
        public List<Weather> getWeather() { return weather; }
        public void setWeather(List<Weather> weather) { this.weather = weather; }
        public Clouds getClouds() { return clouds; }
        public void setClouds(Clouds clouds) { this.clouds = clouds; }
        public Wind getWind() { return wind; }
        public void setWind(Wind wind) { this.wind = wind; }
        public Integer getVisibility() { return visibility; }
        public void setVisibility(Integer visibility) { this.visibility = visibility; }
        public Double getPop() { return pop; }
        public void setPop(Double pop) { this.pop = pop; }
        public Rain getRain() { return rain; }
        public void setRain(Rain rain) { this.rain = rain; }
        public Snow getSnow() { return snow; }
        public void setSnow(Snow snow) { this.snow = snow; }
        public Sys getSys() { return sys; }
        public void setSys(Sys sys) { this.sys = sys; }
        public String getDt_txt() { return dt_txt; }
        public void setDt_txt(String dt_txt) { this.dt_txt = dt_txt; }
        
        public LocalDateTime getDataTimestamp() {
            if (dt != null) {
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneId.systemDefault());
            }
            return null;
        }
    }
    
    public static class Main {
        private Double temp;
        private Double feels_like;
        private Double temp_min;
        private Double temp_max;
        private Integer pressure;
        private Integer sea_level;
        private Integer grnd_level;
        private Integer humidity;
        private Double temp_kf;
        
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
        public Integer getSea_level() { return sea_level; }
        public void setSea_level(Integer sea_level) { this.sea_level = sea_level; }
        public Integer getGrnd_level() { return grnd_level; }
        public void setGrnd_level(Integer grnd_level) { this.grnd_level = grnd_level; }
        public Integer getHumidity() { return humidity; }
        public void setHumidity(Integer humidity) { this.humidity = humidity; }
        public Double getTemp_kf() { return temp_kf; }
        public void setTemp_kf(Double temp_kf) { this.temp_kf = temp_kf; }
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
    
    public static class Clouds {
        private Integer all;
        
        public Integer getAll() { return all; }
        public void setAll(Integer all) { this.all = all; }
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
    
    public static class Rain {
        private Double threeHours;
        
        public Double getThreeHours() { return threeHours; }
        public void setThreeHours(Double threeHours) { this.threeHours = threeHours; }
    }
    
    public static class Snow {
        private Double threeHours;
        
        public Double getThreeHours() { return threeHours; }
        public void setThreeHours(Double threeHours) { this.threeHours = threeHours; }
    }
    
    public static class Sys {
        private String pod;
        
        public String getPod() { return pod; }
        public void setPod(String pod) { this.pod = pod; }
    }
    
    public static class City {
        private Long id;
        private String name;
        private Coord coord;
        private String country;
        private Long population;
        private Long timezone;
        private Long sunrise;
        private Long sunset;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Coord getCoord() { return coord; }
        public void setCoord(Coord coord) { this.coord = coord; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public Long getPopulation() { return population; }
        public void setPopulation(Long population) { this.population = population; }
        public Long getTimezone() { return timezone; }
        public void setTimezone(Long timezone) { this.timezone = timezone; }
        public Long getSunrise() { return sunrise; }
        public void setSunrise(Long sunrise) { this.sunrise = sunrise; }
        public Long getSunset() { return sunset; }
        public void setSunset(Long sunset) { this.sunset = sunset; }
    }
    
    public static class Coord {
        private Double lat;
        private Double lon;
        
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }
    }
    
    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }
    public Long getMessage() { return message; }
    public void setMessage(Long message) { this.message = message; }
    public Integer getCnt() { return cnt; }
    public void setCnt(Integer cnt) { this.cnt = cnt; }
    public List<ForecastItem> getList() { return list; }
    public void setList(List<ForecastItem> list) { this.list = list; }
    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }
}
