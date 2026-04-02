package com.college.service;

import com.college.dto.EventDTO;
import com.college.dto.PageResponse;
import com.college.dto.UserDTO;
import com.college.entity.Event;
import com.college.entity.Registration;
import com.college.entity.User;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.EventRepository;
import com.college.repository.RegistrationRepository;
import com.college.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for admin operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    /**
     * Get all users with pagination (admin only).
     */
    public PageResponse<UserDTO> getAllUsers(int page, int size) {
        log.debug("Fetching all users - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserDTO> dtoPage = userPage.map(UserDTO::fromEntity);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Get all pending events (admin only).
     */
    public PageResponse<EventDTO> getPendingEvents(int page, int size) {
        log.debug("Fetching pending events - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Event> eventPage = eventRepository.findByStatus(Event.EventStatus.PENDING, pageable);
        Page<EventDTO> dtoPage = eventPage.map(this::toEventDto);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Approve event (admin only).
     */
    @Transactional
    public EventDTO approveEvent(Long eventId) {
        log.info("Approving event with id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        event.setStatus(Event.EventStatus.APPROVED);
        Event updatedEvent = eventRepository.save(event);

        log.info("Event {} approved successfully", eventId);
        return toEventDto(updatedEvent);
    }

    /**
     * Reject event (admin only).
     */
    @Transactional
    public EventDTO rejectEvent(Long eventId) {
        log.info("Rejecting event with id: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        event.setStatus(Event.EventStatus.REJECTED);
        Event updatedEvent = eventRepository.save(event);

        log.info("Event {} rejected successfully", eventId);
        return toEventDto(updatedEvent);
    }

    /**
     * Delete user (admin only).
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);
        log.info("User {} deleted successfully", userId);
    }

    /**
     * Change user role (admin only).
     */
    @Transactional
    public UserDTO changeUserRole(Long userId, String newRole) {
        log.info("Changing role for user {} to {}", userId, newRole);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        try {
            User.Role role = User.Role.valueOf(newRole.toUpperCase());
            user.setRole(role);
            User updatedUser = userRepository.save(user);
            log.info("User {} role changed to {}", userId, newRole);
            return UserDTO.fromEntity(updatedUser);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid role: {}", newRole);
            throw new ResourceNotFoundException("Invalid role: " + newRole);
        }
    }

    private EventDTO toEventDto(Event event) {
        int registeredCount = Math.toIntExact(registrationRepository.countByEventIdAndStatus(
                event.getId(),
                Registration.RegistrationStatus.REGISTERED
        ));
        return EventDTO.fromEntity(event, registeredCount);
    }
}
