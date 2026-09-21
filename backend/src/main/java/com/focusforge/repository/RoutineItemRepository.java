package com.focusforge.repository;

import com.focusforge.domain.RoutineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoutineItemRepository extends JpaRepository<RoutineItem, Long> {
    List<RoutineItem> findByUserIdOrderBySortOrderAscIdAsc(Long userId);
    List<RoutineItem> findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(Long userId);
    Optional<RoutineItem> findByIdAndUserId(Long id, Long userId);
    void deleteByUserIdAndDemoTrue(Long userId);
}
