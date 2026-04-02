package com.college.service;

import com.college.dto.BudgetDTO;
import com.college.dto.PageResponse;
import com.college.entity.Budget;
import com.college.entity.Budget.BudgetStatus;
import com.college.entity.Event;
import com.college.entity.User;
import com.college.exception.ResourceNotFoundException;
import com.college.repository.BudgetRepository;
import com.college.repository.EventRepository;
import com.college.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing budgets.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    /**
     * Get all budgets with pagination.
     */
    public PageResponse<BudgetDTO> getAllBudgets(int page, int size, String sort) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<Budget> budgets = budgetRepository.findAll(pageable);
        return convertToPageResponse(budgets);
    }

    /**
     * Get budgets for an event.
     */
    public PageResponse<BudgetDTO> getBudgetsForEvent(Long eventId, int page, int size, String sort) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<Budget> budgets = budgetRepository.findByEventId(eventId, pageable);
        return convertToPageResponse(budgets);
    }

    /**
     * Get my budgets (current user).
     */
    public PageResponse<BudgetDTO> getMyBudgets(int page, int size, String sort) {
        String username = getAuthenticatedUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Sort.Direction direction = "desc".equalsIgnoreCase(sort) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Page<Budget> budgets = budgetRepository.findByCreatedById(user.getId(), pageable);
        return convertToPageResponse(budgets);
    }

    /**
     * Get budget by ID.
     */
    @Transactional(readOnly = true)
    public BudgetDTO getBudgetById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        return convertToDTO(budget);
    }

    /**
     * Create a new budget.
     */
    public BudgetDTO createBudget(BudgetDTO budgetDTO) {
        String username = getAuthenticatedUsername();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = null;
        if (budgetDTO.getEventId() != null) {
            event = eventRepository.findById(budgetDTO.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        }

        Budget budget = Budget.builder()
                .title(budgetDTO.getTitle())
                .description(budgetDTO.getDescription())
                .event(event)
                .createdBy(user)
                .totalAmount(budgetDTO.getTotalAmount())
                .allocatedAmount(budgetDTO.getAllocatedAmount() != null ? budgetDTO.getAllocatedAmount() : 0.0)
                .spentAmount(budgetDTO.getSpentAmount() != null ? budgetDTO.getSpentAmount() : 0.0)
                .status(BudgetStatus.ACTIVE)
                .category(budgetDTO.getCategory())
                .notes(budgetDTO.getNotes())
                .build();

        Budget savedBudget = budgetRepository.save(budget);
        log.info("Budget created with id: {}", savedBudget.getId());
        return convertToDTO(savedBudget);
    }

    /**
     * Update a budget.
     */
    public BudgetDTO updateBudget(Long id, BudgetDTO budgetDTO) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (budgetDTO.getTitle() != null) budget.setTitle(budgetDTO.getTitle());
        if (budgetDTO.getDescription() != null) budget.setDescription(budgetDTO.getDescription());
        if (budgetDTO.getTotalAmount() != null) budget.setTotalAmount(budgetDTO.getTotalAmount());
        if (budgetDTO.getAllocatedAmount() != null) budget.setAllocatedAmount(budgetDTO.getAllocatedAmount());
        if (budgetDTO.getSpentAmount() != null) budget.setSpentAmount(budgetDTO.getSpentAmount());
        if (budgetDTO.getCategory() != null) budget.setCategory(budgetDTO.getCategory());
        if (budgetDTO.getNotes() != null) budget.setNotes(budgetDTO.getNotes());

        Budget updated = budgetRepository.save(budget);
        log.info("Budget updated with id: {}", id);
        return convertToDTO(updated);
    }

    /**
     * Approve a budget (admin only).
     */
    public BudgetDTO approveBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        budget.setStatus(BudgetStatus.APPROVED);
        Budget updated = budgetRepository.save(budget);
        log.info("Budget approved with id: {}", id);
        return convertToDTO(updated);
    }

    /**
     * Delete a budget.
     */
    public void deleteBudget(Long id) {
        budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        budgetRepository.deleteById(id);
        log.info("Budget deleted with id: {}", id);
    }

    private BudgetDTO convertToDTO(Budget budget) {
        return BudgetDTO.builder()
                .id(budget.getId())
                .title(budget.getTitle())
                .description(budget.getDescription())
                .eventId(budget.getEvent() != null ? budget.getEvent().getId() : null)
                .eventTitle(budget.getEvent() != null ? budget.getEvent().getTitle() : null)
                .createdById(budget.getCreatedBy().getId())
                .createdByName(budget.getCreatedBy().getEmail())
                .totalAmount(budget.getTotalAmount())
                .allocatedAmount(budget.getAllocatedAmount())
                .spentAmount(budget.getSpentAmount())
                .status(budget.getStatus().name())
                .category(budget.getCategory())
                .notes(budget.getNotes())
                .remainingAmount(budget.getRemainingAmount())
                .utilizationPercentage(budget.getUtilizationPercentage())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }

    private PageResponse<BudgetDTO> convertToPageResponse(Page<Budget> budgets) {
        return PageResponse.<BudgetDTO>builder()
                .content(budgets.getContent().stream().map(this::convertToDTO).toList())
                .totalElements(budgets.getTotalElements())
                .totalPages(budgets.getTotalPages())
                .pageNumber(budgets.getNumber())
                .pageSize(budgets.getSize())
                .last(budgets.isLast())
                .build();
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
