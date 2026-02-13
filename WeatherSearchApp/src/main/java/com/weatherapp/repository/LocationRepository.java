package com.weatherapp.repository;

import com.weatherapp.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    Optional<Location> findByCityNameAndCountryCode(String cityName, String countryCode);
    
    List<Location> findByIsFavoriteTrue();
    
    @Query("SELECT l FROM Location l WHERE l.cityName LIKE %:search% OR l.displayName LIKE %:search%")
    List<Location> findBySearchTerm(@Param("search") String search);
    
    @Query("SELECT l FROM Location l WHERE l.lastSyncAt IS NULL OR l.lastSyncAt < " +
           "(SELECT MAX(w.timestamp) FROM WeatherSnapshot w WHERE w.location = l)")
    List<Location> findLocationsNeedingSync();
    
    boolean existsByCityNameAndCountryCode(String cityName, String countryCode);
}
