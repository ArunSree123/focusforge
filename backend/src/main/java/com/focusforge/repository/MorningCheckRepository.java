package com.focusforge.repository;

import com.focusforge.domain.MorningCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MorningCheckRepository extends JpaRepository<MorningCheck, Long> {
    List<MorningCheck> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);
    Optional<MorningCheck> findByUserIdAndDateAndMorningItemId(Long userId, LocalDate date, Long morningItemId);
    void deleteByUserIdAndDemoTrue(Long userId);
    void deleteByUserIdAndMorningItemId(Long userId, Long morningItemId);
}
