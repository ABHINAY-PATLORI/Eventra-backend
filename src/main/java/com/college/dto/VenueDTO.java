package com.college.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for Venue entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueDTO {
    
    private Long id;
    private String name;
    private String description;
    private String address;
    private Integer capacity;
    private String city;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private String contactPhone;
    private String contactEmail;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
