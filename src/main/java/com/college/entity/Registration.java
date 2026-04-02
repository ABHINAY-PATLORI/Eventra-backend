package com.college.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Registration entity representing a user's registration for an event.
 */
@Entity
@Table(name = "registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many registrations belong to one user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Many registrations belong to one event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    // Attendance tracking
    @Column(nullable = false)
    private Boolean attended = false;

    // Prize/Achievement details
    @Column(length = 500)
    private String prize;

    // Approval status
    @Column(nullable = false)
    private Boolean approved = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Enum representing registration status.
     */
    public enum RegistrationStatus {
        REGISTERED, CANCELLED
    }
}
