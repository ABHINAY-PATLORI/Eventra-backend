package com.college.repository;

import com.college.entity.Event;
import com.college.entity.Registration;
import com.college.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Registration entity.
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    /**
     * Find registration by user and event.
     */
    Optional<Registration> findByUserAndEvent(User user, Event event);

    /**
     * Check if user is registered for an event.
     */
    boolean existsByUserAndEventAndStatus(
            User user, Event event, Registration.RegistrationStatus status);

    /**
     * Find all events registered by a user.
     */
    Page<Registration> findByUser(User user, Pageable pageable);

    /**
     * Find all registrations for an event.
     */
    Page<Registration> findByEventAndStatus(
            Event event, Registration.RegistrationStatus status, Pageable pageable);

    /**
     * Count active registrations for an event.
     */
    long countByEventAndStatus(Event event, Registration.RegistrationStatus status);

    /**
     * Count active registrations using event id.
     */
    long countByEventIdAndStatus(Long eventId, Registration.RegistrationStatus status);

    /**
     * Find registration by event id and user id.
     */
    Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Check if user is registered for event by ids.
     */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}
