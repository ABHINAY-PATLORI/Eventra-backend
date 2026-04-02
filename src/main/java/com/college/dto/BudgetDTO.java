package com.college.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for Budget entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDTO {
    
    private Long id;
    private String title;
    private String description;
    private Long eventId;
    private String eventTitle;
    private Long createdById;
    private String createdByName;
    private Double totalAmount;
    private Double allocatedAmount;
    private Double spentAmount;
    private String status;
    private String category;
    private String notes;
    private Double remainingAmount;
    private Double utilizationPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
