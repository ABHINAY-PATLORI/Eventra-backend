package com.college.controller;

import com.college.dto.ApiResponse;
import com.college.dto.PageResponse;
import com.college.dto.UserDTO;
import com.college.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user endpoints.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get all users (admin only).
     * GET /api/users?page=0&size=10&sort=asc
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching all users");
        PageResponse<UserDTO> response = userService.getAllUsers(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", response));
    }

    /**
     * Get my profile (current user).
     * GET /api/users/me
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getMyProfile() {
        log.info("Fetching my profile");
        UserDTO user = userService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", user));
    }

    /**
     * Update my profile.
     * PUT /api/users/me
     */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateMyProfile(@Valid @RequestBody UserDTO userDTO) {
        log.info("Updating my profile");
        UserDTO user = userService.updateMyProfile(userDTO);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    /**
     * Search users by name or email.
     * GET /api/users/search?q=john&page=0&size=10&sort=asc
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> searchUsers(
            @RequestParam(name = "q") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Searching users with term: {}", searchTerm);
        PageResponse<UserDTO> response = userService.searchUsers(searchTerm, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Users found", response));
    }

    /**
     * Get users by role (admin only).
     * GET /api/users/role/{role}?page=0&size=10&sort=asc
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching users with role: {}", role);
        PageResponse<UserDTO> response = userService.getUsersByRole(role, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Users with role " + role + " fetched successfully", response));
    }

    /**
     * Get user by ID (admin only).
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", user));
    }
}
