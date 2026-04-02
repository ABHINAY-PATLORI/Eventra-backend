package com.college.controller;

import com.college.dto.ApiResponse;
import com.college.dto.PageResponse;
import com.college.dto.RegistrationDTO;
import com.college.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for registration endpoints.
 */
@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * Register user for an event.
     * POST /api/registrations/{eventId}
     */
    @PostMapping("/{eventId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RegistrationDTO>> registerForEvent(@PathVariable Long eventId) {
        log.info("Registering user for event id: {}", eventId);
        RegistrationDTO registration = registrationService.registerForEvent(eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registered successfully", registration));
    }

    /**
     * Unregister user from an event.
     * DELETE /api/registrations/{eventId}
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> unregisterFromEvent(@PathVariable Long eventId) {
        log.info("Unregistering user from event id: {}", eventId);
        registrationService.unregisterFromEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success("Unregistered successfully"));
    }

    /**
     * Get all events registered by current user.
     * GET /api/registrations/my-events
     */
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<RegistrationDTO>>> getMyRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching registrations for current user");
        PageResponse<RegistrationDTO> response = registrationService.getMyRegistrations(page, size);
        return ResponseEntity.ok(ApiResponse.success("User's registrations fetched successfully", response));
    }

    /**
     * Get event registrations (admin only).
     * GET /api/registrations/events/{eventId}
     */
    @GetMapping("/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<RegistrationDTO>>> getEventRegistrations(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching registrations for event id: {}", eventId);
        PageResponse<RegistrationDTO> response = registrationService.getEventRegistrations(eventId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Event registrations fetched successfully", response));
    }
}
