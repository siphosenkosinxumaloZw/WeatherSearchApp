package com.weatherapp.repository;

import com.weatherapp.entity.WeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshot, Long> {
    
    List<WeatherSnapshot> findByLocationIdOrderByTimestampDesc(Long locationId);
    
    Optional<WeatherSnapshot> findTopByLocationIdOrderByTimestampDesc(Long locationId);
    
    @Query("SELECT w FROM WeatherSnapshot w WHERE w.location.id = :locationId " +
           "AND w.timestamp >= :since ORDER BY w.timestamp DESC")
    List<WeatherSnapshot> findByLocationIdSince(@Param("locationId") Long locationId, 
                                                 @Param("since") LocalDateTime since);
    
    @Query("SELECT w FROM WeatherSnapshot w WHERE w.location.id = :locationId " +
           "AND w.timestamp BETWEEN :start AND :end ORDER BY w.timestamp DESC")
    List<WeatherSnapshot> findByLocationIdBetween(@Param("locationId") Long locationId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(w) FROM WeatherSnapshot w WHERE w.location.id = :locationId")
    long countByLocationId(@Param("locationId") Long locationId);
    
    void deleteByLocationIdAndTimestampBefore(Long locationId, LocalDateTime before);
}
