package com.focusforge.repository;

import com.focusforge.domain.TopicArea;
import com.focusforge.domain.TopicProgress;
import com.focusforge.domain.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TopicProgressRepository extends JpaRepository<TopicProgress, Long> {
    List<TopicProgress> findByUserIdAndAreaOrderBySortOrderAscIdAsc(Long userId, TopicArea area);
    List<TopicProgress> findByUserIdAndStatusAndCompletedOnBetween(Long userId, TopicStatus status, LocalDate from, LocalDate to);
    List<TopicProgress> findByUserIdAndDemoTrue(Long userId);
    Optional<TopicProgress> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserId(Long userId);
    long countByUserIdAndAreaAndStatus(Long userId, TopicArea area, TopicStatus status);
}
