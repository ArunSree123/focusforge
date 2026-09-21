package com.focusforge.dto;

import com.focusforge.domain.RoutineKey;
import com.focusforge.dto.ScoreDtos.DayScore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class RoutineDtos {
    private RoutineDtos() {}

    public record RoutineItemDto(Long id, RoutineKey key, String title, int targetMinutes, int targetCount,
                                 LocalTime scheduledTime, boolean active, int sortOrder) {}

    public record RoutineItemRequest(
            @NotBlank(message = "Give this block a name") @Size(max = 100) String title,
            RoutineKey key,
            @Min(value = 0, message = "Target minutes cannot be negative") @Max(value = 1440, message = "Target cannot exceed 24 hours") int targetMinutes,
            @Min(0) @Max(500) int targetCount,
            LocalTime scheduledTime,
            Boolean active) {}

    public record WakeDto(LocalTime target, LocalTime actual, Integer diffMinutes, boolean completed) {}

    public record WakeRequest(LocalDate date, LocalTime actual) {}

    public record MorningItemDto(Long id, String title, boolean done) {}

    public record MorningItemRequest(@NotBlank(message = "Give this item a name") @Size(max = 100) String title) {}

    public record MorningCheckRequest(LocalDate date, Long itemId, boolean done) {}

    public record RoutineDayDto(LocalDate date, WakeDto wake, List<MorningItemDto> morning,
                                DayScore score, List<RoutineItemDto> items) {}
}
