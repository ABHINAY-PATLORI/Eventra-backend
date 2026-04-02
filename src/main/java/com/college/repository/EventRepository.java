package com.college.repository;

import com.college.entity.Event;
import com.college.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Event entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find all approved events with pagination and sorting.
     */
    Page<Event> findByStatus(Event.EventStatus status, Pageable pageable);

    /**
     * Find all events created by a specific user.
     */
    Page<Event> findByCreatedBy(User createdBy, Pageable pageable);

    /**
     * Find events by title (search).
     */
    Page<Event> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Find approved events by title.
     */
    Page<Event> findByStatusAndTitleContainingIgnoreCase(
            Event.EventStatus status, String title, Pageable pageable);

    /**
     * Find all events for a creator by status.
     */
    Page<Event> findByCreatedByAndStatus(User createdBy, Event.EventStatus status, Pageable pageable);
}
