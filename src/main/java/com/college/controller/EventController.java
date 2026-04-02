package com.college.controller;

import com.college.dto.ApiResponse;
import com.college.dto.EventDTO;
import com.college.dto.EventRequest;
import com.college.dto.PageResponse;
import com.college.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for event endpoints.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    /**
     * Get all approved events with pagination.
     * GET /api/events?page=0&size=10&sort=asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EventDTO>>> getAllApprovedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching all approved events");
        PageResponse<EventDTO> response = eventService.getAllApprovedEvents(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Events fetched successfully", response));
    }

    /**
     * Search events by title.
     * GET /api/events/search?title=Java&page=0&size=10&sort=asc
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<EventDTO>>> searchEvents(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Searching events with title: {}", title);
        PageResponse<EventDTO> response = eventService.searchEvents(title, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Events found", response));
    }

    /**
     * Get event by ID.
     * GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventDTO>> getEventById(@PathVariable Long id) {
        log.info("Fetching event with id: {}", id);
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event fetched successfully", event));
    }

    /**
     * Create new event (organizer only).
     * POST /api/events
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventDTO>> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("Creating new event: {}", request.getTitle());
        EventDTO event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", event));
    }

    /**
     * Update event (organizer only - own events or admin).
     * PUT /api/events/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventDTO>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        log.info("Updating event with id: {}", id);
        EventDTO event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
    }

    /**
     * Delete event (organizer only - own events or admin).
     * DELETE /api/events/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        log.info("Deleting event with id: {}", id);
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully"));
    }

    /**
     * Get events created by current user.
     * GET /api/events/my-events
     */
    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<EventDTO>>> getMyEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching events for current user");
        PageResponse<EventDTO> response = eventService.getMyEvents(page, size);
        return ResponseEntity.ok(ApiResponse.success("User's events fetched successfully", response));
    }

    /**
     * Register for event (student only).
     * POST /api/events/{id}/register
     */
    @PostMapping("/{id}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<String>> registerForEvent(@PathVariable Long id) {
        log.info("Student registering for event with id: {}", id);
        eventService.registerForEvent(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Successfully registered for event", "Registration confirmed"));
    }

    /**
     * Mark attendance for event (organizer/admin only).
     * PUT /api/events/{eventId}/attendance/{userId}
     */
    @PutMapping("/{eventId}/attendance/{userId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> markAttendance(
            @PathVariable Long eventId,
            @PathVariable Long userId) {
        log.info("Marking attendance for user {} in event {}", userId, eventId);
        eventService.markAttendance(eventId, userId);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked", "User marked as present"));
    }

    /**
     * Complete event and record results (organizer/admin only).
     * PUT /api/events/{id}/complete
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> completeEvent(@PathVariable Long id) {
        log.info("Completing event with id: {}", id);
        eventService.completeEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event completed", "Event marked as completed"));
    }

    /**
     * Award prize to participant.
     * PUT /api/events/{eventId}/participants/{userId}/prize
     */
    @PutMapping("/{eventId}/participants/{userId}/prize")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> awardPrize(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @RequestParam String prize) {
        log.info("Awarding prize to user {} in event {}", userId, eventId);
        eventService.awardPrize(eventId, userId, prize);
        return ResponseEntity.ok(ApiResponse.success("Prize awarded", "Prize: " + prize));
    }
}
