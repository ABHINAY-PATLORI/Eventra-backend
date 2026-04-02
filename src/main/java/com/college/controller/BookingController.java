package com.college.controller;

import com.college.dto.ApiResponse;
import com.college.dto.BookingDTO;
import com.college.dto.PageResponse;
import com.college.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for booking endpoints.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Get all bookings with pagination (admin only).
     * GET /api/bookings?page=0&size=10&sort=asc
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BookingDTO>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching all bookings");
        PageResponse<BookingDTO> response = bookingService.getAllBookings(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Bookings fetched successfully", response));
    }

    /**
     * Get my bookings (current user).
     * GET /api/bookings/my-bookings
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BookingDTO>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching my bookings");
        PageResponse<BookingDTO> response = bookingService.getMyBookings(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("My bookings fetched successfully", response));
    }

    /**
     * Get booking by ID.
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(@PathVariable Long id) {
        log.info("Fetching booking with id: {}", id);
        BookingDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success("Booking fetched successfully", booking));
    }

    /**
     * Create a new booking.
     * POST /api/bookings
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        log.info("Creating new booking");
        BookingDTO booking = bookingService.createBooking(bookingDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", booking));
    }

    /**
     * Update a booking.
     * PUT /api/bookings/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingDTO bookingDTO) {
        log.info("Updating booking with id: {}", id);
        BookingDTO booking = bookingService.updateBooking(id, bookingDTO);
        return ResponseEntity.ok(ApiResponse.success("Booking updated successfully", booking));
    }

    /**
     * Confirm a booking (admin only).
     * PUT /api/bookings/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> confirmBooking(@PathVariable Long id) {
        log.info("Confirming booking with id: {}", id);
        BookingDTO booking = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", booking));
    }

    /**
     * Cancel a booking.
     * PUT /api/bookings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(@PathVariable Long id) {
        log.info("Cancelling booking with id: {}", id);
        BookingDTO booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }

    /**
     * Delete a booking (admin only).
     * DELETE /api/bookings/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        log.info("Deleting booking with id: {}", id);
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking deleted successfully"));
    }
}
