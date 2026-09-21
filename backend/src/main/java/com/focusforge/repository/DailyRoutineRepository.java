package com.focusforge.repository;

import com.focusforge.domain.DailyRoutine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRoutineRepository extends JpaRepository<DailyRoutine, Long> {
    Optional<DailyRoutine> findByUserIdAndDate(Long userId, LocalDate date);
    List<DailyRoutine> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);
    void deleteByUserIdAndDemoTrue(Long userId);
}
