package com.college.service;

import com.college.dto.LoginRequest;
import com.college.dto.LoginResponse;
import com.college.dto.RegisterRequest;
import com.college.dto.UserDTO;
import com.college.dto.VerifyOtpRequest;
import com.college.entity.User;
import com.college.exception.BadRequestException;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.UserRepository;
import com.college.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final OtpService otpService;

    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already registered: {}", request.getEmail());
            throw new BadRequestException("Email is already registered");
        }

        User.Role role = User.Role.STUDENT;
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                role = User.Role.valueOf(request.getRole().toUpperCase());
                if (role == User.Role.ADMIN) {
                    role = User.Role.STUDENT;
                    log.warn("Admin role cannot be self-assigned, defaulting to STUDENT");
                }
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid role: {}, defaulting to STUDENT", request.getRole());
                role = User.Role.STUDENT;
            }
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        otpService.generateAndSendOtp(savedUser);
        log.info("User registered successfully with email: {}", savedUser.getEmail());

        return UserDTO.fromEntity(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (!user.isVerified()) {
            SecurityContextHolder.clearContext();
            log.warn("Blocked JWT login for unverified user {}", user.getEmail());
            throw new BadRequestException("Email not verified. Please verify the OTP sent to your email before logging in");
        }

        String token = jwtProvider.generateToken(authentication);

        log.info("User logged in successfully: {}", user.getEmail());

        return new LoginResponse(token, user.getId(), user.getName(),
                user.getEmail(), user.getRole().name(), true, false);
    }

    public LoginResponse verifyOtpLogin(VerifyOtpRequest request) {
        log.info("Verifying OTP login for email: {}", request.getEmail());
        User user = otpService.verifyOtp(request);
        String token = jwtProvider.generateToken(user.getEmail());
        return new LoginResponse(token, user.getId(), user.getName(),
                user.getEmail(), user.getRole().name(), user.isVerified(), false);
    }

    public User getCurrentUser() {
        User user = getCurrentUserNullable();
        if (user == null) {
            throw new ResourceNotFoundException("No authenticated user found");
        }
        return user;
    }

    public User getCurrentUserNullable() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }
}
