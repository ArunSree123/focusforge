package com.focusforge.repository;

import com.focusforge.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<Project> findByIdAndUserId(Long id, Long userId);
    void deleteByUserIdAndDemoTrue(Long userId);
}
