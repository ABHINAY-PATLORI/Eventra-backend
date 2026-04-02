package com.college.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Budget entity representing a budget allocation for events.
 */
@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private Double totalAmount;

    private Double allocatedAmount = 0.0;

    private Double spentAmount = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetStatus status = BudgetStatus.ACTIVE;

    @Column(nullable = false)
    private String category;

    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.allocatedAmount == null) {
            this.allocatedAmount = 0.0;
        }
        if (this.spentAmount == null) {
            this.spentAmount = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enum representing budget status.
     */
    public enum BudgetStatus {
        ACTIVE, APPROVED, COMPLETED, CANCELLED
    }

    /**
     * Get remaining budget amount.
     */
    public Double getRemainingAmount() {
        return totalAmount - (allocatedAmount != null ? allocatedAmount : 0.0);
    }

    /**
     * Get budget utilization percentage.
     */
    public Double getUtilizationPercentage() {
        if (totalAmount == 0) {
            return 0.0;
        }
        return ((spentAmount != null ? spentAmount : 0.0) / totalAmount) * 100;
    }
}
