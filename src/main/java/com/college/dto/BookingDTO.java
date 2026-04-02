package com.college.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for Booking entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    
    private Long id;
    private Long userId;
    private String userName;
    private Long venueId;
    private String venueName;
    private Long eventId;
    private String eventTitle;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer attendees;
    private String purpose;
    private String status;
    private Double totalCost;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
