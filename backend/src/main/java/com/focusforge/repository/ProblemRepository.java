package com.focusforge.repository;

import com.focusforge.domain.Problem;
import com.focusforge.domain.ProblemKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByUserIdAndKindOrderByDateDescIdDesc(Long userId, ProblemKind kind);
    List<Problem> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);
    Optional<Problem> findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndKindAndSolvedTrue(Long userId, ProblemKind kind);
    void deleteByUserIdAndDemoTrue(Long userId);
}
