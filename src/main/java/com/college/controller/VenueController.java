package com.college.controller;

import com.college.dto.ApiResponse;
import com.college.dto.PageResponse;
import com.college.dto.VenueDTO;
import com.college.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for venue endpoints.
 */
@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@Slf4j
public class VenueController {

    private final VenueService venueService;

    /**
     * Get all active venues with pagination.
     * GET /api/venues?page=0&size=10&sort=asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VenueDTO>>> getAllVenues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching all active venues");
        PageResponse<VenueDTO> response = venueService.getAllVenues(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Venues fetched successfully", response));
    }

    /**
     * Search venues by name.
     * GET /api/venues/search?name=Main&page=0&size=10&sort=asc
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<VenueDTO>>> searchVenues(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Searching venues with name: {}", name);
        PageResponse<VenueDTO> response = venueService.searchVenues(name, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Venues found", response));
    }

    /**
     * Get venue by ID.
     * GET /api/venues/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueDTO>> getVenueById(@PathVariable Long id) {
        log.info("Fetching venue with id: {}", id);
        VenueDTO venue = venueService.getVenueById(id);
        return ResponseEntity.ok(ApiResponse.success("Venue fetched successfully", venue));
    }

    /**
     * Create a new venue (admin only).
     * POST /api/venues
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VenueDTO>> createVenue(@Valid @RequestBody VenueDTO venueDTO) {
        log.info("Creating new venue: {}", venueDTO.getName());
        VenueDTO venue = venueService.createVenue(venueDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Venue created successfully", venue));
    }

    /**
     * Update a venue (admin only).
     * PUT /api/venues/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VenueDTO>> updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody VenueDTO venueDTO) {
        log.info("Updating venue with id: {}", id);
        VenueDTO venue = venueService.updateVenue(id, venueDTO);
        return ResponseEntity.ok(ApiResponse.success("Venue updated successfully", venue));
    }

    /**
     * Delete a venue (admin only).
     * DELETE /api/venues/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVenue(@PathVariable Long id) {
        log.info("Deleting venue with id: {}", id);
        venueService.deleteVenue(id);
        return ResponseEntity.ok(ApiResponse.success("Venue deleted successfully"));
    }
}
