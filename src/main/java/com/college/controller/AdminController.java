package com.college.controller;

import com.college.dto.ApiResponse;
import com.college.dto.EventDTO;
import com.college.dto.PageResponse;
import com.college.dto.UserDTO;
import com.college.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for admin endpoints.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * Get all users.
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin: Fetching all users");
        PageResponse<UserDTO> response = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", response));
    }

    /**
     * Get all pending events for approval.
     * GET /api/admin/events/pending
     */
    @GetMapping("/events/pending")
    public ResponseEntity<ApiResponse<PageResponse<EventDTO>>> getPendingEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Admin: Fetching pending events");
        PageResponse<EventDTO> response = adminService.getPendingEvents(page, size);
        return ResponseEntity.ok(ApiResponse.success("Pending events fetched successfully", response));
    }

    /**
     * Approve event.
     * PUT /api/admin/events/{id}/approve
     */
    @PutMapping("/events/{id}/approve")
    public ResponseEntity<ApiResponse<EventDTO>> approveEvent(@PathVariable Long id) {
        log.info("Admin: Approving event id: {}", id);
        EventDTO event = adminService.approveEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event approved successfully", event));
    }

    /**
     * Reject event.
     * PUT /api/admin/events/{id}/reject
     */
    @PutMapping("/events/{id}/reject")
    public ResponseEntity<ApiResponse<EventDTO>> rejectEvent(@PathVariable Long id) {
        log.info("Admin: Rejecting event id: {}", id);
        EventDTO event = adminService.rejectEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event rejected successfully", event));
    }

    /**
     * Delete user.
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Admin: Deleting user id: {}", id);
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    /**
     * Change user role.
     * PUT /api/admin/users/{id}/role
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserDTO>> changeUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        log.info("Admin: Changing user {} role to {}", id, role);
        UserDTO user = adminService.changeUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("User role changed successfully", user));
    }
}
