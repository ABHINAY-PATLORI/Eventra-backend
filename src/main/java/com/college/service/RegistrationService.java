package com.college.service;

import com.college.dto.PageResponse;
import com.college.dto.RegistrationDTO;
import com.college.entity.Event;
import com.college.entity.Registration;
import com.college.entity.User;
import com.college.exception.BadRequestException;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service class for registration operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final AuthService authService;

    /**
     * Register user for an event.
     */
    @Transactional
    public RegistrationDTO registerForEvent(Long eventId) {
        log.info("User registering for event id: {}", eventId);

        User currentUser = authService.getCurrentUser();
        Event event = eventService.getEventEntityById(eventId);

        // Check if event is approved
        if (event.getStatus() != Event.EventStatus.APPROVED) {
            log.warn("Event {} is not approved", eventId);
            throw new BadRequestException("Event is not approved for registration");
        }

        // Check if already registered
        if (registrationRepository.existsByUserAndEventAndStatus(
                currentUser, event, Registration.RegistrationStatus.REGISTERED)) {
            log.warn("User {} already registered for event {}", currentUser.getId(), eventId);
            throw new BadRequestException("You are already registered for this event");
        }

        // Check event capacity
        long activeRegistrations = registrationRepository.countByEventAndStatus(
                event, Registration.RegistrationStatus.REGISTERED);
        if (activeRegistrations >= event.getCapacity()) {
            log.warn("Event {} has no available capacity", eventId);
            throw new BadRequestException("Event has reached maximum capacity");
        }

        // Create registration or reactivate cancelled
        Registration registration = registrationRepository
                .findByUserAndEvent(currentUser, event)
                .orElse(Registration.builder()
                        .user(currentUser)
                        .event(event)
                        .build());

        registration.setStatus(Registration.RegistrationStatus.REGISTERED);
        registration.setCancelledAt(null);

        Registration savedRegistration = registrationRepository.save(registration);
        log.info("User {} registered for event {}", currentUser.getId(), eventId);

        return RegistrationDTO.fromEntity(savedRegistration);
    }

    /**
     * Cancel registration for an event.
     */
    @Transactional
    public void unregisterFromEvent(Long eventId) {
        log.info("User unregistering from event id: {}", eventId);

        User currentUser = authService.getCurrentUser();
        Event event = eventService.getEventEntityById(eventId);

        Registration registration = registrationRepository
                .findByUserAndEvent(currentUser, event)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for this event"));

        if (registration.getStatus() == Registration.RegistrationStatus.CANCELLED) {
            throw new BadRequestException("Already unregistered from this event");
        }

        registration.setStatus(Registration.RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(registration);

        log.info("User {} unregistered from event {}", currentUser.getId(), eventId);
    }

    /**
     * Get all events registered by current user.
     */
    public PageResponse<RegistrationDTO> getMyRegistrations(int page, int size) {
        log.debug("Fetching registrations for current user");

        User currentUser = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<Registration> registrationPage = registrationRepository.findByUser(currentUser, pageable);
        Page<RegistrationDTO> dtoPage = registrationPage.map(RegistrationDTO::fromEntity);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Get registration count for an event.
     */
    public long getEventRegistrationCount(Long eventId) {
        Event event = eventService.getEventEntityById(eventId);
        return registrationRepository.countByEventAndStatus(
                event, Registration.RegistrationStatus.REGISTERED);
    }

    /**
     * Get registrations for an event (admin only).
     */
    public PageResponse<RegistrationDTO> getEventRegistrations(Long eventId, int page, int size) {
        log.debug("Fetching registrations for event {}", eventId);

        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new com.college.exception.ForbiddenException(
                    "Only admins can view event registrations");
        }

        Event event = eventService.getEventEntityById(eventId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<Registration> registrationPage = registrationRepository
                .findByEventAndStatus(event, Registration.RegistrationStatus.REGISTERED, pageable);
        Page<RegistrationDTO> dtoPage = registrationPage.map(RegistrationDTO::fromEntity);

        return PageResponse.fromPage(dtoPage);
    }
}
