package com.focusforge.dto;

import com.focusforge.domain.ActivityType;
import com.focusforge.domain.WorkoutType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class FitnessDtos {
    private FitnessDtos() {}

    public record ExerciseDto(@NotBlank(message = "Exercise name is required") @Size(max = 120) String name,
                              @Min(0) @Max(100) int sets, @Min(0) @Max(1000) int reps,
                              @DecimalMin("0") BigDecimal weightKg) {}

    /** durationMinutes may be 0 when start and end times are supplied; the service derives it. */
    public record FitnessRequest(
            @NotNull(message = "Choose an activity") ActivityType type,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            @Min(value = 0, message = "Duration cannot be negative") @Max(value = 1440, message = "Duration cannot exceed 24 hours") int durationMinutes,
            WorkoutType workoutType,
            @DecimalMin("0") BigDecimal distanceKm,
            @Min(0) Integer steps,
            @Size(max = 2000) String notes,
            @Valid List<ExerciseDto> exercises) {}

    public record FitnessDto(Long id, ActivityType type, LocalDate date, LocalTime startTime, LocalTime endTime,
                             int durationMinutes, WorkoutType workoutType, BigDecimal distanceKm, Integer steps,
                             String notes, List<ExerciseDto> exercises, boolean demo) {}

    public record FitnessSummary(LocalDate weekStart, int gymSessions, int gymTarget, int gymMinutes,
                                 int walkDays, int basketballDays, int activeMornings) {}
}
