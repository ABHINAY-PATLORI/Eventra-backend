package com.college.repository;

import com.college.entity.Budget;
import com.college.entity.Budget.BudgetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Budget entity.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    Page<Budget> findByEventId(Long eventId, Pageable pageable);
    
    Page<Budget> findByCreatedById(Long userId, Pageable pageable);
    
    Page<Budget> findByStatus(BudgetStatus status, Pageable pageable);
    
    Page<Budget> findByCategory(String category, Pageable pageable);
    
    Page<Budget> findByEventIdAndStatus(Long eventId, BudgetStatus status, Pageable pageable);
}
