package com.college.repository;

import com.college.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Venue entity.
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    
    Page<Venue> findByActive(Boolean active, Pageable pageable);
    
    Page<Venue> findByNameContainingIgnoreCaseAndActive(String name, Boolean active, Pageable pageable);
    
    Page<Venue> findByCity(String city, Pageable pageable);
    
    List<Venue> findByCapacityGreaterThanEqual(Integer capacity);
}
