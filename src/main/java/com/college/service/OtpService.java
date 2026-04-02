package com.college.service;

import com.college.dto.SendOtpRequest;
import com.college.dto.VerifyOtpRequest;
import com.college.entity.User;
import com.college.exception.BadRequestException;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void generateAndSendOtp(User user) {
        issueOtp(user, true);
    }

    @Transactional
    public void sendLoginOtp(SendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        issueOtp(user, false);
    }

    @Transactional
    public User verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            throw new BadRequestException("No OTP is available for verification");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            clearOtp(user);
            userRepository.save(user);
            throw new BadRequestException("OTP has expired. Please request a new OTP");
        }

        if (!user.getOtp().equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }

        if (!user.isVerified()) {
            user.setVerified(true);
        }
        clearOtp(user);
        User verifiedUser = userRepository.save(user);
        log.info("User verified successfully for email {}", request.getEmail());

        return verifiedUser;
    }

    private void issueOtp(User user, boolean markUnverified) {
        emailService.validateConfiguration();

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        if (markUnverified) {
            user.setVerified(false);
        }
        userRepository.save(user);

        log.info("Generated OTP for user {}", user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp)
                .exceptionally(ex -> {
                    log.error("Asynchronous OTP email send failed for {}", user.getEmail(), ex);
                    return null;
                });
    }

    private void clearOtp(User user) {
        user.setOtp(null);
        user.setOtpExpiry(null);
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int otpValue = SECURE_RANDOM.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", otpValue);
    }
}
