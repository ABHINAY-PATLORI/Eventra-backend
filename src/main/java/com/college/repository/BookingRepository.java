package com.college.repository;

import com.college.entity.Booking;
import com.college.entity.Booking.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * Repository for Booking entity.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    
    Page<Booking> findByVenueId(Long venueId, Pageable pageable);
    
    Page<Booking> findByEventId(Long eventId, Pageable pageable);
    
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    
    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);
    
    Page<Booking> findByBookingDate(LocalDate bookingDate, Pageable pageable);
}
