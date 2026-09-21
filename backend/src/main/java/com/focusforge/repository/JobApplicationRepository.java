package com.focusforge.repository;

import com.focusforge.domain.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {
    List<JobApplication> findByUserIdAndDateAppliedBetween(Long userId, LocalDate from, LocalDate to);
    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndStatusNot(Long userId, com.focusforge.domain.JobStatus status);
    void deleteByUserIdAndDemoTrue(Long userId);
}
