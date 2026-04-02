package com.college.dto;

import com.college.entity.Event;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for Event data transfer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private String location;
    private Integer capacity;
    private String imageUrl;
    private String status;
    private Integer registeredCount;
    private Boolean hasCapacity;
    private Boolean registrationOpen;
    private UserDTO createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EventDTO fromEntity(Event event, int registeredCount) {
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate())
                .time(event.getTime())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .imageUrl(event.getImageUrl())
                .status(event.getStatus().name())
                .registeredCount(registeredCount)
                .hasCapacity(registeredCount < event.getCapacity())
                .registrationOpen(event.getStatus() == Event.EventStatus.APPROVED
                        && registeredCount < event.getCapacity())
                .createdBy(UserDTO.fromEntity(event.getCreatedBy()))
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    /**
     * Convert Event entity to EventDTO.
     */
    public static EventDTO fromEntity(Event event) {
        return fromEntity(event, 0);
    }

    /**
     * Convert Event entity to EventDTO without nested user details.
     */
    public static EventDTO fromEntitySimple(Event event, int registeredCount) {
        return fromEntity(event, registeredCount).toBuilder().createdBy(null).build();
    }
}
