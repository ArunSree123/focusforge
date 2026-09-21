package com.focusforge.repository;

import com.focusforge.domain.WeeklyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan, Long> {
    List<WeeklyPlan> findByUserIdAndWeekStart(Long userId, LocalDate weekStart);
}
