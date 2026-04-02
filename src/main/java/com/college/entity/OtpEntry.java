package com.college.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an OTP entry stored temporarily in memory.
 * Used for email-based OTP verification without database persistence.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpEntry {
    private String email;
    private String otp;
    private LocalDateTime expiryTime;

    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
