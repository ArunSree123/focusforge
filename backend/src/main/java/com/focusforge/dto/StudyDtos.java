package com.focusforge.dto;

import com.focusforge.domain.StudyCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class StudyDtos {
    private StudyDtos() {}

    public record StudySessionRequest(
            @NotNull(message = "Choose a category") StudyCategory category,
            LocalDate date,
            @Min(value = 1, message = "Invalid study session duration") @Max(value = 720, message = "Invalid study session duration") int durationMinutes,
            @Size(max = 200) String topic,
            @Size(max = 200) String source,
            @Size(max = 2000) String notes) {}

    public record StudySessionDto(Long id, StudyCategory category, LocalDate date, int durationMinutes,
                                  String topic, String source, String notes, boolean demo) {}
}
