package com.focusforge.repository;

import com.focusforge.domain.ActivityType;
import com.focusforge.domain.FitnessSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FitnessSessionRepository extends JpaRepository<FitnessSession, Long> {
    List<FitnessSession> findByUserIdAndDateBetweenOrderByDateDescIdDesc(Long userId, LocalDate from, LocalDate to);
    Optional<FitnessSession> findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndType(Long userId, ActivityType type);
    void deleteByUserIdAndDemoTrue(Long userId);
}
