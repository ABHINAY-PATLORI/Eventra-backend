package com.college.controller;

import com.college.dto.ApiResponse;
import com.college.dto.BudgetDTO;
import com.college.dto.PageResponse;
import com.college.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for budget endpoints.
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * Get all budgets with pagination.
     * GET /api/budgets?page=0&size=10&sort=asc
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BudgetDTO>>> getAllBudgets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching all budgets");
        PageResponse<BudgetDTO> response = budgetService.getAllBudgets(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Budgets fetched successfully", response));
    }

    /**
     * Get budgets for an event.
     * GET /api/budgets/event/{eventId}?page=0&size=10&sort=asc
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BudgetDTO>>> getBudgetsForEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching budgets for event id: {}", eventId);
        PageResponse<BudgetDTO> response = budgetService.getBudgetsForEvent(eventId, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("Event budgets fetched successfully", response));
    }

    /**
     * Get my budgets (current user).
     * GET /api/budgets/my-budgets
     */
    @GetMapping("/my-budgets")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BudgetDTO>>> getMyBudgets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort) {
        log.info("Fetching my budgets");
        PageResponse<BudgetDTO> response = budgetService.getMyBudgets(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success("My budgets fetched successfully", response));
    }

    /**
     * Get budget by ID.
     * GET /api/budgets/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BudgetDTO>> getBudgetById(@PathVariable Long id) {
        log.info("Fetching budget with id: {}", id);
        BudgetDTO budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(ApiResponse.success("Budget fetched successfully", budget));
    }

    /**
     * Create a new budget.
     * POST /api/budgets
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BudgetDTO>> createBudget(@Valid @RequestBody BudgetDTO budgetDTO) {
        log.info("Creating new budget: {}", budgetDTO.getTitle());
        BudgetDTO budget = budgetService.createBudget(budgetDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", budget));
    }

    /**
     * Update a budget.
     * PUT /api/budgets/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BudgetDTO>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetDTO budgetDTO) {
        log.info("Updating budget with id: {}", id);
        BudgetDTO budget = budgetService.updateBudget(id, budgetDTO);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", budget));
    }

    /**
     * Approve a budget (admin only).
     * PUT /api/budgets/{id}/approve
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BudgetDTO>> approveBudget(@PathVariable Long id) {
        log.info("Approving budget with id: {}", id);
        BudgetDTO budget = budgetService.approveBudget(id);
        return ResponseEntity.ok(ApiResponse.success("Budget approved successfully", budget));
    }

    /**
     * Delete a budget.
     * DELETE /api/budgets/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        log.info("Deleting budget with id: {}", id);
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully"));
    }
}
