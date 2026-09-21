package com.focusforge.repository;

import com.focusforge.domain.DailyReflection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyReflectionRepository extends JpaRepository<DailyReflection, Long> {
    Optional<DailyReflection> findByUserIdAndDate(Long userId, LocalDate date);
    void deleteByUserIdAndDemoTrue(Long userId);
}
