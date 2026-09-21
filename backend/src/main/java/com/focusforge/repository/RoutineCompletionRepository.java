package com.focusforge.repository;

import com.focusforge.domain.RoutineCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineCompletionRepository extends JpaRepository<RoutineCompletion, Long> {
    List<RoutineCompletion> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);
    Optional<RoutineCompletion> findByUserIdAndDateAndRoutineItemId(Long userId, LocalDate date, Long routineItemId);
    void deleteByUserIdAndDemoTrue(Long userId);
    void deleteByUserIdAndRoutineItemId(Long userId, Long routineItemId);
}
