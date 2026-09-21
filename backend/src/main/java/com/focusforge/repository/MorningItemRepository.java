package com.focusforge.repository;

import com.focusforge.domain.MorningItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MorningItemRepository extends JpaRepository<MorningItem, Long> {
    List<MorningItem> findByUserIdAndActiveTrueOrderBySortOrderAscIdAsc(Long userId);
    Optional<MorningItem> findByIdAndUserId(Long id, Long userId);
}
