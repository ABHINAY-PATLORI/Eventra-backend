package com.college.service;

import com.college.dto.EventDTO;
import com.college.dto.EventRequest;
import com.college.entity.Event;
import com.college.entity.Registration;
import com.college.entity.User;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.EventRepository;
import com.college.repository.RegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldHidePendingEventFromAnonymousUsers() {
        Event event = buildEvent(Event.EventStatus.PENDING, 7L);
        when(eventRepository.findById(7L)).thenReturn(Optional.of(event));
        when(authService.getCurrentUserNullable()).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(7L));
    }

    @Test
    void shouldResetOrganizerEventToPendingOnUpdate() {
        User organizer = User.builder()
                .id(5L)
                .name("Organizer")
                .email("organizer@college.com")
                .password("encoded")
                .role(User.Role.ORGANIZER)
                .build();

        Event event = buildEvent(Event.EventStatus.APPROVED, 9L);
        event.setCreatedBy(organizer);

        EventRequest request = EventRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .date(LocalDate.now().plusDays(2))
                .time(LocalTime.of(18, 0))
                .location("Auditorium")
                .capacity(300)
                .imageUrl("https://example.com/banner.jpg")
                .build();

        when(eventRepository.findById(9L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(authService.getCurrentUser()).thenReturn(organizer);
        when(registrationRepository.countByEventIdAndStatus(9L, Registration.RegistrationStatus.REGISTERED))
                .thenReturn(0L);

        EventDTO response = eventService.updateEvent(9L, request);

        assertEquals("PENDING", response.getStatus());
        assertEquals(LocalDate.now().plusDays(2), event.getDate());
        assertEquals(LocalTime.of(18, 0), event.getTime());
        verify(eventRepository).save(event);
    }

    private Event buildEvent(Event.EventStatus status, Long id) {
        User organizer = User.builder()
                .id(1L)
                .name("Organizer")
                .email("org@college.com")
                .password("encoded")
                .role(User.Role.ORGANIZER)
                .build();

        return Event.builder()
                .id(id)
                .title("Hackathon")
                .description("Coding event")
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.NOON)
                .location("Main Hall")
                .capacity(100)
                .status(status)
                .createdBy(organizer)
                .build();
    }
}
