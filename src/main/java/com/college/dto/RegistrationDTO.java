package com.college.dto;

import com.college.entity.Registration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Registration data transfer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDTO {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
    private String status;
    private LocalDateTime timestamp;
    private LocalDateTime cancelledAt;

    /**
     * Convert Registration entity to RegistrationDTO.
     */
    public static RegistrationDTO fromEntity(Registration registration) {
        return RegistrationDTO.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .userName(registration.getUser().getName())
                .userEmail(registration.getUser().getEmail())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .status(registration.getStatus().name())
                .timestamp(registration.getTimestamp())
                .cancelledAt(registration.getCancelledAt())
                .build();
    }
}
