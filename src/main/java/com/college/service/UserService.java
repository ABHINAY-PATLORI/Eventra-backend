package com.college.service;

import com.college.dto.PageResponse;
import com.college.dto.UserDTO;
import com.college.entity.User;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user profiles.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users (admin only).
     */
    public PageResponse<UserDTO> getAllUsers(int page, int size, String sort) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);
        return convertToPageResponse(users);
    }

    /**
     * Get my profile (current user).
     */
    @Transactional(readOnly = true)
    public UserDTO getMyProfile() {
        String username = getAuthenticatedUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    /**
     * Update my profile.
     */
    public UserDTO updateMyProfile(UserDTO userDTO) {
        String username = getAuthenticatedUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (userDTO.getName() != null && !userDTO.getName().isBlank()) {
            user.setName(userDTO.getName());
        }

        User updated = userRepository.save(user);
        log.info("User profile updated for email: {}", username);
        return convertToDTO(updated);
    }

    /**
     * Search users by name or email.
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> searchUsers(String searchTerm, int page, int size, String sort) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        
        // Search for users by name or email containing the search term
        Page<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
        return convertToPageResponse(users);
    }

    /**
     * Get users by role (admin only).
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getUsersByRole(String role, int page, int size, String sort) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
            Page<User> users = userRepository.findByRole(userRole, pageable);
            return convertToPageResponse(users);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .verified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private PageResponse<UserDTO> convertToPageResponse(Page<User> users) {
        return PageResponse.<UserDTO>builder()
                .content(users.getContent().stream().map(this::convertToDTO).toList())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .pageNumber(users.getNumber())
                .pageSize(users.getSize())
                .last(users.isLast())
                .build();
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
