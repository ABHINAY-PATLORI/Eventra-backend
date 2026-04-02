package com.college.service;

import com.college.dto.EventDTO;
import com.college.dto.EventRequest;
import com.college.dto.PageResponse;
import com.college.entity.Event;
import com.college.entity.Registration;
import com.college.entity.User;
import com.college.exception.ForbiddenException;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.EventRepository;
import com.college.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for event operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final AuthService authService;

    /**
     * Get all approved events with pagination and sorting.
     */
    public PageResponse<EventDTO> getAllApprovedEvents(int page, int size, String sort) {
        log.debug("Fetching approved events - page: {}, size: {}", page, size);

        Sort.Direction direction = sort.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "date").and(Sort.by(direction, "time")));

        Page<Event> eventPage = eventRepository.findByStatus(Event.EventStatus.APPROVED, pageable);
        Page<EventDTO> dtoPage = eventPage.map(this::toEventSummaryDto);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Search approved events by title.
     */
    public PageResponse<EventDTO> searchEvents(String title, int page, int size, String sort) {
        log.debug("Searching approved events with title: {}", title);

        Sort.Direction direction = sort.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "date").and(Sort.by(direction, "time")));

        Page<Event> eventPage = eventRepository
                .findByStatusAndTitleContainingIgnoreCase(Event.EventStatus.APPROVED, title, pageable);
        Page<EventDTO> dtoPage = eventPage.map(this::toEventSummaryDto);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Get event by ID.
     */
    public EventDTO getEventById(Long id) {
        log.debug("Fetching event with id: {}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        validateEventVisibility(event, authService.getCurrentUserNullable());
        return toEventDetailDto(event);
    }

    /**
     * Create new event (organizer only).
     */
    @Transactional
    public EventDTO createEvent(EventRequest request) {
        log.info("Creating new event: {}", request.getTitle());

        User organizer = authService.getCurrentUser();

        // Verify user has ORGANIZER or ADMIN role
        if (organizer.getRole() != User.Role.ORGANIZER && organizer.getRole() != User.Role.ADMIN) {
            log.warn("User {} attempted to create event without ORGANIZER role", organizer.getEmail());
            throw new ForbiddenException("Only organizers can create events");
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .time(request.getTime())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .imageUrl(request.getImageUrl())
                .createdBy(organizer)
                .status(organizer.getRole() == User.Role.ADMIN ? 
                        Event.EventStatus.APPROVED : Event.EventStatus.PENDING)
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully with id: {}", savedEvent.getId());

        return toEventDetailDto(savedEvent);
    }

    /**
     * Update event (organizer only - own events or admin).
     */
    @Transactional
    public EventDTO updateEvent(Long id, EventRequest request) {
        log.info("Updating event with id: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        User currentUser = authService.getCurrentUser();

        // Check authorization: only creator or admin can update
        if (!event.getCreatedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            log.warn("User {} attempted to update event {} they don't own", currentUser.getEmail(), id);
            throw new ForbiddenException("You can only update your own events");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setDate(request.getDate());
        event.setTime(request.getTime());
        event.setLocation(request.getLocation());
        event.setCapacity(request.getCapacity());
        event.setImageUrl(request.getImageUrl());
        if (currentUser.getRole() == User.Role.ORGANIZER) {
            event.setStatus(Event.EventStatus.PENDING);
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully with id: {}", id);

        return toEventDetailDto(updatedEvent);
    }

    /**
     * Delete event (organizer only - own events or admin).
     */
    @Transactional
    public void deleteEvent(Long id) {
        log.info("Deleting event with id: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        User currentUser = authService.getCurrentUser();

        // Check authorization: only creator or admin can delete
        if (!event.getCreatedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            log.warn("User {} attempted to delete event {} they don't own", currentUser.getEmail(), id);
            throw new ForbiddenException("You can only delete your own events");
        }

        eventRepository.delete(event);
        log.info("Event deleted successfully with id: {}", id);
    }

    /**
     * Get events created by current user.
     */
    public PageResponse<EventDTO> getMyEvents(int page, int size) {
        log.debug("Fetching events for current user");

        User currentUser = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Event> eventPage = currentUser.getRole() == User.Role.ADMIN
                ? eventRepository.findAll(pageable)
                : eventRepository.findByCreatedBy(currentUser, pageable);
        Page<EventDTO> dtoPage = eventPage.map(this::toEventSummaryDto);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Get entity for internal operations.
     */
    protected Event getEventEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    private void validateEventVisibility(Event event, User currentUser) {
        if (event.getStatus() == Event.EventStatus.APPROVED) {
            return;
        }
        if (currentUser == null) {
            throw new ResourceNotFoundException("Event not found with id: " + event.getId());
        }
        boolean isOwner = event.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResourceNotFoundException("Event not found with id: " + event.getId());
        }
    }

    private EventDTO toEventSummaryDto(Event event) {
        int registeredCount = getRegisteredCount(event.getId());
        return EventDTO.fromEntitySimple(event, registeredCount);
    }

    private EventDTO toEventDetailDto(Event event) {
        int registeredCount = getRegisteredCount(event.getId());
        return EventDTO.fromEntity(event, registeredCount);
    }

    private int getRegisteredCount(Long eventId) {
        return Math.toIntExact(registrationRepository.countByEventIdAndStatus(
                eventId,
                Registration.RegistrationStatus.REGISTERED
        ));
    }

    /**
     * Register student for event.
     */
    @Transactional
    public void registerForEvent(Long eventId) {
        log.info("Student registering for event: {}", eventId);

        User student = authService.getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.getStatus() != Event.EventStatus.APPROVED) {
            throw new com.college.exception.BadRequestException("Only approved events can accept registrations");
        }

        // Check if already registered
        boolean alreadyRegistered = registrationRepository
                .existsByEventIdAndUserId(eventId, student.getId());
        if (alreadyRegistered) {
            throw new com.college.exception.BadRequestException("Already registered for this event");
        }

        // Check capacity
        int registrationCount = Math.toIntExact(registrationRepository.countByEventIdAndStatus(
                eventId, Registration.RegistrationStatus.REGISTERED
        ));
        if (registrationCount >= event.getCapacity()) {
            throw new com.college.exception.BadRequestException("Event capacity full");
        }

        Registration registration = Registration.builder()
                .user(student)
                .event(event)
                .status(Registration.RegistrationStatus.REGISTERED)
                .attended(false)
                .approved(false)
                .build();

        registrationRepository.save(registration);
        log.info("Registration successful for user {} in event {}", student.getId(), eventId);

        // Send confirmation email
        sendRegistrationConfirmationEmail(student.getEmail(), event.getTitle());
    }

    /**
     * Mark attendance for a participant.
     */
    @Transactional
    public void markAttendance(Long eventId, Long userId) {
        log.info("Marking attendance for user {} in event {}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        Registration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        registration.setAttended(true);
        registrationRepository.save(registration);
        log.info("Attendance marked for user {} in event {}", userId, eventId);
    }

    /**
     * Complete event and mark it as completed.
     */
    @Transactional
    public void completeEvent(Long eventId) {
        log.info("Completing event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        User currentUser = authService.getCurrentUser();
        if (!event.getCreatedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new ForbiddenException("You can only complete your own events");
        }

        event.setCompleted(true);
        event.setCompletedAt(java.time.LocalDateTime.now());
        event.setStatus(Event.EventStatus.COMPLETED);
        eventRepository.save(event);
        log.info("Event completed: {}", eventId);
    }

    /**
     * Award prize to a participant.
     */
    @Transactional
    public void awardPrize(Long eventId, Long userId, String prize) {
        log.info("Awarding prize to user {} in event {}: {}", userId, eventId, prize);

        Registration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        registration.setPrize(prize);
        registrationRepository.save(registration);
        log.info("Prize awarded to user {} in event {}", userId, eventId);
    }

    /**
     * Send registration confirmation email.
     */
    private void sendRegistrationConfirmationEmail(String email, String eventTitle) {
        log.debug("Sending registration confirmation email to: {}", email);
        // Email sending would be implemented here
        // For now, just logged
    }
}
