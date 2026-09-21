package com.focusforge.repository;

import com.focusforge.domain.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByUserIdAndDateBetweenOrderByDateDescIdDesc(Long userId, LocalDate from, LocalDate to);
    Optional<StudySession> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndDemoTrue(Long userId);
    void deleteByUserIdAndDemoTrue(Long userId);
}
