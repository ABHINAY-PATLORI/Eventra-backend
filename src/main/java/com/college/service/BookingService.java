package com.college.service;

import com.college.dto.BookingDTO;
import com.college.dto.PageResponse;
import com.college.entity.Booking;
import com.college.entity.Booking.BookingStatus;
import com.college.entity.Event;
import com.college.entity.User;
import com.college.entity.Venue;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.BookingRepository;
import com.college.repository.EventRepository;
import com.college.repository.UserRepository;
import com.college.repository.VenueRepository;
import com.college.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing bookings.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    /**
     * Get all bookings with pagination (admin only).
     */
    public PageResponse<BookingDTO> getAllBookings(int page, int size, String sort) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<Booking> bookings = bookingRepository.findAll(pageable);
        return convertToPageResponse(bookings);
    }

    /**
     * Get my bookings (current user).
     */
    public PageResponse<BookingDTO> getMyBookings(int page, int size, String sort) {
        String username = getAuthenticatedUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<Booking> bookings = bookingRepository.findByUserId(user.getId(), pageable);
        return convertToPageResponse(bookings);
    }

    /**
     * Get booking by ID.
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return convertToDTO(booking);
    }

    /**
     * Create a new booking.
     */
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        String username = getAuthenticatedUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Venue venue = venueRepository.findById(bookingDTO.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        Event event = null;
        if (bookingDTO.getEventId() != null) {
            event = eventRepository.findById(bookingDTO.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        }

        Booking booking = Booking.builder()
                .user(user)
                .venue(venue)
                .event(event)
                .bookingDate(bookingDTO.getBookingDate())
                .startTime(bookingDTO.getStartTime())
                .endTime(bookingDTO.getEndTime())
                .attendees(bookingDTO.getAttendees())
                .purpose(bookingDTO.getPurpose())
                .status(BookingStatus.PENDING)
                .totalCost(bookingDTO.getTotalCost())
                .notes(bookingDTO.getNotes())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with id: {}", savedBooking.getId());
        return convertToDTO(savedBooking);
    }

    /**
     * Update a booking.
     */
    public BookingDTO updateBooking(Long id, BookingDTO bookingDTO) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (bookingDTO.getBookingDate() != null) booking.setBookingDate(bookingDTO.getBookingDate());
        if (bookingDTO.getStartTime() != null) booking.setStartTime(bookingDTO.getStartTime());
        if (bookingDTO.getEndTime() != null) booking.setEndTime(bookingDTO.getEndTime());
        if (bookingDTO.getAttendees() != null) booking.setAttendees(bookingDTO.getAttendees());
        if (bookingDTO.getPurpose() != null) booking.setPurpose(bookingDTO.getPurpose());
        if (bookingDTO.getTotalCost() != null) booking.setTotalCost(bookingDTO.getTotalCost());
        if (bookingDTO.getNotes() != null) booking.setNotes(bookingDTO.getNotes());

        Booking updated = bookingRepository.save(booking);
        log.info("Booking updated with id: {}", id);
        return convertToDTO(updated);
    }

    /**
     * Confirm a booking (admin only).
     */
    public BookingDTO confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking updated = bookingRepository.save(booking);
        log.info("Booking confirmed with id: {}", id);
        return convertToDTO(updated);
    }

    /**
     * Cancel a booking.
     */
    public BookingDTO cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);
        log.info("Booking cancelled with id: {}", id);
        return convertToDTO(updated);
    }

    /**
     * Delete a booking (admin only).
     */
    public void deleteBooking(Long id) {
        bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        bookingRepository.deleteById(id);
        log.info("Booking deleted with id: {}", id);
    }

    private BookingDTO convertToDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getEmail())
                .venueId(booking.getVenue().getId())
                .venueName(booking.getVenue().getName())
                .eventId(booking.getEvent() != null ? booking.getEvent().getId() : null)
                .eventTitle(booking.getEvent() != null ? booking.getEvent().getTitle() : null)
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .attendees(booking.getAttendees())
                .purpose(booking.getPurpose())
                .status(booking.getStatus().name())
                .totalCost(booking.getTotalCost())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private PageResponse<BookingDTO> convertToPageResponse(Page<Booking> bookings) {
        return PageResponse.<BookingDTO>builder()
                .content(bookings.getContent().stream().map(this::convertToDTO).toList())
                .totalElements(bookings.getTotalElements())
                .totalPages(bookings.getTotalPages())
                .pageNumber(bookings.getNumber())
                .pageSize(bookings.getSize())
                .last(bookings.isLast())
                .build();
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
