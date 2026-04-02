package com.college.controller;

import com.college.dto.ApiResponse;
import com.college.dto.LoginRequest;
import com.college.dto.LoginResponse;
import com.college.dto.RegisterRequest;
import com.college.dto.SendOtpRequest;
import com.college.dto.UserDTO;
import com.college.dto.VerifyOtpRequest;
import com.college.service.AuthService;
import com.college.service.EmailService;
import com.college.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for email: {}", request.getEmail());
        UserDTO user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. OTP sent to email", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("Passwordless OTP request for email: {}", request.getEmail());
        otpService.sendLoginOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("Resend OTP request for email: {}", request.getEmail());
        otpService.sendLoginOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP resent successfully"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("OTP verification request for email: {}", request.getEmail());
        LoginResponse response = authService.verifyOtpLogin(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
    }

    /**
     * Test email service configuration
     * Sends a test email to verify email configuration is working
     * GET /api/auth/test-mail?email=your@email.com
     */
    @GetMapping("/test-mail")
    public ResponseEntity<ApiResponse<String>> testMailConfiguration(
            @RequestParam(value = "email") String recipientEmail) {
        try {
            log.info("🧪 Testing email configuration for: {}", recipientEmail);
            
            // Validate email
            if (recipientEmail == null || !recipientEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid email address provided"));
            }
            
            // Verify configuration first
            emailService.verifyEmailService();
            log.info("✅ Email service configuration validated");
            
            // Send test email
            emailService.sendTestEmail(recipientEmail);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "✅ Test email sent successfully to " + recipientEmail +
                    ". Please check your inbox and spam folder.",
                    recipientEmail
            ));
        } catch (Exception ex) {
            log.error("❌ Email test failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Email service test failed: " + ex.getMessage()));
        }
    }

    /**
     * Check email service health
     * GET /api/auth/mail-status
     */
    @GetMapping("/mail-status")
    public ResponseEntity<ApiResponse<Object>> getMailStatus() {
        try {
            emailService.validateConfiguration();
            return ResponseEntity.ok(ApiResponse.success("Email service is configured and operational", 
                    "status: READY"));
        } catch (Exception ex) {
            log.error("❌ Email service not configured: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Email service is not configured. Error: " + ex.getMessage()));
        }
    }
}
